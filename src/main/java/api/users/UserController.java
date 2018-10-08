package api.users;

import api.ApiError;
import api.Constants;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

import static io.micronaut.http.HttpResponse.*;

/**
 * Created by Salvador Montiel on 03/oct/2018.
 */
@Controller("/users")
public class UserController {
    @Inject Database db;
    @Inject JWTCreator.Builder jwtSigner;
    @Inject Algorithm jwtAlgorithm;
    @Inject JWTVerifier jwtVerifier;

    @Get("/me")
    public Flowable<HttpResponse> currentUser(HttpRequest request/* @Header("Authorization") String authorization*/) {
        Optional<String> authorization = request.getHeaders().getAuthorization();
        if (!authorization.isPresent()) {
            return ApiError.of(unauthorized(), "La petición NO incluye el header 'Authorization' con el token");
        }
        String token = authorization.get().substring(7);
        DecodedJWT jwt;
        try {
            jwt = jwtVerifier.verify(token);
        } catch (TokenExpiredException e) {
            Date date = JWT.decode(token).getExpiresAt();
            return ApiError.of(unauthorized(), "El token expiró. Fecha de expiración: " + DateFormat.getDateTimeInstance().format(date));
        }
        Claim emailClaim = jwt.getClaim("email");
        if (!emailClaim.isNull()) {
            UsuarioResponse user = db.select("SELECT * FROM usuario WHERE email = ?")
                    .parameters(emailClaim.asString())
                    .get(rs -> {
                        String email = emailClaim.asString();
                        int id = rs.getInt("id");
                        String nombre = rs.getString("nombre");
                        String apellidos = rs.getString("apellidos");
                        String urlFoto = rs.getString("url_foto");
                        String telefono = rs.getString("telefono");

                        return db.select("SELECT r.nombre FROM role r, usuario u, user_role ur WHERE u.id = ? AND u.id = ur.id_usuario AND r.id = ur.id_role")
                                .parameters(id).get(rs1 -> rs1.getString("nombre"))
                                .toList()
                                .map(roles -> new UsuarioResponse(email, getToken(email, roles), nombre, apellidos, urlFoto, telefono, roles))
                                .blockingGet();
                    }).blockingFirst(null);
            if (user == null) {
                return ApiError.of(notFound(), "El usuario con el correo: '" + emailClaim.asString() + "' NO existe");
            }
            return Flowable.just(ok(user));
        } else {
            return ApiError.of(unauthorized(), "El token es inválido.");
        }
    }

    @Post("/login")
    public Flowable<HttpResponse> autenticacion(@Body ObjectNode body) {
        List<String> requiredFields = Arrays.asList("email", "password");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }
        String email = body.get("email").asText();
        String password = body.get("password").asText();
        boolean userExists = db.select("SELECT * FROM usuario WHERE email = ?")
                .parameters(email)
                .get(rs -> {
                    String hashedPasswd = rs.getString("password");
                    return BCrypt.checkpw(password, hashedPasswd);
                })
                .blockingFirst(false);
        if (!userExists) {
            return ApiError.of(notFound(), "El usuario con el correo: '" + email + "' NO existe ó la contraseña es incorrecta.");
        } else {
            return db.select("SELECT * FROM usuario WHERE email = ?")
                .parameters(email)
                .get(rs -> {
                    List<String> roles = db.select("SELECT r.nombre FROM role r, usuario u, user_role ur WHERE u.id = ? AND u.id = ur.id_usuario AND r.id = ur.id_role")
                            .parameters(rs.getInt("id"))
                            .get(rs1 -> rs1.getString("nombre"))
                            .toList().blockingGet();
                    String nombre = rs.getString("nombre");
                    String apellidos = rs.getString("apellidos");
                    String urlFoto = rs.getString("url_foto");
                    String telefono = rs.getString("telefono");

                    return new UsuarioResponse(email, getToken(email, roles), nombre, apellidos, urlFoto, telefono, roles);
                }).map(HttpResponse::ok);
        }
    }

    @Post
    public Flowable<HttpResponse> registration(@Body ObjectNode body) {
        List<String> requiredFields = Arrays.asList("nombre", "apellidos", "email", "password");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }
        String email = body.get("email").asText();
        Integer a =db.select("SELECT COUNT(*) FROM usuario WHERE email = '" + email + "'")
            .get(rs -> rs.getInt("count")).blockingFirst();
        if (a > 0) {
            return ApiError.of(badRequest(), "Ya existe un usuario con el correo: '" + email + "'");
        } else {
            String nombre = body.get("nombre").asText();
            String apellidos = body.get("apellidos").asText();
            String password = hashPassword(body.get("password").asText());
            String url_foto = Constants.URL_FOTO_USUARIO_BY_DEFAULT;
            if (body.get("url_foto") != null) {
                url_foto = body.get("url_foto").asText();
            }
            String telefono = "";
            if (body.get("telefono") != null) {
                telefono = body.get("telefono").asText();
            }
            int result = db.update("INSERT INTO usuario (nombre, apellidos, email, password, url_foto, telefono) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING *")
                .parameters(nombre, apellidos, email, password, url_foto, telefono)
                .returnGeneratedKeys().get(rs -> rs.getInt("id"))
                .blockingSingle();

            UsuarioResponse user = new UsuarioResponse(email, getToken(email, Arrays.asList()), nombre, apellidos, url_foto, telefono);
            return Flowable.just(ok(user));
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(5));
    }

    private String getToken(String email, List<String> roles) {
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("email", email);
        headerClaims.put("roles", roles);
        return jwtSigner.withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24)))
                .sign(jwtAlgorithm);
    }

    static class UsuarioResponse {
        public final String email;
        public final String token;
        public final String nombre;
        public final String apellidos;
        public final String url_foto;
        public final String telefono;
        public final List<String> roles;

        public UsuarioResponse(String email, String token, String nombre, String apellidos, String url_foto, String telefono) {
            this(email, token, nombre, apellidos, url_foto, telefono, Arrays.asList());
        }

        public UsuarioResponse(String email, String token, String nombre, String apellidos, String url_foto, String telefono, List<String> roles) {
            this.email = email;
            this.token = token;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.url_foto = url_foto;
            this.telefono = telefono;
            this.roles = roles;
        }
    }
}

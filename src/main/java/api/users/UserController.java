package api.users;

import api.ApiError;
import api.Constants;
import api.data.users.UsersRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.reactivex.Flowable;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.micronaut.http.HttpResponse.*;

/**
 * Created by Salvador Montiel on 03/oct/2018.
 */
@Controller("/users")
public class UserController {
    @Inject UsersRepository usersRepo;
    @Inject JWTCreator.Builder jwtSigner;
    @Inject Algorithm jwtAlgorithm;
    @Inject JWTVerifier jwtVerifier;

    @Get("/me")
    public Flowable<HttpResponse> currentUser(HttpRequest request) {
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
            UsuarioResponse user = usersRepo.getUserByEmail(emailClaim.asString())
                    .map(u -> new UsuarioResponse(u.email, getToken(u.email, u.roles), u.nombre, u.apellidos, u.url_foto, u.telefono, u.roles))
                    .blockingFirst(null);
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

        boolean userExists = usersRepo.getUserByEmail(email)
                .map(u -> {
                    String hashedPasswd = u.password;
                    return BCrypt.checkpw(password, hashedPasswd);
                }).blockingFirst(false);
        if (!userExists) {
            return ApiError.of(notFound(), "El usuario con el correo: '" + email + "' NO existe ó la contraseña es incorrecta.");
        } else {
            return usersRepo.getUserByEmail(email)
                    .map(u -> new UsuarioResponse(email, getToken(email, u.roles), u.nombre, u.apellidos, u.url_foto, u.telefono, u.roles))
                    .map(HttpResponse::ok);
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
        boolean existsUser = usersRepo.existsUserWithEmail(email).blockingFirst(true);
        if (existsUser) {
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
            usersRepo.saveUser(nombre, apellidos, email, password, url_foto, telefono)
                    .blockingSingle();

            UsuarioResponse user = new UsuarioResponse(email, getToken(email, Arrays.asList()), nombre, apellidos, url_foto, telefono);
            return Flowable.just(ok(user));
        }
    }

    @Put
    public Flowable<HttpResponse> actualizarUsuario(HttpRequest request, @Body ObjectNode body) {
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
            boolean existsUser = usersRepo.existsUserWithEmail(emailClaim.asString())
                    .blockingFirst(true);
            if (!existsUser) {
                return ApiError.of(notFound(), "El usuario con el correo: '" + emailClaim.asString() + "' NO existe");
            }
            String nombre = null;
            if (body.get("nombre") != null) {
                nombre = body.get("nombre").asText();
            }
            String apellidos = null;
            if (body.get("apellidos") != null) {
                apellidos = body.get("apellidos").asText();
            }
            String urlFoto = null;
            if (body.get("url_foto") != null) {
                urlFoto = body.get("url_foto").asText();
            }
            String telefono = null;
            if (body.get("telefono") != null) {
                telefono = body.get("telefono").asText();
            }
            return usersRepo.updateUserData(emailClaim.asString(), nombre, apellidos, urlFoto, telefono)
                    .map(HttpResponse::ok);
        } else {
            return ApiError.of(unauthorized(), "El token es inválido.");
        }
    }

    @Put("/roles")
    public Flowable<HttpResponse> agregarRolesUsuario(HttpRequest request, @Body ObjectNode body) {
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
            boolean existsUser = usersRepo.existsUserWithEmail(emailClaim.asString())
                    .blockingFirst(true);
            if (!existsUser) {
                return ApiError.of(notFound(), "El usuario con el correo: '" + emailClaim.asString() + "' NO existe");
            }
            Claim rolesClaim = jwt.getHeaderClaim("roles");
            if (rolesClaim.isNull()) {
                return ApiError.of(HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN),
                        "El usuario '" + emailClaim.asString() + "' NO cuenta con los permisos necesarios");
            } else {
                List<String> userRoles = rolesClaim.asList(String.class);
                if (userRoles.contains("SUPER_ADMIN")) {
                    List<String> requiredFields = Arrays.asList("email", "roles");
                    String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                            .collect(Collectors.joining(", "));
                    if (!fields.equals("")) {
                        return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
                    }

                    String userEmail = body.get("email").asText();
                    List<String> roles = new ArrayList<>();
                    Iterator<JsonNode> iterator = body.get("roles").elements();
                    while (iterator.hasNext()) {
                        roles.add(iterator.next().asText());
                    }
                    return usersRepo.addRoles(userEmail, roles)
                            .map(HttpResponse::ok);
                } else {
                    return ApiError.of(HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN),
                            "El usuario '" + emailClaim.asString() + "' NO cuenta con los permisos necesarios");
                }
            }
        } else {
            return ApiError.of(unauthorized(), "El token es inválido.");
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(5));
    }

    private String getToken(String email, List<String> roles) {
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("email", email);
        headerClaims.put("roles", roles);
        return jwtSigner.withHeader(headerClaims)
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 3)))
                .sign(jwtAlgorithm);
    }
}

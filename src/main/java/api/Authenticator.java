package api;

import api.data.users.UsersRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.reactivex.Flowable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

import static io.micronaut.http.HttpResponse.unauthorized;

/**
 * Created by Salvador Montiel on 15/oct/2018.
 */
@Singleton
public class Authenticator {
    private JWTVerifier jwtVerifier;
    private UsersRepository usersRepo;

    @Inject
    public Authenticator(JWTVerifier jwtVerifier, UsersRepository usersRepo) {
        this.jwtVerifier = jwtVerifier;
        this.usersRepo = usersRepo;
    }

    public Optional<Flowable<HttpResponse>> authorize(HttpRequest request, boolean hasToBeAdmin, boolean hasToBeSuperAdmin) {
        Optional<String> authorization = request.getHeaders().getAuthorization();
        if (!authorization.isPresent()) {
            return Optional.of(ApiError.of(unauthorized(), "La petición NO incluye el header 'Authorization' con el token"));
        }
        String token = authorization.get().substring(7);
        DecodedJWT jwt;
        try {
            jwt = jwtVerifier.verify(token);
        } catch (TokenExpiredException e) {
            Date date = JWT.decode(token).getExpiresAt();
            return Optional.of(ApiError.of(unauthorized(), "El token expiró. Fecha de expiración: " + DateFormat.getDateTimeInstance().format(date)));
        }
        Claim emailClaim = jwt.getClaim("email");
        if (!emailClaim.isNull()) {
            if (hasToBeAdmin) {
                if (usersRepo.userIsAdmin(emailClaim.asString())) return Optional.empty();
                else return Optional.of(ApiError.of(HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN),
                        "El usuario '" + emailClaim.asString() + "' NO cuenta con los permisos necesarios"));
            }
            if (hasToBeSuperAdmin) {
                Claim superAdmin = jwt.getHeaderClaim("is_super_admin");
                if (!superAdmin.isNull() && superAdmin.asBoolean()) return Optional.empty();
                else return Optional.of(ApiError.of(HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN),
                        "El usuario '" + emailClaim.asString() + "' NO cuenta con los permisos necesarios"));
            }
            return Optional.empty();
        } else {
            return Optional.of(ApiError.of(unauthorized(), "El token es inválido."));
        }
    }
}

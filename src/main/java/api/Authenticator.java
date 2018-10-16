package api;

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
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.HttpResponse.unauthorized;

/**
 * Created by Salvador Montiel on 15/oct/2018.
 */
@Singleton
public class Authenticator {
    private JWTVerifier jwtVerifier;

    @Inject
    public Authenticator(JWTVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    public Optional<Flowable<HttpResponse>> authenticate(HttpRequest request, String... requiredRoles) {
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
            if (requiredRoles.length > 0) {
                Optional<Flowable<HttpResponse>> rolesError = checkRoles(jwt, emailClaim.asString(), requiredRoles);
                return rolesError;
            } else return Optional.empty();
        } else {
            return Optional.of(ApiError.of(unauthorized(), "El token es inválido."));
        }
    }

    private Optional<Flowable<HttpResponse>> checkRoles(DecodedJWT jwt, String userEmail, String... requiredRoles) {
        Claim rolesClaim = jwt.getHeaderClaim("roles");
        if (rolesClaim.isNull()) {
            return Optional.of(ApiError.of(HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN),
                    "El usuario '" + userEmail + "' NO cuenta con los permisos necesarios"));
        } else {
            List<String> userRoles = rolesClaim.asList(String.class);
            for (String r : requiredRoles) {
                if (userRoles.contains(r)) return Optional.empty();
            }
            return Optional.of(ApiError.of(HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN),
                    "El usuario '" + userEmail + "' NO cuenta con los permisos necesarios"));
        }
    }
}

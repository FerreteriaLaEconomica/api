package api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Salvador Montiel on 01/oct/2018.
 */
@Factory
public class AppConfig {

    @Bean
    @Singleton
    Database buildDatabase() throws URISyntaxException {
        String postgresUrl = System.getenv("DATABASE_URL");
        if (postgresUrl == null) {
            throw new RuntimeException("Agrega una variable de entorno DATABASE_URL con la url de la base de datos a conectar");
        }
        URI dbUri = new URI(postgresUrl);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        HikariDataSource hikariDataSource = new HikariDataSource(config);

        return Database.fromBlocking(hikariDataSource);
    }

    @Bean
    @Singleton
    public Algorithm jwtAlgorithm() {
        String secretKey = System.getenv("SECRET_KEY");
        if (secretKey == null) {
            throw new RuntimeException("Agrega una variable de entorno SECRET_KEY con una contraseña");
        }
        return Algorithm.HMAC256(secretKey);
    }

    @Bean
    @Singleton
    public JWTCreator.Builder jwtSigner() {
        return JWT.create()
                .withIssuer("Ferretería");
    }

    @Bean
    @Singleton
    public JWTVerifier jwtVerifier() {
        return JWT.require(jwtAlgorithm())
                .withIssuer("Ferretería")
                .acceptLeeway(60)
                .build();
    }
}

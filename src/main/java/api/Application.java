package api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.micronaut.runtime.Micronaut;

import java.io.FileInputStream;
import java.io.IOException;

public class Application {

    public static void main(String[] args) throws IOException {
        Micronaut.run(Application.class);

        String configPath = System.getenv("CONFIG_PATH");
        FileInputStream serviceAccount = new FileInputStream(configPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://images-smt.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
    }
}

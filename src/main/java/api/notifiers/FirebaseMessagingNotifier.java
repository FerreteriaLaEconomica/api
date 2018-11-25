package api.notifiers;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

/**
 * Created by Salvador Montiel on 24/nov/2018.
 */
public class FirebaseMessagingNotifier implements Notifier {
    private static final String topic = "inventario";

    @Override
    public void notify(String message) {
        Message msg = Message.builder()
                .setNotification(new Notification("Producto por agotarse!", message))
                .setTopic(topic)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(msg);
            // Response is a message ID string.
            System.out.println("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            System.out.println("Error sending message: " + e.getErrorCode());
            e.printStackTrace();
        }
    }
}

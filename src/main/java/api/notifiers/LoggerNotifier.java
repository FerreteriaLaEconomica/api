package api.notifiers;

/**
 * Created by Salvador Montiel on 24/nov/2018.
 */
public class LoggerNotifier implements Notifier {

    @Override
    public void notify(String message) {
        System.out.println("Notification: " + message);
    }
}

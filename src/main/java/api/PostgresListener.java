package api;

import api.notifiers.Notifier;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Salvador Montiel on 24/nov/2018.
 */
public class PostgresListener extends Thread {
    private Connection conn;
    private org.postgresql.PGConnection pgconn;
    private final List<Notifier> notifiers = new ArrayList<>();

    public PostgresListener(Connection conn) throws SQLException {
        this.conn = conn;
        this.pgconn = (org.postgresql.PGConnection)conn;
        Statement stmt = conn.createStatement();
        stmt.execute("LISTEN inventario");
        stmt.close();
    }

    public void addNotifier(Notifier notifier) {
        if (!notifiers.contains(notifier)) notifiers.add(notifier);
    }

    public void run() {
        while (true) {
            try {
                // issue a dummy query to contact the backend
                // and receive any pending notifications.
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                rs.close();
                stmt.close();

                org.postgresql.PGNotification notifications[] = pgconn.getNotifications();
                if (notifications != null) {
                    for (int i=0; i<notifications.length; i++) {
                        int finalI = i;
                        notifiers.forEach(notifier -> notifier.notify(notifications[finalI].getParameter()));
                    }
                }
                // wait a while before checking again for new notifications
                Thread.sleep(500);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}

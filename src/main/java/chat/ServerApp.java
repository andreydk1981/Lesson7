package chat;

import java.sql.SQLException;

/**
 * класс для запуска приложения
 */
public class ServerApp {
    public static void main(String[] args) throws SQLException {
        new MyServer();
    }
}

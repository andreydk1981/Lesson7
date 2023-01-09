package chat;

import java.sql.SQLException;

public interface AuthService {
    void start() throws SQLException;

    void stop() throws SQLException;

    String getNickByLoginAndPass(String login, String pass) throws SQLException;
    String change_nickname(String oldNick, String newNick) throws SQLException;


    }

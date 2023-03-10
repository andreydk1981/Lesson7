package chat;

public class ChatConstants {
    public static final String HOST = "localhost";
    public static final int PORT  = 8589;

    public static final String AUTH_COMMAND = "/auth";
    public static final String CHANGE_NICK = "/newNick";
    public static final String AUTH_OK= "/authOk";
    public static final String STOP_WORD = "/stop";
    public static final String CLEAR = "/clear";

    public static final String CLIENTS_LIST = "/clients";
    public static final String SEND_TO_NICK = "/w";

    public static final String SEND_TO_LIST = "/list";
    public static final Integer SECONDS_TO_AUTH = 120;

    static final String DATABASE_URL = "jdbc:sqlite:javadb.db";

}

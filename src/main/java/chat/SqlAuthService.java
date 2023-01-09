package chat;

import java.sql.*;

import static chat.ChatConstants.DATABASE_URL;

public class SqlAuthService implements AuthService {
    static Connection connection;
    static Statement statement;

    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
            createTable();
            initEntries();
            System.out.println(this.getClass().getName() + " server started");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws SQLException {
        dropTable();
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(this.getClass().getName() + " server stopped");
    }

    @Override
    public String getNickByLoginAndPass(String login, String pass) throws SQLException {
        String nick = null;
        String getNick = "select nick from entries where login = ? and pass = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(getNick);
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, pass);
        ResultSet resultSet = preparedStatement.executeQuery();
        nick = resultSet.getString("nick");
        System.out.println(nick);
        return nick;
    }

    public void dropTable() throws SQLException {
        String dropSql = "drop table IF EXISTS entries";
        statement.execute(dropSql);
    }

    private void createTable() throws SQLException {
        statement.execute("delete from entries");
        String createTable = "create table if not exists entries (" +
                "id integer not null primary key," +
                "nick TEXT not null, " +
                "login TEXT," +
                "pass text)";
        statement.execute(createTable);
    }

    private void initEntries() throws SQLException {
        PreparedStatement preparedStatement =
                connection.prepareStatement("insert into entries(nick, login, pass)values(?,?,?)");
        for (int i = 1; i <= 3; i++) {
            preparedStatement.setString(1, "nick" + i);
            preparedStatement.setString(2, "login" + i);
            preparedStatement.setString(3, "pass" + i);
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
    }

    public String change_nickname(String oldNick, String newNick) throws SQLException {
        listDatabase();
        String strUpdate = "UPDATE entries SET nick = ?  WHERE nick = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(strUpdate);
        preparedStatement.setString(1, newNick);
        preparedStatement.setString(2, oldNick);
        preparedStatement.execute();
        listDatabase();

        return newNick;
    }

    private void listDatabase() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM entries;");
        while (resultSet.next()) {
            System.out.println(resultSet.getInt("id") + " " +
                    resultSet.getString("nick"));
        }

    }

}

package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static chat.ChatConstants.*;

/**
 * обслуживает клиента и отвечает за свяь между клинетом и сервером
 */
public class ClientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String name;

    private Integer count = 0;
    private boolean autorised = false;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        authentication();
                        readMessages();
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    } finally {
                        closeConnection();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (count < SECONDS_TO_AUTH) {
                        System.out.println(count += 5);
                        System.out.println("autorised = " + autorised);
                        if (autorised) break;
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (!autorised) closeConnection();

                }
            }).start();
        } catch (IOException ex) {
            System.out.println("Problem with make client");
        }
    }

    private void readMessages() throws IOException, SQLException {
        while (true) {
            String message = inputStream.readUTF();
            System.out.println("from " + name + ": " + message);
            if (message.equals(STOP_WORD)) {
                return;
            }
            if (message.startsWith(CLIENTS_LIST)) {
                server.clientListMessage(this.name);
                /*sendMsg("online: " + server.getClients().stream()
                        .map(ClientHandler::getName)
                        .collect(Collectors.joining(" ")));*/
                sendMsg("-------");

            } else if (message.startsWith(SEND_TO_NICK)) {
                boolean sendOK = false;
                String[] pats = message.split("\\s+");
                if (pats.length > 1) {
                    for (ClientHandler client : server.getClients()) {
                        if (client.getName().equals(pats[1])) {
                            server.privateMessage(message, this.name);
                            sendMsg("to [" + pats[1] + "] " + message);
                            sendOK = true;
                        }
                    }
                }
                if (!sendOK) sendMsg("No such nick online");

            } else if (message.startsWith(SEND_TO_LIST)) {
                String[] splitedStr = message.split("\\s+");
                List<String> nicknames = new ArrayList<>();
                for (int i = 1; i < splitedStr.length - 1; i++) {
                    nicknames.add(splitedStr[i]);
                }

            } else if (message.startsWith(CHANGE_NICK)) {
                String[] pats = message.split("\\s+");
                name = server.getAuthService().change_nickname(pats[1], pats[2]);
                server.broarcastMessage("[" + pats[1] + "] " + " new nick " + "["+name+ "]");
            } else {
                server.broarcastMessage("[" + name + "] " + " speak " + message);
            }


        }
    }

    private void authentication() throws IOException, SQLException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(AUTH_COMMAND)) {
                String[] parts = message.split("\\s+");
                if (parts.length > 1) {
                    String nick = server.getAuthService().getNickByLoginAndPass(parts[1], parts[2]);
                    if (nick != null) {
                        if (!server.isNickBusy(nick)) {
                            sendMsg(AUTH_OK + " " + nick);
                            name = nick;
                            server.subscribe(this);
                            server.broarcastMessage("[" + name + "] " + " come to chat");
                            autorised = true;
                            return;
                        } else {
                            sendMsg("Nick is busy");
                        }
                    } else {
                        sendMsg("Invalid login/pass");
                    }
                } else sendMsg("Enter login and pass");
            }

        }
    }

    public void sendMsg(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setName(String name){
        this.name = name;
    }

    public void closeConnection() {
        server.unsubscribe(this);
        server.broarcastMessage(name + " out of chat");
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

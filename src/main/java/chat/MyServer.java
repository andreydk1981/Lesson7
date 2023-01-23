package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * непсредственно сервер
 */
public class MyServer {
    private List<ClientHandler> clients;
    private AuthService authService;
    public MyServer() throws SQLException {
        try (ServerSocket server = new ServerSocket(ChatConstants.PORT)) {
            authService = new SqlAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Server waiting for connections");
                Socket socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (authService != null) authService.stop();
        }

    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickBusy(String nick) {
        return clients.stream().anyMatch(client -> client.getName().equals(nick));
        /*for (ClientHandler client : clients) {
            return client.getName().equals(nick);
        }
        return false;*/
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void broarcastMessage(String message) {
        clients.forEach(client -> client.sendMsg(message));
        /*for (ClientHandler client : clients) {
            client.sendMsg(message);
        }*/
    }

    public synchronized void clientListMessage(String name){
        for (ClientHandler client : clients) {
            if (client.getName().equals(name)){
                client.sendMsg("online: " + clients.stream()
                        .map(ClientHandler::getName)
                        .collect(Collectors.joining(" ")));
            }
        }
    }
    public synchronized void broadcastMessageToClients(String message, List<String> nicknames){
        clients.stream()
                .filter(c -> nicknames.contains(c.getName()))
                .forEach(c -> c.sendMsg(message));
//        for (ClientHandler client : clients) {
//            if (nicknames.contains(client.getName())){
//                client.sendMsg(message);
//            }
//        }
    }
    public synchronized void privateMessage(String message, String name) {
        String[] parts = message.split("\\s+");
        for (ClientHandler client : clients) {
            if (client.getName().equals(parts[1])) {
                StringBuilder newMessage = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    newMessage.append(" ").append(parts[i]);
                }
                client.sendMsg("private [" + name + "] " + newMessage);
            }

        }
    }

    public List<ClientHandler> getClients() {
        return clients;
    }
}


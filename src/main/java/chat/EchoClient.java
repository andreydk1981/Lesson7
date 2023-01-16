package chat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import static chat.ChatConstants.*;


public class EchoClient extends JFrame {

    private JTextField textField;
    private JTextArea textArea;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String login;
    private HistoryFile historyFile;


    public EchoClient() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareUI();
    }

    private void openConnection() throws IOException {
        socket = new Socket(HOST, PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = dataInputStream.readUTF();
                    if (messageFromServer.equals(STOP_WORD)) {
                        textArea.append("Соединение разорвано");
                        textField.setEnabled(false);
                        closeConnection();
                        break;
                    } else if (messageFromServer.startsWith(AUTH_OK)) {
                        String[] tokens = messageFromServer.split("\\s+");
                        this.login = tokens[1];
                        textArea.append("Успешно авторизован как " + login);
                        textArea.append("\n");
                        historyFile = new HistoryFile(login);
                        historyFile.makeFile();

                        for (String mess : historyFile.readMessages()) {
                            textArea.append(mess);
                            textArea.append("\n");
                        }
                    } else if (messageFromServer.equals(CLEAR)) {
                        historyFile.clearHistory();
                        textArea.append("History deleted");
                        textArea.append("\n");
                    } else {
                        textArea.append(messageFromServer);
                        textArea.append("\n");
                        historyFile.write(messageFromServer);

                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }


    private void closeConnection() {
        System.out.println(login + " closeConnection");
        try {
            dataOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            dataInputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        historyFile.close();
    }

    private void sendMessage() {
        if (textField.getText().trim().isEmpty()) {
            return;
        }
        try {
            dataOutputStream.writeUTF(textField.getText());
            textField.setText("");
            textField.grabFocus();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void prepareUI() {
        setBounds(200, 200, 500, 500);
        setTitle("EchoClient");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);


        JPanel panel = new JPanel(new BorderLayout());
        JButton button = new JButton("Send");
        panel.add(button, BorderLayout.EAST);
        textField = new JTextField();
        panel.add(textField, BorderLayout.CENTER);

        add(panel, BorderLayout.SOUTH);

        JPanel loginPanel = new JPanel(new BorderLayout());
        JTextField loginField = new JTextField();
        loginPanel.add(loginField, BorderLayout.WEST);
        JTextField passField = new JTextField();
        loginPanel.add(passField, BorderLayout.CENTER);
        JButton authButton = new JButton("Авторизоваться");
        loginPanel.add(authButton, BorderLayout.EAST);
        add(loginPanel, BorderLayout.NORTH);

        authButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dataOutputStream.writeUTF(AUTH_COMMAND + " " + loginField.getText() + " " + passField.getText());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EchoClient::new);
    }


}
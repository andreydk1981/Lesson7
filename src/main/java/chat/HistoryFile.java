package chat;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HistoryFile {
    String nick;
    File file;
    DataOutputStream dataOutputStream;
    BufferedReader bufferedReader;

    public HistoryFile(String nick) {
        this.nick = nick;
        this.file = new File("history_" + nick + ".txt");
        try {
            dataOutputStream = new DataOutputStream(new FileOutputStream(file, true));
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeFile() {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("history file " + file.getName() + " for " + nick + " created");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("history file " + file.getName() + " for " + nick + " already exist");
        }
    }

    public void write(String message) {
        if (file.exists()) {
            try {
                dataOutputStream.writeUTF(message + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() {
        try {
            dataOutputStream.close();
            bufferedReader.close();
            System.out.println("file " + nick + " closed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<String> readMessages() throws IOException {
        if (file.exists()) {
            LinkedList<String> history = new LinkedList<>();
            String str;

            while ((str = bufferedReader.readLine()) != null) {
                if (history.size() < 10) {
                    history.addLast(str);
                } else {
                    history.addLast(str);
                    history.removeFirst();
                }
            }
            System.out.println("History loaded");
            return history;
        }
        LinkedList<String> history = new LinkedList<>();
        history.add("No history");
        return history;
    }

    public void clearHistory() throws IOException {
        dataOutputStream.close();
        if (file.delete()) {
            System.out.println("File del");
        }
        dataOutputStream = new DataOutputStream(new FileOutputStream(file, true));
        System.out.println("history file " + file.getName() + " for " + nick + " created");
    }
}

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5050;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Chat server started...");
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Başlangıçta mesajları alıp bağlı kullanıcılara gönder

        while (true) {
            new ClientHandler(serverSocket.accept()).start();
            sendInitialMessages();
        }
    }

    private static void sendInitialMessages() {
        MessageDAO messageDAO = new MessageDAO();
        List<String> messages = messageDAO.getMessages(); // Kaydedilen mesajları al

        for (String message : messages) {
            broadcastMessage(message); // Her bağlı kullanıcıya mesajı gönder
        }
    }

    private static void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                MessageDAO messageDAO = new MessageDAO();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    messageDAO.saveMessage("Server", message);

                    broadcastMessage(message); // Mesajı diğer kullanıcılara gönder
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}

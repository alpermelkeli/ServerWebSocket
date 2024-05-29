import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5050;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws IOException {

        System.out.println("Chat server started...");

        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private static void sendInitialMessages(PrintWriter writer) {
        MessageDAO messageDAO = new MessageDAO();
        List<String> messages = messageDAO.getMessages(); // Kaydedilen mesajları al

        for (String message : messages) {
            writer.println(message); // Yeni bağlanan kullanıcıya mesajı gönder
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
                UserDAO userDAO = new UserDAO();

                MessageDAO messageDAO = new MessageDAO();

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(socket.getOutputStream(), true);

                // Kullanıcı doğrulaması
                out.println("Enter username:");

                String username = in.readLine();

                out.println("Enter password:");

                String password = in.readLine();

                if (!userDAO.authenticateUser(username, password)) {
                    out.println("Authentication failed. Connection closing.");
                    socket.close();
                    return;
                }

                out.println("Authentication successful. You can start chatting.");

                sendInitialMessages(out);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;

                while ((message = in.readLine()) != null) {
                    System.out.printf("Received(%s): " + message+"\n", username);
                    messageDAO.saveMessage(username, message); // Mesajı veritabanına kaydet
                    broadcastMessage(username + ": " + message); // Mesajı diğer kullanıcılara gönder
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

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "34.27.179.203";
    private static final int SERVER_PORT = 5050;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        new Thread(new Reader(socket)).start();

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String message;
        while ((message = in.readLine()) != null) {
            out.println(message);
        }

        socket.close();
    }

    private static class Reader implements Runnable {
        private Socket socket;

        public Reader(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }
}

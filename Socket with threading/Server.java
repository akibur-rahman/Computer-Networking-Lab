import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        int maxClients = 5;
        int clientCount = 0;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Listening on port " + port);

            while (clientCount < maxClients) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;

                System.out.println("Client " + clientCount + " connected.");

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientCount);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server has reached its limit of serving clients.");
    }
}

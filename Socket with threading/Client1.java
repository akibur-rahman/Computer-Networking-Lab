import java.io.*;
import java.net.*;

public class Client1 {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 8080;

        try (
                Socket socket = new Socket(serverAddress, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            String userInput;
            System.out.println("Connected to server. You can start sending requests.");

            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equals("ENDS")) {
                    break;
                }

                String serverResponse = in.readLine();
                System.out.println("Server response: " + serverResponse);
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverAddress);
        }
    }
}

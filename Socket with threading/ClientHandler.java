import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private int clientNumber;

    public ClientHandler(Socket socket, int clientNumber) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
    }

    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("ENDS")) {
                    break;
                }

                String[] parts = inputLine.split(",");
                if (parts.length != 3) {
                    out.println("Invalid input. Please provide two integers and an operator separated by commas.");
                    continue;
                }

                try {
                    int num1 = Integer.parseInt(parts[0].trim());
                    int num2 = Integer.parseInt(parts[1].trim());
                    String operator = parts[2].trim();

                    int result = 0;
                    switch (operator) {
                        case "Sum":
                            result = num1 + num2;
                            break;
                        case "Subtract":
                            result = num1 - num2;
                            break;
                        case "Multiplication":
                            result = num1 * num2;
                            break;
                        case "Division":
                            result = num1 / num2;
                            break;
                        case "Modules":
                            result = num1 % num2;
                            break;
                        default:
                            out.println(
                                    "Invalid operator. Please choose Sum, Subtract, Multiplication, Division, or Modules.");
                            continue;
                    }

                    out.println("Result for Client " + clientNumber + ": " + result);
                } catch (NumberFormatException e) {
                    out.println("Invalid input. Please provide two integers and an operator separated by commas.");
                } catch (ArithmeticException e) {
                    out.println("Error: Division by zero.");
                }
            }

            System.out.println("Client " + clientNumber + " disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

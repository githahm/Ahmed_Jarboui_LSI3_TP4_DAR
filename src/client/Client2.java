package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client2 {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask for username manually
        System.out.print("Entrez votre nom d'utilisateur (Client 2) : ");
        String username = scanner.nextLine().trim();

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            // Receive thread
            Thread receiver = new Thread(() -> {
                byte[] buffer = new byte[1024];
                try {
                    while (!socket.isClosed()) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String msg = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                        System.out.println("\n" + msg);
                        System.out.print("> ");
                    }
                } catch (Exception e) { /* Ignore */ }
            });
            receiver.setDaemon(true);
            receiver.start();

            System.out.println("=== [" + username + "] Connecté ===");
            System.out.println("Tapez vos messages ('quit' pour quitter).");

            // Send loop
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("quit")) break;

                String message = "[" + username + "] : " + input;
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT));
            }
        } catch (Exception e) {
            System.err.println("Erreur Client 2 : " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UDPServer {

    private static final int PORT = 1234;
    private static final int BUFFER_SIZE = 1024;

    // Liste thread-safe des adresses clients enregistrées
    private static final Set<SocketAddress> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(PORT))) {
            System.out.println("[SERVEUR] En écoute sur le port " + PORT + " ...");

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(
                        packet.getData(), packet.getOffset(), packet.getLength(),
                        StandardCharsets.UTF_8);

                SocketAddress sender = packet.getSocketAddress();

                // Si c'est la première fois qu'on voit ce client, on l'ajoute à la liste
                if (clients.add(sender)) {
                    System.out.println("[SERVEUR] Nouveau client enregistré : " + sender);
                }

                System.out.printf("[SERVEUR] Message de %s -> %s%n", sender, message);

                // Rediffusion du message à tous les autres clients (sauf l'expéditeur)
                byte[] out = message.getBytes(StandardCharsets.UTF_8);
                for (SocketAddress client : clients) {
                    if (!client.equals(sender)) {
                        socket.send(new DatagramPacket(out, out.length, client));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SERVEUR] Erreur : " + e.getMessage());
        }
    }
}
package Bai2;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                writer.println("Enter your username:");
                username = reader.readLine();
                System.out.println(username + " has joined the chat.");

                synchronized (clientWriters) {
                    for (PrintWriter pw : clientWriters) {
                        pw.println(username + " has joined the chat.");
                    }
                }
                clientWriters.add(writer);

                String clientMessage;
                while ((clientMessage = reader.readLine()) != null) {
                    System.out.println(username + ": " + clientMessage);
                    synchronized (clientWriters) {
                        for (PrintWriter pw : clientWriters) {
                            pw.println(username + ": " + clientMessage);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    System.out.println(username + " has left the chat.");
                    synchronized (clientWriters) {
                        clientWriters.remove(writer);
                        for (PrintWriter pw : clientWriters) {
                            pw.println(username + " has left the chat.");
                        }
                    }
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

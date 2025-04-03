package sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This program is a server that takes connection requests on
 * the port specified by the constant LISTENING_PORT. When a
 * connection is opened, the program should allow the client to send it
 * messages. The messages should then
 * become visible to all other clients. The program will continue to receive
 * and process connections until it is killed (by a CONTROL-C,
 * for example).
 * 
 * This version of the program creates a new thread for
 * every connection request.
 */
public class ChatServer {
    private static final int LISTENING_PORT = 9876;
    private static ArrayBlockingQueue<ConnectionHandler> connections = new ArrayBlockingQueue<ConnectionHandler>(3);

    private static String generateId() {
        String chars = "1234567890ABCDEF";
        String id = "";

        for (int i = 0; i < 12; i++) {
            id += chars[Math.floor(Math.random() * 16)];
        }

        return id;
    }
    
    public static void main(String[] args) {

        ServerSocket listener; // Listens for incoming connections.
        Socket socket; // For communication with the connecting program.

        /* Accept and process connections forever, or until some error occurs. */

        try {
            listener = new ServerSocket(LISTENING_PORT);
            System.out.println("Listening on port " + LISTENING_PORT);

            while (true) {
                socket = listener.accept();
                ConnectionHandler conn = new ConnectionHandler(socket, generateId());
                conn.start();
            }
        } catch (Exception e) {
            System.out.println("Sorry, the server has shut down.");
            System.out.println("Error:  " + e);
            return;
        }

    } // end main()

    /**
     * Defines a thread that handles the connection with one
     * client.
     */
    private static class ConnectionHandler extends Thread {
        private Socket client;
        private String id;
        private ObjectOutputStream os;
        private ObjectInputStream is;

        ConnectionHandler(Socket socket, String assignedId) {
            client = socket;
            id = assignedId;
            
            try {
                // set up your streams, make sure this order is reversed on the client side!
                os = new ObjectOutputStream(socket.getOutputStream());
                is = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                try {
                    is = new ObjectInputStream(client.getInputStream());
                    os = new ObjectOutputStream(client.getOutputStream());


                    synchronized (connections) {
                        connections.add(this);
                    }

                    String message = (String) is.readObject();
                    while (message != null) {
                        System.out.println("Received: " + message);
                        broadcastMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (connections) {
                for (ConnectionHandler connection : connections) {
                    try {
                        connection.os.writeObject(message);
                    } catch (Exception e) {
                        System.out.println("Error");
                    }
                }
            }
        }
    }
}

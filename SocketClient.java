package sockets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
    static SendMessages sender;
    static Socket socket;

    static JFrame frame = new JFrame("Chat Client");
    static JLabel label = new JLabel("Chat Window");
    static JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
    static JTextField textField = new JTextField();
    static JButton button = new JButton("Send Message");
    
    public static void main(String[] args) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        label.setVerticalAlignment(1);
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        frame.add(label, BorderLayout.CENTER);

        try { 
            socket = new Socket(InetAddress.getLocalHost().getHostName(), 9876);
        } catch(Exception e) {
            System.out.println("Failed to establish connection");
        }

        sender = new SendMessages();
        Thread reciever = new Thread(new MessageReceiver(socket));

        sender.start();
        reciever.start();

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                try {
                    sender.send(message, socket);
                } catch(Exception err) {
                    System.out.println(err);
                }
            }
        });

        bottomPanel.add(textField);
        bottomPanel.add(button);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static class SendMessages extends Thread {
        public void send(String message, Socket socket) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
            System.out.println("Sent: " + message);

            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(message);

            oos.flush();
            oos.close();
            ois.close();
            socket.close();
        }

        public void run() {
            System.out.println("Waiting to send messages...");
        }
    }

    private static class MessageReceiver implements Runnable {
        private Socket socket;

        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                String serverMessage = (String) is.readObject();

                while (serverMessage != null) {
                    textField.setText(textField.getText() + serverMessage);
                }
            } catch (Exception e) {
                System.out.println("Connection closed.");
            }
        }
    }
}

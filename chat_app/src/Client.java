import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class Client extends JFrame {

    private String hostname;
    private int port = 8080;
    private String username;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public Client(String hostname) {
        this.hostname = hostname;
    }

    public Client () {
        displayHostnameInput();
    }

    private void displayHostnameInput() {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hostname Input");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1));

            JLabel label = new JLabel("Enter hostname:");
            JTextField textField = new JTextField();
            JButton connectButton = new JButton("Connect");

            connectButton.addActionListener(e -> {
                hostname = textField.getText().trim();
                if (!hostname.isEmpty()) {
                    frame.dispose();
                    connect();
                } else {
                    JOptionPane.showMessageDialog(null, "Hostname cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            panel.add(label);
            panel.add(textField);
            panel.add(connectButton);

            frame.add(panel);
            frame.setPreferredSize(new Dimension(400, 200));

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


    public void connect() {
        try {
            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to the server");
            startLogin();
        } catch (IOException e) {
            System.out.println("Client exception: " + e.getMessage());
        }
    }

    public void startLogin(){

        SwingUtilities.invokeLater(() -> {

            LoginWindow loginWindow = new LoginWindow(this);
            loginWindow.setVisible(true);

            loginWindow.setLoginListener((username, password) -> {
                out.println("LOGIN:" + username + ":" + password);
                out.flush();
                try {
                    String response = in.readLine();
                    if(response.startsWith("LOGIN_RESPONSE:")){
                        boolean loginResult = Boolean.parseBoolean(response.substring(15));
                        if(loginResult) {
                            Client.this.username = username;
                            loginWindow.dispose();
                            GlobalChatWindow globalChatWindow = new GlobalChatWindow(this);
                            globalChatWindow.setSize(500, 500);

                            new Thread(() -> {
                                try {
                                    String line;
                                    while ((line = in.readLine()) != null) {
                                        GlobalChatWindow.IndividualChatWindow individualChatWindow = null;
                                        if (line.startsWith("CHAT_HISTORY:")) {
                                            String chatHistory = line.substring(13);
                                            globalChatWindow.displayChatHistory(chatHistory);
                                        } else if (line.startsWith("CONNECTED_USERS:")) {
                                            String connectedUsers = line.substring(16);
                                            globalChatWindow.displayConnectedUsers(connectedUsers);
                                        } else if (line.startsWith("FRIEND_REQUEST:")) {
                                            String friend = line.substring(15);
                                            globalChatWindow.displayFriendRequest(friend);
                                        } else if (line.startsWith("ACCEPT_FRIEND_RESPONSE:")) {
                                            String friend = line.substring(23);
                                            globalChatWindow.displayFriendRequestAccepted(friend);
                                        } else if (line.startsWith("REJECT_FRIEND_RESPONSE:")) {
                                            String friend = line.substring(23);
                                            globalChatWindow.displayFriendRequestRejected(friend);

                                        } else if(line.startsWith("ADD_FRIEND_FAILURE_RESPONSE:")) {
                                            JOptionPane.showMessageDialog(null, "Failed to add friend", "Error", JOptionPane.ERROR_MESSAGE);
                                        } else if (line.startsWith("REMOVE_FRIEND_RESPONSE:")) {
                                            boolean removeFriendResult = Boolean.parseBoolean(line.substring(23));
                                            if (removeFriendResult) {
                                                JOptionPane.showMessageDialog(null, "Friend removed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                JOptionPane.showMessageDialog(null, "Failed to remove friend", "Error", JOptionPane.ERROR_MESSAGE);
                                            }

                                        } else if (line.startsWith("VIEW_FRIENDS_RESPONSE:")) {
                                            String friends = line.substring(22);
                                            globalChatWindow.displayFriends(friends);
                                        } else if (line.startsWith("CHAT_REQUEST:")) {
                                            String friend = line.substring(13);
                                            globalChatWindow.displayChatRequest(friend);
                                        } else if (line.startsWith("ACCEPT_CHAT_REQUEST:")) {
                                            String[] parts = line.substring(20).split(":");
                                            String friend = parts[0];
                                            individualChatWindow = new GlobalChatWindow.IndividualChatWindow(this, friend);
                                            individualChatWindow.setSize(300, 200);

                                        } else if (line.startsWith("REJECT_CHAT_REQUEST:")) {
                                            String friend = line.substring(20);
                                            globalChatWindow.displayChatRequestRejected(friend);
                                        } else {
                                            globalChatWindow.appendMessage(line);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.getMessage();
                                }
                            }).start();

                        } else {
                            //login failure
                            loginWindow.showLoginFailure();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            loginWindow.setRegisterListener((username, password) -> {
                out.println("REGISTER:" + username + ":" + password);
                out.flush();
                try {
                    String response = in.readLine();
                    if(response.startsWith("REGISTER_RESPONSE:")){
                        boolean registerResult = Boolean.parseBoolean(response.substring(18));
                        if(registerResult) {
                            JOptionPane.showMessageDialog(null, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public String getusername() {
        return username;
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Client client = new Client(args[0]);
            client.connect();
        } else {
            new Client();
        }
    }

}

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class GlobalChatWindow extends JFrame {

    private JTextArea displayMessageArea;
    private JTextField textField;
    private JButton sendButton;
    private JMenuBar menuBar;
    private Client client;
    private String friendUsername;

    private ArrayList<String> chatMessages = new ArrayList<>();

    public GlobalChatWindow(Client client) {

        this.client = client;

        setTitle("Global Chat: " + client.getusername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        setLookAndFeel();
    }

    private void initComponents() {

        initDisplayMessageArea();
        initMenuBar();
        initUserInputComponents();

        pack();
        setVisible(true);
        setLocationRelativeTo(null);

    }

    private void setLookAndFeel() {
        try {
            // gives the UI a modern look
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDisplayMessageArea() {

        displayMessageArea = new JTextArea();
        displayMessageArea.setEditable(false);
        displayMessageArea.setBackground(new Color(100, 170, 200));
        displayMessageArea.setForeground(Color.WHITE);
        displayMessageArea.setLineWrap(true);
        displayMessageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(displayMessageArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

    }

    private void initMenuBar() {

        menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        file.add(exit);

        JMenu chat = new JMenu("Chat History");
        JMenuItem save = new JMenuItem("Save Chat History");
        JMenuItem load = new JMenuItem("Load Chat History");
        JMenuItem clearHistory = new JMenuItem("Clear Chat History");
        chat.add(save);
        chat.add(load);
        chat.add(clearHistory);

        JMenu friends = new JMenu("Friends List");
        JMenuItem addFriend = new JMenuItem("Add Friend");
        JMenuItem removeFriend = new JMenuItem("Remove Friend");
        JMenuItem viewFriends = new JMenuItem("View Friends");
        JMenuItem requestChat = new JMenuItem("Request Chat");
        friends.add(addFriend);
        friends.add(removeFriend);
        friends.add(viewFriends);
        friends.add(requestChat);

        JMenu connectedUsers = new JMenu("Connected Users");
        JMenuItem viewConnectedUsers = new JMenuItem("View Connected Users");
        connectedUsers.add(viewConnectedUsers);

        menuBar.add(file);
        menuBar.add(chat);
        menuBar.add(friends);
        menuBar.add(connectedUsers);
        setJMenuBar(menuBar);

        exit.addActionListener(e -> exit());
        save.addActionListener(e -> saveChatHistory());
        load.addActionListener(e -> loadChatHistory());
        clearHistory.addActionListener(e -> clearHistory());
        viewConnectedUsers.addActionListener(e -> viewConnectedUsers());
        addFriend.addActionListener(e -> addFriend());
        removeFriend.addActionListener(e -> removeFriend());
        viewFriends.addActionListener(e -> viewFriends());
        requestChat.addActionListener(e -> requestChat());
    }

    private void initUserInputComponents() {

        textField = new JTextField();
        textField.setBackground(Color.GRAY);
        textField.setBorder(new LineBorder(Color.BLACK));
        textField.setForeground(Color.WHITE);
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        sendButton = new JButton("Send Message");
        sendButton.setBackground(new Color(255, 255, 200));
        sendButton.setForeground(Color.BLACK);
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        JPanel messagePanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("Enter a message:");
        messageLabel.setForeground(Color.BLACK);
        messagePanel.add(messageLabel, BorderLayout.WEST);
        messagePanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(messagePanel, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage("GLOBAL_MESSAGE:" + message);
            textField.setText("");
        } else if(!message.isEmpty()) {
            client.sendMessage("INDIVIDUAL_MESSAGE:" + friendUsername + ":" + message);
            textField.setText("");
        }
    }

    void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            displayMessageArea.append(message + "\n");
            chatMessages.add(message);
        });
    }

    private void exit() {
        client.sendMessage("LOGOUT:");
        dispose();
    }

    private void saveChatHistory() {

        ArrayList<String> chatHistory = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = now.format(formatter);

        chatHistory.add("Chat history saved on --> " + timestamp);
        chatHistory.addAll(chatMessages);
        client.sendMessage("SAVE_CHAT_HISTORY:" + client.getusername() + ":" + chatHistory);
    }

    private void loadChatHistory() {
        client.sendMessage("LOAD_CHAT_HISTORY:" + client.getusername());
    }

    private void clearHistory() {
        client.sendMessage("REMOVE_USER_CHAT_HISTORY:" + client.getusername());
    }

    private void viewConnectedUsers() {
        client.sendMessage("VIEW_CONNECTED_USERS:");
    }

    private void addFriend() {

        String friendUsername = JOptionPane.showInputDialog(this, "Enter the username of the friend you want to add:");
        if (friendUsername != null && !friendUsername.isEmpty() && !friendUsername.equals(client.getusername())) {
            client.sendMessage("ADD_FRIEND:" + client.getusername() + ":" + friendUsername);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeFriend() {

        String friendUsername = JOptionPane.showInputDialog(this, "Enter the username of the friend you want to remove:");
        if (friendUsername != null && !friendUsername.isEmpty()) {
            client.sendMessage("REMOVE_FRIEND:" + client.getusername() + ":" + friendUsername);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewFriends() {
        client.sendMessage("VIEW_FRIENDS:" + client.getusername());
    }

    private void requestChat() {
        //ask for the friend's username
        String friendUsername = JOptionPane.showInputDialog(this, "Enter the username of the friend you want to chat with:");
        if (friendUsername != null && !friendUsername.isEmpty() && !friendUsername.equals(client.getusername())) {
            client.sendMessage("REQUEST_CHAT:" + client.getusername() + ":" + friendUsername);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void displayChatHistory(String chatHistory) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chat History");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextArea chatHistoryArea = new JTextArea();
            chatHistoryArea.setEditable(false);
            chatHistoryArea.setBackground(new Color(100, 170, 200));
            chatHistoryArea.setForeground(Color.WHITE);
            chatHistoryArea.setLineWrap(true);
            chatHistoryArea.setWrapStyleWord(true);
            chatHistoryArea.append(chatHistory);

            JScrollPane scrollPane = new JScrollPane(chatHistoryArea);
            frame.add(scrollPane);
            frame.setPreferredSize(new Dimension(600, 400));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    void displayConnectedUsers(String connectedUsers) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Connected Users");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextArea connectedUsersArea = new JTextArea();
            connectedUsersArea.setEditable(false);
            connectedUsersArea.setBackground(new Color(100, 170, 200));
            connectedUsersArea.setForeground(Color.WHITE);

            connectedUsersArea.append(connectedUsers);

            JScrollPane scrollPane = new JScrollPane(connectedUsersArea);
            frame.add(scrollPane);
            frame.setPreferredSize(new Dimension(400, 200));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    void displayFriends(String friends) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Friends List");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextArea friendsArea = new JTextArea();
            friendsArea.setEditable(false);
            friendsArea.setBackground(new Color(100, 170, 200));
            friendsArea.setForeground(Color.WHITE);

            friendsArea.append(friends);

            JScrollPane scrollPane = new JScrollPane(friendsArea);
            frame.add(scrollPane);
            frame.setPreferredSize(new Dimension(400, 200));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


    void displayChatRequest(String friendUsername) {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(this, friendUsername + " wants to chat with you. Do you accept?", "Chat Request", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                client.sendMessage("ACCEPT_CHAT_REQUEST:" + client.getusername() + ":" + friendUsername);
            } else {
                client.sendMessage("REJECT_CHAT_REQUEST:" + client.getusername() + ":" + friendUsername);
            }
        });
    }


    void displayChatRequestRejected(String friendUsername) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, friendUsername + " has rejected your chat request", "Chat Request Rejected", JOptionPane.INFORMATION_MESSAGE);
        });
    }


    void displayFriendRequest(String friendUsername) {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(this, friendUsername + " wants to be your friend. Do you accept?", "Friend Request", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                client.sendMessage("ACCEPT_FRIEND_REQUEST:" + client.getusername() + ":" + friendUsername);
            } else {
                client.sendMessage("REJECT_FRIEND_REQUEST:" + client.getusername() + ":" + friendUsername);
            }
        });
    }


    void displayFriendRequestAccepted(String friendUsername) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, friendUsername + " has accepted your friend request", "Friend Request Accepted", JOptionPane.INFORMATION_MESSAGE);
        });
    }


    void displayFriendRequestRejected(String friendUsername) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, friendUsername + " has rejected your friend request", "Friend Request Rejected", JOptionPane.INFORMATION_MESSAGE);
        });
    }


    public static class IndividualChatWindow extends JFrame {

        private JTextArea displayMessageArea;
        private JTextField textField;
        private JButton sendButton;
        private String friendUsername;
        private Client client;

        public IndividualChatWindow(Client client, String friendUsername) {
            this.friendUsername = friendUsername;
            this.client = client;

            setTitle("Chat with " + friendUsername);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            initComponents();
            setLookAndFeel();
        }

        private void initComponents() {

            initDisplayMessageArea();
            initUserInputComponents();

            pack();
            setVisible(true);
            setLocationRelativeTo(null);

        }

        private void setLookAndFeel() {
            try {
                // gives the UI a modern look
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void initDisplayMessageArea() {

            displayMessageArea = new JTextArea();
            displayMessageArea.setEditable(false);
            displayMessageArea.setBackground(new Color(100, 170, 200));
            displayMessageArea.setForeground(Color.WHITE);
            JScrollPane scrollPane = new JScrollPane(displayMessageArea);
            getContentPane().add(scrollPane, BorderLayout.CENTER);

        }

        private void initUserInputComponents() {

            textField = new JTextField();
            textField.setBackground(Color.GRAY);
            textField.setBorder(new LineBorder(Color.BLACK));
            textField.setForeground(Color.WHITE);
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });

            sendButton = new JButton("Send Message");
            sendButton.setBackground(new Color(255, 255, 200));
            sendButton.setForeground(Color.BLACK);
            sendButton.addActionListener(e -> sendMessage());

            JPanel inputPanel = new JPanel(new BorderLayout());
            getContentPane().add(inputPanel, BorderLayout.SOUTH);

            JPanel messagePanel = new JPanel(new BorderLayout());
            JLabel messageLabel = new JLabel("Enter a message:");
            messageLabel.setForeground(Color.BLACK);
            messagePanel.add(messageLabel, BorderLayout.WEST);
            messagePanel.add(textField, BorderLayout.CENTER);
            inputPanel.add(messagePanel, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
        }

        private void sendMessage() {
            String message = textField.getText().trim();
            if (!message.isEmpty()) {
                client.sendMessage("INDIVIDUAL_MESSAGE:" + friendUsername + ":" + message);
                textField.setText("");
            }
        }

        //append message to the chat window
        void appendMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                displayMessageArea.append(message + "\n");
            });
        }
    }

}

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private int PORT;
    private Data data;
    private List<ClientHandler> connectedClients;
    private ServerSocket serverSocket;

    public Server(int port, Data data) {

        this.PORT = port;
        this.data = data;
        this.connectedClients = Collections.synchronizedList(new ArrayList<>());

    }

    public void serverStart() {

        try {

            InetAddress inetAddress = InetAddress.getByName("insert Server IP here");
            serverSocket = new ServerSocket(PORT, 0, inetAddress);

            System.out.println("Server is listening on port " + PORT);

            // add a shutdown hook to close the server socket when the server is stopped
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    data.clearOnlineStatus();
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Exception while closing server socket: " + e.getMessage());
                }
            }));

            while(true) {

                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                //create a new thread for the client
                ClientHandler clientHandler = new ClientHandler(socket, data);
                connectedClients.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch(IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    public class ClientHandler implements Runnable {

        private Socket clientSocket;
        private Data data;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket clientSocket, Data data) {
            this.clientSocket = clientSocket;
            this.data = data;

            try {
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Exception in client handler: " + e.getMessage());
            }
        }

        @Override
        public void run(){
            try{
                String input;
                while((input = in.readLine()) != null) {
                    System.out.println("Received message from client --> " + username + ": " +  input);
                    processMessage(input);
                }
            } catch (IOException e) {
                System.out.println("Exception in client handler: " + e.getMessage());
            } finally {
                System.out.println("Client disconnected --> " + username);
                handleClientDisconnect();
            }
        }

        private void processMessage(String message) {
            String[] parts = message.split(":", 2);
            String command = parts[0];
            String payload;

            switch (command) {
                case "LOGIN":
                    payload = parts[1];
                    handleLogin(payload);
                    break;
                case "LOGOUT":
                    handleClientDisconnect();
                    break;
                case "REGISTER":
                    payload = parts[1];
                    handleRegistration(payload);
                    break;
                case "GLOBAL_MESSAGE":
                    payload = parts[1];
                    handleGlobalMessage(payload);
                    break;
                case "INDIVIDUAL_MESSAGE":
                    payload = parts[1];
                    handleIndividualMessage(payload);
                    break;
                case "SAVE_CHAT_HISTORY":
                    payload = parts[1];
                    handleSaveChatHistory(payload);
                    break;
                case "LOAD_CHAT_HISTORY":
                    payload = parts[1];
                    handleLoadChatHistory(payload);
                    break;
                case "REMOVE_USER_CHAT_HISTORY":
                    payload = parts[1];
                    handleClearUserChatHistory(payload);
                    break;
                case "VIEW_CONNECTED_USERS":
                    handleViewConnectedUsers();
                    break;
                case "ADD_FRIEND":
                    payload = parts[1];
                    handleAddFriend(payload);
                    break;
                case "ACCEPT_FRIEND_REQUEST":
                    payload = parts[1];
                    handleAcceptFriendRequest(payload);
                    break;
                case "REJECT_FRIEND_REQUEST":
                    payload = parts[1];
                    handleRejectFriendRequest(payload);
                    break;
                case "REMOVE_FRIEND":
                    payload = parts[1];
                    handleRemoveFriend(payload);
                    break;
                case "VIEW_FRIENDS":
                    payload = parts[1];
                    handleViewFriends(payload);
                    break;
                case "REQUEST_CHAT":
                    payload = parts[1];
                    handleRequestChat(payload);
                    break;
                case "ACCEPT_CHAT_REQUEST":
                    payload = parts[1];
                    handleAcceptChatRequest(payload);
                    break;
                case "REJECT_CHAT_REQUEST":
                    payload = parts[1];
                    handleRejectChatRequest(payload);
                    break;
                //add more cases for other commands as we go
                default:
                    System.out.println("Unknown command: " + command);
            }
        }

        private void handleLogin(String payload) {
            String[] credentials = payload.split(":");
            boolean loginResult;

            if(credentials.length != 2) {
                sendMessage("LOGIN_RESPONSE:" + false);
                return;
            }

            String username = credentials[0];
            String password = credentials[1];

            if(username == null || password == null) {
                sendMessage("LOGIN_RESPONSE:" + false);
            } else {
                if(data.passwordCorrect(username, password) && !data.isOnline(username)) {
                    loginResult = true;
                } else {
                    loginResult = false;
                    sendMessage("LOGIN_RESPONSE:" + false);
                }
                if (loginResult) {
                    this.username = username;
                    data.addOnlineStatus(username, true);
                    data.saveOnlineStatus();
                    sendMessage("LOGIN_RESPONSE:" + true);
                    sendMessageToAllClients(username + " connected to the server.");
                }
            }
        }

        private void handleClientDisconnect() {
            data.loadOnlineStatus();
            if(username != null) {
                data.removeOnlineStatus(username);
            } else {
                System.out.println("Username is null");
            }
            data.saveOnlineStatus();
            sendMessageToAllClients(username + " disconnected from the server.");
            connectedClients.remove(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Exception while closing client socket: " + e.getMessage());
            }
        }

        private void handleRegistration(String payload) {
            String[] credentials = payload.split(":");
            if(credentials.length != 2 || credentials[0].isEmpty() || credentials[1].isEmpty()) {
                sendMessage("REGISTER_RESPONSE:" + false);
                return;
            }
            String username = credentials[0];
            String password = credentials[1];
            boolean registrationResult = data.registerUser(username, password);
            sendMessage("REGISTER_RESPONSE:" + registrationResult);
        }


        private void handleGlobalMessage(String payload) {
            sendMessageToAllClients(username + ": " + payload);
        }

        private void handleIndividualMessage(String payload) {
            String[] parts = payload.split(":");
            String receiver = parts[0];
            String message = parts[1];
            sendMessageToClient(receiver, username + ": " + message);
        }

        private void handleSaveChatHistory(String payload) {
            String[] parts = payload.split(":", 2);
                String username = parts[0];
                String chatHistory = parts[1];
                chatHistory = chatHistory.trim();
                data.addMessage(username, chatHistory);
                data.saveChatHistory();
        }

        private void handleLoadChatHistory(String username) {
            ArrayList<String> chatHistory = data.getChatHistory(username);
            sendMessage("CHAT_HISTORY:" + chatHistory);
        }

        private void handleClearUserChatHistory(String payload) {
            data.clearUserChatHistory(payload);
        }

        private void handleViewConnectedUsers() {
            ArrayList<String> connectedUsers = data.getOnlineUsers();
            sendMessage("CONNECTED_USERS:" + connectedUsers);
        }

        private void handleAddFriend(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            // if the friend is not already a friend and the friend is online
            if(!data.isFriend(username, friend) && data.isOnline(friend)) {
                sendMessageToClient(friend, "FRIEND_REQUEST:" + username);
            } else {
                sendMessage("ADD_FRIEND_FAILURE_RESPONSE:");
            }
        }

        private void handleAcceptFriendRequest(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            data.addFriend(username, friend);
            sendMessageToClient(friend, "ACCEPT_FRIEND_RESPONSE:" + username);
        }

        private void handleRejectFriendRequest(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            sendMessageToClient(friend, "REJECT_FRIEND_RESPONSE:" + username);
        }

        private void handleRemoveFriend(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            boolean removeFriendResult = data.removeFriend(username, friend);
            sendMessage("REMOVE_FRIEND_RESPONSE:" + removeFriendResult);
        }

        private void handleViewFriends(String username) {
            ArrayList<String> friends = data.getFriendsList(username);
            sendMessage("VIEW_FRIENDS_RESPONSE:" + friends);
        }

        private void handleRequestChat(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            if(data.isFriend(username, friend) && data.isOnline(friend)){
                sendMessageToClient(friend, "CHAT_REQUEST:" + username);
            }
        }

        private void handleAcceptChatRequest(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            if(data.isFriend(username, friend)){
                sendMessageToClient(friend, "ACCEPT_CHAT_REQUEST:" + username + ":" + friend);
                sendMessageToClient(username, "ACCEPT_CHAT_REQUEST:" + friend + ":" + username);
            }
        }

        private void handleRejectChatRequest(String payload) {
            String[] parts = payload.split(":");
            String username = parts[0];
            String friend = parts[1];
            if(data.isFriend(username, friend)){
                sendMessageToClient(friend, "REJECT_CHAT_REQUEST:" + username);
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }

        private void sendMessageToAllClients(String message) {
            for (ClientHandler client : connectedClients) {
                client.sendMessage(message);
            }
        }

        private void sendMessageToClient(String receiver, String message) {
            for (ClientHandler client : connectedClients) {
                if (client.username.equals(receiver)) {
                    client.sendMessage(message);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8080, new Data());
        server.serverStart();
    }
}
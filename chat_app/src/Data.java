import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Data implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LOGIN_PATH = "data_files/user_login_data.ser";
    private static final String CHAT_HISTORY_PATH = "data_files/chat_history.ser";
    private static final String FRIENDS_LIST_PATH = "data_files/friends_list.ser";
    private static final String ONLINE_STATUS_PATH = "data_files/online_status.ser";

    //data structures
    private ConcurrentHashMap<String, String> loginInfo;
    private ConcurrentHashMap<String, ArrayList<String>> chatHistory;
    private ConcurrentHashMap<String, ArrayList<String>> friendsList;
    private ConcurrentHashMap<String, Boolean> onlineStatus;

    //constructor
    public Data() {
        loginInfo = new ConcurrentHashMap<>();
        chatHistory = new ConcurrentHashMap<>();
        friendsList = new ConcurrentHashMap<>();
        onlineStatus = new ConcurrentHashMap<>();
        loadData();
    }

    public void loadData() {
        loadLoginInfo();
        loadChatHistory();
        loadFriendsList();
        loadOnlineStatus();
    }

    //serialization methods
    public synchronized void saveLoginInfo() {
        try (FileOutputStream fileOut = new FileOutputStream(LOGIN_PATH);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(loginInfo);
            System.out.println("Login info file updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadLoginInfo() {
        try (FileInputStream fileIn = new FileInputStream(LOGIN_PATH);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            Object obj = in.readObject();
            if (obj instanceof ConcurrentHashMap) {
                loginInfo.clear();
                loginInfo.putAll((ConcurrentHashMap<String, String>) obj);
                System.out.println("Login data loaded from file.");
            } else {
                System.out.println("Invalid data format found in " + LOGIN_PATH);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveChatHistory() {
        try (FileOutputStream fileOut = new FileOutputStream(CHAT_HISTORY_PATH);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(chatHistory);
            System.out.println("Chat history file updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadChatHistory() {
        try (FileInputStream fileIn = new FileInputStream(CHAT_HISTORY_PATH);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            Object obj = in.readObject();
            if (obj instanceof ConcurrentHashMap) {
                chatHistory.clear();
                chatHistory.putAll((ConcurrentHashMap<String, ArrayList<String>>) obj);
                System.out.println("Chat history loaded from file.");
            } else {
                System.out.println("Invalid data format found in chat_history.ser");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveFriendsList() {
        try (FileOutputStream fileOut = new FileOutputStream(FRIENDS_LIST_PATH);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(friendsList);
            System.out.println("Friends list file updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadFriendsList() {
        try (FileInputStream fileIn = new FileInputStream(FRIENDS_LIST_PATH);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            Object obj = in.readObject();
            if (obj instanceof ConcurrentHashMap) {
                friendsList.clear();
                friendsList.putAll((ConcurrentHashMap<String, ArrayList<String>>) obj);
                System.out.println("Friends list loaded from file.");
            } else {
                System.out.println("Invalid data format found in friends_list.ser");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveOnlineStatus() {
        try (FileOutputStream fileOut = new FileOutputStream(ONLINE_STATUS_PATH);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(onlineStatus);
            System.out.println("Online status file updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadOnlineStatus() {
        try (FileInputStream fileIn = new FileInputStream(ONLINE_STATUS_PATH);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            Object obj = in.readObject();
            if (obj instanceof ConcurrentHashMap) {
                onlineStatus.clear();
                onlineStatus.putAll((ConcurrentHashMap<String, Boolean>) obj);
                System.out.println("Online status loaded from file.");
            } else {
                System.out.println("Invalid data format found in online_status.ser");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //data manipulation methods
    public synchronized void addMessage(String username, String message) {
        if (chatHistory.containsKey(username)) {
            chatHistory.get(username).add(message);
        } else {
            ArrayList<String> messages = new ArrayList<>();
            messages.add(message);
            chatHistory.put(username, messages);
        }
    }

    public synchronized boolean addFriend(String username, String friend) {
        loadLoginInfo();
        if(loginInfo.contains(friend)) {
            friendsList.get(username).add(friend);
            friendsList.get(friend).add(username);
            saveFriendsList();
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean removeFriend(String username, String friend) {
        loadFriendsList();
        if (friendsList.containsKey(username) && friendsList.get(username).contains(friend)) {
            friendsList.get(username).remove(friend);
            friendsList.get(friend).remove(username);
            saveFriendsList();
            return true;
        } else {
            return false;
        }
    }

    public synchronized ArrayList<String> getFriendsList(String username) {
        return friendsList.get(username);
    }

    public synchronized boolean isFriend(String username, String friend) {
        if (friendsList.containsKey(username)) {
            return friendsList.get(username).contains(friend);
        }
        return false;
    }

    public synchronized ArrayList<String> getChatHistory(String username) {
        return chatHistory.get(username);
    }

    public synchronized void addUser(String username, String password) {
        loginInfo.put(username, password);
    }

    public synchronized boolean userExists(String username) {
        return loginInfo.containsKey(username);
    }

    public synchronized boolean passwordCorrect(String username, String password) {
        return loginInfo.containsKey(username) && loginInfo.get(username).equals(password);
    }

    public synchronized boolean registerUser(String username, String password) {
        if (!userExists(username)) {
            loginInfo.put(username, password);
            saveLoginInfo();
            chatHistory.put(username, new ArrayList<>());
            saveChatHistory();
            friendsList.put(username, new ArrayList<>());
            saveFriendsList();
            return true;
        }
        return false;
    }

    public synchronized boolean isOnline(String username) {
        return onlineStatus.containsKey(username);
    }

    public synchronized void addOnlineStatus(String username, boolean status) {
        onlineStatus.put(username, status);
    }

    public synchronized void removeOnlineStatus(String username) {
        onlineStatus.remove(username);
    }

    public synchronized ArrayList<String> getOnlineUsers() {
        ArrayList<String> onlineUsers = new ArrayList<>();
        for (String key : onlineStatus.keySet()) {
            if (onlineStatus.get(key)) {
                onlineUsers.add(key);
            }
        }
        return onlineUsers;
    }


    //clearing methods
    private void clearLoginInfo() {
        loginInfo.clear();
        System.out.println("Login info cleared.");
        saveLoginInfo();
    }

    private void clearChatHistory() {
        chatHistory.clear();
        System.out.println("Chat history cleared.");
        saveChatHistory();
    }

    public synchronized void clearUserChatHistory(String username) {
        chatHistory.get(username).clear();
        System.out.println("Chat history for " + username + " cleared.");
        saveChatHistory();
    }

    private void clearFriendsList() {
        friendsList.clear();
        System.out.println("Friends list cleared.");
        saveFriendsList();
    }

    public void clearOnlineStatus() {
        onlineStatus.clear();
        System.out.println("Online status cleared.");
        saveOnlineStatus();
    }

    //printing methods
    public void printLoginInfo() {
        for (String key : loginInfo.keySet()) {
            System.out.println(key + ": " + loginInfo.get(key));
        }
    }

    public void printChatHistory() {
        for (String key : chatHistory.keySet()) {
            System.out.println(key + ": " + chatHistory.get(key));
        }
    }

    public void printFriendsList() {
        for (String key : friendsList.keySet()) {
            System.out.println(key + ": " + friendsList.get(key));
        }
    }

    public void printOnlineStatus() {
        for (String key : onlineStatus.keySet()) {
            System.out.println(key + ": " + onlineStatus.get(key));
        }
    }

    //main method
    public static void main(String[] args) {
        Data data = new Data();
        data.clearChatHistory();
        data.clearLoginInfo();
        data.clearOnlineStatus();
        data.clearFriendsList();
    }
}

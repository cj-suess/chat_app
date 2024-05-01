public class Deserialize {
    //class to deserialize the data from a .ser file and methods to display it
    public static void main(String[] args) {
        //create an instance of the Data class
        Data data = new Data();
        System.out.println();

        System.out.println("Login info:");
        data.printLoginInfo();
        System.out.println();

        System.out.println("Chat history:");
        data.printChatHistory();
        System.out.println();

        System.out.println("Friends list:");
        data.printFriendsList();
        System.out.println();

        System.out.println("Online status:");
        data.printOnlineStatus();
    }
}



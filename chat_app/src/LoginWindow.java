import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private LoginListener loginListener;
    private RegisterListener registerListener;
    private Client client;

    public LoginWindow(Client client){

        this.client = client;

        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        JLabel usernameLabel = new JLabel("Username:");
        panel.add(usernameLabel);

        usernameField = new JTextField();
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (loginListener != null) {
                loginListener.onLogin(username, password);
            }
        });
        panel.add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            if (registerListener != null) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                registerListener.onRegister(username, password);
            }
        });
        panel.add(registerButton);

        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        add(panel);
        setLocationRelativeTo(null);

    }

    private void handleKeyPress(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (loginListener != null) {
                loginListener.onLogin(username, password);
            }
        }

    }

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public void setRegisterListener(RegisterListener registerListener) {
        this.registerListener = registerListener;
    }

    public void showLoginFailure(){
        JOptionPane.showMessageDialog(this, "Login failed. Incorrect credentials or user is already logged in.", "Error", JOptionPane.ERROR_MESSAGE);
    }

}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BankUI {
    private JFrame frame;
    private JLabel balanceLabel;
    private JTextField amountField;
    private double balance = 1000.00; // Starting balance

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankUI::new);
    }

    public BankUI() {
        // Create the main frame
        frame = new JFrame("Bank Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(5, 1));

        // Title Label
        JLabel titleLabel = new JLabel("Welcome to My Bank", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(titleLabel);

        // Balance Label
        balanceLabel = new JLabel("Current Balance: $1000.00", JLabel.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(balanceLabel);

        // Amount Input
        JPanel amountPanel = new JPanel();
        amountPanel.add(new JLabel("Enter Amount: "));
        amountField = new JTextField(10);
        amountPanel.add(amountField);
        frame.add(amountPanel);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        frame.add(buttonPanel);

        // Message Label
        JLabel messageLabel = new JLabel("", JLabel.CENTER);
        frame.add(messageLabel);

        // Add Action Listeners
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTransaction(true, messageLabel);
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTransaction(false, messageLabel);
            }
        });

        // Show frame
        frame.setVisible(true);
    }

    private void handleTransaction(boolean isDeposit, JLabel messageLabel) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                messageLabel.setText("Enter a positive amount!");
                messageLabel.setForeground(Color.RED);
                return;
            }

            if (isDeposit) {
                balance += amount;
                messageLabel.setText("Deposit successful!");
                messageLabel.setForeground(Color.GREEN);
            } else {
                if (amount > balance) {
                    messageLabel.setText("Insufficient balance!");
                    messageLabel.setForeground(Color.RED);
                    return;
                }
                balance -= amount;
                messageLabel.setText("Withdrawal successful!");
                messageLabel.setForeground(Color.GREEN);
            }

            // Update balance
            balanceLabel.setText(String.format("Current Balance: $%.2f", balance));
            amountField.setText("");

        } catch (NumberFormatException ex) {
            messageLabel.setText("Invalid amount! Please enter a number.");
            messageLabel.setForeground(Color.RED);
        }
    }
}

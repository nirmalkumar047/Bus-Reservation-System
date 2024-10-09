import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EasyBusReservationSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/BusReservationSystem";
    private static final String USER = "root";
    private static final String PASSWORD = "Geetha@1517";
    private static String loggedInUsername;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    static class BasePanel extends JPanel {
        private Image backgroundImage;

        public BasePanel() {
            // Load the background image
            backgroundImage = Toolkit.getDefaultToolkit().getImage("bus1.jpg");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw the image to cover the entire background
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Frame 1: Login Frame
    static class LoginFrame extends JFrame {
        private JTextField userField;
        private JPasswordField passField;

        public LoginFrame() {
            setTitle("Login");
            setSize(500, 500);
            setLayout(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BasePanel basePanel = new BasePanel();
            setContentPane(basePanel);
            basePanel.setLayout(null);

            JLabel userLabel = new JLabel("Username:");
            userLabel.setBounds(100, 100, 150, 30);
            userField = new JTextField();
            userField.setBounds(250, 100, 150, 30);

            JLabel passLabel = new JLabel("Password:");
            passLabel.setBounds(100, 150, 150, 30);
            passField = new JPasswordField();
            passField.setBounds(250, 150, 150, 30);

            JButton loginButton = new JButton("Login");
            loginButton.setBounds(200, 250, 100, 30);
            loginButton.addActionListener(e -> {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                loggedInUsername = username;
                saveUserToDatabase(username, password);
                dispose();
                new LocationFrame();
            });

            basePanel.add(userLabel);
            basePanel.add(userField);
            basePanel.add(passLabel);
            basePanel.add(passField);
            basePanel.add(loginButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void saveUserToDatabase(String username, String password) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String query = "INSERT INTO Users (username, password) VALUES (?, ?) ON DUPLICATE KEY UPDATE password = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    preparedStatement.setString(3, password);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving user: " + e.getMessage());
            }
        }
    }

    // Frame 2: Location Selection Frame
    static class LocationFrame extends JFrame {
        private JComboBox<String> boardingComboBox;
        private JComboBox<String> destinationComboBox;

        public LocationFrame() {
            setTitle("Select Location");
            setSize(500, 500);
            setLayout(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BasePanel basePanel = new BasePanel();
            setContentPane(basePanel);
            basePanel.setLayout(null);

            JLabel boardingLabel = new JLabel("Leaving from:");
            boardingLabel.setBounds(100, 100, 150, 30);
            boardingComboBox = new JComboBox<>(new String[]{"Chennai", "Chengalpattu", "Hyderabad", "Tirupati", "KanchiPuram"});
            boardingComboBox.setBounds(250, 100, 150, 30);

            JLabel destinationLabel = new JLabel("Going to:");
            destinationLabel.setBounds(100, 150, 150, 30);
            destinationComboBox = new JComboBox<>(new String[]{"Vizag", "Vijayawada", "Hyderabad", "Rajahmundry", "Kurnool"});
            destinationComboBox.setBounds(250, 150, 150, 30);

            JButton nextButton = new JButton("Next");
            nextButton.setBounds(200, 350, 100, 30);
            nextButton.addActionListener(e -> {
                dispose();
                new BusSelectionFrame(boardingComboBox.getSelectedItem().toString(),
                        destinationComboBox.getSelectedItem().toString());
            });

            basePanel.add(boardingLabel);
            basePanel.add(boardingComboBox);
            basePanel.add(destinationLabel);
            basePanel.add(destinationComboBox);
            basePanel.add(nextButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    // Frame 3: Bus and Seat Selection Frame
    static class BusSelectionFrame extends JFrame {
        private JComboBox<String> busComboBox;
        private String boardingPoint;
        private String destinationPoint;

        public BusSelectionFrame(String boardingPoint, String destinationPoint) {
            this.boardingPoint = boardingPoint;
            this.destinationPoint = destinationPoint;

            setTitle("Select Bus");
            setSize(500, 500);
            setLayout(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BasePanel basePanel = new BasePanel();
            setContentPane(basePanel);
            basePanel.setLayout(null);

            JLabel busLabel = new JLabel("Select Bus:");
            busLabel.setBounds(100, 100, 150, 30);
            busComboBox = new JComboBox<>(new String[]{"CHennaiExpress","INDIGO","indra travels","ICON bus", "Amaravati Travels", "Arthreya Travels" ,"shivatravels", "AirIndia", "Bharat_journey","Gudur travels"});
            busComboBox.setBounds(250, 100, 150, 30);

            JButton nextButton = new JButton("Select Seat");
            nextButton.setBounds(200, 250, 100, 30);
            nextButton.addActionListener(e -> {
                dispose();
                new SeatSelectionFrame(busComboBox.getSelectedItem().toString(), boardingPoint, destinationPoint);
            });

            basePanel.add(busLabel);
            basePanel.add(busComboBox);
            basePanel.add(nextButton);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    // Frame 4: Seat Selection Frame
    static class SeatSelectionFrame extends JFrame {
        private JPanel seatPanel;
        private String boardingPoint;
        private String destinationPoint;
        private List<Integer> bookedSeats = new ArrayList<>();

        public SeatSelectionFrame(String busName, String boardingPoint, String destinationPoint) {
            this.boardingPoint = boardingPoint;
            this.destinationPoint = destinationPoint;

            setTitle("Select Seat for " + busName);
            setSize(500, 500);
            setLayout(new BorderLayout());

            // Modify GridLayout to have 5 rows and 4 columns (4 seats)
            seatPanel = new JPanel();
            seatPanel.setLayout(new GridLayout(5, 4, 10, 10));

            // Load booked seats from the database
            loadBookedSeats(busName);

            for (int i = 1; i <= 20; i++) {
                final int seatNumber = i;
                JButton seatButton = new JButton("Seat " + seatNumber);

                // Color the button based on whether the seat is booked
                if (bookedSeats.contains(seatNumber)) {
                    seatButton.setBackground(Color.WHITE); // Set booked seats to white
                    seatButton.setEnabled(false); // Disable booking for already booked seats
                } else {
                    seatButton.setBackground(Color.GREEN);
                    seatButton.addActionListener(e -> {
                        String seatType = determineSeatType(seatNumber);
                        int price = seatType.equals("Window") ? 500 : 400;
                        saveBookingToDatabase(busName, seatNumber, seatType, boardingPoint, destinationPoint, price);
                        new TicketConfirmationFrame(busName, seatNumber, seatType, boardingPoint, destinationPoint, price);
                    });
                }

                seatPanel.add(seatButton); // Add the seat button directly
            }

            add(seatPanel, BorderLayout.CENTER);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void loadBookedSeats(String busName) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String query = "SELECT seat_number FROM Reservations WHERE bus_name = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, busName);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        bookedSeats.add(resultSet.getInt("seat_number"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error loading booked seats: " + e.getMessage());
            }
        }

        private String determineSeatType(int seatNumber) {
            if (seatNumber % 4 == 1 || seatNumber % 4 == 0) {
                return "Window";
            } else {
                return "Side";
            }
        }

        private void saveBookingToDatabase(String busName, int seatNumber, String seatType, String boardingPoint, String destinationPoint, int price) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String query = "INSERT INTO Reservations (username, bus_name, seat_number, seat_type, boarding_point, destination_point, price) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, loggedInUsername);
                    preparedStatement.setString(2, busName);
                    preparedStatement.setInt(3, seatNumber);
                    preparedStatement.setString(4, seatType);
                    preparedStatement.setString(5, boardingPoint);
                    preparedStatement.setString(6, destinationPoint);
                    preparedStatement.setInt(7, price);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving booking: " + e.getMessage());
            }
        }
    }

    // Frame 5: Ticket Confirmation Frame
static class TicketConfirmationFrame extends JFrame {
    public TicketConfirmationFrame(String busName, int seatNumber, String seatType, String boardingPoint, String destinationPoint, int price) {
        setTitle("Ticket Confirmation");
        setSize(400, 360);
        setLayout(null); // Set layout to null for absolute positioning
        getContentPane().setBackground(new Color(220, 240, 255)); // Light blue background

        // Create a panel for content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null); // Set layout to null for absolute positioning
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        contentPanel.setBackground(new Color(255, 255, 255)); // White background for content
        contentPanel.setBounds(20, 20, 360, 280); // Set bounds for the content panel
        add(contentPanel);

        // Title label
        JLabel titleLabel = new JLabel("Ticket Confirmation", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setBounds(0, 10, 360, 30); // Set bounds for the title label
        contentPanel.add(titleLabel);

        // Details labels
        JLabel busLabel = new JLabel("Bus Name: " + busName);
        busLabel.setBounds(20, 60, 320, 30);
        contentPanel.add(busLabel);

        JLabel seatLabel = new JLabel("Seat Number: " + seatNumber);
        seatLabel.setBounds(20, 90, 320, 30);
        contentPanel.add(seatLabel);

        JLabel typeLabel = new JLabel("Seat Type: " + seatType);
        typeLabel.setBounds(20, 120, 320, 30);
        contentPanel.add(typeLabel);

        JLabel boardingLabel = new JLabel("Boarding Point: " + boardingPoint);
        boardingLabel.setBounds(20, 150, 320, 30);
        contentPanel.add(boardingLabel);

        JLabel destinationLabel = new JLabel("Destination Point: " + destinationPoint);
        destinationLabel.setBounds(20, 180, 320, 30);
        contentPanel.add(destinationLabel);

        JLabel priceLabel = new JLabel("Price: " + price + " INR");
        priceLabel.setBounds(20, 200, 320, 30);
        contentPanel.add(priceLabel);

        // Continue button
        JButton thankYouButton = new JButton("Continue");
        thankYouButton.setBackground(new Color(0, 153, 76)); // Green background
        thankYouButton.setForeground(Color.WHITE);
        thankYouButton.setFont(new Font("Arial", Font.BOLD, 14));
        thankYouButton.setBounds(130, 240, 100, 30); // Set bounds for the button
        thankYouButton.addActionListener(e -> {
            dispose();
            new ThankYouFrame();
        });
        add(thankYouButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}


    // Frame 6: Thank You Frame
static class ThankYouFrame extends JFrame {
    public ThankYouFrame() {
        setTitle("Thank You");
        setSize(450, 300);
        setLayout(new GridLayout(3, 1));
        getContentPane().setBackground(new Color(240, 248, 255)); // Light cyan background

        // Create a panel for content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(2, 1));
        contentPanel.setBackground(new Color(255, 255, 255)); // White background for content

        JLabel thankYouLabel = new JLabel("Thank You for Booking!", JLabel.CENTER);
        thankYouLabel.setFont(new Font("Arial", Font.BOLD, 20));
        thankYouLabel.setForeground(new Color(0, 102, 204)); // Dark blue text
        contentPanel.add(thankYouLabel);

        JLabel messageLabel = new JLabel("We hope you have a great journey | Book EASY with EASYbus", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(messageLabel);

        add(contentPanel);

        // Close button
        JButton closeButton = new JButton("Back");
        closeButton.setBackground(new Color(0, 153, 76)); // Green background
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.addActionListener(e -> {
            dispose(); // Close the current frame
            new LocationFrame(); // Redirect to the LocationFrame for a new booking
        });
        add(closeButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
}
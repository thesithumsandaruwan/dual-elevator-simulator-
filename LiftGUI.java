import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.border.*;

public class LiftGUI extends JFrame {
    private static final int TOTAL_FLOORS = 10;
    private Lift lift1;
    private Lift lift2;
    private JButton[][] liftButtons;  // [lift][floor]
    private JButton[] upButtons;      // External up buttons
    private JButton[] downButtons;    // External down buttons
    private JLabel[] floorDisplays;   // Current floor displays
    private Timer timer;
    private JTextArea statusArea;  // Add this field
    private static final Color ACTIVE_BUTTON_COLOR = new Color(255, 200, 100);
    private static final Color DOOR_OPEN_COLOR = new Color(120, 200, 120);
    private static final Color DOOR_CLOSED_COLOR = new Color(200, 120, 120);

    public LiftGUI() {
        lift1 = new Lift(TOTAL_FLOORS);
        lift2 = new Lift(TOTAL_FLOORS);
        
        setTitle("Elevator Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
        startTimer();
        
        setSize(800, 800);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        liftButtons = new JButton[2][TOTAL_FLOORS + 1];
        upButtons = new JButton[TOTAL_FLOORS + 1];
        downButtons = new JButton[TOTAL_FLOORS + 1];
        floorDisplays = new JLabel[2];

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel liftsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        liftsPanel.add(createLiftPanel(lift1, 0, "Lift 1"));
        liftsPanel.add(createExternalButtonPanel());
        liftsPanel.add(createLiftPanel(lift2, 1, "Lift 2"));
        mainPanel.add(liftsPanel, BorderLayout.CENTER);

        // Create status display with larger, more visible text
        statusArea = new JTextArea(12, 40);
        statusArea.setFont(new Font("Consolas", Font.BOLD, 16));
        statusArea.setBackground(new Color(240, 240, 240));
        statusArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Elevator Status", 
            TitledBorder.CENTER, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createLiftPanel(Lift lift, int liftIndex, String title) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Floor display with larger text
        floorDisplays[liftIndex] = new JLabel("Floor: 1", SwingConstants.CENTER);
        floorDisplays[liftIndex].setFont(new Font("Arial", Font.BOLD, 20));
        floorDisplays[liftIndex].setOpaque(true);
        floorDisplays[liftIndex].setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(floorDisplays[liftIndex], BorderLayout.NORTH);

        // Button panel with improved styling
        JPanel buttonPanel = new JPanel(new GridLayout(TOTAL_FLOORS, 1, 3, 3));
        for (int floor = TOTAL_FLOORS; floor >= 1; floor--) {
            final int f = floor;
            JButton button = new JButton("Floor " + floor);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                lift.requestFloor(f);
                button.setBackground(ACTIVE_BUTTON_COLOR);
            });
            liftButtons[liftIndex][floor] = button;
            buttonPanel.add(button);
        }
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createExternalButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(TOTAL_FLOORS, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Call Buttons"));

        for (int floor = TOTAL_FLOORS; floor >= 1; floor--) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            final int f = floor;

            if (floor < TOTAL_FLOORS) {
                upButtons[floor] = new JButton("▲");
                upButtons[floor].addActionListener(e -> callNearestLift(f, Direction.UP));
                buttonPanel.add(upButtons[floor]);
            }

            if (floor > 1) {
                downButtons[floor] = new JButton("▼");
                downButtons[floor].addActionListener(e -> callNearestLift(f, Direction.DOWN));
                buttonPanel.add(downButtons[floor]);
            }

            panel.add(buttonPanel);
        }
        return panel;
    }

    private void callNearestLift(int floor, Direction direction) {
        int dist1 = Math.abs(lift1.getCurrentFloor() - floor);
        int dist2 = Math.abs(lift2.getCurrentFloor() - floor);

        if (dist1 <= dist2) {
            lift1.externalRequest(floor, direction);
        } else {
            lift2.externalRequest(floor, direction);
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                lift1.move();
                lift2.move();
                updateDisplay();
            }
        }, 0, 100);
    }

    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            // Update floor displays with colors
            for (int i = 0; i < 2; i++) {
                Lift lift = (i == 0) ? lift1 : lift2;
                JLabel display = floorDisplays[i];
                display.setText(String.format("Floor: %d  %s", 
                    lift.getCurrentFloor(),
                    lift.getDirection() == Direction.IDLE ? "IDLE" :
                    lift.getDirection().toString()
                ));
                display.setBackground(lift.isDoorOpen() ? DOOR_OPEN_COLOR : DOOR_CLOSED_COLOR);
            }
            
            // Update status area with improved visualization
            StringBuilder status = new StringBuilder();
            for (int i = 0; i < 40; i++) {
                status.append("█");
            }
            status.append("\n");
            for (int floor = TOTAL_FLOORS; floor >= 1; floor--) {
                status.append(String.format("%2d |%s|%s|\n",
                    floor,
                    getElevatorVisual(lift1, floor),
                    getElevatorVisual(lift2, floor)
                ));
            }
            for (int i = 0; i < 40; i++) {
                status.append("█");
            }
            statusArea.setText(status.toString());
            
            // Update button states
            updateButtonStates();
        });
    }
    
    private String getElevatorVisual(Lift lift, int floor) {
        String visual = "        ";  // 8 spaces
        if (lift.getCurrentFloor() == floor) {
            if (lift.isDoorOpen()) {
                visual = "  ]||[  ";  // Open doors
            } else if (lift.getDirection() == Direction.UP) {
                visual = "  ▲||▲  ";  // Moving up
            } else if (lift.getDirection() == Direction.DOWN) {
                visual = "  ▼||▼  ";  // Moving down
            } else {
                visual = "  |██|  ";  // Stopped
            }
        }
        return visual;
    }
    
    private void updateButtonStates() {
        for (int floor = 1; floor <= TOTAL_FLOORS; floor++) {
            // Reset button colors if the floor is reached
            if (lift1.getCurrentFloor() == floor && lift1.isDoorOpen()) {
                liftButtons[0][floor].setBackground(null);
            }
            if (lift2.getCurrentFloor() == floor && lift2.isDoorOpen()) {
                liftButtons[1][floor].setBackground(null);
            }
        }
    }
}
package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ContinuousCharReaderUI extends JFrame {
    private SimpleDriver simpleDriver;
    private JTextField inputField;

    public ContinuousCharReaderUI(SimpleDriver simpleDriver) {
        this.simpleDriver = simpleDriver;

        // Set up the frame
        setTitle("Continuous Character Reader");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Initialize the text field for input
        inputField = new JTextField(20);
        add(inputField);

        // Add key listener to the text field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();

                switch (ch) {
                    case 'x':
                        simpleDriver.setAccel(true);
                        break;
                    case 'z':
                        simpleDriver.setBrake(true);
                        break;
                    case 'j':
                        simpleDriver.setSteerLeft(true);
                        break;
                    case 'l':
                        simpleDriver.setSteerRight(true);
                        break;
                    case 'q':
                        System.exit(0);
                        break;
                }

                // Clear the text field
                inputField.setText("");
            }

            @Override
            public void keyReleased(KeyEvent e) {
                char ch = e.getKeyChar();

                switch (ch) {
                    case 'x':
                        simpleDriver.setAccel(false);
                        break;
                    case 'z':
                        simpleDriver.setBrake(false);
                        break;
                    case 'j':
                        simpleDriver.setSteerLeft(false);
                        break;
                    case 'l':
                        simpleDriver.setSteerRight(false);
                        break;
                    case 'q':
                        System.exit(0);
                        break;
                }
            }
        });

        // Make the frame visible
        setVisible(true);
    }

    public static void main(String[] args) {
        // Run the UI in the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            SimpleDriver simpleDriver = new SimpleDriver();
            new ContinuousCharReaderUI(simpleDriver);
        });
    }
}

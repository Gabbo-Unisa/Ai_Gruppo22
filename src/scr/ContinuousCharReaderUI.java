package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ContinuousCharReaderUI extends JFrame {
    private AcquisisciDriver acquisisciDriver;
    private JTextField inputField;

    public ContinuousCharReaderUI(AcquisisciDriver acquisisciDriver) {
        this.acquisisciDriver = acquisisciDriver;

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
            public void keyPressed(KeyEvent e) {
                char ch = e.getKeyChar();

                switch (ch) {
                    case ',':
                        acquisisciDriver.setAccel(true);
                        break;
                    case '.':
                        acquisisciDriver.setBrake(true);
                        break;
                    case 'a':
                        acquisisciDriver.setSteerLeft(true);
                        break;
                    case 'd':
                        acquisisciDriver.setSteerRight(true);
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
                    case ',':
                        acquisisciDriver.setAccel(false);
                        break;
                    case '.':
                        acquisisciDriver.setBrake(false);
                        break;
                    case 'a':
                        acquisisciDriver.setSteerLeft(false);
                        break;
                    case 'd':
                        acquisisciDriver.setSteerRight(false);
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
}

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

        // Creo un JTextField ma lo rendo “invisibile”
        inputField = new JTextField(20);
        /*
        inputField.setFocusable(true);              // deve poter catturare i tasti
        inputField.setOpaque(false);                // trasparente
        inputField.setBorder(null);
        inputField.setForeground(getBackground());  // testo invisibile
        inputField.setCaretColor(getBackground());  // caret invisibile
         */
        add(inputField);

        // Add key listener to the text field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char ch = e.getKeyChar();

                switch (ch) {
                    case 'w':
                        acquisisciDriver.setAccel(true);
                        break;
                    case 's':
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
                    case 'w':
                        acquisisciDriver.setAccel(false);
                        break;
                    case 's':
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

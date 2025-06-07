package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ContinuousCharReaderUI extends JFrame {
    private AcquisisciDriver acquisisciDriver;

    public ContinuousCharReaderUI(AcquisisciDriver acquisisciDriver) {
        this.acquisisciDriver = acquisisciDriver;

        // Imposta la finestra
        setTitle("Controller Input"); // Titolo più chiaro
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Rendi la finestra in grado di ricevere il focus per gli eventi della tastiera
        setFocusable(true);

        // Aggiungi il KeyListener direttamente alla finestra (JFrame)
        this.addKeyListener(new KeyAdapter() {
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
                }
            }
        });

        // Rendi la finestra visibile e richiedi il focus
        setVisible(true);
        requestFocusInWindow();
    }
}

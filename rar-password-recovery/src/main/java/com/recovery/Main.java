package com.recovery;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.recovery.view.RecoveryFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Se conserva el look and feel predeterminado.
                }
                RecoveryFrame frame = new RecoveryFrame();
                frame.setVisible(true);
            }
        });
    }
}

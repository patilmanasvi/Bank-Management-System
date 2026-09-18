package com.bank.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main - Application Entry Point
 * Corresponds to Section 12.6 of Student Implementation Manual.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}

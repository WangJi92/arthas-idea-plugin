package com.github.idea.arthas.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Vector;

/**
 * @author https://github.com/imyzt
 * @date 2024/3/12
 * @description add tunnel server
 */
public class AddTunnelServer extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField name;
    private JTextField tunnelAddress;
    private JTextField wsAddress;

    public AddTunnelServer(Project project) {

        setLocationRelativeTo(null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private static void showMessageDialog(String message) {
        String msg = new String(message.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        Messages.showErrorDialog(msg, "Tips");
    }

    private void onOK() {
        String nameText = this.name.getText();
        String addressText = this.tunnelAddress.getText();
        String wsAddressText = this.wsAddress.getText();
        if (addressText.isEmpty()) {
            showMessageDialog("Tunnel Address Field cannot be empty.");
        }
        if (nameText.isEmpty()) {
            showMessageDialog("Name Field cannot be empty.");
            return;
        }
        Vector<Vector> dataVector = AppSettingsPage.tableModel.getDataVector();
        if (dataVector != null) {
            for (Vector vector : dataVector) {
                String nameServer = vector.get(0).toString();
                if (Objects.equals(nameServer, nameText)) {
                    showMessageDialog("The Name must be unique.");
                    return;
                }
            }
        }

        AppSettingsPage.tableModel.addRow(new Object[]{nameText, addressText, wsAddressText});
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    /**
     * Open Window
     */
    public void open() {
        setTitle("Add Tunnel Server");
        setMaximumSize(new Dimension(500, 200));
        setMinimumSize(new Dimension(500, 200));
        setPreferredSize(new Dimension(500, 200));
        pack();
        setVisible(true);
    }
}

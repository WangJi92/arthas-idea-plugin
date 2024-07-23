package com.github.wangji92.arthas.plugin.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.wangji92.arthas.plugin.utils.HttpUtil;
import com.github.wangji92.arthas.plugin.utils.TunnelServerPath;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private JTextField address;

    public AddTunnelServer(Project project) {

        setLocationRelativeTo(null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });


        name.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextComponent) input).getText();
                if (text.isEmpty()) {
                    JLabel label = new JLabel("Field cannot be empty");
                    label.setPreferredSize(new Dimension(400, 200));
                    JOptionPane.showMessageDialog(contentPane, label);
                    return false;
                }
                Vector<Vector> dataVector = AppSettingsPage.tableModel.getDataVector();
                if (dataVector != null) {
                    for (Vector vector : dataVector) {
                        String nameServer = vector.get(0).toString();
                        if (Objects.equals(nameServer, text)) {
                            JLabel label = new JLabel("The Name must be unique.");
                            label.setPreferredSize(new Dimension(400, 200));
                            JOptionPane.showMessageDialog(contentPane, label);
                            return false;
                        }
                    }
                }
                return true;
            }
        });
        address.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextComponent) input).getText();
                if (text.isEmpty()) {
                    JLabel label = new JLabel("Field cannot be empty");
                    label.setPreferredSize(new Dimension(400, 200));
                    JOptionPane.showMessageDialog(contentPane, label);
                    return false;
                } else {
                    try {
                        String appsUrl = TunnelServerPath.getAppsPath(text);
                        JSONArray appIds = JSON.parseArray(HttpUtil.get(appsUrl));
                        if (appIds.isEmpty()) {
                            JLabel label = new JLabel("Agent not retrieved from URL");
                            label.setPreferredSize(new Dimension(400, 200));
                            JOptionPane.showMessageDialog(contentPane, label);
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        JLabel label = new JLabel("Invalid URL");
                        label.setPreferredSize(new Dimension(400, 200));
                        JOptionPane.showMessageDialog(contentPane, label);
                        return false;
                    }
                }
            }
        });

        name.addActionListener(e -> {
            String text = name.getText();
            System.out.println(text);
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String nameText = this.name.getText();
        String addressText = this.address.getText();
        AppSettingsPage.tableModel.addRow(new Object[]{nameText, addressText});
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
        pack();
        setVisible(true);
    }
}

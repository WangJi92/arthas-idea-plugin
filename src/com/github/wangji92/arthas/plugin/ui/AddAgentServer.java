package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.setting.ApplicationSettingsState;
import com.github.wangji92.arthas.plugin.web.entity.AgentServerInfo;
import com.intellij.openapi.components.ServiceManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author imyzt
 * @date 2024/3/12
 * @description add agent server
 */
public class AddAgentServer extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField name;
    private JTextField address;
    private JTextField gitBranch;

    private ApplicationSettingsState service;

    public AddAgentServer() {
        this.service = ApplicationSettingsState.getInstance();
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
                        new URL(text);
                        return true;
                    } catch (MalformedURLException e) {
                        JLabel label = new JLabel("Invalid URL");
                        label.setPreferredSize(new Dimension(400, 200));
                        JOptionPane.showMessageDialog(contentPane, label);
                        return false;
                    }
                }
            }
        });

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = name.getText();
                System.out.println(text);
            }
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
        String gitBranchText = this.gitBranch.getText();

        this.service.agentServerInfoList = this.service.agentServerInfoList == null ? new ArrayList<>() : this.service.agentServerInfoList;
        this.service.agentServerInfoList.add(new AgentServerInfo(nameText, addressText, gitBranchText));

        AppSettingsPage.tableModel.addRow(new Object[]{nameText, addressText, gitBranchText});
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("Add Agent Server");
        pack();
        setVisible(true);
    }
}

package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测
 *
 * @author 汪小哥
 * @date 11-01-2020
 */
public class ArthasTimeTunnelDialog extends JDialog {
    private JPanel contentPane;
    /**
     * tt -t 表达式构造
     */
    private JTextField ttTextField;
    /**
     * 获取tt 命令行Button
     */
    private JButton ttButton;
    /**
     * 常用命令下拉框
     */
    private JComboBox comboBox;

    /**
     * 获取命令的信息
     */
    private JButton comboBoxValueGetButton;
    /**
     * 关闭按钮
     */
    private JButton closeButton;

    /**
     * 帮助链接
     */
    private LinkLabel helpLink;

    /**
     * 最佳案列
     */
    private LinkLabel ttBestLink;

    /**
     * 表达式
     */
    private String timeTunnelExpression;
    /**
     * 工程信息
     */
    private Project project;


    public ArthasTimeTunnelDialog(Project project, String timeTunnelExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.timeTunnelExpression = timeTunnelExpression;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
    }

    private void init() {
        ttTextField.setText(this.timeTunnelExpression);
        ttButton.addActionListener(e -> {
            String text = ttTextField.getText();
            if (StringUtils.isNotBlank(text)) {
                ClipboardUtils.setClipboardString(text);
                NotifyUtils.notifyMessageDefault(project);
            }

        });

        comboBoxValueGetButton.addActionListener(e -> {
            String selectedItem = (String) comboBox.getSelectedItem();
            if (StringUtils.isNotBlank(selectedItem)) {
                ClipboardUtils.setClipboardString(selectedItem);
                NotifyUtils.notifyMessage(project, "选中的tt常用命令已经复制");
            }
        });

        //值太长展示不全处理 https://www.java-forums.org/awt-swing/16196-item-too-big-jcombobox.html
        comboBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                if (isSelected) {
                    comboBox.setToolTipText(value.toString());
                }

                setToolTipText(value.toString());
                Rectangle textRect =
                        new Rectangle(comboBox.getSize().width,
                                getPreferredSize().height);
                String shortText = SwingUtilities.layoutCompoundLabel(this,
                        getFontMetrics(getFont()),
                        value.toString(), null,
                        getVerticalAlignment(), getHorizontalAlignment(),
                        getHorizontalTextPosition(), getVerticalTextPosition(),
                        textRect, new Rectangle(), textRect,
                        getIconTextGap());
                setText(shortText);
                return this;
            }

        });
        closeButton.addActionListener(e -> onCancel());
    }

    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }


    private void createUIComponents() {
        ttBestLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/482");
            }
        });
        ttBestLink.setPaintUnderline(false);

        helpLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://alibaba.github.io/arthas/tt.html");
            }
        });
        helpLink.setPaintUnderline(false);
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);

    }
}

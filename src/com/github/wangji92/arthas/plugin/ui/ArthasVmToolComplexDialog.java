package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.command.CommandContext;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.ui.components.ActionLink;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;

public class ArthasVmToolComplexDialog extends JDialog {
    private static final Logger LOG = Logger.getInstance(ArthasVmToolComplexDialog.class);
    private JPanel contentPane;
    private ActionLink vmtoolHelpLabel;
    private JTextField vmToolExpressTextField;
    private ActionLink classloaderHelpLabel;
    private JTextField classloaderHashValueTextField;
    private JButton clearCacheButton;
    private JButton copyScCommandButton;
    private JButton instancesCommandButton;
    private JButton copyCommandButton;
    private JTable table1;

    private CommandContext commandContext;


    public ArthasVmToolComplexDialog(CommandContext commandContext) {
        this.commandContext = commandContext;
        setContentPane(contentPane);
        setModal(false);
        //getRootPane().setDefaultButton(buttonOK);

//        buttonOK.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onOK();
//            }
//        });

//        buttonCancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        });

        if ((commandContext.getPsiElement() instanceof PsiMethod)) {
            PsiMethod psiMethod = (PsiMethod) commandContext.getPsiElement();
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            if (parameters.length > 0) {
                table1.setVisible(true);
                DefaultTableModel model = new DefaultTableModel(parameters.length, 2);
                table1.setModel(model);
                table1.setRowHeight(30);

                // 创建并设置 JTextArea 渲染器
                JTextArea textAreaRenderer = new JTextArea();
                textAreaRenderer.setLineWrap(true);
                textAreaRenderer.setWrapStyleWord(true);
                //textAreaRenderer.setRows(3);
                textAreaRenderer.setEditable(true);
                TableColumn column1 = table1.getColumnModel().getColumn(1);
                // 设置渲染器
                column1.setCellRenderer(new TableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                        textAreaRenderer.setText((String) value);
                        textAreaRenderer.setFont(table.getFont());
                        return textAreaRenderer;
                    }
                });

                JLabel jLabel = new JLabel();
                TableColumn column0 = table1.getColumnModel().getColumn(0);
                column0.setMaxWidth(180);
                column0.setMinWidth(150);
                column0.setResizable(true);
                column0.setCellRenderer(new TableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        jLabel.setText((String) value);
                        jLabel.setFont(table.getFont());
                        return jLabel;
                    }
                });


                //table1.setDragEnabled(true);
                //table1.setFillsViewportHeight(true);
                int index = 0;
                for (PsiParameter parameter : parameters) {
                    //PsiElement declarationScope = parameter.getDeclarationScope();
                    String jsonString ="";
                    try {
                       // jsonString = PsiParserToJson.getInstance().toJSONString(parameter);
                    } catch (Exception e) {
                        LOG.error("error",e);
                    }
                    // String json = this.pojo2JSONParser.uElementToJSONString(uElement);
                   // String defaultParamValue = OgnlPsUtils.getDefaultString(parameter.getType(), parameter.getProject());
                    model.setValueAt(parameter.getName(), index, 0);
                    model.setValueAt(jsonString, index, 1);
                    index++;
                }
            }

        } else {
            table1.setVisible(false);
        }


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
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
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        classloaderHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
        vmtoolHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/vmtool.html");
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.commandContext.getProject()));
        setVisible(true);
    }
}

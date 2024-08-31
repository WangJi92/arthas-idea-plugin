package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
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

public class ArthasTimeTunnelSpringContextDialog extends JDialog {
    /**
     * 构造前缀
     */
    private static final String TT_FOR_SPRING_PROPERTY_PRE = "tt -w";
    /**
     * 构造获取spring 表达式的信息 参考 {@link com.github.wangji92.arthas.plugin.action.arthas.ArthasOgnlSpringAllPropertySourceCommandAction}
     */
    private static final String TT_FOR_SPRING_PROPERTY_CONTEXT = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=target.getApplicationContext()";

//    public static void main(String[] args) {
//        String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, TT_FOR_SPRING_PROPERTY_PRE, TT_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
//        System.out.println(command);
//    }

    private JButton closeButton;

    /**
     * 全量表达式
     */
    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private LinkLabel ognlOfficeLinkLabel;
    private LinkLabel oglSpecialLink;
    private LinkLabel ttInvokeAfterLink;
    private JTextField ttRequestMappingHandlerAdapterInvokeField;
    private JTextField timeTunnelIndexField;
    private JButton ttBeginButton;
    private LinkLabel ttInvokeBeforeHelp;
    private LinkLabel ttIndexLabel;
    /**
     * spring all 环境变量信息
     */
    private JButton springPropertyButton;

    /**
     * 获取目标对象的表达式
     */
    private JTextField aopTargetTextField;

    /**
     * 获取目标对象
     */
    private JButton aopTargetCommandButton;


    private String className;

    private String staticOgnlExpression;

    /**
     * aop 获取目标对象的表达式
     */
    private String aopTargetOgnlExpression;

    private Project project;


    public ArthasTimeTunnelSpringContextDialog(Project project, String className, String staticOgnlExpression, String aopTargetOgnlExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(null);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;
        this.aopTargetOgnlExpression = aopTargetOgnlExpression;


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
        ognlExpressionEditor.setText(this.staticOgnlExpression);
        // aop 获取目标对象 https://github.com/alibaba/arthas/issues/482
        // tt -i 1000 -w '#userServers=target.getApplicationContext().getBean("userService"),@org.springframework.aop.support.AopUtils@getTargetClass(#userServers)'
        aopTargetTextField.setText(this.aopTargetOgnlExpression);
        ttBeginButton.addActionListener(e -> {
            String text = ttRequestMappingHandlerAdapterInvokeField.getText();
            ClipboardUtils.setClipboardString(text);
            NotifyUtils.notifyMessage(project, "通过tt 获取spring context的命令可以多次使用,第一次使用需要触发一下一个接口的调用");
        });
        springPropertyButton.addActionListener((e -> {
            String timeTunnelIndex = timeTunnelIndexField.getText();
            if (StringUtils.isBlank(timeTunnelIndex)) {
                timeTunnelIndex = "1000";
            }
            String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, TT_FOR_SPRING_PROPERTY_PRE, TT_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
            String invokeCommand = String.join(" ", command, "-x", "3", "-i", timeTunnelIndex);
            ClipboardUtils.setClipboardString(invokeCommand);
            NotifyUtils.notifyMessage(project, "这里的-i 参数必须是通过tt 获取spring context的命令的tt index的值,获取指定项的值可以可以参考Ognl get selected spring property");
        }));

        // aop target 对象的信息
        aopTargetCommandButton.addActionListener(e -> onOK(aopTargetTextField.getText(), true));

        // 原始的获取方法的数据
        closeButton.addActionListener(e -> onOK(ognlExpressionEditor.getText(), false));

        //初始化数据
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        String ttSpringContextBeginPrefix = "tt -t org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter invokeHandlerMethod";
        String ttSpringContextBegin = String.join(" ", ttSpringContextBeginPrefix, "-n",invokeCount, conditionExpressDisplay);
        ttRequestMappingHandlerAdapterInvokeField.setText(ttSpringContextBegin);
    }


    /**
     * 关闭按钮回调
     */
    private void onOK(String ognCurrentExpression, boolean isAop) {
        String timeTunnelIndex = timeTunnelIndexField.getText();
        if (StringUtils.isBlank(timeTunnelIndex)) {
            timeTunnelIndex = "1000";
        }
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            AppSettingsState instance = AppSettingsState.getInstance(project);
            String depthPrintPropertyX = instance.depthPrintProperty;
            if (isAop) {
                depthPrintPropertyX = "1";
            }
            String invokeCommand = String.join(" ", ognCurrentExpression, "-x", depthPrintPropertyX, "-i", timeTunnelIndex);
            ClipboardUtils.setClipboardString(invokeCommand);
            NotifyUtils.notifyMessage(project, "这里的-i 参数必须是通过tt 获取spring context的命令的tt index的值，bean 名称可能不正确，可以手动修改");
        }
    }


    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        setMinimumSize(new Dimension(854, 200));
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);

    }


    private void createUIComponents() {
        ognlOfficeLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://commons.apache.org/dormant/commons-ognl/language-guide.html");
            }
        });
        ognlOfficeLinkLabel.setPaintUnderline(false);

        oglSpecialLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/71");
            }
        });
        oglSpecialLink.setPaintUnderline(false);

        //https://github.com/WangJi92/arthas-idea-plugin/issues/5

        ttInvokeBeforeHelp = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/482");
            }
        });
        ttInvokeBeforeHelp.setPaintUnderline(false);

        ttIndexLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/tt");
            }
        });
        ttIndexLabel.setPaintUnderline(false);

        ttInvokeAfterLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/4");
            }
        });
        ttInvokeAfterLink.setPaintUnderline(false);
    }

}

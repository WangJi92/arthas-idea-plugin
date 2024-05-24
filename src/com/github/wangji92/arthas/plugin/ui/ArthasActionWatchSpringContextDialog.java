package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ArthasActionWatchSpringContextDialog extends JDialog {

    /**
     * 构造前缀
     */
    private static final String WATCH_FOR_SPRING_PROPERTY_PRE = "watch -x 3 -n 1  org.springframework.web.servlet.DispatcherServlet doDispatch";
    /**
     * 构造获取spring 表达式的信息 参考 {@link com.github.wangji92.arthas.plugin.action.arthas.ArthasOgnlSpringAllPropertySourceCommandAction}
     */
    private static final String WATCH_FOR_SPRING_PROPERTY_CONTEXT = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=@org.springframework.web.context.support.WebApplicationContextUtils@getWebApplicationContext(params[0].getServletContext())";

    //    public static void main(String[] args) {
//        String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, WATCH_FOR_SPRING_PROPERTY_PRE, WATCH_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
//        System.out.println(command);
//    }
    private JButton closeButton;

    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private ActionLink ognlOfficeActionLink;
    private ActionLink ognlDemoLink;
    private ActionLink watchHelpLink;
    /**
     * spring 所有环境配置项信息获取
     */
    private JButton springPropertyButton;
    /**
     * 代理对象的原始对象的信息
     */
    private JButton springNonProxyTargetButton;


    private String className;

    private String staticOgnlExpression;

    private Project project;

    private String aopTargetOgnlExpression;


    public ArthasActionWatchSpringContextDialog(Project project, String className, String staticOgnlExpression, String aopTargetOgnlExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;
        this.aopTargetOgnlExpression = aopTargetOgnlExpression;

        closeButton.addActionListener(e -> onOK());


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
        springPropertyButton.addActionListener((e -> {
            String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, WATCH_FOR_SPRING_PROPERTY_PRE, WATCH_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, "由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同,获取指定项的值可以可以参考Ognl get selected spring property");
        }));

        springNonProxyTargetButton.addActionListener(e -> {
            ClipboardUtils.setClipboardString(aopTargetOgnlExpression);
            NotifyUtils.notifyMessage(project, "Bean 名称可能不正确可以手动修改,由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同");
        });
    }


    /**
     * 取人按钮回调
     */
    private void onOK() {
        String ognCurrentExpression = ognlExpressionEditor.getText();
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            ClipboardUtils.setClipboardString(ognCurrentExpression);
            NotifyUtils.notifyMessage(project, "Bean 名称可能不正确可以手动修改,由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同");
        }
        dispose();
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
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);

    }


    private void createUIComponents() {
        ognlOfficeActionLink = ActionLinkUtils.newActionLink("https://commons.apache.org/dormant/commons-ognl/language-guide.html");
        ognlDemoLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin/issues/5");

        //https://github.com/WangJi92/arthas-idea-plugin/issues/5

        watchHelpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/arthas/watch");
    }
}

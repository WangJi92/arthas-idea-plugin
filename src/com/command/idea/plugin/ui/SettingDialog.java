package com.command.idea.plugin.ui;

import com.command.idea.plugin.constants.ArthasCommandConstants;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * 设置界面处理
 * {@literal https://cloud.tencent.com/developer/article/1348741 }
 * {@literal https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html }
 * {@literal http://corochann.com/intellij-plugin-development-introduction-persiststatecomponent-903.html }
 * {@literal https://blog.xiaohansong.com/idea-plugin-development.html}
 * {@literal http://www.dcalabresi.com/blog/java/spring-context-static-class/}
 *
 * @author jet
 * @date 22-12-2019
 */
public class SettingDialog implements Configurable {
    private JTextField springContextStaticOgnlExpressionTextFiled;
    private JLabel springContextParamLabel;
    private JPanel contentPane;
    private LinkLabel linkLable;

    public SettingDialog() {
        initData();
    }

    private void initData() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String oldspringContextEx = propertiesComponent.getValue(ArthasCommandConstants.PRO_PREFIX + ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        springContextStaticOgnlExpressionTextFiled.setText(oldspringContextEx);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "ArthasIdeaPlugin";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPane;
    }

    @Override
    public boolean isModified() {
        String springContextStaticOgnlExpressionText = springContextStaticOgnlExpressionTextFiled.getText();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String oldspringContextEx = propertiesComponent.getValue(ArthasCommandConstants.PRO_PREFIX + ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        return !Objects.equals(springContextStaticOgnlExpressionText, oldspringContextEx);
    }

    @Override
    public void apply() throws ConfigurationException {

        //获取 application 级别的 PropertiesComponent
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String springContextStaticOgnlExpressionText = springContextStaticOgnlExpressionTextFiled.getText();
        propertiesComponent.setValue(ArthasCommandConstants.PRO_PREFIX + ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, springContextStaticOgnlExpressionText);


    }

    @Override
    public void reset() {

    }

    private void createUIComponents() {
        /**
         * 处理访问链接 参考 {@literal https://github.com/YiiGuxing/TranslationPlugin}
         */
        linkLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("http://www.dcalabresi.com/blog/java/spring-context-static-class/");
            }
        });
        linkLable.setPaintUnderline(false);
    }
}

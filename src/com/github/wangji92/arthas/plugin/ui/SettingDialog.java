package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.ActionLink;
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
 * @author 汪小哥
 * @date 22-12-2019
 */
@Deprecated
public class SettingDialog implements Configurable {
    private JTextField springContextStaticOgnlExpressionTextFiled;
    private JPanel contentPane;
    private ActionLink linkLable;

    public SettingDialog() {
        initData();
    }

    private void initData() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String oldspringContextEx = propertiesComponent.getValue(ArthasCommandConstants.PRO_PREFIX + ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        if (StringUtils.isBlank(oldspringContextEx)) {
            oldspringContextEx = ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING;
            PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, oldspringContextEx);

        }
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
        return !Objects.equals(springContextStaticOgnlExpressionText, PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION));
    }

    @Override
    public void apply() throws ConfigurationException {
        String springContextStaticOgnlExpressionText = springContextStaticOgnlExpressionTextFiled.getText();
        PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, springContextStaticOgnlExpressionText);


    }

    @Override
    public void reset() {

    }

    private void createUIComponents() {
        /**
         * 处理访问链接 参考 {@literal https://github.com/YiiGuxing/TranslationPlugin}
         */
        linkLable = ActionLinkUtils.newActionLink("http://www.dcalabresi.com/blog/java/spring-context-static-class/");
    }
}

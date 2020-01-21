package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.google.common.base.Splitter;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 参考 : https://blog.csdn.net/xunjiushi9717/article/details/94050139
 * <p>
 * org.springframework.core.env.MutablePropertySources 中的优先级 addFirst高  addLast低
 *
 * 下一个版本可以考虑一下添加spring 属性值，感觉这个用得比较少 https://my.oschina.net/u/2263272/blog/1824864
 *
 * @author 汪小哥
 * @date 21-01-2020
 */
public class ArthasOgnlSpringAllPropertySourceCommandAction extends AnAction {

    private static final String SPRING_ALL_PROPERTY = "%s '%s#allProperties={},#standardServletEnvironment=#propertySourceIterator=%s.getEnvironment(),#propertySourceIterator=#standardServletEnvironment.getPropertySources().iterator(),#propertySourceIterator.{#key=#this.getName(),#allProperties.add(\"                \"),#allProperties.add(\"------------------------- name:\"+#key),#this.getSource() instanceof java.util.Map ?#this.getSource().entrySet().iterator.{#key=#this.key,#allProperties.add(#key+\"=\"+#standardServletEnvironment.getProperty(#key))}:#{}},#allProperties'";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }

        //这里获取spring context的信息
        String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        if (StringUtils.isBlank(springContextValue)) {
            NotifyUtils.notifyMessage(project, "配置 arthas 插件spring context 获取的信息", NotificationType.ERROR);
            return;
        }
        // 获取class的classloader
        List<String> springContextCLass = Splitter.on('@').omitEmptyStrings().splitToList(springContextValue);
        if (CollectionUtils.isEmpty(springContextCLass)) {
            NotifyUtils.notifyMessage(project, "配置 arthas 插件spring context 获取的信息", NotificationType.ERROR);
        }
        String className = springContextCLass.get(0);

        springContextValue = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=" + springContextValue;
        if (!springContextValue.endsWith(",")) {
            springContextValue = springContextValue + ",";
        }
        //ognl -x 3 '#springContext=@applicationContextProvider@context,
        String join = String.join(" ", "ognl", "-x", ArthasCommandConstants.RESULT_X);

        String command = String.format(SPRING_ALL_PROPERTY, join, springContextValue, ArthasCommandConstants.SPRING_CONTEXT_PARAM);

        new ArthasActionStaticDialog(project, className, command).open("arthas ognl spring get all property");
    }
}

package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.common.command.CommandContext;
import com.github.idea.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.idea.arthas.plugin.ui.ArthasVmToolDialog;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.SpringStaticContextUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 参考 : https://blog.csdn.net/xunjiushi9717/article/details/94050139
 * 参考 : https://github.com/alibaba/arthas/issues/641
 * 参考 : https://github.com/alibaba/arthas/issues/71
 * 参考 : https://commons.apache.org/dormant/commons-ognl/language-guide.html
 * <p>
 * org.springframework.core.env.MutablePropertySources 中的优先级 addFirst高  addLast低
 * <p>
 * 下一个版本可以考虑一下添加spring 属性值，感觉这个用得比较少 https://my.oschina.net/u/2263272/blog/1824864
 *
 * @author 汪小哥
 * @date 21-01-2020
 */
public class ArthasOgnlSpringAllPropertySourceCommandAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }

        if (!SpringStaticContextUtils.booleanConfigStaticSpringContext(project)) {
            //if you not set static spring context,you can use vmtool
            CommandContext commandContext = new CommandContext(e);
            ShellScriptCommandEnum vmToolSpringEnv = ShellScriptCommandEnum.VM_TOOL_SPRING_ENV;
            String arthasCommand = vmToolSpringEnv.getArthasCommand(commandContext);
            String instancesCommand = "vmtool -x  1 --action getInstances --className org.springframework.core.env.ConfigurableEnvironment  --limit 5 ";
            ArthasVmToolDialog dialog = new ArthasVmToolDialog(project, "org.springframework.core.env.ConfigurableEnvironment", arthasCommand, instancesCommand);
            dialog.open("vmtool get all spring property,first keyword has the highest priority");
            return;
        }

        // ognl static spring context
        try {
            // 获取class的classloader @applicationContextProvider@context的前面部分 xxxApplicationContextProvider
            String className = SpringStaticContextUtils.getStaticSpringContextClassName(project);

            //#springContext=@applicationContextProvider@context
            String springContextValue = SpringStaticContextUtils.getStaticSpringContextPrefix(project);

            //ognl -x 3 
            AppSettingsState instance = AppSettingsState.getInstance(project);
            String depthPrintProperty = instance.depthPrintProperty;
            String join = String.join(" ", "ognl", "-x", depthPrintProperty);

            String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, join, springContextValue);

            SwingUtilities.invokeLater(() -> {
                new ArthasActionStaticDialog(project, className, command, "").open("Ognl get all spring property,first keyword has the highest priority");
            });
        } catch (Exception ex) {
            NotifyUtils.notifyMessage(project, ex.getMessage(), NotificationType.ERROR);
            return;
        }


    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

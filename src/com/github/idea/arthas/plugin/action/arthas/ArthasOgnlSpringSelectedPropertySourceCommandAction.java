package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.SpringStaticContextUtils;
import com.github.idea.arthas.plugin.utils.StringUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 获取当前spring 中的配置文件的信息
 * <p>
 * ognl -x 3 '#springContext=@applicationContextProvider@context,#springContext.getEnvironment().getProperty("spring.gd.config.type")' -c 68ceda24
 *
 * @author 汪小哥
 * @date 21-01-2020
 */
public class ArthasOgnlSpringSelectedPropertySourceCommandAction extends AnAction {

    /**
     * 构造表达式
     */
    private static final String SPRING_ENVIRONMENT_PROPERTY = "%s '%s,#springContext.getEnvironment().getProperty(\"%s\")'";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        if (!SpringStaticContextUtils.booleanConfigStaticSpringContextFalseOpenConfig(project)) {
            return;
        }

        // ognl static spring context
        //https://zhuanlan.zhihu.com/p/47740017
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText != null) {
            //去掉多余的空格
            selectedText = selectedText.trim();
        }
        if (StringUtils.isBlank(selectedText)) {
            selectedText = "spring.profiles.active";
        }
        try {
            // 获取class的classloader @applicationContextProvider@context的前面部分 xxxApplicationContextProvider
            String className = SpringStaticContextUtils.getStaticSpringContextClassName(project);

            //#springContext=@applicationContextProvider@context
            String springContextValue = SpringStaticContextUtils.getStaticSpringContextPrefix(project);

            AppSettingsState instance = AppSettingsState.getInstance(project);
            String depthPrintProperty = instance.depthPrintProperty;

            //ognl -x 3 '#springContext=@applicationContextProvider@context
            String join = String.join(" ", "ognl", "-x", depthPrintProperty);

            String command = String.format(SPRING_ENVIRONMENT_PROPERTY, join, springContextValue, selectedText);

            SwingUtilities.invokeLater(() -> {
                new ArthasActionStaticDialog(project, className, command, "").open("Ognl get selected spring property");
            });

        } catch (Exception ex) {
            NotifyUtils.notifyMessage(project, ex.getMessage(), NotificationType.ERROR);
            return;
        }


    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            e.getPresentation().setEnabled(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

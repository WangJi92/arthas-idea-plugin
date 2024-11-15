package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.common.command.CommandContext;
import com.github.idea.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.idea.arthas.plugin.ui.ArthasVmToolDialog;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 获取当前spring 中的配置文件的信息
 * <p>
 *
 * @author 汪小哥
 * @date 21-01-2020
 */
public class ArthasVmtoolSpringSelectedPropertySourceCommandAction extends AnAction {

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

        SwingUtilities.invokeLater(() -> {
            //if you not set static spring context,you can use vmtool
            CommandContext commandContext = new CommandContext(e);
            ShellScriptCommandEnum toolSpringSelectEnv = ShellScriptCommandEnum.VM_TOOL_SPRING_SELECT_ENV;
            String arthasCommand = toolSpringSelectEnv.getArthasCommand(commandContext);
            String instancesCommand = "vmtool -x  1 --action getInstances --className org.springframework.core.env.ConfigurableEnvironment  --limit 5 ";
            ArthasVmToolDialog dialog = new ArthasVmToolDialog(project, "org.springframework.core.env.ConfigurableEnvironment", arthasCommand, instancesCommand);
            dialog.open("vmtool get selected spring property");
        });
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

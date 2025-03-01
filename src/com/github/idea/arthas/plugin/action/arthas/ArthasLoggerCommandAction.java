package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.ui.ArthasLoggerDialog;
import com.github.idea.arthas.plugin.utils.OgnlPsUtils;
import com.github.idea.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * logger --name sample.mybatis.SampleXmlApplication --l warn
 *
 * @author 汪小哥
 * @date 18-04-2020
 */
public class ArthasLoggerCommandAction extends AnAction {


    public void doCommand(String loggerName, Project project) {

        SwingUtilities.invokeLater(() -> {
            new ArthasLoggerDialog(project, loggerName).open("Print logger info, and update the logger level");
        });

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        /**
         * {@link com.intellij.ide.actions.CopyReferenceAction}
         */
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        String loggerName = "";
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (psiElement != null) {
            loggerName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiElement);
        }
        // https://github.com/WangJi92/arthas-idea-plugin/issues/165
        if (StringUtils.isBlank(loggerName)) {
            try {
                // 获取选中的部分的logger Name ..
                // private static Logger log = org.slf4j.LoggerFactory.getLogger("GlobalExceptionHandler");
                Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
                if (editor != null) {
                    String selectedText = editor.getSelectionModel().getSelectedText();
                    if (StringUtils.isNotBlank(selectedText)) {
                        //去掉多余的空格
                        loggerName = selectedText.trim();
                    }
                }
            } catch (Exception e) {
                //ignore
            }
        }

        if (StringUtils.isBlank(loggerName)) {
            loggerName = "ROOT";
        }
        doCommand(loggerName, project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

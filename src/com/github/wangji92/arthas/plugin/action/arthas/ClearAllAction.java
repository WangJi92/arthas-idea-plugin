package com.github.wangji92.arthas.plugin.action.arthas;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * ClearAllAction
 * @author huangxingguang
 */
public class ClearAllAction extends DumbAwareAction {
    private final ConsoleViewImpl consoleView;

    public ClearAllAction(ConsoleViewImpl consoleView) {
        super(ExecutionBundle.message("clear.all.from.console.action.name"), "Clear All", AllIcons.Actions.GC);
        this.consoleView = consoleView;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = consoleView.getContentSize() > 0;
        if (!enabled) {
            enabled = e.getData(LangDataKeys.CONSOLE_VIEW) != null;
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor != null && editor.getDocument().getTextLength() == 0) {
                enabled = false;
            }
        }
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        if (consoleView != null) {
            consoleView.clear();
        }
    }
}

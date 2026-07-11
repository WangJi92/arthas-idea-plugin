package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Arthas line 命令：光标放在方法体内部的某一行上，获取包含该行的外层方法，
 * 构造 line 命令用于观察指定行的入参、局部变量等信息。
 * <p>
 * 使用示例：
 * line --class demo.MathGame --method primeFactors --line 51 -n 2 -x 2
 *
 * @see <a href="https://arthas.aliyun.com/doc/line.html">Arthas line 命令文档</a>
 */
public class ArthasLineCommandAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);

        if (project == null || editor == null || psiFile == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        // 通过光标位置找到包含的外层方法
        PsiMethod containingMethod = findContainingMethod(editor, psiFile);
        if (containingMethod != null) {
            e.getPresentation().setEnabled(true);
        } else {
            e.getPresentation().setEnabled(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);

        if (project == null || editor == null || psiFile == null) {
            return;
        }

        // 找到包含光标位置的外层方法
        PsiMethod containingMethod = findContainingMethod(editor, psiFile);
        if (containingMethod == null) {
            return;
        }

        // 获取类名
        String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(containingMethod);
        // 获取方法名
        String methodName = containingMethod.getName();
        if (containingMethod.isConstructor()) {
            methodName = "<init>";
        }

        // 获取当前光标所在行号（1-based）
        int lineNumber = 1;
        try {
            int caretOffset = editor.getCaretModel().getOffset();
            lineNumber = editor.getDocument().getLineNumber(caretOffset) + 1;
        } catch (Exception e) {
            // ignore, use default
        }

        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        String depthPrintProperty = instance.depthPrintProperty;

        String command = String.join(" ",
                "line",
                "--class", className,
                "--method", methodName,
                "--line", String.valueOf(lineNumber),
                "--express", "'{lineNumber, params, localVarMap}'",
                "-n", invokeCount,
                "-x", depthPrintProperty
        );

        ClipboardUtils.setClipboardString(command);
        String message = NotifyUtils.COMMAND_COPIED
                + " (Observe line " + lineNumber + " in method " + className + "." + methodName + ")";
        NotifyUtils.notifyMessageOpenTerminal(project, message, command, editor);
    }

    /**
     * 根据 editor 光标位置找到包含该位置的 PsiMethod。
     * 光标必须在方法体内部，不能落在方法声明行上。
     */
    private PsiMethod findContainingMethod(Editor editor, PsiFile psiFile) {
        try {
            int caretOffset = editor.getCaretModel().getOffset();
            PsiElement elementAtCaret = psiFile.findElementAt(caretOffset);
            if (elementAtCaret == null) {
                return null;
            }
            // 光标必须在方法体内部，排除方法声明行
            PsiCodeBlock methodBody = PsiTreeUtil.getParentOfType(elementAtCaret, PsiCodeBlock.class);
            if (methodBody == null) {
                return null;
            }
            return PsiTreeUtil.getParentOfType(methodBody, PsiMethod.class);
        } catch (Exception e) {
            return null;
        }
    }
}

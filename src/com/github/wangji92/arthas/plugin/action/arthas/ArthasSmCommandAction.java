package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * “Search-Method” 的简写，这个命令能搜索出所有已经加载了 Class 信息的方法信息。
 * sm 命令只能看到由当前类所声明 (declaring) 的方法，父类则无法看到。
 * 搜索 当前类的方法
 * sm java.lang.String
 * sm -d org.apache.commons.lang.StringUtils
 * sm -d org/apache/commons/lang/StringUtils
 * sm *StringUtils *
 * sm -Ed org\\.apache\\.commons\\.lang\.StringUtils .*
 *
 * @author 汪小哥
 * @date 24-06-2020
 */
public class ArthasSmCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        String smPreCommand = "sm";
        // 只有方法的情况才展示详情
        if (psiElement instanceof PsiMethod) {
            smPreCommand = "sm -d";
        }
        String command = String.join(" ", smPreCommand, className, methodName);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageDefault(project);
    }
}

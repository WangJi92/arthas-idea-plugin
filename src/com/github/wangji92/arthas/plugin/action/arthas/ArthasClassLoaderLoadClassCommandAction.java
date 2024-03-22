package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasClassLoaderLoadClassCommandDialog;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * 使用指定的classloader 加载class
 * 1、classloader -l  find classloader hash value
 * 2、classloader --load com.wangji92.arthas.plugin.demo.common.config.TestLifeConfiguration -c 18b4aac2  load class with special classload
 * 3、sc -d com.wangji92.arthas.plugin.demo.common.config.TestLifeConfiguration findAll class in jvm
 *
 * @author 汪小哥
 * @date 15-08-2020
 */
public class ArthasClassLoaderLoadClassCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        if (className.contains("*$*")) {
            NotifyUtils.notifyMessage(project, "匿名类不支持 使用sc -d xxxClass*$* 查找具体的类处理", NotificationType.ERROR);
            return;
        }
        new ArthasClassLoaderLoadClassCommandDialog(project,className).open("class loader load class");
    }
}

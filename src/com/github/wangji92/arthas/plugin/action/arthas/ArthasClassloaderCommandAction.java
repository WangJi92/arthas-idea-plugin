package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * classloader 使用
 * <p>
 * classloader -t  继承tree
 * classloader -l  按类加载实例查看统计信息
 * classloader -c 327a647b  查看URLClassLoader实际的urls
 * classloader -c 327a647b -r java/lang/String.class  使用ClassLoader去查找资源
 * classloader -a 所有加载的类的信息
 * classloader -a -c 327a647b 当前classloader 加载类的信息
 * classloader -c 659e0bfd --load demo.MathGame  使用这个classloader 去加载类
 *
 * @author 汪小哥
 * @date 20-06-2020
 */
public class ArthasClassloaderCommandAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        ClipboardUtils.setClipboardString("classloader -l");
        NotifyUtils.notifyMessage(project, "classloader -l  按类加载实例查看统计信息,classloader -a -c 327a647b 当前classloader 加载类的信息");
    }
}

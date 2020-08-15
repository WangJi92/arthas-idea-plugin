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
 * [查看 JVM 性能相关的参数](https://arthas.aliyun.com/doc/perfcounter.html)
 * [jvm 性能调优工具之 jcmd](https://www.jianshu.com/p/388e35d8a09b)
 * @author 汪小哥
 * @date 20-06-2020
 */
public class ArthasPerCountCommandAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        ClipboardUtils.setClipboardString("perfcounter -d");
        NotifyUtils.notifyMessage(project, "查看 JVM 性能相关的参数 [jcmd PID PerfCounter.print]");
    }
}

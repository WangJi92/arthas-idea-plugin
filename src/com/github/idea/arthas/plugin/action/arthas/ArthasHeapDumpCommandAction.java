package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * https://arthas.aliyun.com/doc/heapdump
 * heapdump  /tmp/dump.hprof 打印堆栈信息 ,有点类似 jmap -dump:format=b,file=/temp/dump.hprof pid
 *
 * @author 汪小哥
 * @date 21-03-2020
 */
public class ArthasHeapDumpCommandAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        // 下载好了 使用 mac 工具分析哦  https://www.jianshu.com/p/d9f9fb221c30
        ClipboardUtils.setClipboardString("heapdump /tmp/dump.hprof");
        NotifyUtils.notifyMessage(project, "heapdump /tmp/dump.hprof 已经复制,只要存活对象可以--live,然后下载下来使用 Memory Analyzer(MAT)分析");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

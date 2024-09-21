package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * thread -i 3000 -n 5
 * 这里的cpu统计的是，一段采样间隔内，当前JVM里各个线程所占用的cpu时间占总cpu时间的百分比。其计算方法为： 首先进行一次采样，获得所有线程的cpu的使用时间(调用的是java.lang.management.ThreadMXBean#getThreadCpuTime这个接口)，然后睡眠一段时间，默认100ms，可以通过-i参数指定，然后再采样一次，最后得出这段时间内各个线程消耗的cpu时间情况，最后算出百分比。
 * https://arthas.aliyun.com/doc/thread thread
 * @author 汪小哥
 * @date 21-03-2020
 */
public class ArthasThreadAction extends BaseArthasPluginAction {

    private static final String MESSAGE = "-n 5打印cpu占比最高的堆栈,-b 某个线程拿住了某个锁,只支持持找出synchronized关键字锁住的";

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        String command = "thread -i 3000 -n 5";
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageOpenTerminal(project, NotifyUtils.COMMAND_COPIED, command, editor);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}


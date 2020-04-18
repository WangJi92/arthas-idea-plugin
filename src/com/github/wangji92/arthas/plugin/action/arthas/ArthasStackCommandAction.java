package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;

/**
 * 输出当前方法被调用的调用路径
 * <p>
 * 很多时候我们都知道一个方法被执行，但这个方法被执行的路径非常多，或者你根本就不知道这个方法是从那里被执行了，此时你需要的是 stack 命令。
 *
 * @author 汪小哥
 * @date 09-01-2020
 */
public class ArthasStackCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project) {
        String command = String.join(" ", "stack", className, methodName, "-n", ArthasCommandConstants.INVOKE_COUNT,ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project,"源码分析，查看方法调用栈非常方便");
    }

}

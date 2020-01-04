package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;

/**
 * trace 命令
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasTraceCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project) {

        String command = String.join(" ", "trace", className, methodName, "-n", ArthasCommandConstants.INVOKE_COUNT);

        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageDefault(project);

    }
}

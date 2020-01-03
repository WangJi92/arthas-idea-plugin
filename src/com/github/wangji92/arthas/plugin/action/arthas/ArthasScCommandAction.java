package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;

/**
 * sc -d 获取classloader
 *
 * @author jet
 * @date 21-12-2019
 */
public class ArthasScCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project) {
        String command = String.join(" ", "sc", "-d", className);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageDefault(project);
    }
}

package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;

/**
 * watch展开结构
 *
 * @author jet
 * @date 21-12-2019
 */
public class ArthasWatchCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project) {
        String command = String.join(" ", "watch", className, methodName, "'{params,returnObj,throwExp}'", "-n", ArthasCommandConstants.INVOKE_COUNT, "-x", ArthasCommandConstants.RESULT_X);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageDefault(project);
    }
}

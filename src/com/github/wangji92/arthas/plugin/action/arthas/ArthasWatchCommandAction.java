package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;

/**
 * watch展开结构
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasWatchCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project) {
        String command = String.join(" ", "watch", className, methodName, "'{params,returnObj,throwExp}'", "-n", ArthasCommandConstants.INVOKE_COUNT, "-x", ArthasCommandConstants.RESULT_X, ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "支持ognl条件表达式(默认1==1) eg:'params[0].name=\"name\" and params.size == 1' eg:'returnObj instanceof java.lang.String && returnObj.length>5'");
    }
}

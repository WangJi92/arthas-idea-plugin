package com.command.idea.plugin.Action.arthas;

import com.command.idea.plugin.constants.ArthasCommandConstants;

/**
 * watch展开结构
 *
 * @author jet
 * @date 21-12-2019
 */
public class ArthasWatchCommandAction extends BaseArthasPluginAction {
    @Override
    public String doBuildCommand(String className, String methodName) {
        return String.join(" ", "watch", className, methodName, "'{params,returnObj,throwExp}'", "-n", ArthasCommandConstants.INVOKE_COUNT, "-x", ArthasCommandConstants.RESULT_X);
    }
}

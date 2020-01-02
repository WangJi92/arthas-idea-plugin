package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;

/**
 * trace 命令
 *
 * @author jet
 * @date 21-12-2019
 */
public class ArthasTraceCommandAction extends BaseArthasPluginAction {
    @Override
    public String doBuildCommand(String className, String methodName) {

        return String.join(" ", "trace", className, methodName, "-n", ArthasCommandConstants.INVOKE_COUNT);
    }
}

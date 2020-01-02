package com.github.wangji92.arthas.plugin.action.arthas;

/**
 * sc -d 获取classloader
 * @author jet
 * @date 21-12-2019
 */
public class ArthasScCommandAction extends BaseArthasPluginAction {
    @Override
    public String doBuildCommand(String className, String methodName) {
        return String.join(" ", "sc", "-d", className);
    }
}

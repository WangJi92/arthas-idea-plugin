package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;

/**
 * sc -d 获取classloader
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasScCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project) {
        String command = String.join(" ", "sc", "-d", className);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "查看JVM已加载的类信息 Search-Class,类似命令Search-Method(sm);-d 输出类的详情,加载的ClassLoader,-f:类的成员变量信息（配合-d一起使用");
    }
}

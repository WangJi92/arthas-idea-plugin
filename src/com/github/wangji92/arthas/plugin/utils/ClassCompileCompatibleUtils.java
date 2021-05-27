package com.github.wangji92.arthas.plugin.utils;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.task.ProjectTaskManager;
import com.intellij.task.ProjectTaskNotification;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * 编译工程 代码兼容方案
 *
 * @author 汪小哥
 * @date 27-05-2021
 */
public class ClassCompileCompatibleUtils {
    /**
     * 编译兼容的方案
     *
     * @param project
     * @param virtualFileFiles
     * @param successRunnable
     */
    public static void compile(Project project, VirtualFile[] virtualFileFiles, Runnable successRunnable) {
        if (ApplicationInfo.getInstance().getBuild().getBaselineVersion() <= 201) {
            //2018.2 编译报错
            WriteActionCompatibleUtils.runAndWait(() -> {
                ProjectTaskManager instance = ProjectTaskManager.getInstance(project);

                ProjectTaskNotification projectTaskNotification = projectTaskResult -> {
                    int errorCount = 0;
                    try {
                        errorCount = (int) MethodUtils.invokeMethod(projectTaskResult, "getErrors");
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        //ignore
                    }
                    if (errorCount > 0) {
                        NotifyUtils.notifyMessage(project, "File compilation errors (it's best to make sure you compile the whole project at least once before a hot update) This will only partially compile the current file", NotificationType.ERROR);
                        return;
                    }
                    WriteActionCompatibleUtils.runAndWait(successRunnable::run);
                };
                MethodUtils.invokeMethod(instance, "compile", new Object[]{virtualFileFiles, projectTaskNotification}, new Class[]{VirtualFile[].class, ProjectTaskNotification.class});
            });
        } else {
            WriteActionCompatibleUtils.runAndWait(() -> {
                ProjectTaskManager instance = ProjectTaskManager.getInstance(project);
                Object promise = MethodUtils.invokeMethod(instance, "compile", new Object[]{virtualFileFiles}, new Class[]{VirtualFile[].class});
                Object isSucceeded = MethodUtils.invokeMethod(promise, "isSucceeded");
                if (!Boolean.TRUE.equals(isSucceeded)) {
                    NotifyUtils.notifyMessage(project, "File compilation errors (it's best to make sure you compile the whole project at least once before a hot update) This will only partially compile the current file", NotificationType.ERROR);
                    return;
                }
                WriteActionCompatibleUtils.runAndWait(successRunnable::run);
            });
        }
    }


}

package com.github.wangji92.arthas.plugin.utils;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.task.ProjectTaskManager;
import org.jetbrains.concurrency.Promise;

/**
 * 编译工程 代码兼容方案
 *
 * @author 汪小哥
 * @date 27-05-2021
 */
public class ClassCompileCompatibleUtils {
    private static final Logger LOG = Logger.getInstance(ClassCompileCompatibleUtils.class);

    /**
     * 编译兼容的方案
     *
     * @param project
     * @param virtualFileFiles
     * @param successRunnable
     */
    public static void compile(Project project, VirtualFile[] virtualFileFiles, Runnable successRunnable) {
//        if (ApplicationInfo.getInstance().getBuild().getBaselineVersion() <= 201) {
//            //编译兼容一下代码
//            WriteActionCompatibleUtils.runAndWait(project, () -> {
//                ProjectTaskManager instance = ProjectTaskManager.getInstance(project);
//                ProjectTaskNotification taskNotification = new ProjectTaskNotification() {
//                    @Override
//                    public void finished(@NotNull ProjectTaskResult projectTaskResult) {
//                        int errorCount = 0;
//                        try {
//                            errorCount = (int) MethodUtils.invokeMethod(projectTaskResult, "getErrors");
//                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                            //ignore
//                        }
//                        if (errorCount > 0) {
//                            NotifyUtils.notifyMessage(project, "File compilation errors (it's best to make sure you compile the whole project at least once before a hot update) This will only partially compile the current file", NotificationType.ERROR);
//                            return;
//                        }
//                        WriteActionCompatibleUtils.runAndWait(project, successRunnable::run);
//                    }
//                };
//                MethodUtils.invokeMethod(instance, "compile", new Object[]{virtualFileFiles, taskNotification}, new Class[]{VirtualFile[].class, ProjectTaskNotification.class});
//            });
//        } else {
        WriteActionCompatibleUtils.runAndWait(project, () -> {
            ProjectTaskManager instance = ProjectTaskManager.getInstance(project);
            Promise<ProjectTaskManager.Result> compilePromise = instance.compile(virtualFileFiles);
            compilePromise.onSuccess(result -> {
                boolean hasErrors = result.hasErrors();
                if (hasErrors) {
                    NotifyUtils.notifyMessage(project, "File compilation errors (it's best to make sure you compile the whole project at least once before a hot update) This will only partially compile the current file", NotificationType.ERROR);
                } else {
                    WriteActionCompatibleUtils.runAndWait(project, successRunnable::run);
                }
            });
            compilePromise.onError(throwable -> {
                LOG.error("compile error", throwable.getCause());
                NotifyUtils.notifyMessage(project, "File compilation errors (it's best to make sure you compile the whole project at least once before a hot update) This will only partially compile the current file", NotificationType.ERROR);
            });
        });
//        }
    }


}

package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.ui.AppSettingsPage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * 打开配置界面
 *
 * @author 汪小哥
 * @date 03-06-2021
 */
public class OpenConfigDialogUtils {

    /**
     * 打开配置界面
     *
     * @param project
     */
    public static void openConfigDialog(Project project) {
        OpenConfigDialogUtils.openConfigDialog(project, AppSettingsPage.class);
    }

    /**
     * 打开配置界面
     *
     * @param project
     * @param configClass
     */
    public static void openConfigDialog(Project project, Class<? extends Configurable> configClass) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, configClass);
        }, ModalityState.defaultModalityState());
    }


}

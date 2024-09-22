package com.github.idea.arthas.plugin.utils;

import com.github.idea.arthas.plugin.ui.AppSettingsPage;
import com.intellij.ide.util.PropertiesComponent;
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

    public static final String OPEN_CONFIG_TAB = "openConfigTab";

    /**
     * 打开配置界面
     *
     * @param project
     */
    public static void openConfigDialog(Project project) {
        OpenConfigDialogUtils.openConfigDialog(project, AppSettingsPage.class, 0);
    }

    /**
     * 打开配置界面
     *
     * @param project
     */
    public static void openConfigDialog(Project project, int tabbedPaneIndex) {
        OpenConfigDialogUtils.openConfigDialog(project, AppSettingsPage.class, tabbedPaneIndex);
    }

    /**
     * 打开配置界面
     *
     * @param project
     * @param configClass
     * @param tabbedPaneIndex
     */
    public static void openConfigDialog(Project project, Class<? extends Configurable> configClass, int tabbedPaneIndex) {
        PropertiesComponent.getInstance().setValue(OPEN_CONFIG_TAB, String.valueOf(tabbedPaneIndex));
        ApplicationManager.getApplication().invokeLater(() -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, configClass);
        }, ModalityState.defaultModalityState());
    }


}

package com.github.wangji92.arthas.plugin.utils;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ThrowableRunnable;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * 2018.1 以下版本 不支持  {@link com.intellij.openapi.application.WriteAction#runAndWait(com.intellij.util.ThrowableRunnable)}
 *
 * @author 汪小哥
 * @date 15-01-2021
 */
public class WriteActionCompatibleUtils {

    private static final Logger LOG = Logger.getInstance(WriteActionCompatibleUtils.class);

    /**
     * 兼容方案 {@literal https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html?from=jetbrains.org#modality-and-invokelater}
     *
     * @param run
     */
    public static void runAndWait(ThrowableRunnable run) {
        if (ApplicationInfo.getInstance().getBuild().getBaselineVersion() >= 182) {
            // 2018.2 版本不支持 com.intellij.openapi.application.WriteAction#runAndWait(com.intellij.util.ThrowableRunnable)
            try {
                Class<?> writeAction = ClassUtils.getClass("com.intellij.openapi.application.WriteAction");
                MethodUtils.invokeStaticMethod(writeAction, "runAndWait", run);
                return;
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LOG.error("invoke runAndWait error", e);
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    run.run();
                } catch (Throwable e) {
                    LOG.error("invokeLater", e);
                }
            });

        }


    }

}

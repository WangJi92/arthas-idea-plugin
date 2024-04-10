package com.github.wangji92.arthas.plugin.utils;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

/**
 * 通知消息
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class NotifyUtils {


    public static final String COMMAND_COPIED = "arthas command copied to clipboard,open arthas to execute command";

    /**
     * 通知消息
     *
     * @param project
     */
    public static void notifyMessageDefault(Project project) {
        notifyMessage(project, COMMAND_COPIED);
    }

    /**
     * 消息
     *
     * @param project
     * @param message
     */
    public static void notifyMessage(Project project, String message) {
        notifyMessage(project, message, NotificationType.INFORMATION);
    }

    /**
     * 推送消息哦
     *
     * @param project
     * @param message
     * @param type
     */
    public static void notifyMessage(Project project, String message, @NotNull NotificationType type) {
        try {
            Notification arthas = NotificationGroupManager.getInstance().getNotificationGroup("arthas").createNotification(message, type);
            arthas.setTitle("Arthas idea plugin");
            arthas.setListener(new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        String url = event.getDescription();
                        BrowserUtil.browse(url);
                    }
                }
            });
            arthas.notify(project);
        } catch (Exception e) {
            //
        }
    }

}

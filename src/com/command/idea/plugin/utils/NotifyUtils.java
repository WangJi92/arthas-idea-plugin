package com.command.idea.plugin.utils;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 通知消息
 *
 * @author jet
 * @date 21-12-2019
 */
public class NotifyUtils {

    private static final NotificationGroup NOTIFICATION = new NotificationGroup("arthas", NotificationDisplayType.BALLOON, false);


    public static final String COMMAND_COPIED = "命令已经复制";

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
        try {
            Notification currentNotify = NOTIFICATION.createNotification(message, NotificationType.INFORMATION);
            Notifications.Bus.notify(currentNotify, project);
        } catch (Exception e) {
            //
        }
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
            Notification currentNotify = NOTIFICATION.createNotification(message, type);
            Notifications.Bus.notify(currentNotify, project);
        } catch (Exception e) {
            //
        }
    }

}

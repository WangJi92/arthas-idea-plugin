package com.github.wangji92.arthas.plugin.action.arthas;

import com.aliyun.oss.OSS;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.AliyunOssUtils;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

/**
 * 本地文件上传到oss
 *
 * @author 汪小哥
 * @date 20-08-2020
 */
public class LocalFileUploadToOssAction extends AnAction {

    public static final String OSS_UP_LOAD_FILE = "curl -Lk  \"%s\" > \"%s\"";

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            event.getPresentation().setEnabled(false);
            return;
        }
        event.getPresentation().setEnabled(true);
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);

        AppSettingsState settings = AppSettingsState.getInstance(project);
        if (!settings.aliYunOss) {
            NotifyUtils.notifyMessage(project, "请先配置阿里oss的信息", NotificationType.ERROR);
        }

        VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        VirtualFile virtualFileBefore = null;
        if (virtualFileFiles != null) {
            virtualFileBefore = virtualFileFiles[0];
        }
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
        descriptor.setTitle("上传文件到Oss下载");
        descriptor.setHideIgnored(true);
        VirtualFile selectVirtualFile = FileChooser.chooseFile(descriptor, event.getProject(), virtualFileBefore);
        if (selectVirtualFile == null || selectVirtualFile.isDirectory()) {
            NotifyUtils.notifyMessage(project, "请选择要上传的文件", NotificationType.ERROR);
            return;
        }

        OSS oss = null;
        try {
            oss = AliyunOssUtils.buildOssClient(project);
            String filePathKey = settings.directoryPrefix + UUID.randomUUID().toString();
            String urlEncodeKeyPath = AliyunOssUtils.putFile(oss, settings.bucketName, filePathKey, selectVirtualFile.getInputStream());
            String presignedUrl = AliyunOssUtils.generatePresignedUrl(oss, settings.bucketName, urlEncodeKeyPath, new Date(System.currentTimeMillis() + 3600L * 1000));
            String command = String.format(OSS_UP_LOAD_FILE, presignedUrl, selectVirtualFile.getName());
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, "直接到目标服务器任意路径 粘贴脚本执行下载文件");
        } catch (Exception e) {
            StackTraceUtils.printSanitizedStackTrace(e);
            NotifyUtils.notifyMessage(project, "上传命令到oss 失败" + e.getMessage());
            return;
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
            IOUtils.closeQuietly();
        }


    }
}

package com.github.idea.arthas.plugin.action.arthas;

import com.aliyun.oss.OSS;
import com.amazonaws.services.s3.AmazonS3;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.OsS3Utils;
import com.github.idea.arthas.plugin.utils.AliyunOssUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Date;
import java.util.UUID;

/**
 * 本地文件上传到oss
 *
 * @author 汪小哥
 * @date 20-08-2020
 */
public class LocalFileUploadToOssAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(LocalFileUploadToOssAction.class);

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
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);

        AppSettingsState settings = AppSettingsState.getInstance(project);
        if (!(settings.aliYunOss || settings.awsS3)) {
            NotifyUtils.notifyMessage(project, "Please configure Aliyun Oss Or S3 As Storage <a href=\"https://www.yuque.com/arthas-idea-plugin/help/ugrc8n\">arthas idea setting</a>", NotificationType.ERROR);
            return;
        }

        VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        VirtualFile virtualFileBefore = null;
        if (virtualFileFiles != null) {
            virtualFileBefore = virtualFileFiles[0];
        }
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false);
        descriptor.setTitle("Upload To Object Storage");
        descriptor.setHideIgnored(true);
        VirtualFile selectVirtualFile = FileChooser.chooseFile(descriptor, event.getProject(), virtualFileBefore);
        if (selectVirtualFile == null || selectVirtualFile.isDirectory()) {
            NotifyUtils.notifyMessage(project, "Please select the file to be uploaded", NotificationType.ERROR);
            return;
        }

        Runnable runnable = () -> {
            OSS oss = null;
            AmazonS3 aw3 = null;
            String presignedUrl = "";
            try {
                if (settings.aliYunOss) {
                    oss = AliyunOssUtils.buildOssClient(project);
                    String filePathKey = settings.directoryPrefix + UUID.randomUUID().toString();
                    String urlEncodeKeyPath = AliyunOssUtils.putFile(oss, settings.bucketName, filePathKey, selectVirtualFile.getInputStream());
                    presignedUrl = AliyunOssUtils.generatePresignedUrl(oss, settings.bucketName, urlEncodeKeyPath, new Date(System.currentTimeMillis() + 24 * 365 * 3600L * 1000));
                } else if (settings.awsS3) {
                    aw3 = OsS3Utils.buildS3Client(project);
                    String filePathKey = settings.s3DirectoryPrefix + UUID.randomUUID().toString();
                    String urlEncodeKeyPath = OsS3Utils.putFile(aw3, settings.s3BucketName, filePathKey, selectVirtualFile.getInputStream());
                    presignedUrl = OsS3Utils.generatePresignedUrl(aw3, settings.s3BucketName, urlEncodeKeyPath, new Date(System.currentTimeMillis() + 24 * 6 * 3600L * 1000));
                }
                String command = String.format(OSS_UP_LOAD_FILE, presignedUrl, selectVirtualFile.getName());
                ClipboardUtils.setClipboardString(command);
                NotifyUtils.notifyMessage(project, "linux shell command has been copied to the clipboard Go to the server and paste it");
            } catch (Exception e) {
                LOG.info("upload to object stage error", e);
                NotifyUtils.notifyMessage(project, "Object Storage" + e.getMessage());
                return;
            } finally {
                if (oss != null) {
                    oss.shutdown();
                }
                if (aw3 != null) {
                    aw3.shutdown();
                }
            }
        };

        // https://stackoverflow.com/questions/18725340/create-a-background-task-in-intellij-plugin
        ProgressManager.getInstance().run(new Backgroundable(project, "Upload To object storage") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // Set the progress bar percentage and text
                try {
                    progressIndicator.setFraction(0.30);
                    progressIndicator.setText("70% to finish");
                    runnable.run();
                    // Finished
                    progressIndicator.setFraction(1.0);
                    progressIndicator.setText("Finished");
                } catch (Exception e) {
                    try {
                        SwingUtilities.invokeAndWait(runnable::run);
                    } catch (Exception ex) {
                    }
                }
            }
        });


    }
}

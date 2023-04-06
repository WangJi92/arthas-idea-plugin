package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.common.exception.CompilerFileNotFoundException;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 汪小哥
 * @date 16-08-2020
 */
public class ArthasHotRedefineCommandAction extends AnAction implements DumbAware {
    private static final Logger LOG = Logger.getInstance(ArthasHotRedefineCommandAction.class);

    public ArthasHotRedefineCommandAction() {
        this.setEnabledInModalContext(true);
    }

    private static final String RETRANSFORM_NOTE = "【retransform 增强后 stop/rest 不影响,先删除retransform entry,显式触发 retransform 失效】【不能修改、添加、删除类的field和method】";

    private static final String REDEFINE_NOTE = "【redefine 增强后 stop/rest 不影响,watch/jad/trace 等等增强后失效】【不能修改、添加、删除类的field和method】";

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        //右侧选择了一个或者多个文件
        VirtualFile[] virtualFileFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFileFiles == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        List<PsiFile> psiFileJavaFiles = Arrays.stream(virtualFileFiles).map(PsiManager.getInstance(project)::findFile).filter(psiFileElement -> psiFileElement instanceof PsiJavaFile).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(psiFileJavaFiles)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    /**
     * @param event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        String hotCommand = "retransform";
        //4.6.1 升级 retransform https://github.com/alibaba/arthas/issues/1651
        if ("Redefine".equals(event.getPresentation().getDescription())) {
            hotCommand = "redefine";
        }

        VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        assert virtualFileFiles != null;

        String finalHotCommand = hotCommand;
        Runnable runnable = () -> {
            List<String> fullClassPackagePaths = Lists.newArrayList();
            try {
                fullClassPackagePaths = this.getAllFullTargetClassFilePath(project, virtualFileFiles, psiElement);
            } catch (Exception e) {
                if (e instanceof CompilerFileNotFoundException) {
                    LOG.warn("maybe compiler error", e);
                    NotifyUtils.notifyMessage(project, "查询编译后的文件错误:一般都是没有编译的错误,(建议先编译整个工程,打开热更新先编译),在target class 目录找不到文件 [" + e.getMessage() + "]", NotificationType.ERROR);
                } else {
                    LOG.error("maybe compiler error", e);
                    NotifyUtils.notifyMessage(project, "查询编译后的文件错误:一般都是没有编译的错误,(建议先编译整个工程,打开热更新先编译)", NotificationType.ERROR);
                }
                return;
            }
            if (CollectionUtils.isEmpty(fullClassPackagePaths)) {
                NotifyUtils.notifyMessage(project, "没有找到目标文件编译后的class文件", NotificationType.ERROR);
                return;
            }
            List<String> bash64FileAndPathList = Lists.newArrayList();

            List<String> shellOutPaths = Lists.newArrayList();

            try {
                fullClassPackagePaths.forEach(fullClassPackagePath -> {
                    File file = new File(fullClassPackagePath);
                    if (!file.exists()) {
                        return;
                    }
                    String classBase64 = IoUtils.readFileToBase64String(file);
                    // 内部类 的时候回有问题 展示上面 结果没有影响 这里修改一下，windows的文件描述符 和 linux的不一样，最后的生成的路径要修改一下
                    String pathReplaceAll = fullClassPackagePath.substring(fullClassPackagePath.indexOf(File.separator + "target" + File.separator + "classes") + 15)
                            //需要将Windows的文件描述符转换为Linux的，最后一个多余了/.class要转换回来
                            .replace(File.separator, "/").replace("/.class", ".class")
                            // https://github.com/WangJi92/arthas-idea-plugin/issues/23 为什么要转义 shell 脚本执行的时候这个字符特殊不能直接使用
                            // 最初使用 replace("$","\\$") mac 没有问题  windows有问题

                            // https://blog.csdn.net/xrt95050/article/details/6651571 替换$ 为 \$
                            // 要把 $ 替换成 \$ ，则要使用 \\\\\\& 来替换，因为一个 \ 要使用 \\\ 来进行替换，
                            // 而一个 $ 要使用 \\$ 来进行替换，因 \ 与  $ 在作为替换内容时都属于特殊字符：$ 字符表示反向引用组，而 \ 字符又是用来转义 $ 字符的
                            .replaceAll("\\$", "\\\\\\$");

                    String pathAndClass = classBase64 + "|" + ArthasCommandConstants.HOT_SWAMP_BASH_PACKAGE_PATH + pathReplaceAll;
                    shellOutPaths.add(ArthasCommandConstants.HOT_SWAMP_BASH_PACKAGE_PATH + pathReplaceAll);
                    bash64FileAndPathList.add(pathAndClass);
                });

                if (bash64FileAndPathList.size() <= 0) {
                    NotifyUtils.notifyMessage(project, "当前选择对于的类文件在target目录.class文件不存在,请编译", NotificationType.ERROR);
                    return;
                }


                AppSettingsState settings = AppSettingsState.getInstance(project);
                String selectProjectName = settings.selectProjectName;

                //不为空就删除
                String deleteClassFile = settings.hotRedefineDelete ? "delete" : "";
                if (settings.manualSelectPid) {
                    // 手动选择pid
                    selectProjectName = "";
                } else if (StringUtils.isBlank(selectProjectName)) {
                    NotifyUtils.notifyMessage(project, "必须配置才能使用 jps -l 查看名称,hot swap use project name select process and batch support; as.sh --select projectName -c 'redefine /tmp/test.class'", NotificationType.ERROR);
                    return;
                }
                String arthasIdeaPluginBase64AndPathCommand = String.join(",", bash64FileAndPathList);
                String arthasIdeaPluginRedefineCommand = finalHotCommand + " " + String.join(" ", shellOutPaths);
                Map<String, String> params = Maps.newHashMap();
                params.put("arthasIdeaPluginBase64AndPathCommand", arthasIdeaPluginBase64AndPathCommand);
                params.put("arthasIdeaPluginRedefineCommand", arthasIdeaPluginRedefineCommand);
                params.put("arthasIdeaPluginApplicationName", selectProjectName);
                params.put("deleteClassFile", deleteClassFile);
                params.put("arthasPackageZipDownloadUrl", settings.arthasPackageZipDownloadUrl);


                String redefineSh = StringUtils.stringSubstitutorFromFilePath("/template/arthas-idea-plugin-hot-swap.sh", params);

                String base64RedefineSh = BaseEncoding.base64().encode(redefineSh.getBytes(StandardCharsets.UTF_8));
                DirectScriptUtils.buildDirectScript(project, settings, base64RedefineSh, "arthas-idea-plugin-hot-swap.sh", directScriptResult -> {
                    if (directScriptResult.getResult()) {
                        if ("redefine".equals(finalHotCommand)) {
                            directScriptResult.getTip().append(REDEFINE_NOTE);
                        } else {
                            directScriptResult.getTip().append(RETRANSFORM_NOTE);
                        }
                        NotifyUtils.notifyMessage(project, directScriptResult.getTip().toString());
                    }
                });
            } catch (Exception e) {
                LOG.error("unknown error", e);
                NotifyUtils.notifyMessage(project, "unknown error", NotificationType.ERROR);
            }
        };
        try {
            this.doHotRunnable(project, virtualFileFiles, runnable);
        } catch (Exception e) {
            LOG.error("record arthas hot swap error", e);
            NotifyUtils.notifyMessage(project, "unknown error", NotificationType.ERROR);
        }

    }

    /**
     * 后台执行任务
     *
     * @param project
     * @param virtualFileFiles
     * @param runnable
     */
    private void doHotRunnable(Project project, VirtualFile[] virtualFileFiles, Runnable runnable) {
        // https://stackoverflow.com/questions/18725340/create-a-background-task-in-intellij-plugin
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Hot Swap") {

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // Set the progress bar percentage and text
                try {
                    AppSettingsState settings = AppSettingsState.getInstance(project);
                    if (settings.redefineBeforeCompile) {
                        ClassCompileCompatibleUtils.compile(project, virtualFileFiles, runnable);
                    } else {
                        WriteActionCompatibleUtils.runAndWait(project, runnable::run);

                    }

                } catch (Exception e) {
                    LOG.error("record arthas hot swap error", e);
                    NotifyUtils.notifyMessage(project, "unknown error", NotificationType.ERROR);
                }

            }
        });
    }


    @Nullable
    private List<String> getAllFullTargetClassFilePath(Project project, VirtualFile[] virtualFileFiles, PsiElement psiElement) {
        List<String> fullClassPackagePaths = Lists.newArrayList();
        if (virtualFileFiles.length == 1 && OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            //选择 当个文件 且为 编辑区选择的
            PsiJavaFile psiJavaFile = OgnlPsUtils.getContainingPsiJavaFile(psiElement);
            String packageName = psiJavaFile.getPackageName();
            String className = FilenameUtils.getBaseName(psiJavaFile.getName());
            String ideaClassName = packageName + "." + className;

            //主要是根据模块查询 当前编译后的路径的信息
            final String compilerOutputPath = OgnlPsUtils.getCompilerOutputPathV2(project, psiElement);

            //全路径包含 匿名类的处理
            String pathClassName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiElement);

            String packageNamePath = packageName.replace(".", File.separator);
            //处理内部类 匿名类获取class的问题
            boolean isAnonymousClass = pathClassName.contains("*$*");
            if (isAnonymousClass) {
                // 匿名类要处理遍历
                String outClassName = FilenameUtils.getBaseName(psiElement.getContainingFile().getName());
                // 查找当前类下面的所有的匿名类的信息
                File packageDirFile = new File(compilerOutputPath + File.separator + packageNamePath);
                if (!packageDirFile.exists()) {
                    throw new CompilerFileNotFoundException(String.format("compiler target class dir not found,dir:%s", compilerOutputPath + File.separator + packageNamePath));
                }
                List<File> files = Lists.newArrayList(FileUtils.listFiles(packageDirFile, new RegexFileFilter("^(" + outClassName + "\\$).*(\\d\\.class)$"), FalseFileFilter.INSTANCE));
                fullClassPackagePaths = files.stream().map(file -> String.format("%s%s%s", compilerOutputPath + File.separator, packageNamePath + File.separator, file.getName())).collect(Collectors.toList());
            } else {
                // this is maybe inner class
                String currentClassName = pathClassName.replace(packageName + ".", "").replace("$", "\\$");
                File packageDirFile = new File(compilerOutputPath + File.separator + packageNamePath);
                if (!packageDirFile.exists()) {
                    throw new CompilerFileNotFoundException(String.format("compiler target class dir not found,dir:%s", compilerOutputPath + File.separator + packageNamePath));
                }
                List<File> files = Lists.newArrayList(FileUtils.listFiles(new File(compilerOutputPath + File.separator + packageNamePath), new RegexFileFilter("^(" + currentClassName + "\\$).*\\.class$"), FalseFileFilter.INSTANCE));
                List<String> currentClassFullPaths = files.stream().map(file -> String.format("%s%s%s", compilerOutputPath + File.separator, packageNamePath + File.separator, file.getName())).collect(Collectors.toList());

                //add current class
                fullClassPackagePaths.addAll(currentClassFullPaths);
                String path = compilerOutputPath + File.separator + pathClassName.replace(".", File.separator) + ".class";
                fullClassPackagePaths.add(path);

            }

        } else {
            //  https://blog.csdn.net/weixin_34223655/article/details/88112593
            // PsiFile 转 VirtualFile
            List<PsiFile> psiFileJavaFiles = Arrays.stream(virtualFileFiles).map(PsiManager.getInstance(project)::findFile).filter(psiFileElement -> psiFileElement instanceof PsiJavaFile && !OgnlPsUtils.psiElementInEnum(psiElement)).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(psiFileJavaFiles)) {
                NotifyUtils.notifyMessage(project, "请选择.java 先编译 Control /Command F9 编译 或者 .class文件", NotificationType.ERROR);
                return null;
            }
            fullClassPackagePaths = psiFileJavaFiles.stream().flatMap(psiFileJavaFile -> {
                String packageNameBack = ((PsiJavaFile) psiFileJavaFile.getContainingFile()).getPackageName();
                String packageNamePath = packageNameBack.replace(".", File.separator);
                String className = FilenameUtils.getBaseName(psiFileJavaFile.getContainingFile().getName());
                String qualifiedName = packageNameBack + "." + className;
                String qualifiedNamePath = qualifiedName.replace(".", File.separator);
                String currentCompilerOutputPath = OgnlPsUtils.getCompilerOutputPathV2(project, psiFileJavaFile);
                File packageDirFile = new File(currentCompilerOutputPath + File.separator + packageNamePath);
                if (!packageDirFile.exists()) {
                    throw new CompilerFileNotFoundException(String.format("compiler target class dir not found,dir:%s", currentCompilerOutputPath + File.separator + packageNamePath));
                }
                List<File> files = Lists.newArrayList(FileUtils.listFiles(new File(currentCompilerOutputPath + File.separator + packageNamePath), new RegexFileFilter("^(" + className + "\\$).*\\.class$"), FalseFileFilter.INSTANCE));
                List<String> currentClassFullPaths = files.stream().map(file -> String.format("%s%s%s", currentCompilerOutputPath + File.separator, packageNamePath + File.separator, file.getName())).collect(Collectors.toList());
                currentClassFullPaths.add(currentCompilerOutputPath + File.separator + qualifiedNamePath + ".class");
                return currentClassFullPaths.stream();
            }).distinct().collect(Collectors.toList());

        }
        return fullClassPackagePaths;
    }


}


package com.github.wangji92.arthas.plugin.action.arthas;

import com.aliyun.oss.OSS;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.intellij.lang.jvm.JvmMember;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
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
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 汪小哥
 * @date 16-08-2020
 */
public class ArthasHotRedefineCommandAction extends AnAction implements DumbAware {


    public ArthasHotRedefineCommandAction() {
        this.setEnabledInModalContext(true);
    }

    /**
     * oss 获取到链接
     */
    public static final String OSS_HOT_REDEFINE = "curl -sLk  %s --connect-timeout 60 | base64 --decode >arthas-idea-plugin-redefine.sh;chmod a+x arthas-idea-plugin-redefine.sh;./arthas-idea-plugin-redefine.sh;";
    /**
     * 剪切板处理字符串
     */
    public static final String CLIPBOARD_HOT_REDEFINE = "echo %s |base64 --decode >arthas-idea-plugin-redefine.sh;chmod a+x arthas-idea-plugin-redefine.sh;./arthas-idea-plugin-redefine.sh;";

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();

        //右侧选择了一个或者多个文件
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files != null && files.length > 0) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    /**
     * 游戏规则
     * 1、文件导航多选 psiElement ==null psiFile==null virtualFileFiles>2
     * 2、文件导航单选 psiElement !=null  等同于选择了class psiElement 可能为空
     * 3、编辑框里面处理 psiElement !=null  内部类  psiFile = OutClass  psiElement == InnerCLass
     * 匿名类  psiFile = OutClass  psiElement == PsiAnonymousClass
     *
     * @param event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        assert virtualFileFiles != null;
        String compilerOutputPath = "";
        @SystemIndependent String basePath = project.getBasePath() == null ? "" : project.getBasePath();
        List<String> fullClassPackagePaths = Lists.newArrayList();
        if (virtualFileFiles.length == 1 && psiElement instanceof JvmMember) {
            //选择 当个文件 且为 编辑区选择的

            String packageName = ((PsiJavaFile) psiElement.getContainingFile()).getPackageName();
            String className = FilenameUtils.getBaseName(psiElement.getContainingFile().getName());
            String ideaClassName = packageName + "." + className;

            //主要是根据模块查询 当前编译后的路径的信息
            compilerOutputPath = OgnlPsUtils.getCompilerOutputPath(project, ideaClassName);

            //全路径包含 匿名类的处理
            String pathClassName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiElement);
            //处理内部类 匿名类获取class的问题
            boolean isAnonymousClass = pathClassName.contains("*$*");
            if (isAnonymousClass) {
                // 匿名类要处理遍历
                String packageNamePath = packageName.replaceAll("\\.", File.separator);
                String outClassName = FilenameUtils.getBaseName(psiElement.getContainingFile().getName());
                // 查找当前类下面的所有的匿名类的信息
                List<File> files = Lists.newArrayList(FileUtils.listFiles(new File(compilerOutputPath + File.separator + packageNamePath), new RegexFileFilter(outClassName + ".*[\\$](\\d{0,4}).class$"), FalseFileFilter.INSTANCE));
                final String compilerOutputPathBack = compilerOutputPath;
                fullClassPackagePaths = files.stream().map(file -> String.format("%s%s%s", compilerOutputPathBack + File.separator, packageNamePath + File.separator, file.getName())).collect(Collectors.toList());
            } else {
                String path = compilerOutputPath + File.separator + pathClassName.replaceAll("\\.", File.separator) + ".class";
                fullClassPackagePaths.add(path);
            }

        } else {
            //  https://blog.csdn.net/weixin_34223655/article/details/88112593
            // PsiFile 转 VirtualFile
            List<PsiFile> psiFileJavaFiles = Arrays.stream(virtualFileFiles).map(PsiManager.getInstance(project)::findFile).filter(psiFileElement -> psiFileElement instanceof PsiJavaFile).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(psiFileJavaFiles)) {
                NotifyUtils.notifyMessage(project, "请选择.java 先编译 Control /Command F9 编译 或者 .class文件", NotificationType.ERROR);
                return;
            }
            fullClassPackagePaths = psiFileJavaFiles.stream().flatMap(psiFileJavaFile -> {
                String packageNameBack = ((PsiJavaFile) psiFileJavaFile.getContainingFile()).getPackageName();
                String packageNamePath = packageNameBack.replaceAll("\\.", File.separator);
                String className = FilenameUtils.getBaseName(psiFileJavaFile.getContainingFile().getName());
                String qualifiedName = packageNameBack + "." + className;
                String qualifiedNamePath = qualifiedName.replaceAll("\\.", File.separator);
                String currentCompilerOutputPath = OgnlPsUtils.getCompilerOutputPath(project, qualifiedName);
                List<File> files = Lists.newArrayList(FileUtils.listFiles(new File(currentCompilerOutputPath + File.separator + packageNamePath), new RegexFileFilter(className + ".*[\\$](\\d{0,4}).class$"), FalseFileFilter.INSTANCE));
                List<String> currentClassFullPaths = files.stream().map(file -> String.format("%s%s%s", currentCompilerOutputPath + File.separator, packageNamePath + File.separator, file.getName())).collect(Collectors.toList());
                currentClassFullPaths.add(currentCompilerOutputPath + File.separator + qualifiedNamePath + ".class");
                return currentClassFullPaths.stream();
            }).distinct().collect(Collectors.toList());

        }


        List<String> bash64FileAndPathList = Lists.newArrayList();

        List<String> shellOutPaths = Lists.newArrayList();

        fullClassPackagePaths.forEach(fullClassPackagePath -> {
            File file = new File(fullClassPackagePath);
            if (!file.exists()) {
                return;
            }
            String classBase64 = IoUtils.readFileToBase64String(file);
            // shell 解析的时候 单引号''，双引号""的区别是单引号''剥夺了所有字符的特殊含义，单引号''内就变成了单纯的字符。双引号""则对于双引号""内的参数替换
            // 内部类 的时候回有问题 展示上面 结果没有影响 这里修改一下
            String pathReplaceAll = fullClassPackagePath.replace("$", "-").replace(basePath, "").replace(File.separator + "target" + File.separator + "classes", "");
            String pathAndClass = classBase64 + "|" + ArthasCommandConstants.REDEFINE_BASH_PACKAGE_PATH + pathReplaceAll;
            shellOutPaths.add(ArthasCommandConstants.REDEFINE_BASH_PACKAGE_PATH + pathReplaceAll);
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
        if (StringUtils.isBlank(selectProjectName)) {
            NotifyUtils.notifyMessage(project, "必须配置才能使用 jps -l 查看名称,Hot Redefine use project name select process and batch support; as.sh --select projectName -c 'redefine /tmp/test.class'", NotificationType.ERROR);
            return;
        }
        String arthasIdeaPluginBase64AndPathCommand = String.join(",", bash64FileAndPathList);
        String arthasIdeaPluginRedefineCommand = "redefine " + String.join(" ", shellOutPaths);
        Map<String, String> params = Maps.newHashMap();
        params.put("arthasIdeaPluginBase64AndPathCommand", arthasIdeaPluginBase64AndPathCommand);
        params.put("arthasIdeaPluginRedefineCommand", arthasIdeaPluginRedefineCommand);
        params.put("arthasIdeaPluginApplicationName", selectProjectName);
        params.put("deleteClassFile", deleteClassFile);

        String redefineSh = StringUtils.stringSubstitutor("/template/arthas-idea-plugin-redefine.sh", params);

        String base64RedefineSh = BaseEncoding.base64().encode(redefineSh.getBytes());

        String command = "";
        if (!settings.aliYunOss) {
            command = String.format(CLIPBOARD_HOT_REDEFINE, base64RedefineSh);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, "直接到目标服务器任意路径 粘贴脚本执行，无需打开arthas 【由于没有使用其他存储 执行的脚本比较长,推荐配置阿里云oss】");
            return;
        }
        OSS oss = null;
        try {
            oss = AliyunOssUtils.buildOssClient(project);
            String filePathKey = settings.depthPrintProperty + UUID.randomUUID().toString();
            String urlEncodeKeyPath = AliyunOssUtils.putFile(oss, settings.bucketName, filePathKey, base64RedefineSh);
            String presignedUrl = AliyunOssUtils.generatePresignedUrl(oss, settings.bucketName, urlEncodeKeyPath, new Date(System.currentTimeMillis() + 3600L * 1000));
            command = String.format(OSS_HOT_REDEFINE, presignedUrl);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, "直接到目标服务器任意路径 粘贴脚本执行，无需打开arthas");
        } catch (Exception e) {
            StackTraceUtils.printSanitizedStackTrace(e);
            NotifyUtils.notifyMessage(project, "上传命令到oss 失败" + e.getMessage());
            return;
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }


    }
}

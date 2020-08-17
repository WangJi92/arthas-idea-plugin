package com.github.wangji92.arthas.plugin.action.arthas;

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
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 汪小哥
 * @date 16-08-2020
 */
public class ArthasHotRedefineCommandAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        boolean enabled = true;
        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        //获取当前事件触发时，光标所在的元素
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (psiElement == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        if (psiElement instanceof PsiClass) {
            enabled = true;
        } else if (psiElement instanceof PsiMethod) {
            enabled = true;
        } else if (psiElement instanceof PsiField) {
            enabled = true;
        } else {
            enabled = false;
        }
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        AppSettingsState settings = AppSettingsState.getInstance(project);
        String selectProjectName = settings.selectProjectName;
        if (StringUtils.isBlank(selectProjectName)) {
            NotifyUtils.notifyMessage(project, "必须配置才能使用 jps -l 查看名称,Hot Redefine use project name select process and batch support; as.sh --select projectName -c 'redefine /tmp/test.class'", NotificationType.ERROR);
            return;
        }
        String pathClassName = "";
        String ideaClassName = "";


        boolean isAnonymousClass = false;
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            //处理内部类 匿名类获取class的问题
            pathClassName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiMethod);
            if (pathClassName.contains("*$*")) {
                isAnonymousClass = true;
            } else {
                ideaClassName = psiMethod.getContainingClass().getQualifiedName();
            }
        }
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            pathClassName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiField);
            if (pathClassName.contains("*$*")) {
                isAnonymousClass = true;
            } else {
                ideaClassName = psiField.getContainingClass().getQualifiedName();

            }
        }
        if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            pathClassName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiClass);
            ideaClassName = psiClass.getQualifiedName();
        }

        if (isAnonymousClass) {
            String packageName = ((PsiJavaFile) psiElement.getContainingFile()).getPackageName();
            String outClassName = FilenameUtils.getBaseName(psiElement.getContainingFile().getName());
            // 匿名类 获取当前最外层的outer的类
            ideaClassName = packageName + "." + outClassName;

        }

        //选择了.class 文件 必须要处理 不然获取不到module 的信息,这里重新获取class 原文件的信息
        //根据类的全限定名查询PsiClass，下面这个方法是查询Project域 https://blog.csdn.net/ExcellentYuXiao/article/details/80273448
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(ideaClassName, GlobalSearchScope.projectScope(project));
        // https://jetbrains.org/intellij/sdk/docs/basics/project_structure.html
        // https://jetbrains.org/intellij/sdk/docs/reference_guide/project_model/module.html
        Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        //找到编译的 出口位置
        String outputPath = ModuleRootManager.getInstance(module).getModifiableModel().getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath().getPath();

        List<String> classPackagePaths = Lists.newArrayList();

        if (isAnonymousClass) {
            // 匿名类要处理遍历
            final String packageName = ((PsiJavaFile) psiElement.getContainingFile()).getPackageName();
            String packageNamePath = packageName.replaceAll("\\.", File.separator);
            String outClassName = FilenameUtils.getBaseName(psiElement.getContainingFile().getName());
            // 查找当前类下面的所有的匿名类的信息
            List<File> files = Lists.newArrayList(FileUtils.listFiles(new File(outputPath + File.separator + packageNamePath), new RegexFileFilter(outClassName + ".*[\\$](\\d{0,4}).class$"), FalseFileFilter.INSTANCE));
            classPackagePaths = files.stream().map(file -> String.format("%s%s%s", packageNamePath, File.separator, file.getName())).collect(Collectors.toList());
        } else {
            String path = pathClassName.replaceAll("\\.", File.separator) + ".class";
            classPackagePaths.add(path);
        }

        List<String> bash64FileAndPathList = Lists.newArrayList();

        List<String> shellOutPaths = Lists.newArrayList();
        classPackagePaths.forEach(classPackagePath -> {
            File file = new File(outputPath + "/" + classPackagePath);
            if (!file.exists()) {
                return;
            }
            String classBase64 = IoUtils.readFileToBase64String(file);
            // shell 解析的时候 单引号''，双引号""的区别是单引号''剥夺了所有字符的特殊含义，单引号''内就变成了单纯的字符。双引号""则对于双引号""内的参数替换
            // 内部类 的时候回有问题 展示上面 结果没有影响 这里修改一下
            String pathReplaceAll = classPackagePath.replace("$", "-");
            String pathAndClass = classBase64 + "|" + ArthasCommandConstants.REDEFINE_BASH_PACKAGE_PATH + pathReplaceAll;
            shellOutPaths.add(ArthasCommandConstants.REDEFINE_BASH_PACKAGE_PATH + pathReplaceAll);
            bash64FileAndPathList.add(pathAndClass);
        });

        if (bash64FileAndPathList.size() <= 0) {
            NotifyUtils.notifyMessage(project, "当前选择对于的类文件在target目录.class文件不存在,请编译", NotificationType.ERROR);
            return;
        }


        String arthasIdeaPluginBase64AndPathCommand = String.join(",", bash64FileAndPathList);
        String arthasIdeaPluginRedefineCommand = "redefine " + String.join(" ", shellOutPaths);
        Map<String, String> params = Maps.newHashMap();
        params.put("arthasIdeaPluginBase64AndPathCommand", arthasIdeaPluginBase64AndPathCommand);
        params.put("arthasIdeaPluginRedefineCommand", arthasIdeaPluginRedefineCommand);
        params.put("arthasIdeaPluginApplicationName", selectProjectName);

        String redefineSh = StringUtils.stringSubstitutor("/template/arthas-idea-plugin-redefine.sh", params);

        String base64RedefineSh = BaseEncoding.base64().encode(redefineSh.getBytes());
        String commandFormat = "echo %s |base64 --decode >arthas-idea-plugin-redefine.sh;chmod a+x arthas-idea-plugin-redefine.sh;./arthas-idea-plugin-redefine.sh;";
        String format = String.format(commandFormat, base64RedefineSh);
        ClipboardUtils.setClipboardString(format);

        NotifyUtils.notifyMessage(project, "由于没有使用其他存储 执行的脚本比较长，命令已经复制到了剪切板可以到目标服务器上去执行shell，无需打开arthas ");
    }
}

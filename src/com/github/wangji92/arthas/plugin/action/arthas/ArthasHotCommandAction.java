package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.IoUtils;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author 汪小哥
 * @date 16-08-2020
 */
public class ArthasHotCommandAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ProjectManager instance = ProjectManager.getInstance();
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        PsiElement data = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (psiFile == null) {
            return;
        }
        String className = "";

        if (!(data instanceof PsiClassImpl)) {
            return;
        }
        className = ((PsiClassImpl) data).getQualifiedName();
        //选择了.class 文件 必须要处理 不然获取不到module 的信息,这里重新获取class 原文件的信息
        //根据类的全限定名查询PsiClass，下面这个方法是查询Project域
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.projectScope(project));
        Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        //找到编译的 出口位置
        String outputPath = ModuleRootManager.getInstance(module).getModifiableModel().getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath().getPath();

        String pathClassName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiClass);
        String path = pathClassName.replaceAll("\\.", File.separator) + ".class";

        List<String> classPackagePaths = Lists.newArrayList();
        classPackagePaths.add(path);

        List<String> bash64FileAndPathList = Lists.newArrayList();

        List<String> shellOutPaths = Lists.newArrayList();
        classPackagePaths.forEach(classPackagePath -> {
            File file = new File(outputPath + "/" + path);
            if (!file.exists()) {
                return;
            }
            String classBase64 = IoUtils.readFileToBase64String(file);
            String pathAndClass = classBase64 + "|" + ArthasCommandConstants.REDEFINE_BASH_PACKAGE_PATH + path;
            shellOutPaths.add(ArthasCommandConstants.REDEFINE_BASH_PACKAGE_PATH + path);
            bash64FileAndPathList.add(pathAndClass);
        });

        String arthasIdeaPluginBase64AndPathCommand = String.join(",", bash64FileAndPathList);
        String arthasIdeaPluginRedefineCommand = "redefine " + String.join(" ", shellOutPaths);
        Map<String, String> params = Maps.newHashMap();
        params.put("arthasIdeaPluginBase64AndPathCommand", arthasIdeaPluginBase64AndPathCommand);
        params.put("arthasIdeaPluginRedefineCommand", arthasIdeaPluginRedefineCommand);
        params.put("arthasIdeaPluginApplicationName", "com.wangji92.arthas.plugin.demo.ArthasPluginDemoApplication");

        String redefineSh = StringUtils.stringSubstitutor("/template/arthas-idea-plugin-redefine.sh", params);

        String base64RedefineSh = BaseEncoding.base64().encode(redefineSh.getBytes());
        String commandFormat = "echo %s |base64 --decode >arthas-idea-plugin-redefine.sh;chmod a+x arthas-idea-plugin-redefine.sh;./arthas-idea-plugin-redefine.sh;";
        String format = String.format(commandFormat, base64RedefineSh);
        ClipboardUtils.setClipboardString(format);
    }
}

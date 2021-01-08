package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 汪小哥
 * @date 21-12-2019
 */
public abstract class BaseArthasPluginAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        //获取当前事件触发时，光标所在的元素
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        VirtualFile[] virtualFileFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFileFiles == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (virtualFileFiles.length >= 2) {
            e.getPresentation().setEnabled(false);
            return;
        }
        /**
         * 1、文件导航多选 psiElement ==null psiFile==null virtualFileFiles>2
         * 2、文件导航单选 psiElement !=null  等同于选择了class   [ psiElement 可能为空]   psiFile !=null virtualFileFiles !=null
         * 3、编辑框里面处理 psiElement !=null  内部类  psiFile = OutClass  psiElement == InnerCLass 匿名类  psiFile = OutClass  psiElement == PsiAnonymousClass  virtualFileFiles !=null
         */
        List<PsiFile> psiFileJavaFiles = Arrays.stream(virtualFileFiles).map(PsiManager.getInstance(project)::findFile).filter(psiFileElement -> psiFileElement instanceof PsiJavaFile).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(psiFileJavaFiles)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);


    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        /**
         * {@link com.intellij.ide.actions.CopyReferenceAction}
         */
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String className = "";
        String methodName = "";
        assert virtualFileFiles != null;
        if (virtualFileFiles.length == 1 && OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) psiElement;
                //处理内部类 匿名类获取class的问题
                className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiMethod);
                methodName = psiMethod.getNameIdentifier().getText();
                if(psiMethod.isConstructor()){
                    methodName ="<init>";
                }
            }
            if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiClass);
                methodName = "*";
            }

            if (psiElement instanceof PsiField) {
                PsiField psiField = (PsiField) psiElement;
                className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiField);
                methodName = "*";
            }
        } else {
            PsiFile psiFileJavaFile = PsiManager.getInstance(project).findFile(virtualFileFiles[0]);
            String packageName = ((PsiJavaFile) psiFileJavaFile.getContainingFile()).getPackageName();
            String shortClassName = FilenameUtils.getBaseName(psiFileJavaFile.getContainingFile().getName());
            className = packageName + "." + shortClassName;
            methodName = "*";
        }
        doCommand(className, methodName, project, psiElement);
    }

    /**
     * 构造命令信息,处理命令后续操作
     *
     * @param className
     * @param methodName
     * @param project
     * @param psiElement
     * @return
     */
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {
    }


}

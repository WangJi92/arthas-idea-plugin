package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.*;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 重新加载 mapper xml 文件
 * {@literal https://github.com/WangJi92/mybatis-mapper-reload-spring-boot-start}
 *
 * @author 汪小哥
 * @date 03-05-2021
 */
public class ArthasMybatisMapperReloadAction extends AnAction implements DumbAware {

    private static final Logger LOG = Logger.getInstance(ArthasMybatisMapperReloadAction.class);

    public ArthasMybatisMapperReloadAction() {
        this.setEnabledInModalContext(true);
    }

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
        List<PsiFile> psiXmlFiles = Arrays.stream(virtualFileFiles).map(PsiManager.getInstance(project)::findFile).filter(psiFileElement -> psiFileElement instanceof XmlFile).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(psiXmlFiles) && psiXmlFiles.size() == 1) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String mapperXmlName = psiElement.getContainingFile().getName();

        try {
            AppSettingsState settings = AppSettingsState.getInstance(project);

            // 获取class的classloader @applicationContextProvider@context的前面部分 xxxApplicationContextProvider
            String classNameClassLoaderGet = SpringStaticContextUtils.getStaticSpringContextClassName(project);
            String springContextScCommand = String.join(" ", "sc", "-d", classNameClassLoaderGet);

            //com.github.wangji92.mybatis.reload.core.MybatisMapperXmlFileReloadService#reloadAllSqlSessionFactoryMapper
            String mybatisMapperXmlReloadServiceBeanName = settings.mybatisMapperReloadServiceBeanName;
            String mybatisMapperXmlReloadServiceMethodName = settings.mybatisMapperReloadMethodName;
            String mybatisMapperXmlReloadServiceMapperPath = ArthasCommandConstants.MYBATIS_MAPPER_RELOAD_BASH_PACKAGE_PATH + "/" + mapperXmlName;
            String mybatisMapperXmlReloadServiceMethodAndParam = String.format("%s(\"%s\")", mybatisMapperXmlReloadServiceMethodName, mybatisMapperXmlReloadServiceMapperPath);


            //region 构建spring static context  调用 mybatisMapperXmlReloadService method
            //#springContext=填充,#springContext.getBean("%s")
            String staticSpringContextGetBeanPrefix = SpringStaticContextUtils.getStaticSpringContextGetBeanPrefix(project, mybatisMapperXmlReloadServiceBeanName);
            String join = String.join(" ", "ognl", "-x", settings.depthPrintProperty);
            StringBuilder arthasOgnlReloadMapperCommand = new StringBuilder(join);
            arthasOgnlReloadMapperCommand.append(" '").append(staticSpringContextGetBeanPrefix).append(".").append(mybatisMapperXmlReloadServiceMethodAndParam).append("'");
            arthasOgnlReloadMapperCommand.append(" -c ").append("$CLASSLOADER_HASH_VALUE");
            String arthasIdeaPluginMybatisMapperXmlReloadCommand = arthasOgnlReloadMapperCommand.toString();

            // 坑 这里需要对 "" 中的 "进行转义
            arthasIdeaPluginMybatisMapperXmlReloadCommand = arthasIdeaPluginMybatisMapperXmlReloadCommand.replaceAll("\"", "\\\\\"");
            //endregion
            VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            assert virtualFileFiles != null;
            String mapperXmlContent = IoUtils.readVirtualFile(virtualFileFiles[0]);
            String base64MapperXmlContent = BaseEncoding.base64().encode(mapperXmlContent.getBytes());
            String arthasIdeaPluginBase64MapperXmlAndPath = String.join("|", base64MapperXmlContent, mybatisMapperXmlReloadServiceMapperPath);

            String selectProjectName = settings.selectProjectName;
            Map<String, String> params = Maps.newHashMap();
            params.put("arthasIdeaPluginApplicationName", selectProjectName);
            params.put("arthasPackageZipDownloadUrl", settings.arthasPackageZipDownloadUrl);
            params.put("arthasIdeaPluginMybatisMapperXmlReloadCommand", arthasIdeaPluginMybatisMapperXmlReloadCommand);
            params.put("BASE64_TXT_AND_PATH", arthasIdeaPluginBase64MapperXmlAndPath);
            params.put("SC_COMMAND", springContextScCommand);

            String commonFunctionSh = StringUtils.stringSubstitutor("/template/plugin-common-function.sh", params);
            String mybatisMapperReloadSh = StringUtils.stringSubstitutor("/template/mybatis-mapper-xml-reload.sh", params);
            mybatisMapperReloadSh = commonFunctionSh + "\n" + mybatisMapperReloadSh;
            String base64MybatisMapperReloadSh = BaseEncoding.base64().encode(mybatisMapperReloadSh.getBytes());
            DirectScriptUtils.buildDirectScript(project, settings, base64MybatisMapperReloadSh, "arthas-idea-plugin-mybatis-mapper-xml-reload.sh", directScriptResult -> {
                if (directScriptResult.getResult()) {
                    directScriptResult.getTip().append("【mybatis mapper reload 使用参考插件配置界面】");
                    NotifyUtils.notifyMessage(project, directScriptResult.getTip().toString());
                }
            });
        } catch (Exception e) {
            NotifyUtils.notifyMessage(project, e.getMessage(), NotificationType.ERROR);
        }

    }


}

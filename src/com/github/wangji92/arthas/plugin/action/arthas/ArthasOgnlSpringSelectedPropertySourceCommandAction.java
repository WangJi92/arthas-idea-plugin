package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 获取当前spring 中的配置文件的信息
 * <p>
 * ognl -x 3 '#springContext=@applicationContextProvider@context,#springContext.getEnvironment().getProperty("spring.gd.config.type")' -c 68ceda24
 *
 * @author 汪小哥
 * @date 21-01-2020
 */
public class ArthasOgnlSpringSelectedPropertySourceCommandAction extends AnAction {

    /**
     * 构造表达式
     */
    private static final String SPRING_ENVIRONMENT_PROPERTY = "%s '%s%s.getEnvironment().getProperty(\"%s\")'";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        //https://zhuanlan.zhihu.com/p/47740017
        String selectedText = editor.getSelectionModel().getSelectedText();

        //这里获取spring context的信息
        String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        if (StringUtils.isBlank(springContextValue)) {
            NotifyUtils.notifyMessage(project, "配置 arthas 插件spring context 获取的信息", NotificationType.ERROR);
            return;
        }

        // 获取class的classloader
        List<String> springContextCLass = Splitter.on('@').omitEmptyStrings().splitToList(springContextValue);
        if(CollectionUtils.isEmpty(springContextCLass)){
            NotifyUtils.notifyMessage(project, "配置 arthas 插件spring context 获取的信息", NotificationType.ERROR);
        }
        String className= springContextCLass.get(0);

        springContextValue = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=" + springContextValue;
        if (!springContextValue.endsWith(",")) {
            springContextValue = springContextValue + ",";
        }
        //ognl -x 3 '#springContext=@applicationContextProvider@context,
        String join = String.join(" ", "ognl", "-x", ArthasCommandConstants.RESULT_X);

        String command = String.format(SPRING_ENVIRONMENT_PROPERTY,join,springContextValue,ArthasCommandConstants.SPRING_CONTEXT_PARAM,selectedText);

        new ArthasActionStaticDialog(project, className, command).open("arthas ognl spring get property");


    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
    }
}

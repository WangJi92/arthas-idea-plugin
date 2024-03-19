package com.github.wangji92.arthas.plugin.ui;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.wangji92.arthas.plugin.gui.MyBatisLogManager;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.setting.ApplicationSettingsState;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.ProjectContextHolder;
import com.github.wangji92.arthas.plugin.web.entity.AgentInfo;
import com.github.wangji92.arthas.plugin.web.entity.AgentServerInfo;
import com.github.wangji92.arthas.plugin.web.entity.Env;
import com.github.wangji92.arthas.plugin.web.service.impl.AgentServiceImpl;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * options
 *
 * @author 汪小哥
 * @date 01-01-2021
 */
public class ArthasLogOptionsDialog extends JDialog {
    public static final String MODULE_NOT_EXIST = "这个项目没有模块";
    private JPanel contentPane;
    private JComboBox<String> envSelect;
    private JTextArea commendEdit;
    private JButton execBtn;
    private JComboBox<String> artifactSelect;
    private JComboBox<String> agentIdSelect;

    private final Project project;

    private AppSettingsState setting;

    private static final AgentServiceImpl agentService = new AgentServiceImpl();

    public ArthasLogOptionsDialog(Project project, String command, Editor editor) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(false);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init(project, command, editor);

    }

    private void init(Project project, String command, Editor editor) {
        // 填充模块选择器
        List<Model> moduleList = new ArrayList<>(4);
        String currentArtifactId = getModuleList(project, moduleList);
        if (CollectionUtils.isEmpty(moduleList)) {
            NotifyUtils.notifyMessage(project, MODULE_NOT_EXIST);
            dispose();
            return;
        }
        moduleList.forEach((module) -> artifactSelect.addItem(module.getArtifactId()));

        ApplicationSettingsState service = ApplicationSettingsState.getInstance();
        for (AgentServerInfo serverInfo : service.agentServerInfoList) {
            envSelect.addItem(serverInfo.getName());
        }
        commendEdit.setText(command);

        // 设置环境和模块选择器
        setting = AppSettingsState.getInstance(project);

        String gitBranch = null;
        try {
            Git git = Git.open(new File(project.getBasePath(), ".git"));
            gitBranch = git.getRepository().getBranch();
        } catch (IOException e) {
            // 获取git失败，不做处理
        }
        envSelect.setSelectedItem(Optional.ofNullable(Env.getEnvNameByGitBranch(gitBranch)).orElse(setting.lastSelectEnv));
        envSelect.addActionListener((e) -> setting.lastSelectEnv = (String) envSelect.getSelectedItem());

        artifactSelect.setSelectedItem(Optional.ofNullable(currentArtifactId).orElse(moduleList.get(0).getArtifactId()));

        // 环境或模块变更时, 触发自动匹配
        this.onAgentSelected(moduleList);
        envSelect.addActionListener(e -> this.onAgentSelected(moduleList));
        artifactSelect.addActionListener(e -> this.onAgentSelected(moduleList));

        execBtn.addActionListener((e) -> {
            String newCommend = commendEdit.getText();
            Env env = Env.valueOf((String) envSelect.getSelectedItem());
            String selectedArtifactId = (String) artifactSelect.getSelectedItem();

            List<AgentInfo> agentInfos = new ArrayList<>(agentService.getAgentInfoArtifactId(agentIdSelect.getSelectedItem().toString(), env).values());

            final MyBatisLogManager manager = MyBatisLogManager.getInstance(project);
            if (Objects.nonNull(manager)) {
                Disposer.dispose(manager);
            }
            if (CollectionUtils.isEmpty(agentInfos)) {
                NotifyUtils.notifyMessage(project, String.format("没有找到合适的agent, env: %s, module: %s, 建议修改或添加pom的<name>标签", env.getAgentServerInfo().getName(), selectedArtifactId));
            } else {
                MyBatisLogManager.run(project, agentInfos, newCommend, env, selectedArtifactId, editor);
            }

            setting.lastSelectAgent = Convert.toStr(agentIdSelect.getSelectedItem());
            onCancel();
        });
    }

    private void onAgentSelected(List<Model> moduleList) {
        agentIdSelect.removeAllItems();
        Env env = Env.valueOf((String) envSelect.getSelectedItem());
        String selectedArtifactId = (String) artifactSelect.getSelectedItem();
        String selectedModuleName = moduleList.stream().filter(module -> module.getArtifactId().equals(selectedArtifactId)).findFirst()
                .map(Model::getName).orElse(null);
        if (!ObjectUtil.isAllNotEmpty(env, selectedArtifactId)) {
            return;
        }
        String agentName = ProjectContextHolder.getAgentName(env, selectedArtifactId, selectedModuleName);
        if (StrUtil.isBlank(agentName)) {
            // 未匹配到agent, 供用户选择
            List<String> appNames = ProjectContextHolder.getAgents(env);
            appNames.forEach(agentIdSelect::addItem);

            // 默认选中之前选中的agent
            agentIdSelect.setSelectedItem(setting.lastSelectAgent);
        } else {
            // 匹配到唯一agent, 禁用选择
            agentIdSelect.addItem(agentName);
            agentIdSelect.setEditable(false);
        }
    }

    /**
     * 扫描project下面的模块名字
     *
     * @param project 项目
     * @return 当前模块artifactId
     */
    private static String getModuleList(Project project, List<Model> moduleList) {
        // 获取当前文件所属模块
        Module currentFileModule = getCurrentFileModule(project);

        String currentArtifactId = null;
        // 填充模块选择器
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            String moduleFilePath = module.getModuleFilePath().replace("/.idea", "");
            moduleFilePath = moduleFilePath.substring(0, moduleFilePath.lastIndexOf("/"));
            String pomFile = moduleFilePath + "/pom.xml";
            try {
                Model model = new MavenXpp3Reader().read(Files.newInputStream(new File(pomFile).toPath()));
                moduleList.add(model);
                if (currentFileModule != null && currentFileModule.equals(module)) {
                    currentArtifactId = model.getArtifactId();
                }
            } catch (IOException | XmlPullParserException e) {
                throw new RuntimeException(e);
            }
        }

        return currentArtifactId;
    }

    @Nullable
    private static Module getCurrentFileModule(Project project) {
        Module currentFileModule = null;
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (currentFile != null) {
                currentFileModule = ModuleUtilCore.findModuleForFile(currentFile, project);
            }
        }
        return currentFileModule;
    }

    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("arthas options use");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }
}

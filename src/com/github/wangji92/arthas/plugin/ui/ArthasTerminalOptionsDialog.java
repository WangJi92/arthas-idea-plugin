package com.github.wangji92.arthas.plugin.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.wangji92.arthas.plugin.common.pojo.AgentInfo;
import com.github.wangji92.arthas.plugin.common.pojo.TunnelServerInfo;
import com.github.wangji92.arthas.plugin.common.swing.PlaceholderTextSearchField;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.ArthasTerminalManager;
import com.github.wangji92.arthas.plugin.utils.HttpUtil;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.github.wangji92.arthas.plugin.utils.TunnelServerPath;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OptionsDialog
 *
 * @author https://github.com/shuxiongwuziqi
 * @date 01-01-2024
 */
public class ArthasTerminalOptionsDialog extends JDialog {
    private static final String PLACEHOLDER = "Searchable Agent";
    private JPanel contentPane;
    private JComboBox<String> nameServerSelector;
    private JTextArea commendEdit;
    private JButton execBtn;
    private JComboBox<String> appSelector;
    private JPanel searchableAgentPanel;

    private final Project project;

    /**
     * 设置信息
     */
    private AppSettingsState setting;

    private Map<String, AgentInfo> agentInfoMap;
    private List<String> appIdList;

    public ArthasTerminalOptionsDialog(Project project, String command, Editor editor) {
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

        commendEdit.setText(command);

        // 设置环境和模块选择器
        setting = AppSettingsState.getInstance(project);
        setting.lastSelectApp = setting.lastSelectApp == null ? new HashMap<>() : setting.lastSelectApp;

        // set search
        var searchableAgent = new PlaceholderTextSearchField(PLACEHOLDER, (fs) -> loadAppSelector(false, fs));
        searchableAgentPanel.add(searchableAgent);

        loadNameServerSelector();
        loadAppSelector(true, null);
        loadAgentInfo();

        nameServerSelector.addItemListener(e -> {
            searchableAgent.clearText();
            loadAppSelector(true, null);
        });
        appSelector.addItemListener(e -> loadAgentInfo());

        execBtn.addActionListener((e) -> {

            String newCommend = commendEdit.getText();
            TunnelServerInfo tunnelServerInfo = setting.tunnelServerList.get(nameServerSelector.getSelectedIndex());
            List<AgentInfo> agentInfos = agentInfoMap.values().stream().toList();
            // open terminal
            ArthasTerminalManager.run(project, agentInfos, newCommend, tunnelServerInfo, editor);
            // save last select
            setting.lastSelectTunnelServer = nameServerSelector.getSelectedItem().toString();
            setting.lastSelectApp.put(nameServerSelector.getSelectedItem().toString(), appSelector.getSelectedItem().toString());
            onCancel();
        });
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

    private void loadNameServerSelector() {
        nameServerSelector.removeAllItems();
        List<TunnelServerInfo> tunnelServerList = setting.tunnelServerList;
        if (CollectionUtils.isNotEmpty(tunnelServerList)) {
            for (int i = 0; i < tunnelServerList.size(); i++) {
                TunnelServerInfo tunnelServerInfo = tunnelServerList.get(i);
                String nameServer = tunnelServerInfo.getName();
                nameServerSelector.addItem(nameServer);
                if (Objects.equals(setting.lastSelectTunnelServer, nameServer)) {
                    // 默认选中之前选中的
                    nameServerSelector.setSelectedIndex(i);
                }
            }
        }
    }

    private void loadAppSelector(boolean refresh, String filterString) {
        if (nameServerSelector.getSelectedIndex() < 0) {
            return;
        }
        List<String> appIds = this.appIdList;
        if (refresh) {
            TunnelServerInfo nameServerSelectIdx = setting.tunnelServerList.get(nameServerSelector.getSelectedIndex());
            String appsUrl = TunnelServerPath.getAppsPath(nameServerSelectIdx.getAddress());
            String resp = HttpUtil.get(appsUrl);
            appIds = JSON.parseArray(resp).toJavaList(String.class).stream().sorted().collect(Collectors.toList());
            appIdList = appIds;
        }
        appSelector.removeAllItems();
        String lastSelectAgent = setting.lastSelectApp.get(nameServerSelector.getSelectedItem().toString());
        for (int i = 0, j = 0; i < appIds.size(); i++) {
            String appId = appIds.get(i);
            if (StringUtils.isNotBlank(filterString) && !appId.contains(filterString)) {
                continue;
            }
            appSelector.addItem(appId);
            if (Objects.equals(lastSelectAgent, appId)) {
                appSelector.setSelectedIndex(j);
            }
            j++;
        }

    }

    /**
     * 把agentId填充到AgentInfo中，方便后续处理
     *
     * @param res 返回
     * @return AgentInfoMap
     */
    private static Map<String, AgentInfo> getAgentInfoMap(String res) {
        return Optional.ofNullable(res).map(str -> JSON.parseObject(str, new TypeReference<Map<String, AgentInfo>>() {
        })).map(infoMap -> {
            infoMap.keySet().forEach(id -> infoMap.get(id).setAgentId(id));
            return infoMap;
        }).orElse(null);
    }

    private void loadAgentInfo() {
        if (appSelector.getSelectedItem() == null) {
            return;
        }
        String appId = appSelector.getSelectedItem().toString();
        String agentsUrl = TunnelServerPath.getAgentInfoPath(setting.tunnelServerList.get(nameServerSelector.getSelectedIndex()).getAddress(), appId);
        String resp = HttpUtil.get(agentsUrl);
        this.agentInfoMap = getAgentInfoMap(resp);
    }
}

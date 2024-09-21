package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.action.terminal.tunnel.ArthasTerminalManager;
import com.github.idea.arthas.plugin.action.terminal.tunnel.service.ArthasTunnelServerService;
import com.github.idea.arthas.plugin.common.pojo.AgentInfo;
import com.github.idea.arthas.plugin.common.pojo.TunnelServerInfo;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 打开 ArthasTunnelTerminal 预处理 Dialog 选择 Arthas Tunnel Server Agent 信息
 *
 * @author https://github.com/shuxiongwuziqi
 * @date 01-01-2024
 */
public class ArthasTunnelTerminalPretreatmentDialog extends JDialog {
    private static final String PLACEHOLDER = "Searchable Agent";

    private JPanel contentPane;

    private JComboBox<String> nameServerSelector;

    private JTextArea commendEdit;

    private JButton execBtn;

    private JComboBox<String> appSelector;

    //private JPanel searchableAgentPanel;

    private ActionLink tunnelAgentLabel;

    private JLabel command;

    private ActionLink tunnelServerLabel;

    private final Project project;

    /**
     * 设置信息
     */
    private AppSettingsState setting;

    private final ArthasTunnelServerService arthasTunnelServerService = new ArthasTunnelServerService();

    private Map<String, AgentInfo> agentInfoMap;
    private List<String> appIdList;

    public ArthasTunnelTerminalPretreatmentDialog(Project project, String command, Editor editor) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(false);
        getRootPane().setDefaultButton(execBtn);
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
        execBtn.setEnabled(false);

        // 设置环境和模块选择器
        setting = AppSettingsState.getInstance(project);
        setting.lastSelectApp = setting.lastSelectApp == null ? new HashMap<>() : setting.lastSelectApp;

        // set search
//        var searchableAgent = new PlaceholderTextSearchField(PLACEHOLDER, (fs) -> loadAppSelector(false, fs));
//        searchableAgentPanel.add(searchableAgent);

        loadNameServerSelector();
        loadAppSelector(true, null);
        loadAgentInfo();

        nameServerSelector.addItemListener(e -> {
           // searchableAgent.clearText();
            loadAppSelector(true, null);
        });
        appSelector.addItemListener(e -> loadAgentInfo());

        execBtn.addActionListener((e) -> {

            String newCommend = commendEdit.getText();
            TunnelServerInfo tunnelServerInfo = setting.tunnelServerList.get(nameServerSelector.getSelectedIndex());
            if (Objects.isNull(agentInfoMap) || agentInfoMap.isEmpty()) {
                String msg = new String("Agent Not Found".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                Messages.showErrorDialog(msg, "Tips");
                return;
            }
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
        setTitle("Arthas Tunnel");
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
            appIdList = appIds = arthasTunnelServerService.getAppIdList(nameServerSelectIdx.getTunnelAddress());
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

    private void loadAgentInfo() {
        if (appSelector.getSelectedItem() == null) {
            execBtn.setEnabled(false);
            return;
        }
        String tunnelAddress = setting.tunnelServerList.get(nameServerSelector.getSelectedIndex()).getTunnelAddress();
        String appId = appSelector.getSelectedItem().toString();
        this.agentInfoMap = arthasTunnelServerService.getAgentInfoMap(tunnelAddress, appId);
        if (this.agentInfoMap != null && !this.agentInfoMap.isEmpty()) {
            execBtn.setEnabled(true);
        } else {
            execBtn.setEnabled(false);
        }
    }

    private void createUIComponents() {
       // agent Id
        tunnelAgentLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tunnel.html#%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5");
        tunnelServerLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tunnel.html#tunnel-server-%E7%9A%84%E7%AE%A1%E7%90%86%E9%A1%B5%E9%9D%A2");
    }
}

package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.action.terminal.tunnel.ArthasTerminalManager;
import com.github.idea.arthas.plugin.action.terminal.tunnel.service.ArthasTunnelServerService;
import com.github.idea.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.idea.arthas.plugin.common.combox.CustomDefaultListCellRenderer;
import com.github.idea.arthas.plugin.common.pojo.AgentInfo;
import com.github.idea.arthas.plugin.common.pojo.TunnelServerInfo;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.OpenConfigDialogUtils;
import com.github.idea.arthas.plugin.utils.StringUtils;
import com.google.common.collect.Lists;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    private JPanel contentPane;

    private JComboBox<CustomComboBoxItem<TunnelServerInfo>> tunnelServerComboBox;

    private JComboBox<String> appComboBox;

    private JComboBox<CustomComboBoxItem<AgentInfo>> agentComboBox;

    private JTextArea commendEdit;

    private JButton execBtn;


    private ActionLink tunnelAppLabel;

    private JLabel command;

    private ActionLink tunnelServerLabel;

    private JButton settingTunnelServerButton;

    private ActionLink agentLabel;


    private final Project project;

    /**
     * 设置信息
     */
    private AppSettingsState setting;

    private final ArthasTunnelServerService arthasTunnelServerService = new ArthasTunnelServerService();


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
        // open setting
        settingTunnelServerButton.addActionListener((event) -> {
            OpenConfigDialogUtils.openConfigDialog(project);
        });
        init(project, command, editor);

    }

    private void init(Project project, String command, Editor editor) {

        commendEdit.setText(command);
        execBtn.setEnabled(false);

        // 设置环境和模块选择器
        setting = AppSettingsState.getInstance(project);
        setting.lastSelectApp = setting.lastSelectApp == null ? new HashMap<>() : setting.lastSelectApp;

        tunnelServerComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                agentComboBox.removeAllItems();
                appComboBox.removeAllItems();
                loadAppList(false);
            }
        });

        appComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                agentComboBox.removeAllItems();
                loadAgentIdList();
            }
        });
        agentComboBox.addItemListener(e -> {
            showExeBtn();
        });

        loadTunnelServerList();
        loadAppList(true);
        loadAgentIdList();


        execBtn.addActionListener((e) -> {
            String newCommend = commendEdit.getText();
            if (StringUtils.isBlank(newCommend)) {
                newCommend = "help";
            }
            TunnelServerInfo tunnelServerInfo = this.getCurrentTunnelServer();
            if (tunnelServerInfo == null) {
                return;
            }
            AgentInfo currentAgentInfo = this.getCurrentAgentInfo();
            List<AgentInfo> agentInfos = Lists.newArrayList(currentAgentInfo);
            // open terminal
            ArthasTerminalManager.run(project, agentInfos, newCommend, tunnelServerInfo, editor);
            // save last select
            setting.lastSelectTunnelServer = tunnelServerInfo.getName();

            String currentAppId = this.getCurrentAppId();
            setting.lastSelectApp.put(tunnelServerInfo.getName(), currentAppId);
            onCancel();
        });
    }

    private void showExeBtn() {
        AgentInfo currentAgentInfo = this.getCurrentAgentInfo();
        String currentAppId = this.getCurrentAppId();
        if (StringUtils.isNotBlank(currentAppId) && currentAgentInfo != null) {
            execBtn.setEnabled(true);
        } else {
            execBtn.setEnabled(false);
        }
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

    /**
     * 加载本地配置的 server 列表
     */
    private void loadTunnelServerList() {
        tunnelServerComboBox.removeAllItems();
        tunnelServerComboBox.setRenderer(new CustomDefaultListCellRenderer(tunnelServerComboBox, null));
        List<TunnelServerInfo> tunnelServerList = setting.tunnelServerList;
        if (CollectionUtils.isNotEmpty(tunnelServerList)) {
            for (TunnelServerInfo tunnelServerInfo : tunnelServerList) {
                String tunnelServerName = tunnelServerInfo.getName();
                CustomComboBoxItem<TunnelServerInfo> boxItem = new CustomComboBoxItem<TunnelServerInfo>();
                boxItem.setContentObject(tunnelServerInfo);
                boxItem.setDisplay(tunnelServerInfo.getName());
                boxItem.setTipText(String.format("%s:%s", tunnelServerInfo.getTunnelAddress(), tunnelServerInfo.getWsAddress()));
                tunnelServerComboBox.addItem(boxItem);
                if (Objects.equals(setting.lastSelectTunnelServer, tunnelServerName)) {
                    // 默认选中之前选中的
                    tunnelServerComboBox.setSelectedItem(tunnelServerInfo);
                }
            }
        }
    }

    private void loadAppList(boolean init) {
        String currentTunnelAddress = this.getCurrentTunnelAddress();
        if (StringUtils.isBlank(currentTunnelAddress)) {
            return;
        }
        List<String> appIds = arthasTunnelServerService.getAppIdList(currentTunnelAddress);
        String lastSelectAgent = setting.lastSelectApp.get(currentTunnelAddress);
        for (String appId : appIds) {
            appComboBox.addItem(appId);
            if (init && Objects.equals(lastSelectAgent, appId)) {
                appComboBox.setSelectedItem(appId);
            }
        }
    }

    private void loadAgentIdList() {
        String currentTunnelAddress = this.getCurrentTunnelAddress();
        if (StringUtils.isBlank(currentTunnelAddress)) {
            return;
        }
        String currentAppId = this.getCurrentAppId();
        if (StringUtils.isBlank(currentAppId)) {
            return;
        }
        Map<String, AgentInfo> agentInfoMap = arthasTunnelServerService.getAgentInfoMap(currentTunnelAddress, currentAppId);
        agentComboBox.setRenderer(new CustomDefaultListCellRenderer(agentComboBox, null));
        for (AgentInfo agent : agentInfoMap.values()) {
            CustomComboBoxItem<AgentInfo> boxItem = new CustomComboBoxItem<AgentInfo>();
            boxItem.setContentObject(agent);
            boxItem.setDisplay(String.format("%s:%s:%s", currentAppId, agent.getHost(), agent.getPort()));
            boxItem.setTipText(agent.toString());
            agentComboBox.addItem(boxItem);
        }
    }

    /**
     * 获取 getTunnelAddress
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getCurrentTunnelAddress() {
        Object selectedItem = tunnelServerComboBox.getSelectedItem();
        if (selectedItem == null) {
            return "";
        }
        CustomComboBoxItem<TunnelServerInfo> selectInfo = (CustomComboBoxItem<TunnelServerInfo>) selectedItem;
        return selectInfo.getContentObject().getTunnelAddress();
    }

    @SuppressWarnings("unchecked")
    private TunnelServerInfo getCurrentTunnelServer() {
        Object selectedItem = tunnelServerComboBox.getSelectedItem();
        if (selectedItem == null) {
            return null;
        }
        CustomComboBoxItem<TunnelServerInfo> selectInfo = (CustomComboBoxItem<TunnelServerInfo>) selectedItem;
        return selectInfo.getContentObject();
    }

    /**
     * 获取当前appId
     *
     * @return
     */
    private String getCurrentAppId() {
        Object selectedItem = appComboBox.getSelectedItem();
        if (selectedItem == null) {
            return "";
        }
        return selectedItem.toString();
    }


    @SuppressWarnings("unchecked")
    private AgentInfo getCurrentAgentInfo() {
        Object selectedItem = agentComboBox.getSelectedItem();
        if (selectedItem == null) {
            return null;
        }
        CustomComboBoxItem<AgentInfo> selectInfo = (CustomComboBoxItem<AgentInfo>) selectedItem;
        return selectInfo.getContentObject();
    }

    private void createUIComponents() {
        // agent Id
        tunnelAppLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tunnel.html#%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5");
        agentLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tunnel.html#%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5");
        tunnelServerLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tunnel.html#tunnel-server-%E7%9A%84%E7%AE%A1%E7%90%86%E9%A1%B5%E9%9D%A2");
    }
}

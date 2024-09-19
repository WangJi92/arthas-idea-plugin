package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.action.terminal.ClearAllAction;
import com.github.wangji92.arthas.plugin.action.terminal.ModifyRerunAction;
import com.github.wangji92.arthas.plugin.action.terminal.RerunAction;
import com.github.wangji92.arthas.plugin.action.terminal.StopAction;
import com.github.wangji92.arthas.plugin.common.pojo.AgentInfo;
import com.github.wangji92.arthas.plugin.common.pojo.TunnelServerInfo;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBusConnection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ArthasTerminalManager
 *
 * @author https://github.com/shuxiongwuziqi
 */
public class ArthasTerminalManager implements Disposable {

    private static final Key<ArthasTerminalManager> KEY = Key.create(ArthasTerminalManager.class.getName());
    private static final String ARTHAS_PLUS = "Arthas Plus";

    private final ConsoleView consoleView;
    private final Project project;
    private final RunContentDescriptor descriptor;

    @Getter
    private volatile boolean running = false;

    private List<ArthasWebSocketClient> webSocketClients;

    private final List<AgentInfo> agentInfos;

    private final String cmd;

    private final TunnelServerInfo tunnelServerInfo;

    private final Editor editor;

    private final List<String> historyCache = new ArrayList<>();
    /**
     * 按下↑键的次数,存储起来用于获取历史记录指令
     */
    private int vkUpCache = 0;

    private ArthasTerminalManager(@NotNull Project project, List<AgentInfo> agentInfos, String cmd, TunnelServerInfo tunnelServerInfo, Editor editor) {
        this.agentInfos = agentInfos;
        this.cmd = cmd;
        // 添加第一条指令
        historyCache.add(this.cmd);

        this.tunnelServerInfo = tunnelServerInfo;
        this.editor = editor;

        this.project = project;

        this.consoleView = createConsoleView();

        final JPanel panel = createConsolePanel(this.consoleView);

        RunnerLayoutUi layoutUi = getRunnerLayoutUi();

        Content content = layoutUi.createContent(UUID.randomUUID().toString(), panel, cmd, Icons.FAVICON, panel);

        content.setCloseable(false);

        layoutUi.addContent(content);

        layoutUi.getOptions().setLeftToolbar(createActionToolbar(), "RunnerToolbar");

        final MessageBusConnection messageBusConnection = project.getMessageBus().connect();

        this.descriptor = getRunContentDescriptor(layoutUi, cmd);

        Disposer.register(this, consoleView);
        Disposer.register(this, content);
        Disposer.register(this, layoutUi.getContentManager());
        Disposer.register(this, messageBusConnection);


        messageBusConnection.subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
            @Override
            public void toolWindowRegistered(@NotNull String id) {

            }

            @Override
            public void stateChanged() {
                if (!getToolWindow().isAvailable()) {
                    Disposer.dispose(ArthasTerminalManager.this);
                }
            }
        });

        ExecutionManager.getInstance(project).getContentManager().showRunContent(ArthasTerminalExecutor.getInstance(),
                descriptor);

        getToolWindow().activate(null);


        this.webSocketClients = createWebSocketClients(agentInfos, cmd, tunnelServerInfo, editor);
    }

    private List<ArthasWebSocketClient> createWebSocketClients(List<AgentInfo> agentInfos, String cmd, TunnelServerInfo tunnelServerInfo, Editor editor) {
        return agentInfos.stream().map(agentInfo ->
                        createSocketConnection(cmd, agentInfo.getAgentId(), agentInfo.getClientConnectHost(), tunnelServerInfo, editor))
                .filter(Objects::nonNull)
                .peek(client -> Disposer.register(this, client))
                .toList();
    }

    public static void run(Project project, List<AgentInfo> agentInfos, String cmd, TunnelServerInfo tunnelServerInfo, Editor editor) {

        final ArthasTerminalManager manager = ArthasTerminalManager.getInstance(project);
        if (Objects.nonNull(manager)) {
            Disposer.dispose(manager);
        }
        ArthasTerminalManager.createInstance(project, agentInfos, cmd, tunnelServerInfo, editor).run();
    }

    private ArthasWebSocketClient createSocketConnection(String cmd, String agentId, String host, TunnelServerInfo tunnelServerInfo, Editor editor) {

        String uri = TunnelServerPath.getWsUrl(agentId, host, tunnelServerInfo);
        ArthasWebSocketClient client = null;
        try {
            client = new ArthasWebSocketClient(new URI(uri), agentId, editor, this.consoleView);
            client.connectBlocking();
            Thread.sleep(500);
            client.sendCmd(cmd);
        } catch (Exception e) {
            NotifyUtils.notifyMessage(editor.getProject(), "连接失败" + agentId);
        }

        return client;
    }

    private ConsoleView createConsoleView() {
        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        final ConsoleViewImpl console = (ConsoleViewImpl) consoleBuilder.getConsole();
        // init editor
        console.getComponent();

        return console;
    }

    private ActionGroup createActionToolbar() {
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RerunAction(this));
        actionGroup.add(new ModifyRerunAction(this.project, this.editor, this.cmd));
        actionGroup.add(new StopAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new ClearAllAction(this.consoleView));
        return actionGroup;
    }

    private JPanel createConsolePanel(ConsoleView consoleView) {
        final JPanel mainPanel = new JPanel();
        // 显示区域
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(consoleView.getComponent(), BorderLayout.CENTER);
        // 操作区域
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        // 命令框
        JTextField cmdInput = new JTextField(10);
        cmdInput.setMaximumSize(new Dimension(800, 30));
        // 执行按钮
        JButton execBtn = new JButton("Exec");
        // 注册监听器
        this.registerListener(execBtn, cmdInput);
        // 添加至面板
        controlPanel.add(Box.createHorizontalStrut(1));
        controlPanel.add(cmdInput);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(execBtn);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void registerListener(JButton execBtn, JTextField cmdInput) {
        // 监听按钮和回车事件
        execBtn.addActionListener(e -> executeCmd(cmdInput));
        cmdInput.addActionListener(e -> executeCmd(cmdInput));
        // 监听文本框的键盘↑↓键
        cmdInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String lastInput = null;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (vkUpCache >= historyCache.size()) {
                            return;
                        }
                        lastInput = historyCache.get(historyCache.size() - ++vkUpCache);
                        break;
                    case KeyEvent.VK_DOWN:
                        if (vkUpCache <= 0) {
                            cmdInput.setText("");
                            return;
                        }
                        lastInput = historyCache.get(historyCache.size() - vkUpCache--);
                        break;
                    default:
                }
                if (StringUtils.isNotBlank(lastInput)) {
                    cmdInput.setText(lastInput);
                    cmdInput.setCaretPosition(cmdInput.getText().length());
                }
            }
        });
    }

    private void executeCmd(JTextField inputField) {
        String command = inputField.getText();
        if (StringUtils.isBlank(command)) {
            return;
        }
        rerun(command);
        historyCache.add(command);
        inputField.setText("");
        vkUpCache = 0;
    }

    private RunContentDescriptor getRunContentDescriptor(RunnerLayoutUi layoutUi, String name) {
        RunContentDescriptor descriptor = new RunContentDescriptor(new RunProfile() {
            @Nullable
            @Override
            public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
                return null;
            }

            @NotNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            @Nullable
            public Icon getIcon() {
                return null;
            }
        }, new DefaultExecutionResult(), layoutUi);
        descriptor.setExecutionId(System.nanoTime());

        return descriptor;
    }

    private RunnerLayoutUi getRunnerLayoutUi() {
        return RunnerLayoutUi.Factory.getInstance(project).create(ARTHAS_PLUS, ARTHAS_PLUS, ARTHAS_PLUS, project);
    }

    public void run() {
        if (running) {
            return;
        }
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        this.webSocketClients.forEach(ArthasWebSocketClient::dispose);
        this.webSocketClients = null;
    }

    public void rerun(String command) {
        if (running) {
            // 先把之前的client全都停掉
            stop();
        }
        running = true;
        command = StringUtils.defaultString(command, this.cmd);
        this.webSocketClients = createWebSocketClients(this.agentInfos, command, this.tunnelServerInfo, editor);
    }

    @Nullable
    public static ArthasTerminalManager getInstance(@NotNull Project project) {
        ArthasTerminalManager manager = project.getUserData(KEY);
        if (Objects.nonNull(manager)) {
            if (!manager.getToolWindow().isAvailable()) {
                Disposer.dispose(manager);
                manager = null;
            }
        }
        return manager;
    }

    @NotNull
    public static ArthasTerminalManager createInstance(@NotNull Project project, List<AgentInfo> agentInfos, String cmd, TunnelServerInfo tunnelServerInfo, Editor editor) {
        ArthasTerminalManager manager = getInstance(project);
        if (Objects.nonNull(manager) && !Disposer.isDisposed(manager)) {
            Disposer.dispose(manager);
        }
        manager = new ArthasTerminalManager(project, agentInfos, cmd, tunnelServerInfo, editor);
        project.putUserData(KEY, manager);
        return manager;
    }

    public ToolWindow getToolWindow() {
        return ToolWindowManager.getInstance(project).getToolWindow(ArthasTerminalExecutor.TOOL_WINDOW_ID);
    }


    @Override
    public void dispose() {
        project.putUserData(KEY, null);
        stop();
        ExecutionManager.getInstance(project).getContentManager().removeRunContent(ArthasTerminalExecutor.getInstance(), descriptor);
    }

}

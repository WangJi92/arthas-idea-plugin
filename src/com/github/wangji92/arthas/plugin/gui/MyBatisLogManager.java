package com.github.wangji92.arthas.plugin.gui;


import com.alibaba.fastjson.JSON;
import com.github.wangji92.arthas.plugin.action.arthas.ClearAllAction;
import com.github.wangji92.arthas.plugin.action.arthas.RerunAction;
import com.github.wangji92.arthas.plugin.action.arthas.StopAction;
import com.github.wangji92.arthas.plugin.common.constant.Icons;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.web.MyWebSocketClient;
import com.github.wangji92.arthas.plugin.web.entity.AgentInfo;
import com.github.wangji92.arthas.plugin.web.entity.Env;
import com.github.wangji92.arthas.plugin.web.request.WsArthasRequest;
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
import com.intellij.execution.ui.ConsoleViewContentType;
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
import org.apache.commons.collections.CollectionUtils;
import org.java_websocket.client.WebSocketClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * MyBatisLogManager
 *
 * @author huangxingguang
 */
public class MyBatisLogManager implements Disposable {

    private static final Key<MyBatisLogManager> KEY = Key.create(MyBatisLogManager.class.getName());

    private final Map<Integer, ConsoleViewContentType> consoleViewContentTypes = new ConcurrentHashMap<>();

    private final ConsoleViewImpl consoleView;
    private final Project project;
    private final RunContentDescriptor descriptor;

    private final AtomicInteger counter;
    private volatile String preparing;
    private volatile String parameters;
    private volatile boolean running = false;

    private List<MyWebSocketClient> webSocketClients;

    private final List<String> keywords = new ArrayList<>(0);

    private final List<AgentInfo> agentInfos;

    private final String cmd;

    private final Env env;

    private Editor editor;

    private MyBatisLogManager(@NotNull Project project, List<AgentInfo> agentInfos, String cmd, Env env, Editor editor) {
        this.agentInfos = agentInfos;
        this.cmd = cmd;
        this.env = env;
        this.editor = editor;

        this.project = project;

        this.consoleView = createConsoleView();

        final JPanel panel = createConsolePanel(this.consoleView);

        RunnerLayoutUi layoutUi = getRunnerLayoutUi();

        Content content = layoutUi.createContent(UUID.randomUUID().toString(), panel, cmd, Icons.MY_BATIS, panel);

        content.setCloseable(false);

        layoutUi.addContent(content);

        layoutUi.getOptions().setLeftToolbar(createActionToolbar(), "RunnerToolbar");

        final MessageBusConnection messageBusConnection = project.getMessageBus().connect();

        this.counter = new AtomicInteger();
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
                    Disposer.dispose(MyBatisLogManager.this);
                }
            }
        });

        ExecutionManager.getInstance(project).getContentManager().showRunContent(ArthasPlusExecutor.getInstance(),
                descriptor);

        getToolWindow().activate(null);

        this.webSocketClients = createWebSocketClients(agentInfos, cmd, env, editor);
    }

    private List<MyWebSocketClient> createWebSocketClients(List<AgentInfo> agentInfos, String cmd, Env env, Editor editor) {
        return agentInfos.stream().map(agentInfo ->
                        createSocketConnection(cmd, agentInfo.getAgentId(), agentInfo.getClientConnectHost(), env, editor))
                .filter(Objects::nonNull)
                .peek(client -> {
                    client.setConsoleView(consoleView);
                    Disposer.register(this, client);
                })
                .collect(Collectors.toList());
    }

    public static void run(Project project, List<AgentInfo> agentInfos, String cmd, Env env, String artifactId, Editor editor) {

        final MyBatisLogManager manager = MyBatisLogManager.getInstance(project);
        if (Objects.nonNull(manager)) {
            Disposer.dispose(manager);
        }
        if (CollectionUtils.isEmpty(agentInfos)) {
            NotifyUtils.notifyMessage(project, String.format("没有找到合适的agent, env: %s, module: %s, 建议修改或添加pom的<name>标签", env.getAgentServerInfo().getName(), artifactId));
        } else {
            MyBatisLogManager.createInstance(project, agentInfos, cmd, env, editor).run();
        }
    }

    private static MyWebSocketClient createSocketConnection(String cmd, String agentId, String host, Env env, Editor editor) {

        String uri = env.getWsUrl(agentId, host);
        MyWebSocketClient client = null;
        try {
            client = new MyWebSocketClient(new URI(uri), agentId, editor);
            client.connectBlocking();
            client.send(JSON.toJSONString(new WsArthasRequest(cmd + "\r")));
        } catch (Exception e) {
            NotifyUtils.notifyMessage(editor.getProject(), "连接失败" + agentId);
        }

        return client;
    }

    private ConsoleViewImpl createConsoleView() {
        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        final ConsoleViewImpl console = (ConsoleViewImpl) consoleBuilder.getConsole();
        // init editor
        console.getComponent();

        return console;
    }

    private ActionGroup createActionToolbar() {

        final ConsoleViewImpl consoleView = this.consoleView;

        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RerunAction(this));
        actionGroup.add(new StopAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new ClearAllAction(consoleView));

        return actionGroup;
    }

    private JPanel createConsolePanel(ConsoleView consoleView) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(consoleView.getComponent(), BorderLayout.CENTER);
        return panel;
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

        return RunnerLayoutUi.Factory.getInstance(project).create("Arthas Plus", "Arthas Plus", "Arthas Plus", project);
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
        this.webSocketClients.forEach(MyWebSocketClient::dispose);
        this.webSocketClients = null;
    }

    public void rerun() {
        if (running) {
            // 先把之前的client全都停掉
            stop();
        }
        running = true;
        this.webSocketClients = createWebSocketClients(this.agentInfos, this.cmd, this.env, editor);
    }

    @Nullable
    public static MyBatisLogManager getInstance(@NotNull Project project) {

        MyBatisLogManager manager = project.getUserData(KEY);

        if (Objects.nonNull(manager)) {
            if (!manager.getToolWindow().isAvailable()) {
                Disposer.dispose(manager);
                manager = null;
            }
        }

        return manager;

    }

    @NotNull
    public static MyBatisLogManager createInstance(@NotNull Project project, List<AgentInfo> agentInfos, String cmd, Env env, Editor editor) {


        MyBatisLogManager manager = getInstance(project);

        if (Objects.nonNull(manager) && !Disposer.isDisposed(manager)) {
            Disposer.dispose(manager);
        }

        manager = new MyBatisLogManager(project, agentInfos, cmd, env, editor);
        project.putUserData(KEY, manager);

        return manager;

    }

    public ToolWindow getToolWindow() {
        return ToolWindowManager.getInstance(project).getToolWindow(ArthasPlusExecutor.TOOL_WINDOW_ID);
    }


    public boolean isRunning() {
        return running;
    }

    @Override
    public void dispose() {

        project.putUserData(KEY, null);

        stop();

        ExecutionManager.getInstance(project).getContentManager().removeRunContent(ArthasPlusExecutor.getInstance(),
                descriptor);

    }

}

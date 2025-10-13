package com.github.idea.arthas.plugin.action.terminal.tunnel;


import com.alibaba.fastjson.JSON;
import com.github.idea.arthas.plugin.common.pojo.AgentInfo;
import com.github.idea.arthas.plugin.common.pojo.TunnelServerInfo;
import com.github.idea.arthas.plugin.common.pojo.WsArthasRequest;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Disposer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.idea.arthas.plugin.action.terminal.tunnel.ArthasWebSocketClient.LISTENING_STR;
import static com.github.idea.arthas.plugin.action.terminal.tunnel.ArthasWebSocketClient.RESIZE_WIDTH;

/**
 * @author imyzt
 * @date 2024/09/20
 * @description 命令打印管理器
 */
public class ArthasTerminalConsoleViewManager implements Disposable {

    private List<ArthasWebSocketClient> webSocketClients;


    private boolean isStartPrint;

    private final MarkupModel markupModel;

    private final List<RangeHighlighter> highlighters;

    private final List<AgentInfo> agentInfos;

    private final String cmd;

    private final TunnelServerInfo tunnelServerInfo;

    private final Pair<String, String> auth;

    private final ConsoleView consoleView;

    private final Editor editor;


    public ArthasTerminalConsoleViewManager(Pair<String, String> auth, List<AgentInfo> agentInfos, String cmd, TunnelServerInfo tunnelServerInfo, Editor editor, ConsoleView consoleView) {
        this.auth = auth;
        this.agentInfos = agentInfos;
        this.cmd = cmd;
        this.tunnelServerInfo = tunnelServerInfo;
        this.editor = editor;
        this.markupModel = editor.getMarkupModel();
        this.highlighters = new ArrayList<>();
        this.consoleView = consoleView;

        this.webSocketClients = this.createWebSocketClients(auth, agentInfos, cmd, tunnelServerInfo);
    }

    public void onStop() {
        if (this.webSocketClients == null) {
            return;
        }
        this.webSocketClients.forEach(ArthasWebSocketClient::dispose);
        this.webSocketClients = null;
        EventQueue.invokeLater(() -> {
            for (RangeHighlighter highlighter : highlighters) {
                this.markupModel.removeHighlighter(highlighter);
            }
        });
    }

    public void onRerun(String command) {
        this.isStartPrint = false;
        command = Objects.toString(command, this.cmd);
        this.webSocketClients = createWebSocketClients(this.auth, this.agentInfos, command, this.tunnelServerInfo);
    }

    public void print(String s, ConsoleViewContentType viewContentType) {
        if (Objects.nonNull(s)) {
            s = s.replaceAll(" \r", "");
            if (isStartPrint) {
                int start = s.indexOf('#');
                if (start != -1) {
                    String substring = s.substring(start + 1);
                    if (NumberUtils.isDigits(substring)) {
                        int lineNumber = Integer.parseInt(substring) - 1;
                        EventQueue.invokeLater(() -> {
                            try {
                                RangeHighlighter highlighter = markupModel.addLineHighlighter(lineNumber, 10, new TextAttributes());
                                highlighters.add(highlighter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                consoleView.print(s, viewContentType);
            }
        }
    }

    private List<ArthasWebSocketClient> createWebSocketClients(Pair<String, String> auth, List<AgentInfo> agentInfos, String cmd, TunnelServerInfo tunnelServerInfo) {
        return agentInfos.stream()
            .map(agentInfo -> createSocketConnection(cmd, auth, agentInfo.getAgentId(), agentInfo.getClientConnectHost(), tunnelServerInfo))
            .filter(Objects::nonNull)
            .peek(client -> Disposer.register(this, client))
            .toList();
    }

    private ArthasWebSocketClient createSocketConnection(String cmd, Pair<String, String> auth, String agentId, String host, TunnelServerInfo tunnelServerInfo) {

        String uri = TunnelServerPath.getWsUrl(agentId, host, tunnelServerInfo);
        ArthasWebSocketClient client = null;
        try {
            client = new ArthasWebSocketClient(new URI(uri), agentId, this);
            client.connectBlocking();

            Thread.sleep(500);
            // 重置窗口宽度
            client.send(RESIZE_WIDTH);
            isStartPrint = true;
            // If auth exists, send auth command
            if (auth != null) {
                String key = auth.getKey();
                String value = auth.getValue();
                if (StringUtils.isNotBlank(value)) {
                    String authCmd = StringUtils.isNotBlank(key)
                            ? String.format("--username %s %s", key, value)
                            : value;
                    client.send(JSON.toJSONString(new WsArthasRequest("auth " + authCmd + "\r")));
                    Thread.sleep(500);
                }
            }

            // 将命令的结果去除 ANSI 颜色
            String command = cmd + " |plaintext";
            client.send(JSON.toJSONString(new WsArthasRequest(command + "\r")));
            this.print(agentId + LISTENING_STR, ConsoleViewContentType.SYSTEM_OUTPUT);
        } catch (Exception e) {
            NotifyUtils.notifyMessage(editor.getProject(), "connect error:" + agentId);
        }

        return client;
    }

    @Override
    public void dispose() {
        this.onStop();
    }
}

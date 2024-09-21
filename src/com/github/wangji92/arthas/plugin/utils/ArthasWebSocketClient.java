package com.github.wangji92.arthas.plugin.utils;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * websocket客户端监听类
 *
 * @author https://github.com/shuxiongwuziqi
 */
public class ArthasWebSocketClient extends WebSocketClient implements Disposable {

    public static final String RESIZE_WIDTH = "{\"action\":\"resize\",\"cols\":180,\"rows\":30}";
    public static final String WEBSOCKET_CLOSE = " websocket close";
    public static final String WEBSOCKET_OPEN = " websocket open";
    public static final String WEBSOCKET_ERROR = "{} websocket error {}";
    public static final String LISTENING_STR = " listening...\n";
    public static final String ENTER = "\n";

    private final String agentId;

    private final ArthasTerminalConsoleViewManager consoleViewManager;

    public ArthasWebSocketClient(URI serverUri, String agentId, ArthasTerminalConsoleViewManager consoleViewManager) {
        super(serverUri);
        this.agentId = agentId;
        this.consoleViewManager = consoleViewManager;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    }

    @Override
    public void onMessage(String s) {
        this.consoleViewManager.print(s, ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        this.consoleViewManager.print(agentId + WEBSOCKET_CLOSE + ENTER, ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    @Override
    public void onError(Exception e) {
        this.consoleViewManager.print(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
    }


    @Override
    public void dispose() {
        close();
    }
}

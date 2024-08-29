package com.github.wangji92.arthas.plugin.utils;

import com.alibaba.fastjson.JSON;
import com.github.wangji92.arthas.plugin.common.pojo.WsArthasRequest;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * websocket客户端监听类
 *
 * @author https://github.com/shuxiongwuziqi
 */
public class ArthasWebSocketClient extends WebSocketClient implements Disposable {

    private static final String RESIZE_WIDTH = "{\"action\":\"resize\",\"cols\":180,\"rows\":30}";
    public static final String WEBSOCKET_CLOSE = " websocket close";
    public static final String WEBSOCKET_OPEN = " websocket open";
    public static final String WEBSOCKET_ERROR = "{} websocket error {}";
    public static final String LISTENING_STR = " listening...\n";
    public static final String ENTER = "\n";
    private ConsoleView consoleView;

    private final String agentId;

    private boolean isStartPrint;

    private MarkupModel markupModel;

    private List<RangeHighlighter> highlighters;


    public ArthasWebSocketClient(URI serverUri, String agentId, Editor editor, ConsoleView consoleView) {
        super(serverUri);
        this.agentId = agentId;
        this.isStartPrint = false;
        this.markupModel = editor.getMarkupModel();
        this.highlighters = new ArrayList<>();
        this.consoleView = consoleView;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    }

    @Override
    public void onMessage(String s) {
        if (Objects.nonNull(s)) {
            s = s.replaceAll(" \r", "");
            if (isStartPrint) {
                int start = s.indexOf('#');
                if (start != -1) {
                    int lineNumber = Integer.parseInt(s.substring(start + 1)) - 1;
                    EventQueue.invokeLater(() -> {
                        try {
                            RangeHighlighter highlighter = markupModel.addLineHighlighter(lineNumber, 10, new TextAttributes());
                            highlighters.add(highlighter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                consoleView.print(s, ConsoleViewContentType.SYSTEM_OUTPUT);
            }
        }
    }

    /**
     * 只有发送指令之后,才打印tunnel-server返回的信息,过滤掉欢迎语
     */
    public void sendCmd(String cmd) {
        if (!isStartPrint) {
            // 重置窗口宽度
            super.send(RESIZE_WIDTH);
        }
        isStartPrint = true;
        // 将命令的结果去除 ANSI 颜色
        cmd = cmd + " |plaintext";
        super.send(JSON.toJSONString(new WsArthasRequest(cmd + "\r")));
        consoleView.print(agentId + LISTENING_STR, ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        //logger.info(agentId + WEBSOCKET_CLOSE);
        consoleView.print(agentId + WEBSOCKET_CLOSE + ENTER, ConsoleViewContentType.SYSTEM_OUTPUT);
        EventQueue.invokeLater(() -> {
            for (RangeHighlighter highlighter : highlighters) {
                this.markupModel.removeHighlighter(highlighter);
            }
        });
    }

    @Override
    public void onError(Exception e) {
        //如果报错后也有业务写在这里
        //logger.error(WEBSOCKET_ERROR, agentId, e);
        consoleView.print(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
    }


    @Override
    public void dispose() {
        close();
    }
}

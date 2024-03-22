package com.github.wangji92.arthas.plugin.web;

import com.intellij.openapi.Disposable;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class SocketServer extends WebSocketServer implements Disposable {

    public SocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
      }
    
      public SocketServer(InetSocketAddress address) {
        super(address);
      }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); // This method sends a message to the new client
        broadcast("new connection: " + handshake
                .getResourceDescriptor()); // This method sends a message to all clients connected
        System.out.println(
                conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(conn + " has left the room!");
        System.out.println(conn + " has left the room!");

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        broadcast(message);
        System.out.println(conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific
            // websocket
        }

    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

    }

    @Override
    public void dispose() {
        try {
            stop();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
}

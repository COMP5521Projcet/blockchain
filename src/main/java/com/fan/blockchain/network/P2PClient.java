package com.fan.blockchain.network;

import com.fan.blockchain.util.RocksDBUtils;
import com.fan.blockchain.util.SerializeUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PClient {
    //所有客户端WebSocket的连接池
    private List<WebSocket> sockets = new ArrayList<WebSocket>();

    public List<WebSocket> getSockets() {
        return sockets;
    }

    /**
     * 连接到peer
     */
    public void connectPeer(String peer) {
        try {
            /**
             * The WebSocketClient is an abstract class that expects a valid "ws://" URI to connect to.
             * When connected, an instance recieves important events related to the life of the connection.
             * A subclass must implement onOpen, onClose, and onMessage to be useful.
             * An instance can send messages to it's connected server via the send method.
             *
             * 创建有一个WebSocket的客户端
             */
            final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    write(this, "My block height is " + RocksDBUtils.getInstance().getCurrentHeight());
                    sockets.add(this);
                }

                @Override
                public void onMessage(String msg) {
                    System.out.println("Receive msg from server: " + msg);
                    int index = msg.indexOf("is");
                    if (index != -1) {
                        int height = Integer.parseInt(msg.substring(index + 3));
                        if (RocksDBUtils.getInstance().getCurrentHeight() < height){
                            System.out.println("Begin update blockchain......");
                        }
                    }
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    byte[] bytes1 = bytes.array();
                    Map<String,byte[]> blockBucket  = SerializeUtils.deserializer(bytes1, Map.class);
                    RocksDBUtils.getInstance().setBlockBucket(blockBucket);
                    RocksDBUtils.getInstance().updateChain();
                    System.out.println("Update Successfully!");
                    System.out.println("My current height is " + RocksDBUtils.getInstance().getCurrentHeight());
                }

                @Override
                public void onClose(int i, String msg, boolean b) {
                    System.out.println("客户端关闭");
                    sockets.remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("客户端报错");
                    sockets.remove(this);
                }
            };
            //客户端 开始连接服务端
            socketClient.connect();
        } catch (URISyntaxException e) {
            System.out.println("连接错误:" + e.getMessage());
        }
    }
    /**
     * 向服务端发送消息
     * 当前WebSocket的远程Socket地址，就是服务器端
     * @param ws：
     * @param message
     */
    public void write(WebSocket ws, String message) {
        System.out.println("Send msg to" + ws.getRemoteSocketAddress().getPort() + ": " + message);
        ws.send(message);
    }
    /**
     * 向所有服务端广播消息
     * @param message
     */
    public void broatcast(String message) {
        if (sockets.size() == 0) {
            return;
        }
        System.out.println("======广播消息开始：");
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
        System.out.println("======广播消息结束");
    }
}

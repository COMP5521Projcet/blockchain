package com.fan.blockchain.network;

import com.fan.blockchain.util.RocksDBUtils;
import com.fan.blockchain.util.SerializeUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PServer {
    //所有服务端的WebSocket
    private List<WebSocket> sockets = new ArrayList<WebSocket>();

    public List<WebSocket> getSockets() {
        return sockets;
    }

    /**
     * 初始化P2P Server端
     *
     * @param port :Server端的端口号
     */
    public void initServer(int port) {
        /**
         * 创建有一个WebSocket的服务端
         * 定义了一个WebSocketServer的匿名子类对象socketServer
         * new InetSocketAddress(port)是WebSocketServer构造器的参数
         * InetSocketAddress是(IP地址+端口号)类型，也就是端口地址类型
         */
        final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
            /**
             * 重写5个事件方法，事件发生时触发该方法
             */

            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {//创建连接成功时触发
                write(webSocket, "My block height is " + RocksDBUtils.getInstance().getCurrentHeight());
                //当成功创建一个WebSocket连接时，将该链接加入连接池
                sockets.add(webSocket);
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {//断开连接时候触发
                System.out.println(webSocket.getRemoteSocketAddress() + "client disconnect with server！");
                //当客户端断开连接时，WebSocket连接池删除该链接
                sockets.remove(webSocket);
            }

            @Override
            public void onMessage(WebSocket webSocket, String msg) {//收到客户端发来消息的时候触发
                System.out.println("Receive msg from " + webSocket.getRemoteSocketAddress().getPort() + ": " +  msg);
                int index = msg.indexOf("is");
                if (index != -1) {
                    int height = Integer.parseInt(msg.substring(index + 3));
                    if (RocksDBUtils.getInstance().getCurrentHeight() < height){
                        System.out.println("Begin update blockchain......");
                    } else if (RocksDBUtils.getInstance().getCurrentHeight() == height){
                        System.out.println("Blockchains are same!");
                    } else {
                        System.out.println("Send my blockchain to " + webSocket.getRemoteSocketAddress().getPort());
                        sendBlockchain(webSocket,RocksDBUtils.getInstance().getBlockBucket());
                    }
                }
            }


            @Override
            public void onError(WebSocket webSocket, Exception e) {//连接发生错误的时候调用,紧接着触发onClose方法
                System.out.println(webSocket.getRemoteSocketAddress() + "客户端链接错误！");
                sockets.remove(webSocket);
            }

            @Override
            public void onStart() {
                System.out.println("WebSocket Server start...");
            }
        };
        socketServer.start();
        System.out.println("socketServer is listening at: " + port);
    }

    /**
     * 向客户端发送消息
     *
     * @param ws
     * @param message
     */
    public void write(WebSocket ws, String message) {
        System.out.println("send msg to " + ws.getRemoteSocketAddress().getPort() + ": " + message);
        ws.send(message);
    }

    public void sendBlockchain(WebSocket ws, Map<String,byte[]> blockBucket){
        ws.send(SerializeUtils.serializer(blockBucket));
    }

    /**
     * 向所有客户端广播消息
     *
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

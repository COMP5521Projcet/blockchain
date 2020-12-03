package com.fan.blockchain.network;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.util.RocksDBUtils;
import com.fan.blockchain.util.SerializeUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.rocksdb.RocksDB;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PServer {
    //all websockets
    private List<WebSocket> sockets = new ArrayList<WebSocket>();

    public List<WebSocket> getSockets() {
        return sockets;
    }

    /**
     * init P2P Server
     *
     */
    public void initServer(int port) {
        /**
         * create a WebSocket server
         */
        final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

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
                        sendBlockchain(webSocket,RocksDBUtils.getInstance().getBlockByHeight(height+1));
                    }
                }
            }

            /**
             * Receive block byte
             */
            @Override
            public void onMessage(WebSocket conn, ByteBuffer message) {
                byte[] bytes1 = message.array();
                Block block  = SerializeUtils.deserializer(bytes1, Block.class);
                RocksDBUtils.getInstance().putBlock(block);
                //RocksDBUtils.getInstance().getBlockByHeight(height);
//                RocksDBUtils.getInstance().updateChain();
                System.out.println("Update Successfully!");
                System.out.println("My current height is " + RocksDBUtils.getInstance().getCurrentHeight());
                write(conn, "My block height is " + RocksDBUtils.getInstance().getCurrentHeight());
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
        System.out.println("SocketServer is listening at: " + port);
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

    public void sendBlockchain(WebSocket ws, Block block){
        ws.send(SerializeUtils.serializer(block));
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
        System.out.println("======broadcast start：");
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
        System.out.println("======broadcast end");
    }

}

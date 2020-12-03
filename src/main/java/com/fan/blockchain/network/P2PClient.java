package com.fan.blockchain.network;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.pow.ProofOfWork;
import com.fan.blockchain.util.RocksDBUtils;
import com.fan.blockchain.util.SerializeUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.rocksdb.RocksDB;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class P2PClient {
    //client WebSocket pool
    private List<WebSocket> sockets = new ArrayList<WebSocket>();

    public List<WebSocket> getSockets() {
        return sockets;
    }

    /**
     * conncet to peer
     */
    public void connectPeer(String peer) {
        try {
            /**
             * The WebSocketClient is an abstract class that expects a valid "ws://" URI to connect to.
             * When connected, an instance recieves important events related to the life of the connection.
             * A subclass must implement onOpen, onClose, and onMessage to be useful.
             * An instance can send messages to it's connected server via the send method.
             *
             * create a WebSocket client
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
                        } else if (RocksDBUtils.getInstance().getCurrentHeight() == height){
                            System.out.println("Blockchains are same!");
                        } else {
                            System.out.println("Send my blockchain to " + this.getRemoteSocketAddress().getPort());
                            sendBlockchain(this,RocksDBUtils.getInstance().getBlockByHeight(height+1));
                        }
                    }
                }
                @Override
                public void onMessage(ByteBuffer bytes) {
                    byte[] bytes1 = bytes.array();
                    Block block1  = SerializeUtils.deserializer(bytes1, Block.class);
                    RocksDBUtils.getInstance().putBlock(block1);
   //                 Map<String,byte[]> blockBucket  = SerializeUtils.deserializer(bytes1, Map.class);
     //               RocksDBUtils.getInstance().setBlockBucket(blockBucket);
//                    RocksDBUtils.getInstance().updateChain();
                    System.out.println("Update Successfully!");
                    System.out.println("My current height is " + RocksDBUtils.getInstance().getCurrentHeight());
                    write(this, "My block height is " + RocksDBUtils.getInstance().getCurrentHeight());
                    System.out.println("======print my blockchain now======");
                    Blockchain blockchain = null;
                    try {
                        blockchain = Blockchain.initBlockchainFromDB();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext();){
                        Block block = iterator.next();
                        if (block != null){
                            boolean validate = ProofOfWork.newProofOfWork(block).validate();
                            System.out.println(block.toString() + ", validate = " + validate);
                        }
                    }
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
     * send message to server
     * @param ws：
     * @param message
     */
    public void write(WebSocket ws, String message) {
        System.out.println("Send msg to " + ws.getRemoteSocketAddress().getPort() + ": " + message);
        ws.send(message);
    }
    public void sendBlockchain(WebSocket ws, Block block){
        ws.send(SerializeUtils.serializer(block));
    }
    /**
     * broadcast to all server
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

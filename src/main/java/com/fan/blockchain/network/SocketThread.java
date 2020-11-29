package com.fan.blockchain.network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class SocketThread extends Thread{
    private String address;
    private int port;
    public SocketThread(String address,int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(address,port);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            String str = "my height is 16";
            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write("My height is 16");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

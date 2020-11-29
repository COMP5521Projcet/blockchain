package com.fan.blockchain.cli;

import com.fan.blockchain.network.ServerSocketThread;
import com.fan.blockchain.network.SocketThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

/**
 * 启动命令行
 */
public class Main {
    public static void main(String[] args) throws IOException {
        CLI cli = new CLI(args);
        cli.parse();
    }
}

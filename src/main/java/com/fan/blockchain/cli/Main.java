package com.fan.blockchain.cli;

import com.fan.blockchain.network.P2PClient;
import com.fan.blockchain.network.P2PServer;

import java.io.IOException;

/**
 * 启动命令行
 */
public class Main {
    public static void main(String[] args) throws IOException {
        CLI cli = new CLI(args);
        cli.parse();
    }
}

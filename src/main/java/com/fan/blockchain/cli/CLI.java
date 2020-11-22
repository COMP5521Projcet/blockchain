package com.fan.blockchain.cli;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.pow.ProofOfWork;
import com.fan.blockchain.util.RocksDBUtils;
import org.apache.commons.cli.*;
import org.rocksdb.RocksDBException;

/**
 * 程序命令行工具入口
 */
public class CLI {
    private String[] args;
    private Options options = new Options();

    public CLI(String[] args) {
        this.args = args;
        options.addOption("h","help",false,"show help");
        options.addOption("add","addblock",true,"add a block to the blockchain");
        options.addOption("print","printchain",false,"print all the blocks of the blockchain");
    }

    /**
     * 命令行解析入口
     */
    public void parse() {
        this.validateArgs(args);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options,args);
            if (cmd.hasOption("h")){
                help();
            }
            if (cmd.hasOption("add")){
                String data = cmd.getOptionValue("add");
                addBlock(data);
            }
            if (cmd.hasOption("print")){
                printChain();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RocksDBUtils.getInstance().closeDB();
        }
    }

    /**
     * 验证参数
     */
    private void validateArgs(String[] args){
        if (args == null || args.length < 1) {
            help();
        }
    }

    private void help() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Main",options);
        System.exit(0);
    }

    private void addBlock(String data) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain();
        blockchain.addBlock(data);
    }

    private void printChain() {
        Blockchain blockchain = Blockchain.newBlockchain();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockIterator();iterator.hashNext();){
            Block block = iterator.next();
            if (block != null){
                boolean validate = ProofOfWork.newProofOfWork(block).validate();
                System.out.println(block.toString() + ", validate = " + validate);
            }
        }
    }
}

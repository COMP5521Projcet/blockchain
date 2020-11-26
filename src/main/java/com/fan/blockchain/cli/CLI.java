package com.fan.blockchain.cli;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.pow.ProofOfWork;
import com.fan.blockchain.transaction.TXOutput;
import com.fan.blockchain.transaction.Transaction;
import com.fan.blockchain.transaction.UTXOSet;
import com.fan.blockchain.util.Base58Check;
import com.fan.blockchain.util.RocksDBUtils;
import com.fan.blockchain.util.WalletUtils;
import com.fan.blockchain.wallet.Wallet;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * 程序命令行工具入口
 */
public class CLI {
    private String[] args;
    private Options options = new Options();

    public CLI(String[] args) {
        this.args = args;
        Option helpCmd = Option.builder("h").desc("show help").build();
        options.addOption(helpCmd);

        Option address = Option.builder("address").hasArg(true).desc("Source com.fan.blockchain.wallet address").build();
        Option sendFrom = Option.builder("from").hasArg(true).desc("Source com.fan.blockchain.wallet address").build();
        Option sendTo = Option.builder("to").hasArg(true).desc("Destination com.fan.blockchain.wallet address").build();
        Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();

        options.addOption(address);
        options.addOption(sendFrom);
        options.addOption(sendTo);
        options.addOption(sendAmount);
    }

    /**
     * 命令行解析入口
     */
    public void parse() {
        this.validateArgs(args);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options,args);
            switch (args[0]) {
                case "createblockchain":
                    String createblockchainAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(createblockchainAddress)){
                        help();
                    }
                    this.createBlockchain(createblockchainAddress);
                    break;
                case "getbalance":
                    String getBalanceAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(getBalanceAddress)){
                        help();
                    }
                    this.getBalance(getBalanceAddress);
                    break;
                case "send":
                    String sendFrom = cmd.getOptionValue("from");
                    String sendTo = cmd.getOptionValue("to");
                    String sendAmount = cmd.getOptionValue("amount");
                    if (StringUtils.isBlank(sendFrom)
                            || StringUtils.isBlank(sendTo)
                            || StringUtils.isBlank(sendAmount)){
                        help();
                    }
                    this.send(sendFrom,sendTo, Integer.parseInt(sendAmount));
                    break;
                case "createwallet":
                    this.createWallet();
                    break;
                case "printaddresses":
                    this.printAddress();
                    break;
                case "printchain":
                    this.printChain();
                    break;
                case "h":
                    this.help();
                    break;
                default:
                    this.help();
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
        System.out.println("Usage:");
        System.out.println("  createwallet - Generates a new key-pair and saves it into the wallet file");
        System.out.println("  printaddresses - print all wallet address");
        System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
        System.exit(0);
    }

    /**
     * 创建区块链
     * @param address
     */
    private void createBlockchain(String address) {
        Blockchain blockchain = Blockchain.createBlockchain(address);
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.reIndex();
        System.out.println("Done!");
    }

    /**
     * 查询钱包余额
     */
    private void getBalance(String address) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e){
            throw new Exception("ERROR: invalid wallet address");
        }
        Blockchain blockchain = Blockchain.createBlockchain(address);
        // 得到公钥Hash
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        UTXOSet utxoSet = new UTXOSet(blockchain);
        TXOutput[] txOutputs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0){
            for (TXOutput txOutput: txOutputs){
                balance += txOutput.getValue();
            }
        }
        System.out.printf("Balance of '%s': %d\n",address,balance);
    }

    private void send(String from,String to,int amount) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(from);
        } catch (Exception e) {
            throw new Exception("ERROR: sender address is invalid ! address=" + from);
        }
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(to);
        } catch (Exception e) {
            throw new Exception("ERROR: receiver address is invalid ! address=" + to);
        }
        Blockchain blockchain = Blockchain.createBlockchain(from);
        // 新交易
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        // 奖励
        Transaction rewardTx = Transaction.newCoinbaseTX(from,"");
        Block newBlock = blockchain.mineBlock(new Transaction[]{transaction,rewardTx});
        new UTXOSet(blockchain).update(newBlock);
        RocksDBUtils.getInstance().closeDB();
        System.out.println("Success!");
    }

    private void printChain() throws Exception {
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext();){
            Block block = iterator.next();
            if (block != null){
                boolean validate = ProofOfWork.newProofOfWork(block).validate();
                System.out.println(block.toString() + ", validate = " + validate);
            }
        }
    }


    private void createWallet() {
        Wallet wallet = WalletUtils.getInstance().createWallet();
        System.out.println("wallet address : " + wallet.getAddress());
    }


    private void printAddress() throws Exception {
        Set<String> addresses = WalletUtils.getInstance().getAddresses();
        if (addresses == null || addresses.isEmpty()){
            System.out.println("There is no address");
            return;
        }
        for (String address: addresses){
            System.out.println("Wallet address: " + address);
        }
    }
}

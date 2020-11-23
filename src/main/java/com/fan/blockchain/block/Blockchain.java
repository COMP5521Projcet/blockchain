package com.fan.blockchain.block;

import com.fan.blockchain.transaction.SpendableOutputResult;
import com.fan.blockchain.transaction.TXInput;
import com.fan.blockchain.transaction.TXOutput;
import com.fan.blockchain.transaction.Transaction;
import com.fan.blockchain.util.ByteUtils;
import com.fan.blockchain.util.RocksDBUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blockchain {
    @Getter
    private String lastBlockHash;

    /**
     * 从database中恢复区块链数据
     */
    public static Blockchain initBlockchainFromDB() throws Exception {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (lastBlockHash == null){
            throw new Exception("ERROR: Fail to init blockchain from database.");
        }
        return new Blockchain(lastBlockHash);
    }

    public static Blockchain createBlockchain(String address) {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)){
            // 创建coinbase交易
            Transaction coinbaseTX = Transaction.newCoinbaseTX(address,"");
            Block genesisBlock = Block.newGenesisBlock(coinbaseTX);
            lastBlockHash = genesisBlock.getHash();
            RocksDBUtils.getInstance().putBlock(genesisBlock);
            RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);
        }
        return new Blockchain(lastBlockHash);
    }

    /**
     * 打包交易，进行挖矿
     */
    public void mineBlock(Transaction[] transactions) throws Exception {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (lastBlockHash == null) {
            throw new Exception("ERROR: Fail to get last block hash!");
        }
        Block block = Block.newBlock(lastBlockHash,transactions);
        this.addBlock(block);
    }

    public void addBlock(Block block) throws RocksDBException {
        RocksDBUtils.getInstance().putLastBlockHash(block.getHash());
        RocksDBUtils.getInstance().putBlock(block);
        this.lastBlockHash = block.getHash();
    }


    public class BlockchainIterator {
        private String currentBlockHash;

        public BlockchainIterator(String currentBlockHash) {
            this.currentBlockHash = currentBlockHash;
        }

        public boolean hashNext() {
            if (ByteUtils.ZERO_HASH.equals(currentBlockHash)){
                return false;
            }
            Block lastBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
            if (lastBlock == null) {
                return false;
            }
            // 创世区块链直接放行
            if (ByteUtils.ZERO_HASH.equals(lastBlock.getPreviousHash())) {
                return true;
            }
            return RocksDBUtils.getInstance().getBlock(lastBlock.getPreviousHash()) != null;
        }

        public Block next() {
            Block currentBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
            if (currentBlock != null) {
                this.currentBlockHash = currentBlock.getPreviousHash();
                return currentBlock;
            }
            return null;
        }
    }
    public BlockchainIterator getBlockchainIterator() {
        return new BlockchainIterator(lastBlockHash);
    }

    /**
     * 查找钱包地址对应的所有UTXO
     * @param address
     * @return
     */
    public TXOutput[] findUTXO(String address) {
        Transaction[] unspentTxs = this.findUnspentTransactions(address);
        TXOutput[] utxos = {};
        if (unspentTxs == null || unspentTxs.length == 0){
            return utxos;
        }
        for (Transaction tx: unspentTxs){
            for (TXOutput txOutput: tx.getOutputs()){
                if (txOutput.canBeUnlockedWith(address)){
                    utxos = ArrayUtils.add(utxos,txOutput);
                }
            }
        }
        return utxos;
    }

    /**
     * 查找钱包地址对应的所有未花费的交易
     * @param address 钱包地址
     * @return
     */
    public Transaction[] findUnspentTransactions(String address){
        Map<String,int[]> allSpentTXOs = this.getAllSpentTXOs(address);
        Transaction[] unspentTxs = {};
        for (BlockchainIterator iterator = this.getBlockchainIterator();iterator.hashNext();){
            Block block = iterator.next();
            for (Transaction transaction: block.getTransactions()){
                String txId = Hex.encodeHexString(transaction.getTxId());
                int[] spentOutIndexArray = allSpentTXOs.get(txId);
                for (int outIndex = 0;outIndex < transaction.getOutputs().length;outIndex++){
                    if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray,outIndex)){
                        continue;
                    }
                    // 保存不在allSpentTXOs中的交易
                    if (transaction.getOutputs()[outIndex].canBeUnlockedWith(address)){
                        unspentTxs = ArrayUtils.add(unspentTxs,transaction);
                    }
                }
            }
        }
        return unspentTxs;
    }

    /**
     * 从交易输入中查询区块链中所有已被花费了的交易输出
     * @param address 钱包地址
     * @return 交易ID以及对应的交易输出下标地址
     */
    private Map<String,int[]> getAllSpentTXOs(String address){
        // 定义TxId --> spentOutIndex[],存储交易ID与已花费的交易输出数组索引值
        Map<String,int[]> spentTXOs = new HashMap<>();
        for (BlockchainIterator iterator = this.getBlockchainIterator();iterator.hashNext();){
            Block block = iterator.next();
            for (Transaction transaction: block.getTransactions()){
                if (transaction.isCoinbase()){
                    continue;
                }
                for (TXInput txInput: transaction.getInputs()){
                    if (txInput.canUnlockOutputWith(address)){
                        String inTxId = Hex.encodeHexString(txInput.getTxId());
                        int[] spentOutIndexArray = spentTXOs.get(inTxId);
                        if (spentOutIndexArray == null){
                            spentTXOs.put(inTxId,new int[]{txInput.getTxOutputIndex()});
                        } else {
                            spentOutIndexArray = ArrayUtils.add(spentOutIndexArray,txInput.getTxOutputIndex());
                            spentTXOs.put(inTxId,spentOutIndexArray);
                        }
                    }
                }
            }
        }
        return spentTXOs;
    }

    /**
     * 寻找能够花费的交易
     * @param address
     * @param amount
     * @return
     */
    public SpendableOutputResult findSpendableOutputs(String address,int amount) {
        Transaction[] unspentTXs = this.findUnspentTransactions(address);
        int accumulated = 0;
        Map<String,int[]> unspentOuts = new HashMap<>();
        for (Transaction tx: unspentTXs) {
            String txId = Hex.encodeHexString(tx.getTxId());
            for (int outIndex = 0;outIndex < tx.getOutputs().length;outIndex++){
                TXOutput txOutput = tx.getOutputs()[outIndex];
                if (txOutput.canBeUnlockedWith(address) && accumulated < amount){
                    accumulated += txOutput.getValue();
                    // 可以花费的output合集
                    int[] outIds = unspentOuts.get(txId);
                    if (outIds == null){
                        outIds = new int[]{outIndex};
                    } else{
                        outIds = ArrayUtils.add(outIds,outIndex);
                    }
                    unspentOuts.put(txId,outIds);
                    if (accumulated >= amount) {
                        break;
                    }
                }
            }
        }
        return new SpendableOutputResult(accumulated,unspentOuts);
    }
}

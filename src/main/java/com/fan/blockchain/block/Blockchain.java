package com.fan.blockchain.block;

import com.fan.blockchain.transaction.SpendableOutputResult;
import com.fan.blockchain.transaction.TXInput;
import com.fan.blockchain.transaction.TXOutput;
import com.fan.blockchain.transaction.Transaction;
import com.fan.blockchain.util.ByteUtils;
import com.fan.blockchain.util.RocksDBUtils;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.rocksdb.RocksDBException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
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
    public Block mineBlock(Transaction[] transactions) throws Exception {
        for (Transaction tx: transactions){
            if (!this.verifyTransactions(tx)){
                log.error("ERROR: Fail to mine block! There are some invalid transactions!");
                throw new Exception("ERROR: Fail to mine block! There are some invalid transactions!");
            }
        }
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (lastBlockHash == null) {
            throw new Exception("ERROR: Fail to get last block hash!");
        }
        Block block = Block.newBlock(lastBlockHash,transactions);
        this.addBlock(block);
        return block;
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
     * 查找所有的 UTXOs
     * @return
     */
    public Map<String,TXOutput[]> findAllUTXOs(){
        Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs();
        HashMap<String, TXOutput[]> allUTXOs = Maps.newHashMap();
        for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext();){
            Block block = iterator.next();
            for (Transaction transaction : block.getTransactions()) {
                String txId = Hex.encodeHexString(transaction.getTxId());
                int[] spentOutIndexArray = allSpentTXOs.get(txId);
                TXOutput[] outputs = transaction.getOutputs();
                for (int outIndex = 0;outIndex < outputs.length;outIndex++){
                    if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray,outIndex)){
                        continue;
                    }
                    TXOutput[] UTXOArray = allUTXOs.get(txId);
                    if (UTXOArray == null){
                        UTXOArray = new TXOutput[]{outputs[outIndex]};
                    } else {
                        UTXOArray = ArrayUtils.add(UTXOArray,outputs[outIndex]);
                    }
                    allUTXOs.put(txId,UTXOArray);
                }
            }
        }
        return allUTXOs;
    }

    /**
     * 从交易输入中查询区块链中所有已被花费了的交易输出
     * @return 交易ID以及对应的交易输出下标地址
     */
    private Map<String,int[]> getAllSpentTXOs(){
        // 定义TxId --> spentOutIndex[],存储交易ID与已花费的交易输出数组索引值
        Map<String,int[]> spentTXOs = new HashMap<>();
        for (BlockchainIterator iterator = this.getBlockchainIterator();iterator.hashNext();){
            Block block = iterator.next();
            for (Transaction transaction: block.getTransactions()){
                if (transaction.isCoinbase()){
                    continue;
                }
                for (TXInput txInput: transaction.getInputs()){
                    String inTxId = Hex.encodeHexString(txInput.getTxId());
                    int[] spentOutIndexArray = spentTXOs.get(inTxId);
                    if (spentOutIndexArray == null){
                        spentOutIndexArray = new int[]{txInput.getTxOutputIndex()};
                    } else {
                        spentOutIndexArray = ArrayUtils.add(spentOutIndexArray,txInput.getTxOutputIndex());
                    }
                    spentTXOs.put(inTxId,spentOutIndexArray);
                }
            }
        }
        return spentTXOs;
    }

    /**
     * 根据交易ID查询交易信息
     */
    private Transaction findTransaction(byte[] txId) throws Exception {
        for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext();){
            Block block = iterator.next();
            for (Transaction tx: block.getTransactions()){
                if (Arrays.equals(tx.getTxId(),txId)) {
                    return tx;
                }
            }
        }
        throw new Exception("Error: Cannot find tx by this txId");
    }

    /**
     * 进行交易签名
     * @param newTx
     * @param privateKey
     */
    public void signTransaction(Transaction newTx, BCECPrivateKey privateKey) throws Exception {
        // 先找到这笔新的交易中，交易输入所引用的前面的多笔交易的数据
        Map<String,Transaction> prevTxMap = new HashMap<>();
        for (TXInput txInput: newTx.getInputs()){
            Transaction prevTx = this.findTransaction(txInput.getTxId());
            prevTxMap.put(Hex.encodeHexString(txInput.getTxId()),prevTx);
        }
        newTx.sign(privateKey,prevTxMap);
    }
    /**
     * 交易签名验证
     */
    private boolean verifyTransactions(Transaction tx) throws Exception {
        if (tx.isCoinbase()){
            return true;
        }
        Map<String,Transaction> prevTx = new HashMap<>();
        for (TXInput txInput: tx.getInputs()){
            Transaction transaction = this.findTransaction(txInput.getTxId());
            prevTx.put(Hex.encodeHexString(txInput.getTxId()),transaction);
        }
        try {
            return tx.verify(prevTx);
        } catch (Exception e){
            log.error("Fail to verify transaction! Transaction is invalid!", e);
            throw new RuntimeException("Fail to verify transaction! Transaction is invalid!", e);
        }
    }
}

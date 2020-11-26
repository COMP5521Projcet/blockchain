package com.fan.blockchain.transaction;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.util.RocksDBUtils;
import com.fan.blockchain.util.SerializeUtils;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UTXOSet {
    private Blockchain blockchain;

    /**
     * 寻找能够花费的交易
     */
    public SpendableOutputResult findSpendableOutputs(byte[] pubKeyHash,int amount){
        Map<String,int[]> unspentOuts = Maps.newHashMap();
        int accumulated = 0;
        Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance().getChainstateBucket();
        for (Map.Entry<String,byte[]> entry: chainstateBucket.entrySet()){
            String txId = entry.getKey();
            TXOutput[] txOutputs = SerializeUtils.deserializer(entry.getValue(), TXOutput[].class);
            for (int outputIndex = 0;outputIndex < txOutputs.length;outputIndex++){
                TXOutput txOutput = txOutputs[outputIndex];
                if (txOutput.isLockedWithKey(pubKeyHash) && accumulated < amount){
                    accumulated += txOutput.getValue();
                    int[] outIds = unspentOuts.get(txId);
                    if (outIds == null){
                        outIds = new int[]{outputIndex};
                    } else {
                        outIds = ArrayUtils.add(outIds,outputIndex);
                    }
                    unspentOuts.put(txId,outIds);
                    if (accumulated >= amount){
                        break;
                    }
                }
            }
        }
        return new SpendableOutputResult(accumulated,unspentOuts);
    }
    /**
     * 查找钱包地址对应的所有UTXO
     */
    public TXOutput[] findUTXOs(byte[] pubKeyHash){
        TXOutput[] utxos = {};
        Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance().getChainstateBucket();
        if (chainstateBucket.isEmpty()) {
            return utxos;
        }
        for (byte[] value : chainstateBucket.values()) {
            TXOutput[] txOutputs = SerializeUtils.deserializer(value, TXOutput[].class);
            for (TXOutput txOutput : txOutputs) {
                if (txOutput.isLockedWithKey(pubKeyHash)){
                    utxos = ArrayUtils.add(utxos,txOutput);
                }
            }
        }
        return utxos;
    }

    /**
     * 重建UTXO池索引
     */
    @Synchronized
    public void reIndex() {
        log.info("Start to reindex UTXO set");
        RocksDBUtils.getInstance().cleanChianstateBucket();
        Map<String, TXOutput[]> allUTXOs = blockchain.findAllUTXOs();
        for (Map.Entry<String, TXOutput[]> entry : allUTXOs.entrySet()) {
            RocksDBUtils.getInstance().putUTXOs(entry.getKey(), entry.getValue());
        }
        log.info("Reindex UTXO set finished!");
    }
    /**
     * 更新UTXO池:
     * 当一个新的区块产生时，需要做两件事情：
     * 1) 从UTXO池中移除花费掉的交易输出
     * 2) 保存新的未花费交易输出
     */
    @Synchronized
    public void update(Block block){
        if (block == null){
            log.error("Fail to update UTXO set ! The Block is null!");
            throw new RuntimeException("Fail to update UTXO set!");
        }
        for (Transaction transaction : block.getTransactions()) {
            // 根据交易输入排查出剩余未被使用的交易输出
            if (!transaction.isCoinbase()){
                for (TXInput txInput : transaction.getInputs()) {
                    // 余下的未被使用的交易输出
                    TXOutput[] remainedUTXOs = {};
                    String txId = Hex.encodeHexString(txInput.getTxId());
                    // 当前这笔交易中所有的UTXOs
                    TXOutput[] utxos = RocksDBUtils.getInstance().getUTXOs(txId);
                    if (utxos == null) {
                        continue;
                    }
                    for (int outIndex = 0;outIndex < utxos.length;outIndex++){
                        // 如果一个交易中的交易输入中交易输出索引不等于
                        if (outIndex != txInput.getTxOutputIndex()){
                            remainedUTXOs = ArrayUtils.add(remainedUTXOs,utxos[outIndex]);
                        }
                    }
                    // 没有剩余就删除，否则更新
                    if (remainedUTXOs.length == 0){
                        RocksDBUtils.getInstance().deleteUTXOs(txId);
                    } else {
                        RocksDBUtils.getInstance().putUTXOs(txId,remainedUTXOs);
                    }
                }
            }
            TXOutput[] txOutputs = transaction.getOutputs();
            String txId = Hex.encodeHexString(transaction.getTxId());
            RocksDBUtils.getInstance().putUTXOs(txId,txOutputs);
        }
    }
}

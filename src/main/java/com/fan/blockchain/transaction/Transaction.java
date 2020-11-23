package com.fan.blockchain.transaction;

import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.util.SerializeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    // 挖矿奖励数量
    private static final int SUBSIDY = 50;
    // 交易的hash
    private byte[] txId;
    // 交易的输入
    private TXInput[] inputs;
    // 交易的输出
    private TXOutput[] outputs;

    private void setTxId() {
        this.setTxId(DigestUtils.sha256(SerializeUtils.serialize(this)));
    }

    /**
     * 创建coinbase交易
     * @param to 收账的钱包地址
     * @param data 解锁脚本数据
     * @return
     */
    public static Transaction newCoinbaseTX(String to, String data){
        if (StringUtils.isBlank(data)){
            data = String.format("Reward to '%s'",to);
        }
        TXInput txInput = new TXInput(new byte[]{},-1,data);
        TXOutput txOutput = new TXOutput(SUBSIDY,to);
        Transaction tx = new Transaction(null,new TXInput[]{txInput},new TXOutput[]{txOutput});
        tx.setTxId();
        return tx;
    }

    /**
     * 是否为 coinbase
     * @return
     */
    public boolean isCoinbase(){
        return this.getInputs().length == 1
                && this.getInputs()[0].getTxId().length == 0
                && this.getInputs()[0].getTxOutputIndex() == -1;
    }

    /**
     * 从from向to支付一定amount的金额
     * @param from
     * @param to
     * @param amount
     * @param blockchain
     * @return
     * @throws Exception
     */
    public static Transaction newUTXOTransaction(String from, String to, int amount, Blockchain blockchain) throws Exception {
        //找到from可花费的outputs
        SpendableOutputResult result = blockchain.findSpendableOutputs(from,amount);
        int accumulated = result.getAccumulated();
        Map<String,int[]> unspentOuts = result.getUnspentOuts();
        if (accumulated < amount) {
            throw new Exception("ERROR: Not enough funds");
        }
        Iterator<Map.Entry<String,int[]>> iterator = unspentOuts.entrySet().iterator();
        // 得到所有的input
        TXInput[] txInputs = {};
        while (iterator.hasNext()) {
            Map.Entry<String,int[]> entry = iterator.next();
            String txIdStr = entry.getKey();
            int[] outIndexes = entry.getValue();
            byte[] txId = Hex.decodeHex(txIdStr);
            for (int outIndex: outIndexes){
                txInputs = ArrayUtils.add(txInputs,new TXInput(txId,outIndex,from));
            }
        }
        TXOutput[] txOutputs = {};
        // 一个output是给to的
        txOutputs = ArrayUtils.add(txOutputs,new TXOutput(amount,to));
        if (accumulated > amount) {
            // 一个output是给from的
            txOutputs = ArrayUtils.add(txOutputs,new TXOutput((accumulated - amount),from));
        }
        Transaction newTx = new Transaction(null,txInputs,txOutputs);
        newTx.setTxId();
        return newTx;
    }
}

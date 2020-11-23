package com.fan.blockchain.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXInput {
    // 交易ID的hash值
    private byte[] txId;
    // 交易输出的索引
    private int txOutputIndex;
    // 解锁脚本
    private String scriptSig;

    /**
     * 判断解锁数据是否能解锁交易输出
     * @param unlockingData
     * @return
     */
    public boolean canUnlockOutputWith(String unlockingData) {
        return this.getScriptSig().endsWith(unlockingData);
    }
}

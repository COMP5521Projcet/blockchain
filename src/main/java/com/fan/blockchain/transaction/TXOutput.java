package com.fan.blockchain.transaction;

import com.fan.blockchain.util.Base58Check;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXOutput implements Serializable {
    private int value;
    // 公钥Hash
    private byte[] pubKeyHash;

    /**
     * 创建交易输出
     * @param value
     * @param address
     * @return
     */
    public static TXOutput newTxOutput(int value, String address){
        // 反向转化为byte数组
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        // 去掉1byte的版本号
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        return new TXOutput(value,pubKeyHash);
    }

    /**
     * 检查交易输出是否能够使用指定的公钥
     * @param pubKeyHash
     * @return
     */
    public boolean isLockedWithKey(byte[] pubKeyHash){
        return Arrays.equals(this.getPubKeyHash(),pubKeyHash);
    }
}

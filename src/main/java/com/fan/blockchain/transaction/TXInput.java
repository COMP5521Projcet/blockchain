package com.fan.blockchain.transaction;

import com.fan.blockchain.util.BtcAddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXInput implements Serializable {
    // 交易ID的hash值
    private byte[] txId;
    // 交易输出的索引
    private int txOutputIndex;
    // 签名
    private byte[] signature;
    // 公钥(未经过hash的公钥)
    private byte[] pubKey;

    public boolean useKey(byte[] pubKeyHash){
        byte[] lockingHash = BtcAddressUtils.ripeMD160Hash(this.getPubKey());
        return Arrays.equals(lockingHash,pubKeyHash);
    }
}

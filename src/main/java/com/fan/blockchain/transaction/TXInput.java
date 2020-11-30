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
    // transaction's ID
    private byte[] txId;
    // index of outputs
    private int txOutputIndex;
    // signature
    private byte[] signature;
    // public key
    private byte[] pubKey;

    public boolean useKey(byte[] pubKeyHash){
        byte[] lockingHash = BtcAddressUtils.ripeMD160Hash(this.getPubKey());
        return Arrays.equals(lockingHash,pubKeyHash);
    }
}

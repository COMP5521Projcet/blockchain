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
    // value of this transaction's output
    private int value;
    // hash of public key
    private byte[] pubKeyHash;

    /**
     * create output
     * @param value
     * @param address
     * @return
     */
    public static TXOutput newTxOutput(int value, String address){
        // convert to byte array
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        // remove version number(1byte)
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        return new TXOutput(value,pubKeyHash);
    }

    /**
     * check the hash of public key whether can unlock this output
     * @param pubKeyHash
     * @return
     */
    public boolean isLockedWithKey(byte[] pubKeyHash){
        return Arrays.equals(this.getPubKeyHash(),pubKeyHash);
    }
}

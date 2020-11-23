package com.fan.blockchain.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXOutput {
    private int value;
    private String scriptPubKey;

    public boolean canBeUnlockedWith(String unlockingData) {
        return this.getScriptPubKey().endsWith(unlockingData);
    }
}

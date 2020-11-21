package com.fan.blockchain.pow;

import lombok.Data;

@Data
public class PowResult {
    private long nonce;
    private String hash;

    public PowResult(long nonce, String hash) {
        this.nonce = nonce;
        this.hash = hash;
    }
}

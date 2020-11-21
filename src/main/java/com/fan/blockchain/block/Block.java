package com.fan.blockchain.block;

import com.fan.blockchain.pow.PowResult;
import com.fan.blockchain.pow.ProofOfWork;
import com.fan.blockchain.util.ByteUtils;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.time.Instant;

@Data
public class Block {
    private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);
    private String hash;
    private String previousHash;
    private String data;
    private long timeStamp;
    private long nonce;

    public Block(){
    }

    public Block(String hash, String previousHash, String data, long timeStamp) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = timeStamp;
    }

    public static Block newGenesisBlock() {
        return Block.newBlock(ZERO_HASH,"Genesis Block");
    }

    public static Block newBlock(String previousHash, String data) {
        Block block = new Block("",previousHash,data, Instant.now().getEpochSecond());
        ProofOfWork pow = ProofOfWork.newProofOfWork(block);
        PowResult powResult = pow.run();
        block.setHash(powResult.getHash());
        block.setNonce(powResult.getNonce());
        return block;
    }
}

package com.fan.blockchain.block;

import com.fan.blockchain.pow.PowResult;
import com.fan.blockchain.pow.ProofOfWork;
import com.fan.blockchain.transaction.MerkleTree;
import com.fan.blockchain.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block implements Serializable {
    private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);
    private String hash;
    private String previousHash;
    private Transaction[] transactions;
    private long timeStamp;
    private long nonce;
    private int height;

    public static Block newGenesisBlock(Transaction coinbase) {
        return Block.newBlock(ZERO_HASH,new Transaction[]{coinbase},0);
    }

    public static Block newBlock(String previousHash, Transaction[] transactions,int height) {
        Block block = new Block("",previousHash,transactions, Instant.now().getEpochSecond(),0,height);
        ProofOfWork pow = ProofOfWork.newProofOfWork(block);
        PowResult powResult = pow.run();
        block.setHash(powResult.getHash());
        block.setNonce(powResult.getNonce());
        return block;
    }

    /**
     * 对区块中的交易信息进行hash计算
     */
    public byte[] hashTransaction() {
        byte[][] txIdArrays = new byte[this.getTransactions().length][];
        for (int i = 0;i < this.getTransactions().length;i++) {
            txIdArrays[i] = this.getTransactions()[i].getTxId();
        }
        return new MerkleTree(txIdArrays).getRoot().getHash();
    }

}

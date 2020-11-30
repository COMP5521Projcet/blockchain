package com.fan.blockchain.pow;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.util.ByteUtils;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

@Data
public class ProofOfWork {
    // difficulty target bits
    public static int target_bits = 20 ;
    private Block block;
    // difficulty target value
    private BigInteger target;

    private ProofOfWork(Block block, BigInteger target) {
        this.block = block;
        this.target = target;
    }

    public static ProofOfWork newProofOfWork(Block block) {
        // change difficulty dynamic: per ten blocks
        target_bits = target_bits + (block.getHeight() / 10);
        // left move 1 by (256 - target_bits) bits to get target value
        BigInteger targetValue = BigInteger.ONE.shiftLeft(256 - target_bits);
        return new ProofOfWork(block,targetValue);
    }

    public PowResult run() {
        long nonce = 0;
        String shaHex = "";
        long startTime = System.currentTimeMillis();
        while (nonce < Long.MAX_VALUE){
            byte[] data = this.prepareData(nonce);
            shaHex = DigestUtils.sha256Hex(data);
            if (new BigInteger(shaHex,16).compareTo(this.target) == -1) {
                System.out.printf("Elapsed Time: %s seconds \n",(float)(System.currentTimeMillis()-startTime) / 1000);
                System.out.printf("Correct hash Hex: %s \n\n",shaHex);
                break;
            } else {
                nonce++;
            }
        }
        return new PowResult(nonce,shaHex);
    }

    public boolean validate() {
        byte[] data = this.prepareData(this.getBlock().getNonce());
        return new BigInteger(DigestUtils.sha256Hex(data),16).compareTo(this.target) == -1;
    }

    /**
     * prepare all the data to calculate hash
     * @param nonce
     * @return
     */
    private byte[] prepareData(long nonce){
        byte[] preBlockHashBytes = {};
        if (StringUtils.isNoneBlank(this.getBlock().getPreviousHash())) {
            preBlockHashBytes = new BigInteger(this.getBlock().getPreviousHash(),16).toByteArray();
        }
        return ByteUtils.merge(
                preBlockHashBytes,
                this.getBlock().hashTransaction(),
                ByteUtils.toBytes(this.getBlock().getTimeStamp()),
                ByteUtils.toBytes(target_bits),
                ByteUtils.toBytes(nonce)
        );
    }
}

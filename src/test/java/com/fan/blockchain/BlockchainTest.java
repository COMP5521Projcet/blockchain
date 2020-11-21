package com.fan.blockchain;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.pow.ProofOfWork;
import com.fan.blockchain.util.RocksDBUtils;
import org.rocksdb.RocksDBException;

public class BlockchainTest {
    public static void main(String[] args) {
        Blockchain blockchain = null;
        try {
            blockchain = Blockchain.newBlockchain();
            blockchain.addBlock("Send 1 BTC to Fan");
            blockchain.addBlock("Send 2 BTC to Owen");
            blockchain.addBlock("Send 3 BTC to Leon");
            for (Blockchain.BlockchainIterator iterator = blockchain.getBlockIterator();iterator.hashNext();){
                Block block = iterator.next();
                if (block != null){
                    boolean validate = ProofOfWork.newProofOfWork(block).validate();
                    System.out.println(block.toString() + ", validate = " + validate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

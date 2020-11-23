package com.fan.blockchain;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.cli.CLI;
import com.fan.blockchain.util.RocksDBUtils;
import org.junit.Test;

import java.util.Map;

public class BlockchainTest {
    private static RocksDBUtils dbUtils = RocksDBUtils.getInstance();
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    private Map<String,byte[]> blockBucket;

    @Test
    public void testStore() {
        Block lastBlock = dbUtils.getBlock(dbUtils.getLastBlockHash());
        while (lastBlock != null){
            System.out.println(lastBlock.toString());
            lastBlock = dbUtils.getBlock(lastBlock.getPreviousHash());
        }
    }
    @Test
    public void testTransaction(){
        try {
//            String[] args = {"getbalance","-address","Owen"};
            String[] args = {"send","-from","Fan","-to","Owen","-amount","1"};
            CLI cli = new CLI(args);
            cli.parse();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

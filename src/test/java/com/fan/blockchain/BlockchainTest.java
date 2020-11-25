package com.fan.blockchain;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.cli.CLI;
import com.fan.blockchain.util.FstUtils;
import com.fan.blockchain.util.RocksDBUtils;
import org.junit.Test;

import java.util.Map;

public class BlockchainTest {
    private static RocksDBUtils dbUtils = RocksDBUtils.getInstance();
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    private Map<String,byte[]> blockBucket;
//
//    @Test
//    public void testStore() {
//        Block lastBlock = dbUtils.getBlock(dbUtils.getLastBlockHash());
//        while (lastBlock != null){
//            System.out.println(lastBlock.toString());
//            lastBlock = dbUtils.getBlock(lastBlock.getPreviousHash());
//        }
//    }
//    @Test
//    public void testTransaction(){
//        try {
////            String[] args = {"getbalance","-address","Owen"};
//            String[] args = {"send","-from","Fan","-to","Owen","-amount","1"};
//            CLI cli = new CLI(args);
//            cli.parse();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    @Test
    public void testWallet(){
        try {
//            String[] args = {"createblockchain","-address","1L4bxX39DykbPvrAfdM5f1o6q8GCbVh7gX"};
//            String[] args = {"getbalance","-address","1L4bxX39DykbPvrAfdM5f1o6q8GCbVh7gX"};
            String[] args = {"printaddresses"};
//            String[] args = {"createwallet"};

            CLI cli = new CLI(args);
            cli.parse();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testFst(){
        String str = "abc";
        byte[] bytes = FstUtils.serializer(str);
        System.out.println(bytes);
        String plainTxt = FstUtils.deserializer(bytes, String.class);
        System.out.println(plainTxt);
    }
}

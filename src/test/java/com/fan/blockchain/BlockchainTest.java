package com.fan.blockchain;

import com.fan.blockchain.cli.CLI;
import com.fan.blockchain.util.SerializeUtils;
import com.fan.blockchain.util.RocksDBUtils;
import org.junit.Test;

import java.util.Map;

public class BlockchainTest {
    private static RocksDBUtils dbUtils = RocksDBUtils.getInstance();
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    private Map<String,byte[]> blockBucket;
    @Test
    public void testWallet(){
        try {
//            String[] args = {"createblockchain","-address","1Q12VkmpC3RWvzEcSpbgJ46aD8b5eCC9ja"};
            String[] args = {"getbalance","-address","1MJTJ4A5XkFRgTyTkXg2fync2mqymyZbae"};
//            String[] args = {"printaddresses"};
//            String[] args = {"createwallet"};
//            String[] args = {"send","-from","1Q12VkmpC3RWvzEcSpbgJ46aD8b5eCC9ja","-to","1MJTJ4A5XkFRgTyTkXg2fync2mqymyZbae","-amount","6"};

            CLI cli = new CLI(args);
            cli.parse();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testFst(){
        String str = "abc";
        byte[] bytes = SerializeUtils.serializer(str);
        System.out.println(bytes);
        String plainTxt = SerializeUtils.deserializer(bytes, String.class);
        System.out.println(plainTxt);
    }
}

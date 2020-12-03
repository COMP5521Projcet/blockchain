package com.fan.blockchain.util;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.transaction.TXOutput;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import com.google.common.collect.Maps;

import java.util.Map;


/**
 * RocksDB tool class
 */
public class RocksDBUtils {
    // blockchain database file
    private static final String DB_FILE = "blockchain.db";
    // block bucket prefix
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    // chain state bucket prefix
    private static final String CHAINSTATE_BUCKET_KEY = "chainstate";
    // latest block key
    private static final String LAST_BLOCK_KEY = "l";
    private volatile static RocksDBUtils instance;
    @Getter
    private RocksDB db;
    @Getter
    @Setter
    private Map<String,byte[]> blockBucket;
    @Getter
    private Map<String,byte[]> chainstateBucket;

    public static RocksDBUtils getInstance() {
        if (instance == null) {
            synchronized (RocksDBUtils.class){
                if (instance == null) {
                    instance = new RocksDBUtils();
                }
            }
        }
        return instance;
    }

    public RocksDBUtils() {
        openDB();
        initRocksDB();
        initChainStateBucket();
    }

    /**
     * open database
     */
    private void openDB() {
        try {
            db = RocksDB.open(new Options().setCreateIfMissing(true),DB_FILE);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize blocks bucket: store block data
     */
    private void initRocksDB() {
        try {
            byte[] blockBucketKey = SerializeUtils.serializer(BLOCKS_BUCKET_KEY);
            byte[] blockBucketBytes = db.get(blockBucketKey);
            if (blockBucketBytes != null) {
                blockBucket =  SerializeUtils.deserializer(blockBucketBytes,Map.class);
            } else {
                blockBucket = Maps.newHashMap();
                db.put(blockBucketKey, SerializeUtils.serializer(blockBucket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * initialize chainstate bucket: store transaction data
     */
    private void initChainStateBucket() {
        try {
            byte[] chainstateBucketKey = SerializeUtils.serializer(CHAINSTATE_BUCKET_KEY);
            byte[] chainstateBucketBytes = db.get(chainstateBucketKey);
            if (chainstateBucketBytes != null){
                chainstateBucket = SerializeUtils.deserializer(chainstateBucketBytes,Map.class);
            } else {
                chainstateBucket = Maps.newHashMap();
                db.put(chainstateBucketKey,SerializeUtils.serializer(chainstateBucket));
            }
        } catch (RocksDBException e){
            throw new RuntimeException("Fail to init chainstate bucket!");
        }
    }

    /**
     * get latest block
     */
    public Block getLastBlock() {
        String lastBlockHash = getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)){
            throw new RuntimeException("ERROR: There is no block now!");
        }
        Block lastBlock = getBlock(lastBlockHash);
        if (lastBlock == null){
            throw new RuntimeException("ERROR: Fail to get last block !");
        }
        return lastBlock;
    }
    /**
     * get the height of latest block
     */
    public int getCurrentHeight(){
        String lastBlockHash = getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)){
            return -1;
        }
        return getBlock(lastBlockHash).getHeight();
    }
    /**
     * save the hash of latest block
     */
    public void putLastBlockHash(String tipBlockHash) {
        try {
            blockBucket.put(LAST_BLOCK_KEY, SerializeUtils.serializer(tipBlockHash));
            db.put(SerializeUtils.serializer(BLOCKS_BUCKET_KEY), SerializeUtils.serializer(blockBucket));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * get the hash of the latest block
     */
    public String getLastBlockHash() {
        byte[] lastBlockHashBytes = blockBucket.get(LAST_BLOCK_KEY);
        if (lastBlockHashBytes != null) {
            return  SerializeUtils.deserializer(lastBlockHashBytes,String.class);
        }
        return "";
    }

    /**
     * save block
     */
    public void putBlock(Block block)  {
        try {
            blockBucket.put(block.getHash(), SerializeUtils.serializer(block));
            //db.put(SerializeUtils.serializer(BLOCKS_BUCKET_KEY), SerializeUtils.serializer(blockBucket));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * update blockchain
     */

    public void updateChain()  {
        try {
            db.put(SerializeUtils.serializer(BLOCKS_BUCKET_KEY), SerializeUtils.serializer(blockBucket));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * get block information by hash
     */
    public Block getBlock(String blockHash) {
        byte[] blockBytes = blockBucket.get(blockHash);
        if (blockBytes != null) {
            return SerializeUtils.deserializer(blockBytes,Block.class);
        }
        return null;
    }
    public Block getBlockByHeight(int height){
        Block block = this.getLastBlock();
        if(block.getHeight() < height){
            return null;
        }
        while (block != null) {
            if (block.getHeight() == height) {
                    return block;
                }else{
                    block = this.getBlock(block.getPreviousHash());
                }

        }
        return null;
    }
    /**
     * clear chainstate bucket
     */
    public void cleanChianstateBucket(){
        try {
            chainstateBucket.clear();
        } catch (Exception e){
            throw new RuntimeException("Fail to clear chainstate bucket!", e);
        }
    }
    /**
     * save UTXO
     */
    public void putUTXOs(String key, TXOutput[] utxos){
        try {
            chainstateBucket.put(key,SerializeUtils.serializer(utxos));
            db.put(SerializeUtils.serializer(CHAINSTATE_BUCKET_KEY),SerializeUtils.serializer(chainstateBucket));
        }catch (Exception e){
            throw new RuntimeException("Fail to put UTXOS into chainstate bucket! key = " + key,e);
        }
    }

    /**
     *query UTXO
     */
    public TXOutput[] getUTXOs(String key) {
        byte[] utxosByte = chainstateBucket.get(key);
        if (utxosByte != null){
            return SerializeUtils.deserializer(utxosByte,TXOutput[].class);
        }
        return null;
    }
    /**
     * delete UTXO
     */
    public void deleteUTXOs(String key) {
        try {
            chainstateBucket.remove(key);
            db.put(SerializeUtils.serializer(CHAINSTATE_BUCKET_KEY),SerializeUtils.serializer(chainstateBucket));
        } catch (Exception e){
            throw new RuntimeException("Fail to delete UTXOs by key! key = " + key,e);
        }
    }

    /**
     * close database
     */
    public void closeDB() {
        try {
            db.close();
        } catch (Exception e) {
            throw new RuntimeException("Fail to close db!",e);
        }
    }
    /**
     * check
     */
    public void updateBlockBucket(){
        try {
            if (instance.getBlockBucket() != SerializeUtils.deserializer(instance.getDb().get(SerializeUtils.serializer(BLOCKS_BUCKET_KEY)),Map.class)){
                try {
                    instance.getDb().put(SerializeUtils.serializer(BLOCKS_BUCKET_KEY),SerializeUtils.serializer(instance.getBlockBucket()));
                } catch (RocksDBException e) {
                    e.printStackTrace();
                }
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

}

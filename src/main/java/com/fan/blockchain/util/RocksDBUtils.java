package com.fan.blockchain.util;

import com.fan.blockchain.block.Block;
import com.fan.blockchain.transaction.TXOutput;
import lombok.Getter;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import com.google.common.collect.Maps;
import sun.rmi.runtime.Log;

import java.util.Map;


/**
 * RocksDB 工具类
 */
public class RocksDBUtils {
    // 区块链数据文件
    private static final String DB_FILE = "blockchain.db";
    // 区块桶前缀
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    // 链状态桶
    private static final String CHAINSTATE_BUCKET_KEY = "chainstate";
    // 最新一个区块
    private static final String LAST_BLOCK_KEY = "l";
    private volatile static RocksDBUtils instance;
    @Getter
    private RocksDB db;
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
     * 打开数据库
     */
    private void openDB() {
        try {
            db = RocksDB.open(new Options().setCreateIfMissing(true),DB_FILE);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化 blocks 数据桶
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
     * 初始化 chainstate 数据桶
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
     * 保存最新一个区块的hash
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
     * 查询最新一个区块的hash
     */
    public String getLastBlockHash() {
        byte[] lastBlockHashBytes = blockBucket.get(LAST_BLOCK_KEY);
        if (lastBlockHashBytes != null) {
            return  SerializeUtils.deserializer(lastBlockHashBytes,String.class);
        }
        return "";
    }

    /**
     * 保存区块
     */
    public void putBlock(Block block)  {
        try {
            blockBucket.put(block.getHash(), SerializeUtils.serializer(block));
            db.put(SerializeUtils.serializer(BLOCKS_BUCKET_KEY), SerializeUtils.serializer(blockBucket));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询区块
     */
    public Block getBlock(String blockHash) {
        byte[] blockBytes = blockBucket.get(blockHash);
        if (blockBytes != null) {
            return SerializeUtils.deserializer(blockBytes,Block.class);
        }
        return null;
    }
    /**
     * 清空chainstate bucket
     */
    public void cleanChianstateBucket(){
        try {
            chainstateBucket.clear();
        } catch (Exception e){
            throw new RuntimeException("Fail to clear chainstate bucket!", e);
        }
    }
    /**
     * 保存UTXO数据
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
     *查询UTXO数据
     */
    public TXOutput[] getUTXOs(String key) {
        byte[] utxosByte = chainstateBucket.get(key);
        if (utxosByte != null){
            return SerializeUtils.deserializer(utxosByte,TXOutput[].class);
        }
        return null;
    }
    /**
     * 删除UTXO数据
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
     * 关闭数据库
     */
    public void closeDB() {
        try {
            db.close();
        } catch (Exception e) {
            throw new RuntimeException("Fail to close db!",e);
        }
    }

}

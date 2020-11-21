package com.fan.blockchain.util;

import com.fan.blockchain.block.Block;
import lombok.Getter;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * RocksDB 工具类
 */
public class RocksDBUtils {
    // 区块链数据文件
    private static final String DB_FILE = "blockchain.db";
    // 区块桶前缀
    private static final String BLOCKS_BUCKET_KEY = "blocks";
    // 最新一个区块
    private static final String LAST_BLOCK_KEY = "l";
    private volatile static RocksDBUtils instance;
    @Getter
    private RocksDB db;
    private Map<String,byte[]> blockBucket;

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
            byte[] blockBucketKey = SerializeUtils.serialize(BLOCKS_BUCKET_KEY);
            byte[] blockBucketBytes = db.get(blockBucketKey);
            if (blockBucketBytes != null) {
                blockBucket = (Map)SerializeUtils.deserialize(blockBucketBytes);
            } else {
                blockBucket = Maps.newHashMap();
                db.put(blockBucketKey,SerializeUtils.serialize(blockBucket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存最新一个区块的hash
     */
    public void putLastBlockHash(String tipBlockHash) {
        try {
            blockBucket.put(LAST_BLOCK_KEY,SerializeUtils.serialize(tipBlockHash));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY),SerializeUtils.serialize(blockBucket));
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
            return (String) SerializeUtils.deserialize(lastBlockHashBytes);
        }
        return "";
    }

    /**
     * 保存区块
     */
    public void putBlock(Block block)  {
        try {
            blockBucket.put(block.getHash(),SerializeUtils.serialize(block));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY),SerializeUtils.serialize(blockBucket));
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
            return (Block) SerializeUtils.deserialize(blockBytes);
        }
        return null;
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

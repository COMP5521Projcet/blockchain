package com.fan.blockchain.util;

import com.fan.blockchain.block.Block;
import lombok.Getter;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * RocksDB 工具类
 */
public class RocksDBUtils {
    // 区块链数据文件
    private static final String DB_FILE = "blockchain.db";
    // 区块桶前缀
    private static final String BLOCKS_BUCKET_PREFIX = "blocks_";
    private volatile static RocksDBUtils instance;

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

    @Getter
    private RocksDB rocksDB;

    public RocksDBUtils() {
        initRocksDB();
    }

    /**
     * 初始化RocksDB
     */
    private void initRocksDB() {
        try {
            rocksDB = RocksDB.open(new Options().setCreateIfMissing(true),DB_FILE);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存最新一个区块的hash
     */
    public void putLastBlockHash(String tipBlockHash) throws RocksDBException {
        rocksDB.put(SerializeUtils.serialize(BLOCKS_BUCKET_PREFIX + "l"),SerializeUtils.serialize(tipBlockHash));
    }

    /**
     * 查询最新一个区块的hash
     */
    public String getLastBlockHash() throws RocksDBException {
        byte[] lastBlockHashBytes = rocksDB.get(SerializeUtils.serialize(BLOCKS_BUCKET_PREFIX+"l"));
        if (lastBlockHashBytes != null) {
            return (String) SerializeUtils.deserialize(lastBlockHashBytes);
        }
        return "";
    }

    /**
     * 保存区块
     */
    public void putBlock(Block block) throws RocksDBException {
        byte[] key = SerializeUtils.serialize(BLOCKS_BUCKET_PREFIX + block.getHash());
        rocksDB.put(key,SerializeUtils.serialize(block));
    }

    /**
     * 查询区块
     */
    public Block getBlock(String blockHash) throws RocksDBException {
        byte[] key = SerializeUtils.serialize(BLOCKS_BUCKET_PREFIX + blockHash);
        return (Block) SerializeUtils.deserialize(rocksDB.get(key));
    }

}

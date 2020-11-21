package com.fan.blockchain.block;

import com.fan.blockchain.util.ByteUtils;
import com.fan.blockchain.util.RocksDBUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDBException;

import java.util.LinkedList;
import java.util.List;

public class Blockchain {
    @Getter
    private String lastBlockHash;

    public Blockchain(String lastBlockHash) {
        this.lastBlockHash = lastBlockHash;
    }

    public static Blockchain newBlockchain() throws RocksDBException {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)){
            Block genesisBlock = Block.newGenesisBlock();
            lastBlockHash = genesisBlock.getHash();
            RocksDBUtils.getInstance().putBlock(genesisBlock);
            RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);
        }
        return new Blockchain(lastBlockHash);
    }

    public void addBlock(String data) throws Exception {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)){
            throw new Exception("Fail to add block into blockchain!");
        }
        this.addBlock(Block.newBlock(lastBlockHash,data));
    }

    public void addBlock(Block block) throws RocksDBException {
        RocksDBUtils.getInstance().putLastBlockHash(block.getHash());
        RocksDBUtils.getInstance().putBlock(block);
        this.lastBlockHash = block.getHash();
    }


    public class BlockchainIterator {
        private String currentBlockHash;

        public BlockchainIterator(String currentBlockHash) {
            this.currentBlockHash = currentBlockHash;
        }

        public boolean hashNext() throws RocksDBException {
            if (ByteUtils.ZERO_HASH.equals(currentBlockHash)){
                return false;
            }
            Block lastBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
            if (lastBlock == null) {
                return false;
            }
            // 创世区块链直接放行
            if (ByteUtils.ZERO_HASH.equals(lastBlock.getPreviousHash())) {
                return true;
            }
            return RocksDBUtils.getInstance().getBlock(lastBlock.getPreviousHash()) != null;
        }

        public Block next() throws RocksDBException {
            Block currentBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
            if (currentBlock != null) {
                this.currentBlockHash = currentBlock.getPreviousHash();
                return currentBlock;
            }
            return null;
        }
    }
    public BlockchainIterator getBlockIterator() {
        return new BlockchainIterator(lastBlockHash);
    }
}

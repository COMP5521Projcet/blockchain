package com.fan.blockchain.util;

import com.fan.blockchain.wallet.Wallet;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Map;
import java.util.Set;

/**
 * 钱包工具类
 */
public class WalletUtils {
    private volatile static WalletUtils instance;
    // 钱包文件
    private final static String WALLET_FILE = "wallet.dat";
    // 加密算法
    private final static String ALGORITHM = "AES";
    // 密文
    private final static byte[] CIPHER_TEXT = "2oF@5sC%DNf32y!TmiZi!tG9W5rLaniD".getBytes();
    /**
     * 钱包工具实例
     * @return
     */
    public static WalletUtils getInstance() {
        if (instance == null){
            synchronized (WalletUtils.class){
                if (instance == null){
                    instance = new WalletUtils();
                }
            }
        }
        return instance;
    }

    private WalletUtils(){
        initWalletFile();
    }

    /**
     * 初始化钱包文件
     */
    private void initWalletFile() {
        File file = new File(WALLET_FILE);
        if (!file.exists()){
            this.saveToDisk(new Wallets());
        } else{
            this.loadFromDisk();
        }
    }
    /**
     * 获取所有的钱包地址
     */
    public Set<String> getAddresses() throws Exception {
        Wallets wallets = this.loadFromDisk();
        return wallets.getAddresses();
    }
    /**
     * 获取钱包数据
     */
    public Wallet getWallet(String address) throws Exception {
        Wallets wallets = this.loadFromDisk();
        return wallets.getWallet(address);
    }

    public Wallet createWallet(){
        Wallet wallet = new Wallet();
        Wallets wallets = this.loadFromDisk();
        wallets.addWallet(wallet);
        this.saveToDisk(wallets);
        return wallet;
    }

    /**
     * 保存钱包数据
     */
    private void saveToDisk(Wallets wallets){
        try {
            if (wallets == null) {
                throw new Exception("Error: Fail to save wallet to file! data is null");
            }
            SecretKeySpec sks = new SecretKeySpec(CIPHER_TEXT, ALGORITHM);
            // create cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,sks);
            SealedObject sealedObject = new SealedObject(wallets,cipher);
            // warp the output stream
            @Cleanup CipherOutputStream cipherOutputStream = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(WALLET_FILE)), cipher);
            @Cleanup ObjectOutputStream outputStream = new ObjectOutputStream(cipherOutputStream);
            outputStream.writeObject(sealedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载钱包数据
     */
    private Wallets loadFromDisk() {
        try {
            SecretKeySpec sks = new SecretKeySpec(CIPHER_TEXT, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,sks);
            @Cleanup CipherInputStream cipherInputStream = new CipherInputStream(new BufferedInputStream(new FileInputStream(WALLET_FILE)), cipher);
            @Cleanup ObjectInputStream objectInputStream = new ObjectInputStream(cipherInputStream);
            SealedObject sealedObject = (SealedObject) objectInputStream.readObject();
            return (Wallets) sealedObject.getObject(cipher);
        } catch (Exception e){
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to load wallet file from disk!");
    }

    /**
     * 钱包存储对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wallets implements Serializable {
        private Map<String,Wallet> walletMap = Maps.newHashMap();
        /**
         * 添加钱包
         */
        private void addWallet(Wallet wallet) {
            this.walletMap.put(wallet.getAddress(),wallet);
        }
        /**
         * 获取所有的钱包地址
         */
        Set<String> getAddresses() throws Exception {
            if (walletMap == null){
                throw new Exception("ERROR: Fail to get address! There is no address");
            }
            return walletMap.keySet();
        }
        /**
         * 获取钱包数据
         */
        Wallet getWallet(String address) throws Exception {
            try {
                Base58Check.base58ToBytes(address);
            } catch (Exception e) {
                throw new Exception("Error: invalid wallet address!");
            }
            Wallet wallet = walletMap.get(address);
            if (wallet == null){
                throw new Exception("Error: Fail to get wallet! The wallet is not existed");
            }
            return wallet;
        }
    }
}

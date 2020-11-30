package com.fan.blockchain.wallet;

import com.fan.blockchain.util.Base58Check;
import com.fan.blockchain.util.BtcAddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

@Data
@AllArgsConstructor
public class Wallet implements Serializable {
    // checksum length
    private static final int ADDRESS_CHECKSUM_LEN = 4;
    // private key
    private BCECPrivateKey privateKey;
    // public key
    private byte[] publicKey;

    public Wallet(){
        initWallet();
    }

    /**
     * 初始化钱包
     */
    private void initWallet() {
        try {
            KeyPair keyPair = newECKeyPair();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
            byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
            this.setPrivateKey(privateKey);
            this.setPublicKey(publicKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * create new key pair
     * @return
     * @throws Exception
     */
    private KeyPair newECKeyPair() throws Exception{
        // register BC Provider
        Security.addProvider(new BouncyCastleProvider());
        // create generator of key pair.the algorithm is ECDSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA",BouncyCastleProvider.PROVIDER_NAME);
        // EC parameters specific
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecSpec,new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * get address of wallet
     */
    public String getAddress() {
        try {
            // 1.获取ripemdHash
            byte[] ripeMD169Hash = BtcAddressUtils.ripeMD160Hash(this.getPublicKey());
            // 2.添加版本0x00
            ByteArrayOutputStream addrStream = new ByteArrayOutputStream();
            addrStream.write((byte)0); // 添加1byte
            addrStream.write(ripeMD169Hash);
            byte[] versionedPayload = addrStream.toByteArray();
            // 3.计算校验码
            byte[] checksum = BtcAddressUtils.checksum(versionedPayload);
            addrStream.write(checksum);
            // 4.得到version(1byte 相当于 16进制的两位 0x00)+payload+checksum(4byte) 组合
            byte[] binaryAddress = addrStream.toByteArray();

            return Base58Check.byteToBase58(binaryAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to get wallet address ! ");
    }

    /**
     * 私钥备份
     */
    public String backupPrivateKey(){
        // TODO
        return "";
    }

    /**
     * 导入私钥
     */
    public void importPrivateKey(){
        // todo
    }
}

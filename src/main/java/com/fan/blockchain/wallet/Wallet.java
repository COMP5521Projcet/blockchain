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
    // 校验码长度
    private static final int ADDRESS_CHECKSUM_LEN = 4;
    // 私钥
    private BCECPrivateKey privateKey;
    // 公钥
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
     * 创建新的密钥对
     * @return
     * @throws Exception
     */
    private KeyPair newECKeyPair() throws Exception{
        // 注册BC Provider
        Security.addProvider(new BouncyCastleProvider());
        // 创建椭圆曲线算法的密钥对生成器，算法为ECDSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA",BouncyCastleProvider.PROVIDER_NAME);
        // 椭圆曲线(EC)域参数设定
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecSpec,new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 获取钱包地址
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

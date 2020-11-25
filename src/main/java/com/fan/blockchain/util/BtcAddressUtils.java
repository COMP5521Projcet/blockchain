package com.fan.blockchain.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

import java.util.Arrays;

public class BtcAddressUtils {
    /**
     * 双重Hash
     * @param data
     * @return
     */
    public static byte[] doubleHash(byte[] data){
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }

    /**
     * 计算公钥的 RIPEMD 160 Hash值
     * @param pubKey
     * @return
     */
    public static byte[] ripeMD160Hash(byte[] pubKey) {
        byte[] shaHashedKey = DigestUtils.sha256(pubKey);
        RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
        ripemd160.update(shaHashedKey,0, shaHashedKey.length);
        byte[] output = new byte[ripemd160.getDigestSize()];
        ripemd160.doFinal(output,0);
        return output;
    }

    /**
     * 生成公钥的校验码
     * @param payload
     * @return
     */
    public static byte[] checksum(byte[] payload){
        // 返回4byte
        return Arrays.copyOfRange(doubleHash(payload),0,4);
    }
}

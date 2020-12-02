package com.fan.blockchain.transaction;

import com.fan.blockchain.block.Blockchain;
import com.fan.blockchain.util.BtcAddressUtils;
import com.fan.blockchain.util.SerializeUtils;
//import com.fan.blockchain.util.SerializeUtils;
import com.fan.blockchain.util.WalletUtils;
import com.fan.blockchain.wallet.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable{
    // reward of mining
    private static final int SUBSIDY = 10;
    // transaction's hash
    private byte[] txId;
    // transaction's inputs
    private TXInput[] inputs;
    // transaction's outputs
    private TXOutput[] outputs;

    /**
     * calculate the hash value of transaction
     * @return
     */
    public byte[] hash() {
        // 使用序列化的方式对Transaction对象进行深度复制
        byte[] serializedBytes = SerializeUtils.serializer(this);
        Transaction copyTx =  SerializeUtils.deserializer(serializedBytes,Transaction.class);
        copyTx.setTxId(new byte[]{});
        return DigestUtils.sha256(SerializeUtils.serializer(copyTx));
    }
    /**
     * create coinbase transaction
     * @param to 收账的钱包地址
     * @param data 解锁脚本数据
     * @return
     */
    public static Transaction newCoinbaseTX(String to, String data){
        if (StringUtils.isBlank(data)){
            data = String.format("Reward to '%s'",to);
        }
        // create input
        TXInput txInput = new TXInput(new byte[]{},-1,null,data.getBytes());
        // create output
        TXOutput txOutput = TXOutput.newTxOutput(SUBSIDY,to);
        // create transaction
        Transaction tx = new Transaction(null,new TXInput[]{txInput},new TXOutput[]{txOutput});
        // set transaction ID
        tx.setTxId(tx.hash());
        return tx;
    }

    /**
     * 是否为 coinbase
     * @return
     */
    public boolean isCoinbase(){
        return this.getInputs().length == 1
                && this.getInputs()[0].getTxId().length == 0
                && this.getInputs()[0].getTxOutputIndex() == -1;
    }

    /**
     * 从from向to支付一定amount的金额
     * @param from
     * @param to
     * @param amount
     * @param blockchain
     * @return
     * @throws Exception
     */
    public static Transaction newUTXOTransaction(String from, String to, int amount, Blockchain blockchain) throws Exception {
        // 获取钱包
        Wallet senderWallet = WalletUtils.getInstance().getWallet(from);
        byte[] publicKey = senderWallet.getPublicKey();
        byte[] pubKeyHash = BtcAddressUtils.ripeMD160Hash(publicKey);
        //找到from可花费的outputs
        SpendableOutputResult result = new UTXOSet(blockchain).findSpendableOutputs(pubKeyHash,amount);
        int accumulated = result.getAccumulated();
        Map<String,int[]> unspentOuts = result.getUnspentOuts();
        if (accumulated < amount) {
            throw new Exception("ERROR: Not enough funds");
        }
        Iterator<Map.Entry<String,int[]>> iterator = unspentOuts.entrySet().iterator();
        // 得到所有的input
        TXInput[] txInputs = {};
        while (iterator.hasNext()) {
            Map.Entry<String,int[]> entry = iterator.next();
            String txIdStr = entry.getKey();
            int[] outIndexes = entry.getValue();
            byte[] txId = Hex.decodeHex(txIdStr);
            for (int outIndex: outIndexes){
                txInputs = ArrayUtils.add(txInputs,new TXInput(txId,outIndex,null,publicKey));
            }
        }
        TXOutput[] txOutputs = {};
        // 一个output是给to的
        txOutputs = ArrayUtils.add(txOutputs,TXOutput.newTxOutput(amount,to));
        if (accumulated > amount) {
            // 一个output是给from的
            txOutputs = ArrayUtils.add(txOutputs,TXOutput.newTxOutput((accumulated - amount),from));
        }
        Transaction newTx = new Transaction(null,txInputs,txOutputs);
        newTx.setTxId(newTx.hash());
        blockchain.signTransaction(newTx,senderWallet.getPrivateKey());
        return newTx;
    }

    /**
     * 创建用于签名的交易数据副本，交易输入的signature和pubKey需要设置为null
     */
    public Transaction trimmedCopy(){
        TXInput[] tempTxInputs = new TXInput[this.getInputs().length];
        for (int i = 0;i < this.getInputs().length;i++){
            TXInput txInput = this.getInputs()[i];
            tempTxInputs[i] = new TXInput(txInput.getTxId(),txInput.getTxOutputIndex(),null,null);
        }
        TXOutput[] tempTxOutputs = new TXOutput[this.getOutputs().length];
        for (int i = 0;i < this.getOutputs().length;i++){
            TXOutput txOutput = this.getOutputs()[i];
            tempTxOutputs[i] = new TXOutput(txOutput.getValue(), txOutput.getPubKeyHash());
        }
        return new Transaction(this.getTxId(),tempTxInputs,tempTxOutputs);
    }

    /**
     * 签名
     */
    public void sign(BCECPrivateKey privateKey,Map<String,Transaction> prevTxMap) throws Exception {
        // coinbase dose not need to sign.Because it does not contain any transactions
        if (this.isCoinbase()){
            return;
        }
        // verify the input is correct
        for (TXInput txInput: this.getInputs()) {
            if (prevTxMap.get(Hex.encodeHexString(txInput.getTxId())) == null){
                throw new Exception("Error: Previous transactions are not correct");
            }
        }
        // create a copy of transaction to sign
        Transaction txCopy = this.trimmedCopy();
        Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);
        for (int i = 0;i < txCopy.getInputs().length;i++){
            TXInput txInputCopy = txCopy.getInputs()[i];
            // get the transaction of the this input
            Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInputCopy.getTxId()));
            // get the output of previous transaction
            TXOutput prevTxOutput = prevTx.getOutputs()[txInputCopy.getTxOutputIndex()];
            txInputCopy.setPubKey(prevTxOutput.getPubKeyHash());
            txInputCopy.setSignature(null);
            // get transaction ID
            txCopy.setTxId(txCopy.hash());
            // reset pubKey
            txInputCopy.setPubKey(null);
            ecdsaSign.update(txCopy.getTxId());
            byte[] signature = ecdsaSign.sign();
            this.getInputs()[i].setSignature(signature);
        }
    }
    /**
     * 验证交易信息
     */
    public boolean verify(Map<String,Transaction> prevTxMap) throws Exception {
        if (this.isCoinbase()) {
            return true;
        }
        // 再次验证交易信息中交易输入是否正确，也就是能否查找对应的交易数据
        for (TXInput txInput: this.getInputs()) {
            if (prevTxMap.get(Hex.encodeHexString(txInput.getTxId())) == null){
                throw new Exception("Error: Previous transactions are not correct");
            }
        }
        // 创建用于签名的交易信息的副本
        Transaction txCopy = this.trimmedCopy();
        Security.addProvider(new BouncyCastleProvider());
        ECNamedCurveParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);

        for (int i = 0;i < this.getInputs().length;i++){
            TXInput txInput = this.getInputs()[i];
            Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInput.getTxId()));
            TXOutput prevTxOutput = prevTx.getOutputs()[txInput.getTxOutputIndex()];

            TXInput txInputCopy = txCopy.getInputs()[i];
            txInputCopy.setSignature(null);
            txInputCopy.setPubKey(prevTxOutput.getPubKeyHash());
            txCopy.setTxId(txCopy.hash());
            txInputCopy.setPubKey(null);
            // 使用椭圆曲线 x,y 点去生成公钥Key
            BigInteger x = new BigInteger(1, Arrays.copyOfRange(txInput.getPubKey(), 1, 33));
            BigInteger y = new BigInteger(1, Arrays.copyOfRange(txInput.getPubKey(), 33, 65));
            ECPoint ecPoint = ecParameters.getCurve().createPoint(x, y);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(txCopy.getTxId());
            if (!ecdsaVerify.verify(txInput.getSignature())){
                return false;
            }
        }
        return true;
    }
}

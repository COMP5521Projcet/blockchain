//package com.fan.blockchain.util;
//
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Input;
//import com.esotericsoftware.kryo.io.Output;
//import com.fan.blockchain.block.Block;
//import com.fan.blockchain.transaction.TXInput;
//import com.fan.blockchain.transaction.TXOutput;
//import com.fan.blockchain.transaction.Transaction;
//import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
//import org.objenesis.strategy.StdInstantiatorStrategy;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 序列号工具
// */
//public class SerializeUtils {
//    /**
//     * 反序列化
//     * @param bytes 对象对应的字节数组
//     * @return
//     */
//    public static Object deserialize(byte[] bytes){
//
////        Input input = new Input(bytes);
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
//        Input input = new Input(inputStream);
////        input.setInputStream(inputStream);
//        // 反序列化和序列化 注册类的顺序应该一致！
//        Kryo kryo = new Kryo();
//        kryo.setReferences(true);
//        kryo.register(Block.class);
//        kryo.register(Transaction.class);
//        kryo.register(Transaction[].class);
//        kryo.register(HashMap.class);
//        kryo.register(byte[].class);
//        kryo.register(TXInput[].class);
//        kryo.register(TXOutput[].class);
//        kryo.register(TXInput.class);
//        kryo.register(TXOutput.class);
//        Object obj = kryo.readClassAndObject(input);
//        input.close();
//        return obj;
//    }
//
//    public static byte[] serialize(Object object){
//        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
////        Output output = new Output(4096 * 2,-1);
//        Output output = new Output(outStream,100000);
//        Kryo kryo = new Kryo();
//        kryo.setReferences(true);
//        kryo.register(Block.class);
//        kryo.register(Transaction.class);
//        kryo.register(Transaction[].class);
//        kryo.register(HashMap.class);
//        kryo.register(byte[].class);
//        kryo.register(TXInput[].class);
//        kryo.register(TXOutput[].class);
//        kryo.register(TXInput.class);
//        kryo.register(TXOutput.class);
//        kryo.writeClassAndObject(output,object);
//        byte[] bytes = output.toBytes();
//        output.close();
//        return bytes;
//    }
//}

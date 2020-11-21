package com.fan.blockchain.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fan.blockchain.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列号工具
 */
public class SerializeUtils {
    /**
     * 反序列化
     * @param bytes 对象对应的字节数组
     * @return
     */
    public static Object deserialize(byte[] bytes){
        Input input = new Input(bytes);
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(Block.class);
        kryo.register(byte[].class);
        Object obj = kryo.readClassAndObject(input);
        input.close();
        return obj;
    }

    public static byte[] serialize(Object object){
        Output output = new Output(4096,-1);
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(Block.class);
        kryo.register(byte[].class);
        kryo.writeClassAndObject(output,object);
        byte[] bytes = output.toBytes();
        output.close();
        return bytes;
    }
}

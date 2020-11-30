package com.fan.blockchain.util;


import org.nustaq.serialization.FSTConfiguration;

/**
 * serialize tool
 */
public class SerializeUtils {

    private static ThreadLocal<FSTConfiguration> confs = new ThreadLocal() {
        public FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };

    private static FSTConfiguration getFST() {
        return confs.get();
    }

    /**
     * serialize class to byte array
     * @param t
     * @param <T>
     * @return
     */
    public static <T> byte[] serializer(T t) {
        return getFST().asByteArray(t);
    }

    /**
     * deserialize byte array to class
     * @param bytes
     * @param c
     * @param <T>
     * @return
     */
    public static <T> T deserializer(byte[] bytes, Class<T> c) {
        return (T) getFST().asObject(bytes);
    }
}



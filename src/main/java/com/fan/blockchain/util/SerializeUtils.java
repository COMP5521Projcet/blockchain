package com.fan.blockchain.util;


import org.nustaq.serialization.FSTConfiguration;

public class SerializeUtils {

    private static ThreadLocal<FSTConfiguration> confs = new ThreadLocal() {
        public FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };

    private static FSTConfiguration getFST() {
        return confs.get();
    }

    public static <T> byte[] serializer(T t) {
        return getFST().asByteArray(t);
    }


    public static <T> T deserializer(byte[] bytes, Class<T> c) {
        return (T) getFST().asObject(bytes);
    }
}



package com.leyou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-04-24 17:20
 **/
public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    @Nullable
    public static String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }

    @Nullable
    public static <T> T parse(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <E> List<E> parseList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Nullable
    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    static class User{
//        String name;
//        Integer age;
//    }
//    public static void main(String[] args) {
//        User user = new User("jack", 21);
//
//        String serialize = serialize(user);
//        System.out.println("json = " + serialize);
//
//        User user1 = parse(serialize, User.class);
//        System.out.println("User = " + user1);
//
//        String json  = "{\"name\": \"jack\", \"age\": 21}";
//        Map<String, String> objectObjectMap = parseMap(json,String.class, String.class);
//
//        System.out.println("map = " + objectObjectMap);
//
//        String listmap  = "[{\"name\": \"jack\", \"age\": 21},{\"name\": \"jerry\", \"age\": 22}]";
//        List<Map<String, String>> maps = nativeRead(listmap, new TypeReference<List<Map<String, String>>>() {});
//        System.out.println("listmap = " + maps);
//    }
}

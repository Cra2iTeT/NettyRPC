package com.Cra2iTeT.factory;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Cra2iTeT
 * @since 2023/1/6 15:51
 */
@Slf4j
public class ServiceFactory implements Serializable {
    private static Map<String, Object> objects;
    private static Map<String, Method> methods;

    public ServiceFactory() {
        objects = new ConcurrentHashMap<>();
        methods = new ConcurrentHashMap<>();
    }

    private <T> void addObject(String name, T service) {
        if (!objects.containsKey(name)) {
            objects.put(name, service);
        }
    }

    public <T> void addMethod(String beanName, T service, String value, Method method) {
        if (!methods.containsKey(value)) {
            methods.put(value, method);
        }
        addObject(beanName, service);

    }

    public static Object getObject(String beanName) {
        if (!objects.containsKey(beanName)) {
            throw new RuntimeException("未发现该+" + beanName + "服务");
        } else {
            return objects.get(beanName);
        }
    }

    public static Method getMethod(String value) {
        if (!methods.containsKey(value)) {
            throw new RuntimeException("未发现该+" + value + "方法");
        } else {
            return methods.get(value);
        }
    }
}

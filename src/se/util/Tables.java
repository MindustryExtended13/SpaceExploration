package se.util;

import arc.func.Boolf2;
import arc.func.Cons2;
import arc.func.Func;
import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;

import org.jetbrains.annotations.Contract;

import se.SpaceExploration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

public class Tables {
    public static boolean hiddenWork = false;

    public static void hiddenWork(Runnable run) {
        boolean old = hiddenWork;
        hiddenWork = true;
        run.run();
        hiddenWork = old;
    }

    public static Class<?> getTableType(Object obj) {
        return obj == null ? null : getTableTypeCl(obj.getClass());
    }

    public static Class<?> getTableTypeCl(Class<?> type) {
        return type.isAnonymousClass() ? getTableTypeCl(type.getSuperclass()) : type;
    }

    @Contract("null -> !null")
    public static String toString(Throwable throwable) {
        if(throwable == null) return "null";
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private static void _tmp_295210(String cl, String name, Throwable err) {
        if(!hiddenWork) SpaceExploration.LOGGERE.log("Error setting field {} of class {} for object:\n{}", name, cl, toString(err));
    }

    public static void deepEdit(Class<?> initial, Cons2<Field, Class<?>> consumer) {
        if(initial == null) return;
        Class<?> cl = initial;

        while(cl != null) {
            for(Field field : cl.getDeclaredFields()) {
                field.setAccessible(true);
                consumer.get(field, cl);
            }

            cl = cl.getSuperclass();
        }
    }

    @Contract("null, _, _ -> null")
    public static<T> T deepCopy(T object, Prov<T> constructor, Seq<String> ignoredFields) {
        return deepCopy(object, (ignored) -> constructor.get(), ignoredFields);
    }

    @Contract("null, _, _ -> null")
    public static<T> T deepCopy(T object, Func<T, T> constructor, Seq<String> ignoredFields) {
        if(object == null) return null;
        T instance = constructor.get(object);
        if(object == instance) {
            return instance;
        }

        Class<?> cl = object.getClass();
        while(cl != null) {
            for(Field field : cl.getDeclaredFields()) {
                String name = field.getName();

                if(!ignoredFields.contains(name, false)) {
                    try {
                        Reflect.set(cl, instance, name, Reflect.get(cl, object, name));
                    } catch(Throwable e) {
                        _tmp_295210(cl.getSimpleName(), name, e);
                    }
                }
            }

            cl = cl.getSuperclass();
        }

        return instance;
    }

    public static<K, V> boolean contains(ObjectMap<K, V> map, Boolf2<K, V> boolf2) {
        return find(map, boolf2) != null;
    }

    @Contract("null, _ -> null; !null, null -> null")
    public static<K, V> K find(ObjectMap<K, V> map, Boolf2<K, V> boolf2) {
        if(map == null || boolf2 == null) return null;

        for(var entry : map) {
            if(boolf2.get(entry.key, entry.value)) {
                return entry.key;
            }
        }

        return null;
    }
}
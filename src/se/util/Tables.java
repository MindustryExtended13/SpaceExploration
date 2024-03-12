package se.util;

import arc.func.Func;
import arc.func.Prov;
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

    public static Class<?> getTableType(Class<?> type) {
        return type.isAnonymousClass() ? getTableType(type.getSuperclass()) : type;
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
                        Reflect.set(cl, instance, name, field.get(object));
                    } catch(Throwable e) {
                        _tmp_295210(cl.getSimpleName(), name, e);
                    }
                }
            }

            cl = cl.getSuperclass();
        }

        return instance;
    }
}
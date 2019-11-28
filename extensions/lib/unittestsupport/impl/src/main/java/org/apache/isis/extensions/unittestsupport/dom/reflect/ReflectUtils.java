package org.apache.isis.extensions.unittestsupport.dom.reflect;

import java.lang.reflect.Field;

public class ReflectUtils {
    private ReflectUtils() {
    }

    public static void inject(
            final Object target,
            final String fieldName,
            final Object toInject) {

        final Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, toInject);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(
            final Object target,
            final Object toInject) {

        final String clsName = toInject.getClass().getSimpleName();
        final String fieldName = Character.toLowerCase(clsName.charAt(0)) + clsName.substring(1);
        inject(target, fieldName, toInject);
    }
}

package org.apache.isis.testing.unittestsupport.applib.bean;

public class FixtureDatumFactoriesForAnyPojo {

    @SuppressWarnings("unchecked")
    public static <T> PojoTester.FixtureDatumFactory<T> pojos(Class<T> compileTimeType, Class<? extends T> runtimeType) {
        try {
            final T obj1 = runtimeType.newInstance();
            final T obj2 = runtimeType.newInstance();
            return new PojoTester.FixtureDatumFactory<>(compileTimeType, obj1, obj2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> PojoTester.FixtureDatumFactory<T> pojos(Class<T> type) {
        return pojos(type, type);
    }

}

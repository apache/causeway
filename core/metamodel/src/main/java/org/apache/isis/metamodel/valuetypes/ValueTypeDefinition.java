package org.apache.isis.metamodel.valuetypes;

import java.util.Map;

import org.apache.isis.schema.common.v1.ValueType;

import lombok.Data;

@Data
public class ValueTypeDefinition {

    public static ValueTypeDefinition collection(Class<?> clazz) {
        return new ValueTypeDefinition(clazz, ValueType.COLLECTION);
    }
    public static ValueTypeDefinition of(Class<?> clazz, ValueType valueType) {
        return new ValueTypeDefinition(clazz, valueType);
    }
    public static ValueTypeDefinition from(final Map.Entry<Class<?>, ValueType> entry) {
        return new ValueTypeDefinition(entry.getKey(), entry.getValue());
    }
    private ValueTypeDefinition(Class<?> clazz, ValueType valueType) {
        this.clazz = clazz;
        this.valueType = valueType;
    }
    private final Class<?> clazz;
    private final ValueType valueType;
}

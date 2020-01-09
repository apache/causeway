package org.apache.isis.metamodel.valuetypes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.schema.common.v1.ValueType;

import lombok.val;

@Component
@Named("isisMetaModel.ValueTypeRegistry")
@Order(OrderPrecedence.MIDPOINT)
public class ValueTypeRegistry {

    private final List<ValueTypeProvider> valueTypeProviders;
    private final Map<Class<?>, Optional<ValueTypeDefinition>> definitionByClazz;

    @Inject
    public ValueTypeRegistry(
            final List<ValueTypeProvider> valueTypeProviders) {
        this.valueTypeProviders = valueTypeProviders;
        this.definitionByClazz = Collections.unmodifiableMap(aggregate(valueTypeProviders));
    }

    static Map<Class<?>, Optional<ValueTypeDefinition>> aggregate(
            final List<ValueTypeProvider> valueTypeProviders) {

        final Map<Class<?>, Optional<ValueTypeDefinition>> map = new LinkedHashMap<>();
        for (ValueTypeProvider valueTypeProvider : valueTypeProviders) {
            final Collection<ValueTypeDefinition> valueTypeDefinitions = valueTypeProvider.definitions();
            valueTypeDefinitions
                    .forEach(valueTypeDefinition -> {
                        map.put(valueTypeDefinition.getClazz(), Optional.of(valueTypeDefinition));
                    });
        }
        return map;
    }

    public ValueType asValueType(final Class<?> clazz) {
        return definitionByClazz.getOrDefault(clazz, Optional.empty())
                .map(ValueTypeDefinition::getValueType)
                .orElse(fallback(clazz));
    }

    private static ValueType fallback(final Class<?> clazz) {
        if (clazz!=null && clazz.isEnum()) {
            return ValueType.ENUM;
        }
        // assume reference otherwise
        return ValueType.REFERENCE;
    }

    public Stream<Class<?>> classes() {
        val classes = new LinkedHashSet<Class<?>>();
        valueTypeProviders.stream()
                .map(ValueTypeProvider::definitions)
                .forEach(definitions -> {
                    definitions.stream()
                            .map(ValueTypeDefinition::getClazz)
                            .forEach(classes::add);
                });
        return classes.stream();
    }

}
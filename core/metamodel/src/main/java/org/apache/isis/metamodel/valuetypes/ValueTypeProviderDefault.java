package org.apache.isis.metamodel.valuetypes;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.schema.common.v1.ValueType;

@Component
@Named("isisMetaModel.ValueTypeProviderBuiltIn")
@Order(OrderPrecedence.MIDPOINT)
public class ValueTypeProviderDefault implements ValueTypeProvider {

    private final Map<Class<?>, ValueTypeDefinition> definitions =
            from(CommonDtoUtils.valueTypeByClass);

    private static Map<Class<?>, ValueTypeDefinition> from(final Map<Class<?>, ValueType> valueTypeByClass) {
        final Map<Class<?>, ValueTypeDefinition> map = new LinkedHashMap<>();
        valueTypeByClass.entrySet()
                .forEach(entry -> map.put(entry.getKey(), ValueTypeDefinition.from(entry)));
        return map;
    }

    @Override
    public Collection<ValueTypeDefinition> definitions() {
        return definitions.values();
    }

}

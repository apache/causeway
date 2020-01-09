package org.apache.isis.metamodel.valuetypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.schema.common.v1.ValueType;

@Component
@Named("isisMetaModel.ValueTypeProviderCollections")
@Order(OrderPrecedence.MIDPOINT)
public class ValueTypeProviderForCollections implements ValueTypeProvider {

    @Override
    public Collection<ValueTypeDefinition> definitions() {
        return Collections.unmodifiableList(Arrays.asList(
                    ValueTypeDefinition.collection(List.class),
                    ValueTypeDefinition.collection(Set.class),
                    ValueTypeDefinition.collection(SortedSet.class),
                    ValueTypeDefinition.collection(FreeStandingList.class)
                ));
    }

}

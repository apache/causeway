package org.apache.isis.metamodel.valuetypes;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.isis.schema.common.v1.ValueType;

public interface ValueTypeProvider {

    Collection<ValueTypeDefinition> definitions();



}
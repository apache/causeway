/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.metamodel.facets.object.value;

import java.util.Optional;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Indicates that this class has value semantics.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>@Value</tt> annotation. However, note that value semantics is just a
 * convenient term for a number of mostly optional semantics all of which are
 * defined elsewhere.
 */
public interface ValueFacet<T> extends Facet {

    LogicalType getValueType();
    Can<ValueSemanticsProvider<T>> getValueSemantics();

    Optional<Parser<T>> selectParserForParameter(final ObjectActionParameter param);
    Optional<Parser<T>> selectParserForProperty(final OneToOneAssociation prop);

    Parser<T> fallbackParser(Identifier featureIdentifier);

    default Parser<T> selectParserForParameterElseFallback(final ObjectActionParameter param) {
        return selectParserForParameter(param)
                .orElseGet(()->fallbackParser(param.getFeatureIdentifier()));
    }

    default Parser<T> selectParserForPropertyElseFallback(final OneToOneAssociation prop) {
        return selectParserForProperty(prop)
                .orElseGet(()->fallbackParser(prop.getFeatureIdentifier()));
    }

}

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
package org.apache.isis.core.metamodel.interactions.managed;

import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.internal.binding._BindableAbstract;
import org.apache.isis.commons.internal.binding._Bindables;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _BindingUtil {

    Bindable<String> bindAsParsableText(
            final @NonNull ObjectActionParameter param,
            final @NonNull _BindableAbstract<ManagedObject> bindableParamValue) {

        val spec = param.getElementType();

        if(param.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION) {
            _Bindables.forValue(String.format("Non-scalar action parameters are not parseable: %s",
                    param.getFeatureIdentifier()));
        }

        // value types should have associated parsers/formatters via value semantics
        return spec.lookupFacet(ValueFacet.class)
        .map(valueFacet->valueFacet.selectParserForParameterElseFallback(param))
        .map(parser->bindAsParsableText(spec, bindableParamValue, parser, null))
        .orElseGet(()->
            // fallback Bindable that is floating free (unbound)
            // writing to it has no effect on the domain
            _Bindables.forValue(String.format("Could not find a ValueFacet for type %s",
                    spec.getLogicalType()))
        );

    }

    Bindable<String> bindAsParsableText(
            final @NonNull OneToOneAssociation prop,
            final @NonNull _BindableAbstract<ManagedObject> bindablePropertyValue) {

        val spec = prop.getElementType();

        // value types should have associated parsers/formatters via value semantics
        return spec.lookupFacet(ValueFacet.class)
        .map(valueFacet->valueFacet.selectParserForPropertyElseFallback(prop))
        .map(parser->bindAsParsableText(spec, bindablePropertyValue, parser, null))
        .orElseGet(()->
            // fallback Bindable that is floating free (unbound)
            // writing to it has no effect on the domain
            _Bindables.forValue(String.format("Could not find a ValueFacet for type %s",
                    spec.getLogicalType()))
        );

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Bindable<String> bindAsParsableText(
            final @NonNull ObjectSpecification spec,
            final @NonNull _BindableAbstract<ManagedObject> bindableValue,
            final @NonNull Parser parser,
            final Context context) {

        return bindableValue.mapToBindable(
                value->{
                    val pojo = ManagedObjects.UnwrapUtil.single(value);
                    val text = parser.parseableTextRepresentation(context, pojo);
                    //System.err.printf("toText: %s -> '%s'%n", ""+value, text);
                    return text;
                },
                text->{
                    val value = ManagedObject.of(spec, parser.parseTextRepresentation(context, text));
                    //System.err.printf("fromText: '%s' -> %s%n", text, ""+value);
                    return value;
                });
    }

}

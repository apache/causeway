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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.lang.reflect.Modifier;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.Identifier.Type;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.SneakyThrows;

@Component
@Named("causeway.metamodel.value.IdentifierValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class IdentifierValueSemantics
extends ValueSemanticsAbstract<Identifier>
implements
    Renderer<Identifier> {

    @Override
    public Class<Identifier> getCorrespondingClass() {
        return Identifier.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.COMPOSITE;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Identifier value) {
        return CommonDtoUtils.typedTupleBuilder(value)
            .addFundamentalType(ValueType.STRING, "logicalName", it->it.logicalType().logicalName())
            .addFundamentalType(ValueType.STRING, "className", it->it.logicalType().className())
            .addFundamentalType(ValueType.STRING, "memberLogicalName", Identifier::memberLogicalName)
            .addFundamentalType(ValueType.STRING, "memberParameterClassNames", it->it.memberParameterClassNames().join(","))
            .addFundamentalType(ValueType.ENUM, "type", Identifier::type)
            .addFundamentalType(ValueType.INT, "parameterIndex", Identifier::parameterIndex)
            .buildAsDecomposition();
    }

    @Override @SneakyThrows
    public Identifier compose(final ValueDecomposition decomposition) {

        var elementMap = CommonDtoUtils.typedTupleAsMap(decomposition.composite());

        LogicalType logicalType = new LogicalType(
                (String)elementMap.get("logicalName"),
                _Context.loadClass((String)elementMap.get("className")));
        String memberLogicalName = (String)elementMap.get("memberLogicalName");
        Can<String> memberParameterClassNames = _Strings.splitThenStream((String)elementMap.get("memberParameterClassNames"), ",")
                .collect(Can.toCan());
        Type type = (Type)elementMap.get("type");
        int parameterIndex = (int)elementMap.get("parameterIndex");

        return new Identifier(logicalType, memberLogicalName, memberParameterClassNames, type, parameterIndex);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final Identifier value) {
        return value == null ? "" : value.getFullIdentityString();
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Identifier value) {
        return renderHtml(value, this::iconify);
    }

    // uses font-awesome
    private String iconify(final Identifier identifier) {
        var faQuickNotation = switch (identifier.type()) {
            case CLASS -> switch(TypeVariant.valueOf(identifier.logicalType().correspondingClass())) {
                case CONCRETE -> "solid copyright .col-green"; // resembling the class symbol from Eclipse IDE
                case INTERFACE -> "solid circle .col-steelblue, solid i .col-white .font-size-60"; // blue circle with inscribed I
                case ABSTRACT -> "solid circle .col-green, solid a .col-white .font-size-60"; // green circle with inscribed A
            };
            case ACTION -> "solid star .col-red";
            case ACTION_PARAMETER -> "regular star .col-red";
            case PROPERTY -> "solid square .col-green";
            case COLLECTION -> "solid list .col-gold";
        };
        var faLayers = FontAwesomeLayers.fromQuickNotation(faQuickNotation);
        return faIconAndTitle(faLayers, toMonospace(identifier.getFullIdentityString()));
    }


    @Override
    public Can<Identifier> getExamples() {
        return Can.of(
                // these are just stubs, not actually valid
                Identifier.classIdentifier(new LogicalType("java.Object", Object.class)),
                Identifier.propertyIdentifier(new LogicalType("java.Object", Object.class), "example")
            );
    }

    // -- HELPER

    private enum TypeVariant {
        CONCRETE,
        ABSTRACT,
        INTERFACE;
        static TypeVariant valueOf(final Class<?> cls) {
            return cls.isInterface()
                ? INTERFACE
                : Modifier.isAbstract(cls.getModifiers())
                    ? ABSTRACT
                    : CONCRETE;
        }
    }

}

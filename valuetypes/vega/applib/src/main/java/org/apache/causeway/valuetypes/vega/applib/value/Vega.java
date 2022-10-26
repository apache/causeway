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
package org.apache.causeway.valuetypes.vega.applib.value;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import javax.inject.Named;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.resources._Json;
import org.apache.causeway.valuetypes.vega.applib.CausewayModuleValVegaApplib;
import org.apache.causeway.valuetypes.vega.applib.jaxb.VegaJaxbAdapter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable value type holding JSON payload to be interpreted as
 * interactive visualization design as specified by corresponding
 * visualization grammar.
 *
 * @since 2.0 {@index}
 */
@Named(CausewayModuleValVegaApplib.NAMESPACE + ".Vega")
@org.apache.causeway.applib.annotation.Value
@EqualsAndHashCode
@XmlJavaTypeAdapter(VegaJaxbAdapter.class)  // for JAXB view model support
public final class Vega implements Serializable {

    private static final long serialVersionUID = 1L;

    @RequiredArgsConstructor
    public static enum Schema {
        NONE(null),
        VEGA("https://vega.github.io/schema/vega/v5.json"),
        VEGA_LITE("https://vega.github.io/schema/vega-lite/v5.json");

        public boolean isNone() {return this==NONE;}
        public boolean isVega() {return this==VEGA;}
        public boolean isVegaLite() {return this==VEGA_LITE;}
        public static String key() { return "$schema"; }
        @Getter @Accessors(fluent = true) private final String value;
        @NonNull String asEmptyJson() {
            return this==NONE
                    ? "{}"
                    : String.format("{\"%s\": \"%s\"}", key(), value());
        }
        /**
         * parses the json input for schema specification
         */
        @NonNull static Schema valueOfJson(final @Nullable String json) {
            return _Json.readJson(Map.class, json).getValue()
            .map(map->map.get(key()))
            .map(schemaValue->{
                for(var schema:Schema.values()) {
                    if(schemaValue.equals(schema.value())) {
                        return schema;
                    }
                }
                return null;
            })
            .orElse(NONE);
        }
    }

    public static Vega valueOf(final @Nullable String json) {
        return new Vega(Schema.valueOfJson(json), json);
    }

    @Getter private final Schema schema;
    @Getter private final String json;

    public Vega() {
        this(Schema.NONE, null);
    }

    public Vega(final @NonNull Schema schema, final @Nullable String json) {
        this.schema = schema;
        this.json = _Strings.isNotEmpty(json)
                ? json
                : schema.asEmptyJson();
    }

    public boolean isEqualTo(final Vega other) {
        return Objects.equals(this.getJson(), other!=null ? other.getJson() : null);
    }

    @Override
    public String toString() {
        return String.format("Vega[schema=%s,length=%d]",
                schema.name(),
                json.length());
    }

}

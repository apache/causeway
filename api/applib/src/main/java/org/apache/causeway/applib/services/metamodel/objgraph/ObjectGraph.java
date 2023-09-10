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
package org.apache.causeway.applib.services.metamodel.objgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.collections._Multimaps;

import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * Can be used to create diagrams (e.g. Plantuml)
 *
 * @since 2.0 {@index}
 */
@lombok.Value @Accessors(fluent=true)
public class ObjectGraph {

    @lombok.Value @Accessors(fluent=true)
    public static class Object {
        private final @With @NonNull String id;
        private final @With @NonNull String packageName;
        private final @With @NonNull String name;
        private final @With @NonNull Optional<String> stereotype;
        private final @With List<ObjectGraph.Field> fields;
    }

    @lombok.Value @Accessors(fluent=true)
    public static class Field {
        private final @With @NonNull String name;
        private final @With @NonNull String elementTypeShortName;
        private final @With boolean isPlural;
    }

    @lombok.Value @Accessors(fluent=true)
    public static class Relation {
        private final @With @NonNull RelationType relationType;
        private final @With @NonNull ObjectGraph.Object from;
        private final @With @NonNull ObjectGraph.Object to;
        private final @With @NonNull String label;
        private final @With @NonNull String label2;
        public String fromId() { return from.id(); }
        public String toId() { return to.id(); }
        public boolean isAssociation() { return relationType!=RelationType.INHERITANCE; }
        /**
         * If this is ONE_TO_MANY, decorate the label with enclosing square brackets.
         */
        public String labelFormatted() {
            return relationType==RelationType.ONE_TO_MANY
                    ? String.format("[%s]", label)
                    : label;
        }
    }

    public static enum RelationType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MERGED_ASSOCIATIONS,
        BIDIR_ASSOCIATION,
        INHERITANCE;
    }

    public static interface Factory {
        ObjectGraph create();
    }

    public static interface Transformer {
        ObjectGraph transform(ObjectGraph objGraph);
    }

    /**
     * Factory providing built in {@link Transformer}(s).
     */
    @UtilityClass
    public static class Transformers {
        @Getter(lazy = true)
        private final Transformer relationMerger = new _ObjectGraphRelationMerger();
    }

    public static interface Renderer {
        void render(StringBuilder sb, ObjectGraph objGraph);
    }

    private final List<ObjectGraph.Object> objects = new ArrayList<>();
    private final List<ObjectGraph.Relation> relations = new ArrayList<>();

    public static ObjectGraph create(final @NonNull ObjectGraph.Factory factory) {
        return factory.create();
    }

    public ObjectGraph transform(final @Nullable ObjectGraph.Transformer transfomer) {
        return transfomer!=null
                ? transfomer.transform(this)
                : this;
    }

    public String render(final @Nullable ObjectGraph.Renderer renderer) {
        if(renderer==null) return "";
        val sb = new StringBuilder();
        renderer.render(sb, this);
        return sb.toString();
    }

    /**
     * Returns objects grouped by package (as list-multimap).
     */
    public Map<String, List<ObjectGraph.Object>> objectsGroupedByPackage() {
        final var objectsGroupedByPackage = _Multimaps.<String, ObjectGraph.Object>newListMultimap();
        objects()
            .forEach(obj->objectsGroupedByPackage.putElement(obj.packageName(), obj));
        return objectsGroupedByPackage;
    }

    /**
     * Returns a {@link Map} from object.id to {@link Object ObjectGraph.Object}
     */
    public Map<String, ObjectGraph.Object> objectById() {
        final var objectById = new HashMap<String, ObjectGraph.Object>();
        objects()
            .forEach(obj->objectById.put(obj.id(), obj));
        return objectById;
    }

}

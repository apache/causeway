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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.graph.GraphUtils;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Strings.StringOperator;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.io.DataSink;
import org.apache.causeway.commons.io.DataSource;

import lombok.Getter;
import lombok.NonNull;
import lombok.With;
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
        private final @With @NonNull Optional<String> description;
        private final @With List<ObjectGraph.Field> fields;
        /** @return {@code packageName + "." + name} */
        public String fqName() {
            return packageName + "." + name;
        }
        public Object copy() {
            return withFields(
                    fields.stream()
                        .map(Field::copy)
                        .collect(Collectors.toCollection(ArrayList::new)));
        }
    }

    @lombok.Value @Accessors(fluent=true)
    public static class Field {
        private final @With @NonNull String name;
        private final @With @NonNull String elementTypeShortName;
        private final @With boolean isPlural;
        private final @With @NonNull Optional<String> description;
        public Field copy() {
            return withName(name);
        }
    }

    @lombok.Value @Accessors(fluent=true)
    public static class Relation {
        private final @With @NonNull RelationType relationType;
        private final @With @NonNull ObjectGraph.Object from;
        private final @With @NonNull ObjectGraph.Object to;
        private final @With @NonNull String description; // usually the middle label
        private final @With @NonNull String nearLabel;
        private final @With @NonNull String farLabel;
        public String fromId() { return from.id(); }
        public String toId() { return to.id(); }
        public boolean isAssociation() { return relationType.isAssociationAny(); }
        public StringOperator multiplicityNotation() {
            return relationType.isOneToMany()
                    ? _Strings.asSquareBracketed
                    : StringOperator.identity();
        }
        public String descriptionFormatted() {
            return multiplicityNotation().apply(description);
        }
        public Relation copy(final Map<String, ObjectGraph.Object> objectById) {
            return withFrom(objectById.get(fromId()))
                    .withTo(objectById.get(toId()));
        }
    }

    public enum RelationType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MERGED_ASSOCIATIONS,
        BIDIR_ASSOCIATION,
        INHERITANCE;
        public boolean isOneToOne() { return this == ONE_TO_ONE; }
        public boolean isOneToMany() { return this == ONE_TO_MANY; }
        public boolean isMerged() { return this == MERGED_ASSOCIATIONS; }
        public boolean isBidir() { return this == BIDIR_ASSOCIATION; }
        public boolean isInheritance() { return this == INHERITANCE; }
        public boolean isAssociationAny() { return this != INHERITANCE; }
    }

    public static interface Factory {
        ObjectGraph create();
    }

    public static interface Transformer {
        /**
         * If called from {@link ObjectGraph#transform(Transformer)},
         * given {@link ObjectGraph objGraph} is a defensive copy, that can safely be mutated.
         */
        ObjectGraph transform(ObjectGraph objGraph);
    }

    /**
     * Factory providing built in {@link Transformer}(s).
     */
    @UtilityClass
    public static class Transformers {
        @Getter(lazy = true)
        private final Transformer relationMerger = new _ObjectGraphRelationMerger();
        public Transformer objectModifier(final @NonNull UnaryOperator<ObjectGraph.Object> modifier) {
            return new _ObjectGraphObjectModifier(modifier);
        }
    }

    public static interface Renderer {
        void render(StringBuilder sb, ObjectGraph objGraph);
    }

    private final List<ObjectGraph.Object> objects = new ArrayList<>();
    private final List<ObjectGraph.Relation> relations = new ArrayList<>();

    public static ObjectGraph create(final @NonNull ObjectGraph.Factory factory) {
        return factory.create();
    }

    /**
     * Passes a (deep clone) copy of this {@link ObjectGraph} to given {@link Transformer}
     * and returns a transformed {@link ObjectGraph}.
     * <p>
     * Hence transformers are not required to create defensive copies.
     */
    public ObjectGraph transform(final @Nullable ObjectGraph.Transformer transfomer) {
        return transfomer!=null
                ? transfomer.transform(this.copy())
                : this;
    }

    public String render(final @Nullable ObjectGraph.Renderer renderer) {
        if(renderer==null) return "";
        var sb = new StringBuilder();
        renderer.render(sb, this);
        return sb.toString();
    }
    public DataSource asDiagramDslSource(final @Nullable ObjectGraph.Renderer renderer) {
        var dsl = render(renderer);
        return dsl==null
                ? DataSource.empty()
                : DataSource.ofStringUtf8(dsl);
    }
    public void writeDiagramDsl(final @Nullable ObjectGraph.Renderer renderer, final DataSink sink) {
        var dsl = render(renderer);
        if(dsl==null) return;
        sink.writeAll(os->
            os.write(dsl.getBytes(StandardCharsets.UTF_8)));
    }
    public void writeDiagramDsl(final @Nullable ObjectGraph.Renderer renderer, final File destinationDslFile) {
        var dsl = render(renderer);
        if(dsl==null) return;
        DataSink.ofFile(destinationDslFile)
            .writeAll(os->
                os.write(dsl.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Returns a (deep clone) copy of this {@link ObjectGraph}.
     */
    public ObjectGraph copy() {
        var copy = new ObjectGraph();
        this.objects().stream()
            .map(ObjectGraph.Object::copy)
            .forEach(copy.objects()::add);
        var copyiedObjectById = copy.objectById();
        this.relations().stream()
            .map(rel->rel.copy(copyiedObjectById))
            .forEach(copy.relations()::add);
        return copy;
    }

    /**
     * Returns a {@link GraphKernel} of given characteristics.
     */
    public GraphKernel kernel(final @NonNull ImmutableEnumSet<GraphCharacteristic> characteristics) {
        var kernel = new GraphUtils.GraphKernel(
                objects().size(), characteristics);
        relations().forEach(rel->{
            kernel.addEdge(
                    objects().indexOf(rel.from()),
                    objects().indexOf(rel.to()));
        });
        return kernel;
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

    /**
     * Returns a sub-graph comprised only of object nodes as picked per zero based indexes {@code int[]}.
     */
    public ObjectGraph subGraph(final int[] objectIndexes) {
        var subGraph = this.transform(g->{
            var subSet = Can.ofCollection(g.objects()).pickByIndex(objectIndexes);
            g.objects().clear();
            subSet.forEach(g.objects()::add);
            var objectIds = g.objectById().keySet();
            var isInSubgraph = (Predicate<ObjectGraph.Relation>) rel->
                objectIds.contains(rel.fromId())
                && objectIds.contains(rel.toId());
            g.relations().removeIf(isInSubgraph.negate());
            return g;
        });
        return subGraph;
    }

}

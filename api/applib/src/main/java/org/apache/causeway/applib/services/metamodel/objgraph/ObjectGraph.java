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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

import lombok.Builder;

/**
 * Can be used to create diagrams (e.g. Plantuml)
 *
 * @since 2.0 {@index}
 */
public record ObjectGraph(
        List<ObjectGraph.Object> objects,
        List<ObjectGraph.Relation> relations) {

    @Builder
    public record Object(
            @NonNull String id,
            @NonNull String packageName,
            @NonNull String name,
            @NonNull Optional<String> stereotype,
            @NonNull Optional<String> description,
            List<ObjectGraph.Field> fields) {
        /** @return {@code packageName + "." + name} */
        public String fqName() {
            return packageName + "." + name;
        }
        public ObjectBuilder asBuilder() {
            return builder().id(id).packageName(packageName).name(name).stereotype(stereotype).description(description)
                    .fields(fields.stream()
                            .map(Field::copy)
                            .collect(Collectors.toCollection(ArrayList::new)));
        }
        public Object copy() {
            return asBuilder().build();
        }
    }

    @Builder
    public record Field(
            @NonNull String name,
            @NonNull String elementTypeShortName,
            boolean isPlural,
            @NonNull Optional<String> description) {
        public FieldBuilder asBuilder() {
            return builder().name(name).elementTypeShortName(elementTypeShortName).isPlural(isPlural).description(description);
        }
        public Field copy() {
            return asBuilder().build();
        }
    }

    @Builder
    public record Relation(
            @NonNull RelationType relationType,
            ObjectGraph.@NonNull Object from,
            ObjectGraph.@NonNull Object to,
            @NonNull String description, // usually the middle label
            @NonNull String nearLabel,
            @NonNull String farLabel) {
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
        public RelationBuilder asBuilder() {
            return builder().relationType(relationType)
                    .from(from)
                    .to(to)
                    .description(description)
                    .nearLabel(nearLabel)
                    .farLabel(farLabel);
        }
        public Relation copy(final Map<String, ObjectGraph.Object> objectById) {
            return asBuilder()
                    .from(objectById.get(fromId()))
                    .to(objectById.get(toId()))
                    .build();
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
    public record Transformers() {
        public static Transformer relationMerger() { return new ObjectGraphRelationMerger(); }
        public static Transformer objectModifier(final @NonNull UnaryOperator<ObjectGraph.Object> modifier) {
            return new ObjectGraphObjectModifier(modifier);
        }
    }

    public static interface Renderer {
        void render(StringBuilder sb, ObjectGraph objGraph);
    }

    public ObjectGraph() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public static ObjectGraph create(final ObjectGraph.@NonNull Factory factory) {
        return factory.create();
    }

    /**
     * Passes a (deep clone) copy of this {@link ObjectGraph} to given {@link Transformer}
     * and returns a transformed {@link ObjectGraph}.
     * <p>
     * Hence transformers are not required to create defensive copies.
     */
    public ObjectGraph transform(final ObjectGraph.@Nullable Transformer transfomer) {
        return transfomer!=null
                ? transfomer.transform(this.copy())
                : this;
    }

    public String render(final ObjectGraph.@Nullable Renderer renderer) {
        if(renderer==null) return "";
        var sb = new StringBuilder();
        renderer.render(sb, this);
        return sb.toString();
    }
    public DataSource asDiagramDslSource(final ObjectGraph.@Nullable Renderer renderer) {
        var dsl = render(renderer);
        return dsl==null
                ? DataSource.empty()
                : DataSource.ofStringUtf8(dsl);
    }
    public void writeDiagramDsl(final ObjectGraph.@Nullable Renderer renderer, final DataSink sink) {
        var dsl = render(renderer);
        if(dsl==null) return;
        sink.writeAll(os->
            os.write(dsl.getBytes(StandardCharsets.UTF_8)));
    }
    public void writeDiagramDsl(final ObjectGraph.@Nullable Renderer renderer, final File destinationDslFile) {
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

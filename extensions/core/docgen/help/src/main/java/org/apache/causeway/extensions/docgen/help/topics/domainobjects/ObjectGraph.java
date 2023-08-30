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
package org.apache.causeway.extensions.docgen.help.topics.domainobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.Value;
import lombok.val;

class ObjectGraph {

    // -- FACTORIES

    public static ObjectGraph.Object object(final String id, final ObjectSpecification objSpec) {
        return new ObjectGraph.Object(id,
                objSpec.getLogicalType().getNamespace(),
                objSpec.getLogicalType().getLogicalTypeSimpleName(),
                objSpec.isAbstract()
                ? Optional.of("abstract")
                : Optional.empty());
    }

    // -- GRAPH

    @Value
    private static class Context {

        private final ListMultimap<String, LogicalType> logicalTypesByNamespace = _Multimaps.newListMultimap();
        private final Map<LogicalType, ObjectGraph.Object> objectByLogicalType = new HashMap<>();
        private final List<ObjectGraph.Relation> relations = new ArrayList<>();

        public ObjectGraph.Object registerObject(final ObjectSpecification objSpec) {

            val addFieldsLater = _Refs.booleanRef(false);

            val obj = objectByLogicalType.computeIfAbsent(objSpec.getLogicalType(), logicalType->{
                logicalTypesByNamespace.putElement(logicalType.getNamespace(), logicalType);
                val newObjId = "o" + objectByLogicalType.size();
                val newObj = object(newObjId, objSpec);
                addFieldsLater.setValue(true);
                return newObj;
            });

            if(addFieldsLater.isTrue()) {
                objSpec.streamAssociations(MixedIn.EXCLUDED)
                .peek(ass->{
                    val elementType = ass.getElementType();
                    if(elementType.isEntity()
                            || elementType.isAbstract()) {
                        val referencedObj = registerObject(elementType);

                        val thisType = objSpec.getLogicalType();
                        val refType = elementType.getLogicalType();

                        val thisNs = thisType.getNamespace();
                        val refNs = refType.getNamespace();

                        // only register association relations if they don't cross namespace boundaries
                        // in other words: only include, if they share the same namespace
                        if(thisNs.equals(refNs)) {
                            val thisCls = thisType.getCorrespondingClass();
                            val refCls = refType.getCorrespondingClass();
                            if(thisCls.equals(refCls)
                                    || !refCls.isAssignableFrom(thisCls)) {
                                // we found a 1-x relation
                                registerRelation(
                                        ass.isOneToOneAssociation()
                                            ? ObjectGraph.Relation.RelationType.ONE_TO_ONE
                                            : ObjectGraph.Relation.RelationType.ONE_TO_MANY,
                                        obj.id, referencedObj.id, ass.getId());
                            }
                        }

                    }
                })
                .map(ObjectGraph.Object.Field::forAss)
                .forEach(obj.getFields()::add);
            }
            return obj;
        }

        public ObjectGraph.Relation registerRelation(
                final ObjectGraph.Relation.RelationType relationType,
                final String fromId,
                final String toId,
                final String label) {
            val relation = new ObjectGraph.Relation(relationType, fromId, toId, label, "");
            relations.add(relation);
            return relation;
        }

        public void consolidateAssociationRelations() {

            // collect association-relations into a list-multi-map,
            // where each key references a list of relations that need to be merged into one
            final ListMultimap<String, ObjectGraph.Relation> shared = _Multimaps.newListMultimap();
            relations.stream()
                .filter(ObjectGraph.Relation::isAssociation)
                .forEach(ass->{
                    shared.putElement(ass.toId + " " + ass.fromId, ass);
                });

            relations.removeIf(ObjectGraph.Relation::isAssociation);

            shared.forEach((key, list) -> {
                val merged = list.stream().reduce((a, b)->new ObjectGraph.Relation(
                        ObjectGraph.Relation.RelationType.MERGED_ASSOCIATIONS,
                        a.fromId, a.toId,
                        a.labelFormatted() + "," + b.labelFormatted(), ""));
                merged.ifPresent(relations::add);
            });

            consolidateBidirRelations();
        }

        private void consolidateBidirRelations() {

            // collect association-relations into a list-multi-map,
            // where each key references a list of relations that need to be merged into one;
            // we are using a sorted key where relation direction does not matter
            final ListMultimap<String, ObjectGraph.Relation> shared = _Multimaps.newListMultimap();
            relations.stream()
                .filter(ObjectGraph.Relation::isAssociation)
                .filter(ass->!ass.toId.equals(ass.fromId)) // exclude self referencing relations
                .forEach(ass->{
                    if(ass.fromId.compareTo(ass.toId)>0) {
                        shared.putElement(ass.toId + " " + ass.fromId, ass);
                    } else {
                        shared.putElement(ass.fromId + " " + ass.toId, ass);
                    }
                });

            shared.forEach((key, list) -> {
                if(list.size()==2) {
                    relations.removeAll(list);
                    val a = list.get(0);
                    val b = list.get(1);
                    relations.add(new ObjectGraph.Relation(
                            ObjectGraph.Relation.RelationType.BIDIR_ASSOCIATION,
                            a.fromId, a.toId,
                            a.labelFormatted(), b.labelFormatted()));
                }
            });
        }


        public void createInheritanceRelations() {

            final Set<ObjectGraph.Relation> inheritanceRelations = new HashSet<>();
            final Set<ObjectGraph.Relation> markedForRemoval = new HashSet<>();

            for(val e1 : objectByLogicalType.entrySet()) {
                for(val e2 : objectByLogicalType.entrySet()) {
                    val type1 = e1.getKey();
                    val type2 = e2.getKey();
                    if(type1.equals(type2)) continue;
                    val cls1 = type1.getCorrespondingClass();
                    val cls2 = type2.getCorrespondingClass();
                    if(cls2.isAssignableFrom(cls1)) {
                        val o1 = e1.getValue();
                        val o2 = e2.getValue();
                        // we found an inheritance relation
                        val relation = new ObjectGraph.Relation(
                                ObjectGraph.Relation.RelationType.INHERITANCE,
                                o1.id, o2.id, "", "");
                        inheritanceRelations.add(relation);
                    }
                }
            }

            // remove any inheritance relations that shortcut others
            outer:
            for(val r1 : inheritanceRelations) {
                for(val r2 : inheritanceRelations) {
                    if(r1==r2) continue;
                    if(!r1.fromId.equals(r2.fromId)) continue;
                    for(val r3 : inheritanceRelations) {
                        if(r1==r3) continue;
                        if(r2==r3) continue;
                        if(!r1.toId.equals(r3.toId)) continue;
                        if(!r2.toId.equals(r3.fromId)) continue;

                        /*
                         * If there exists a non-direct path from [r1.from] to [r1.to],
                         * than r1 needs to be marked for removal.
                         *
                         * It is sufficient to check for paths of length 2 specifically.
                         *
                         * Such a path is found, if following condition is true
                         * [r1.from] == [r2.from]
                         * && [r1.to] == [r3.to]
                         * && [r2.to] == [r3.from].
                         */

                        markedForRemoval.add(r1);
                        continue outer;
                    }
                }

            }

            relations.addAll(_Sets.minus(inheritanceRelations, markedForRemoval));
        }

    }

    @Value
    private static class Relation {
        public static enum RelationType {
            ONE_TO_ONE,
            ONE_TO_MANY,
            MERGED_ASSOCIATIONS,
            BIDIR_ASSOCIATION,
            INHERITANCE;
            public boolean isAssociation() { return this!=INHERITANCE; }
        }
        private final RelationType relationType;
        private final String fromId;
        private final String toId;
        private final String label;
        private final String label2;
        public String labelFormatted() {
            return relationType==RelationType.ONE_TO_MANY
                    ? String.format("[%s]", label)
                    : label;
        }
        public boolean isAssociation() { return relationType.isAssociation(); }
        public String render() {
            switch(relationType) {
            case ONE_TO_ONE:
            case ONE_TO_MANY:
            case MERGED_ASSOCIATIONS:
                return String.format("%s -> \"%s\" %s", fromId, labelFormatted(), toId);
            case BIDIR_ASSOCIATION:
                return String.format("%s \"%s\" -- \"%s\" %s", fromId, label, label2, toId);
            case INHERITANCE:
                return String.format("%s --|> %s", fromId, toId);
            }
            throw _Exceptions.unmatchedCase(relationType);
        }
    }

    @Value
    public static class Object {

        @Value
        public static class Field {

            public static Field forAss(final ObjectAssociation ass) {
                return new Field(ass.getId(), _DiagramUtils.objectShortName(ass.getElementType()), ass.isOneToManyAssociation());
            }

            private final String name;
            private final String elementTypeShortName;
            private final boolean isPlural;

            String render() {
                return isPlural()
                        ? String.format("%s: [%s]", name, elementTypeShortName)
                        : String.format("%s: %s", name, elementTypeShortName);
            }
        }

        private final String id;
        private final String packageName;
        private final String name;
        private final Optional<String> stereotype;
        private final List<ObjectGraph.Object.Field> fields = new ArrayList<>();

        String render() {
            val sb = new StringBuilder()
                .append(String.format("object %s as %s",
                    _DiagramUtils.doubleQuoted(name),
                    stereotype
                        .map(stp->String.format("%s <<%s>>", id, stp)).orElse(id)))
                .append('\n');

            fields.forEach(field->{
                sb.append(id + " : " + field.render()).append('\n');
            });
            return sb.toString();
        }
    }

    // -- IMPL

    private final ObjectGraph.Context context = new ObjectGraph.Context();

    public ObjectGraph.Object registerObject(final ObjectSpecification objSpec) {
        return context.registerObject(objSpec);
    }

    public String render() {

        val sb = new StringBuilder();

        sb.append("left to right direction\n"); // arranges packages vertically

        // group objects by package
        context.logicalTypesByNamespace.forEach((namespace, logicalTypes)->{

            // package start
            sb.append("package ").append(namespace).append(" {\n");

            logicalTypes.stream().map(context.objectByLogicalType::get)
                    .map(ObjectGraph.Object::render)
                    .forEach(s->sb.append(s).append('\n'));

            // package end
            sb.append("}\n");

        });

        context.consolidateAssociationRelations();
        context.createInheritanceRelations();

        context.getRelations().stream()
            .map(ObjectGraph.Relation::render)
            .forEach(s->sb.append(s).append('\n'));

        val plantuml = sb.toString();

        //debug
//        System.err.println("--------PLANTUML------");
//        System.err.printf("%s%n", plantuml);
//        System.err.println("----------------------");

        return plantuml;
    }

}

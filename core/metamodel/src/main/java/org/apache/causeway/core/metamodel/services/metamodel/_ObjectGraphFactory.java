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
package org.apache.causeway.core.metamodel.services.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph.Relation;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

/**
 * @implNote implemented for one shot use only (stateful), hence not made public
 */
@lombok.Value
class _ObjectGraphFactory implements ObjectGraph.Factory {

    private final Collection<ObjectSpecification> objectSpecifications;
    private final ObjectGraph objectGraph = new ObjectGraph();
    private final ListMultimap<String, LogicalType> logicalTypesByNamespace = _Multimaps.newListMultimap();
    private final Map<LogicalType, ObjectGraph.Object> objectByLogicalType = new HashMap<>();
    private final Map<String, ObjectGraph.Object> objectById = new HashMap<>();

    @Override
    public final ObjectGraph create() {
        objectSpecifications.forEach(this::registerObject);

        // single use only! (we cannot call this repeatedly, as it adds more and more duplicates)
        objectGraph.relations().addAll(createInheritanceRelations());

        return objectGraph;
    }

    // -- HELPER

    private ObjectGraph.Object registerObject(final ObjectSpecification objSpec) {

        var addFieldsLater = _Refs.booleanRef(false);

        var obj = objectByLogicalType.computeIfAbsent(objSpec.getLogicalType(), logicalType->{
            logicalTypesByNamespace.putElement(logicalType.getNamespace(), logicalType);
            var newObjId = "o" + objectByLogicalType.size();
            var newObj = object(newObjId, objSpec);
            objectById.put(newObjId, newObj);
            addFieldsLater.setValue(true);
            objectGraph.objects().add(newObj);
            return newObj;
        });

        if(addFieldsLater.isTrue()) {
            objSpec.streamAssociations(MixedIn.EXCLUDED)
            .peek(ass->{
                var elementType = ass.getElementType();
                if(elementType.isEntity()
                        || elementType.isAbstract()) {
                    var referencedObj = registerObject(elementType);

                    var thisType = objSpec.getLogicalType();
                    var refType = elementType.getLogicalType();

                    var thisNs = thisType.getNamespace();
                    var refNs = refType.getNamespace();

                    // only register association relations if they don't cross namespace boundaries
                    // in other words: only include, if they share the same namespace
                    if(thisNs.equals(refNs)) {
                        var thisCls = thisType.getCorrespondingClass();
                        var refCls = refType.getCorrespondingClass();
                        if(thisCls.equals(refCls)
                                || !refCls.isAssignableFrom(thisCls)) {
                            // we found a 1-x relation
                            registerRelation(
                                    ass.isOneToOneAssociation()
                                        ? ObjectGraph.RelationType.ONE_TO_ONE
                                        : ObjectGraph.RelationType.ONE_TO_MANY,
                                    obj.id(), referencedObj.id(), ass.getId());
                        }
                    }

                }
            })
            .map(this::fieldForAss)
            .forEach(obj.fields()::add);
        }
        return obj;
    }

    private ObjectGraph.Relation registerRelation(
            final ObjectGraph.RelationType relationType,
            final String fromId,
            final String toId,
            final String description) {
        var relation = new ObjectGraph.Relation(
                relationType,
                objectById.get(fromId),
                objectById.get(toId),
                description,
                "", "");
        objectGraph.relations().add(relation);
        return relation;
    }

    private static ObjectGraph.Object object(final String id, final ObjectSpecification objSpec) {
        var obj =  new ObjectGraph.Object(id,
                objSpec.getLogicalType().getNamespace(),
                objSpec.getLogicalType().getLogicalTypeSimpleName(),
                objSpec.isAbstract()
                    ? Optional.of("abstract")
                    : Optional.empty(),
                Optional.ofNullable(objSpec.getDescription()),
                new ArrayList<>());
        return obj;
    }

    private ObjectGraph.Field fieldForAss(final ObjectAssociation ass) {
        return new ObjectGraph.Field(
                ass.getId(),
                objectShortName(ass.getElementType()),
                ass.isOneToManyAssociation(),
                ass.getStaticDescription());
    }

    private Set<Relation> createInheritanceRelations() {

        final Set<ObjectGraph.Relation> inheritanceRelations = new HashSet<>();
        final Set<ObjectGraph.Relation> markedForRemoval = new HashSet<>();

        for(var e1 : objectByLogicalType.entrySet()) {
            for(var e2 : objectByLogicalType.entrySet()) {
                var type1 = e1.getKey();
                var type2 = e2.getKey();
                if(type1.equals(type2)) continue;
                var cls1 = type1.getCorrespondingClass();
                var cls2 = type2.getCorrespondingClass();
                if(cls2.isAssignableFrom(cls1)) {
                    var o1 = e1.getValue();
                    var o2 = e2.getValue();
                    // we found an inheritance relation
                    var relation = new ObjectGraph.Relation(
                            ObjectGraph.RelationType.INHERITANCE,
                            objectById.get(o1.id()), objectById.get(o2.id()), "", "", "");
                    inheritanceRelations.add(relation);
                }
            }
        }

        // remove any inheritance relations that shortcut others
        outer:
        for(var r1 : inheritanceRelations) {
            for(var r2 : inheritanceRelations) {
                if(r1==r2) continue;
                if(!r1.fromId().equals(r2.fromId())) continue;
                for(var r3 : inheritanceRelations) {
                    if(r1==r3) continue;
                    if(r2==r3) continue;
                    if(!r1.toId().equals(r3.toId())) continue;
                    if(!r2.toId().equals(r3.fromId())) continue;

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

        return _Sets.minus(inheritanceRelations, markedForRemoval);
    }

    private static String objectShortName(final ObjectSpecification objSpec) {
        var simpleName = objSpec.getLogicalType().getLogicalTypeSimpleName();
        return simpleName;
    }

}

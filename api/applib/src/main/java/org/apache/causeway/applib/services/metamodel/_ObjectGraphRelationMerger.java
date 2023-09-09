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
package org.apache.causeway.applib.services.metamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;

import lombok.val;

@lombok.Value
class _ObjectGraphRelationMerger implements ObjectGraph.Transformer {

    @Override
    public ObjectGraph transform(final ObjectGraph objGraph) {

        val objectById = objGraph.objectById();

        final List<ObjectGraph.Relation> relationsToRender = new ArrayList<>(objGraph.relations());
        consolidateAssociationRelations(objectById, relationsToRender);
        consolidateBidirRelations(objectById, relationsToRender);

        val transformed = new ObjectGraph();
        transformed.objects().addAll(objGraph.objects());
        transformed.relations().addAll(relationsToRender);
        return transformed;
    }

    // -- HELPER

    private void consolidateAssociationRelations(
            final Map<String, ObjectGraph.Object> objectById,
            final List<ObjectGraph.Relation> relations) {

        // collect association-relations into a list-multi-map,
        // where each key references a list of relations that need to be merged into one
        final ListMultimap<String, ObjectGraph.Relation> shared = _Multimaps.newListMultimap();
        relations.stream()
            .filter(ObjectGraph.Relation::isAssociation)
            .forEach(ass->{
                shared.putElement(ass.toId() + " " + ass.fromId(), ass);
            });

        relations.removeIf(ObjectGraph.Relation::isAssociation);

        shared.forEach((key, list) -> {
            val merged = list.stream().reduce((a, b)->new ObjectGraph.Relation(
                    ObjectGraph.RelationType.MERGED_ASSOCIATIONS,
                    objectById.get(a.fromId()), objectById.get(a.toId()),
                    a.labelFormatted() + "," + b.labelFormatted(), ""));
            merged.ifPresent(relations::add);
        });

    }

    private void consolidateBidirRelations(
            final Map<String, ObjectGraph.Object> objectById,
            final List<ObjectGraph.Relation> relations) {

        // collect association-relations into a list-multi-map,
        // where each key references a list of relations that need to be merged into one;
        // we are using a sorted key where relation direction does not matter
        final ListMultimap<String, ObjectGraph.Relation> shared = _Multimaps.newListMultimap();
        relations.stream()
            .filter(ObjectGraph.Relation::isAssociation)
            .filter(ass->!ass.toId().equals(ass.fromId())) // exclude self referencing relations
            .forEach(ass->{
                if(ass.fromId().compareTo(ass.toId())>0) {
                    shared.putElement(ass.toId() + " " + ass.fromId(), ass);
                } else {
                    shared.putElement(ass.fromId() + " " + ass.toId(), ass);
                }
            });

        shared.forEach((key, list) -> {
            if(list.size()==2) {
                relations.removeAll(list);
                val a = list.get(0);
                val b = list.get(1);
                relations.add(new ObjectGraph.Relation(
                        ObjectGraph.RelationType.BIDIR_ASSOCIATION,
                        objectById.get(a.fromId()), objectById.get(a.toId()),
                        a.labelFormatted(), b.labelFormatted()));
            }
        });
    }

}

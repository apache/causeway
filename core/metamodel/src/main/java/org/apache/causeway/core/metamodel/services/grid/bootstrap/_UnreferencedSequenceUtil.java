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
package org.apache.causeway.core.metamodel.services.grid.bootstrap;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.AssociationLayoutConfigOptions;
import org.apache.causeway.core.config.metamodel.facets.AssociationLayoutConfigOptions.SequencePolicy;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.experimental.UtilityClass;

/**
 * Honor {@link AssociationLayoutConfigOptions.SequencePolicy}.
 */
@UtilityClass
class _UnreferencedSequenceUtil {

    List<String> sortProperties(
            final CausewayConfiguration config,
            final Stream<OneToOneAssociation> propertyStream) {
        var sequencePolicy = AssociationLayoutConfigOptions.SequencePolicy.forProperty(config);
        return sequencePolicy == SequencePolicy.ALPHABETICALLY
                ? propertyStream
                    .map(ObjectAssociation::getId)
                    .sorted()
                    .collect(Collectors.toList())
                : propertyStream
                    .sorted(ObjectMember.Comparators.byMemberOrderSequence(false))
                    .map(ObjectAssociation::getId)
                    .collect(Collectors.toList());
    }

    List<String> sortCollections(
            final CausewayConfiguration config,
            final Stream<OneToManyAssociation> collectionStream) {
        var sequencePolicy = AssociationLayoutConfigOptions.SequencePolicy.forCollection(config);

        return sequencePolicy == SequencePolicy.ALPHABETICALLY
                ? collectionStream
                    .map(ObjectAssociation::getId)
                    .sorted()
                    .collect(Collectors.toList())
                : collectionStream
                    .sorted(ObjectMember.Comparators.byMemberOrderSequence(false))
                    .map(ObjectAssociation::getId)
                    .collect(Collectors.toList());
    }

}

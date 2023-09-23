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
package org.apache.causeway.core.metamodel.inspect;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_downloadColumnOrderTxtFile.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT  // to avoid caching
)
@ActionLayout(
        describedAs = "Downloads a .columnOrder.txt file for either this object or one of its collections",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "700.2.2"
)
@RequiredArgsConstructor
public class Object_downloadColumnOrderTxtFile {

    private final Object domainObject; // mixee

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_downloadColumnOrderTxtFile> {}

    @Inject SpecificationLoader specificationLoader;

    @MemberSupport public Clob act(
            @Nullable String collectionId
    ) {

        return collectionId == null
                ? standaloneCollectionTxtFile()
                : parentedCollectionTxtFile(collectionId);
    }

    private Clob standaloneCollectionTxtFile() {
        val parentSpec = specificationLoader.loadSpecification(domainObject.getClass());
        val buf = new StringBuilder();

        parentSpec.streamAssociations(MixedIn.INCLUDED)
                .map(ObjectFeature::getId)
                .forEach(assocId -> buf.append(assocId).append("\n"));

        String fileName = String.format("%s.columnOrder.txt", parentSpec.getShortIdentifier());
        String fileContents = buf.toString();
        return newClon(fileName, fileContents);
    }

    private Clob parentedCollectionTxtFile(String collectionId) {
        val parentSpec = specificationLoader.loadSpecification(domainObject.getClass());
        val buf = new StringBuilder();

        val collection = parentSpec.streamCollections(MixedIn.INCLUDED)
                            .filter(x -> Objects.equals(x.getId(), collectionId))
                            .findFirst()
                            .orElseThrow(); // shouldn't happen because of disableAct guard.
        val collectionIdentifier = collection.getFeatureIdentifier();
        val elementType = collection.getElementType();

        elementType.streamAssociations(MixedIn.INCLUDED)
                .filter(ObjectAssociation.Predicates.visibleAccordingToHiddenFacet(collectionIdentifier))
                .filter(ObjectAssociation.Predicates.referencesParent(parentSpec).negate())
                .map(ObjectFeature::getId)
                .forEach(assocId -> buf.append(assocId).append("\n"));

        String fileName = String.format("%s#%s.columnOrder.txt", parentSpec.getShortIdentifier(), collectionId);
        String fileContents = buf.toString();
        return newClon(fileName, fileContents);
    }

    @MemberSupport public List<String> choices0Act() {
        val objectSpec = specificationLoader.loadSpecification(domainObject.getClass());
        return objectSpec.streamCollections(MixedIn.INCLUDED)
                .map(ObjectFeature::getId)
                .collect(Collectors.toList());
    }

    @MemberSupport public String disableAct() {
        return choices0Act().isEmpty() ? "No collections" : null;
    }

    private static Clob newClon(String fileName, String fileContents) {
        return Clob.of(fileName, NamedWithMimeType.CommonMimeType.TXT, fileContents);
    }

}

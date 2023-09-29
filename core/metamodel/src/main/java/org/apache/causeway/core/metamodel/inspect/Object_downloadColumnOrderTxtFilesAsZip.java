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

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.io.ZipUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_downloadColumnOrderTxtFilesAsZip.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT  // to avoid caching
)
@ActionLayout(
        cssClassFa = "fa-download",
        named = "Download .columnOrder.txt files (ZIP)",
        describedAs = "Downloads all the .columnOrder.txt files for this object and its collections, as a zip file",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "700.2.3"
)
@RequiredArgsConstructor
public class Object_downloadColumnOrderTxtFilesAsZip {

    private final Object domainObject; // mixee

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_downloadColumnOrderTxtFilesAsZip> {}

    @Inject SpecificationLoader specificationLoader;

    @MemberSupport public Blob act(final String fileName) {

        val zipBuilder = ZipUtils.zipEntryBuilder();

        addStandaloneEntry(zipBuilder);
        addCollectionEntries(zipBuilder);

        final byte[] zipBytes = zipBuilder.toBytes();
        return Blob.of(fileName, NamedWithMimeType.CommonMimeType.ZIP, zipBytes);
    }

    @MemberSupport public String default0Act() {
        val parentSpec = specificationLoader.loadSpecification(domainObject.getClass());
        return String.format("%s.columnOrder.zip", parentSpec.getShortIdentifier());
    }


    // HELPERS

    private void addStandaloneEntry(final ZipUtils.EntryBuilder zipBuilder) {
        val parentSpec = specificationLoader.loadSpecification(domainObject.getClass());
        val buf = new StringBuilder();

        parentSpec.streamAssociations(MixedIn.INCLUDED)
                .map(ObjectFeature::getId)
                .forEach(assocId -> buf.append(assocId).append("\n"));

        val fileName = String.format("%s.columnOrder.txt", parentSpec.getShortIdentifier());
        val fileContents = buf.toString();

        zipBuilder.addAsUtf8(fileName, fileContents);
    }

    private void addCollectionEntries(final ZipUtils.EntryBuilder zipBuilder) {
        val parentSpec = specificationLoader.loadSpecification(domainObject.getClass());
        parentSpec.streamCollections(MixedIn.INCLUDED)
                .forEach(collection -> addCollection(collection, parentSpec, zipBuilder));
    }

    private void addCollection(final OneToManyAssociation collection, final ObjectSpecification parentSpec, final ZipUtils.EntryBuilder zipBuilder) {
        val buf = new StringBuilder();

        val collectionIdentifier = collection.getFeatureIdentifier();
        val elementType = collection.getElementType();

        elementType.streamAssociations(MixedIn.INCLUDED)
                .filter(ObjectAssociation.Predicates.visibleAccordingToHiddenFacet(collectionIdentifier))
                .filter(ObjectAssociation.Predicates.referencesParent(parentSpec).negate())
                .map(ObjectFeature::getId)
                .forEach(assocId -> buf.append(assocId).append("\n"));

        val fileName = String.format("%s#%s.columnOrder.txt", parentSpec.getShortIdentifier(), collection.getId());
        val fileContents = buf.toString();

        zipBuilder.addAsUtf8(fileName, fileContents);
    }



}

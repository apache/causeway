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
package org.apache.causeway.applib.services.columnorder;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;

import lombok.RequiredArgsConstructor;

/**
 * This is a utility mixin to support the usage of {@link TableColumnOrderService}, providing the ability to obtain
 * a zip of each of the <code>Xxx.columnOrder.txt</code> files for the specified domain object.  It obtains this zip
 * from the {@link ColumnOrderTxtFileService} domain service.
 *
 * <p>
 * The zip contains:
 * <ul>
 *     <li>DomainClass.columnOrder.txt</li> - as used for standalone collections of <code>DomainClass</code> itself
 *     <li>DomainClass#collection1.columnOrder.txt</li> - for <code>DomainClass</code>' collection with id <code>collection1</code>.
 *     <li>...</li>
 *     <li>DomainClass#collectionN.columnOrder.txt</li> - for <code>DomainClass</code>' collection with id <code>collectionN</code>.
 * </ul>
 * </p>
 *
 * <p>
 * These should be unzipped and copied in the domain class' package, and then their contents updated to specify the
 * order in which the respective object's properties will be shown in the standalone or parented collections.
 * </p>
 *
 * @see TableColumnOrderService
 * @see ColumnOrderTxtFileService
 */
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

    @MemberSupport public Blob act(final String fileName) {
        final byte[] zipBytes = columnOrderTxtFileService.toZip(domainObject);
        return Blob.of(fileName, NamedWithMimeType.CommonMimeType.ZIP, zipBytes);
    }

    @MemberSupport public String default0Act() {
        return String.format("%s.columnOrder.zip", domainObject.getClass().getSimpleName());
    }

    @Inject ColumnOrderTxtFileService columnOrderTxtFileService;

}

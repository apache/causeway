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
package org.apache.causeway.core.metamodel.services.tablecol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderService;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Provides a simple mechanism to order the columns of both parented and standalone collections by reading a flat
 * file containing the list of the associations (usually properties, but collections are also supported), in the
 * desired order, one associationId per line.
 *
 * <p>
 * The files are located relative to the class itself.  A number of conventions are supported:
 *
 * <ul>
 *     <li>
 *         for parented collections:
 *         <ul>
 *             <li> <code>ParentClassName#collectionId.columnOrder.txt</code> </li>
 *             <li> <code>ParentClassName#collectionId.columnOrder.fallback.txt</code> </li>
 *             <li> <code>ParentClassName#_.ElementTypeClassName.columnOrder.txt</code>
 *             <p>
 *                 (where '_' is a wildcard for any collection of the element type
 *             </p>
 *             </li>
 *             <li> <code>ParentClassName#_.ElementTypeClassName.columnOrder.fallback.txt</code></li>
 *             <li> <code>ElementTypeClassName.columnOrder.txt</code></li>
 *             <li> <code>ElementTypeClassName.columnOrder.fallback.txt</code></li>
 *         </ul>
 *     </li>
 *     <li>
 *         for standalone collections:
 *         <ul>
 *             <li> <code>DomainTypeClassName.columnOrder.txt</code></li>
 *             <li> <code>DomainTypeClassName.columnOrder.fallback.txt</code></li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>
 * Any associations omitted from the file will not be shown as columns of the table.  The associationId must also
 * be an exact match, so can be ignored by commenting out, eg with &quot;#&quot;.
 * </p>
 *
 * <p>
 *     Also note that association that have been explicitly hidden from tables using
 *     {@link PropertyLayout#hidden() @PropertyLayout#hidden} or {@link CollectionLayout#hidden()} are never shown,
 *     irrespective of whether they are listed in the files.  You may therefore prefer to <i>not</i> hide properties
 *     with annotations, and then rely solely on these external <i>columnOrder.txt</i> files.  This has the further
 *     benefit that files can be modified at runtime and will be automatically picked up without requiring a restart
 *     of the application.
 * </p>
 *
 * @see TableColumnOrderServiceDefault
 *
 * @since 2.x {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".TableColumnOrderServiceUsingTxtFile")
@Priority(PriorityPrecedence.LATE - 100) // before Default
@Qualifier("UsingFiles")
@Log4j2
public class TableColumnOrderServiceUsingTxtFile implements TableColumnOrderService {

    /**
     * Reads association Ids of the parented collection from a file.
     *
     * <p>
     * The search algorithm is:
     * <ul>
     *     <li> <code>ParentClassName#collectionId.columnOrder.txt</code> </li>
     *     <li> <code>ParentClassName#collectionId.columnOrder.fallback.txt</code> </li>
     *     <li> <code>ParentClassName#_.ElementTypeClassName.columnOrder.txt</code>
     *     <p>
     *         (where '_' is a wildcard for any collection of the element type
     *     </p>
     *     </li>
     *     <li> <code>ParentClassName#_.ElementTypeClassName.columnOrder.fallback.txt</code></li>
     *     <li> <code>ElementTypeClassName.columnOrder.txt</code></li>
     *     <li> <code>ElementTypeClassName.columnOrder.fallback.txt</code></li>
     * </ul>
     * </p>
     *
     * <p>
     * Additional files can be provided by overriding {@link #addResourceNames(Class, String, Class, List)}
     * </p>
     * 
     * @Returns {@code null}, if no matching resource was found
     */
    @Nullable
    @Override
    public List<String> orderParented(
            final Object domainObject,
            final String collectionId,
            final Class<?> elementType,
            final List<String> associationIds) {

        var domainClass = domainObject.getClass();
        var resourceNames = buildResourceNames(domainClass, collectionId, elementType);
        addResourceNames(elementType, resourceNames);   // fallback to reading the element type's own .txt file.
        var contents = tryLoad(domainClass, resourceNames)
                .orElse(null);
        return contentsMatching(contents, associationIds);
    }

    private List<String> buildResourceNames(
            final Class<?> domainClass,
            final String collectionId,
            final Class<?> elementType) {
        var resourceNames = new ArrayList<String>();
        addResourceNames(domainClass, collectionId, elementType, resourceNames);
        return resourceNames;
    }

    /**
     * Builds the list of file names to be read from.
     *
     * <p>
     * The default implementation provides only a single file name, <i>ClassName#collectionId.columnOrder.txt</i>.
     * </p>
     *
     * @param domainClass  - the class with the parent collection
     * @param collectionId - the id of the collection
     * @param addTo        - to be added to
     */
    protected void addResourceNames(
            final Class<?> domainClass,
            final String collectionId,
            final Class<?> elementType,
            final List<String> addTo) {
        addTo.add(String.format("%s#%s.columnOrder.txt", domainClass.getSimpleName(), collectionId));
        addTo.add(String.format("%s#%s.columnOrder.fallback.txt", domainClass.getSimpleName(), collectionId));
        addTo.add(String.format("%s#_.%s.columnOrder.txt", domainClass.getSimpleName(), elementType.getSimpleName()));
        addTo.add(String.format("%s#_.%s.columnOrder.fallback.txt", domainClass.getSimpleName(), elementType.getSimpleName()));
    }

    private static Optional<String> tryLoad(final Class<?> domainClass, final List<String> resourceNames) {
        for (String resourceName : resourceNames) {
            try {
                final String contents = _Resources.loadAsStringUtf8(domainClass, resourceName);
                if (contents != null) {
                    return Optional.of(contents);
                }
            } catch (Exception ignore) {
                // in most cases there won't be a file to load, so we just continue.
                // not an error condition, but we'll log it at lowest (trace) level.
                if (log.isTraceEnabled()) {
                    log.trace("No resource file '{}' relative to {}", resourceName, domainClass.getName());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Reads associationIds of a standalone collection from a file.
     *
     * <p>
     * The search algorithm is:
     * <ul>
     *     <li> <code>DomainTypeClassName.columnOrder.txt</code></li>
     *     <li> <code>DomainTypeClassName.columnOrder.fallback.txt</code></li>
     * </ul>
     * </p>
     *
     * <p>
     * Additional files can be provided by overriding {@link #addResourceNames(Class, List)}.
     * </p>
     * 
     * @Returns {@code null}, if no matching resource was found
     */
    @Nullable
    @Override
    public List<String> orderStandalone(
            final Class<?> domainType,
            final List<String> associationIds) {
        var resourceNames = buildResourceNames(domainType);
        var contents = tryLoad(domainType, resourceNames)
                .orElse(null);
        return contentsMatching(contents, associationIds);
    }

    private List<String> buildResourceNames(final Class<?> domainClass) {
        var resourceNames = new ArrayList<String>();
        addResourceNames(domainClass, resourceNames);
        return resourceNames;
    }

    /**
     * Builds the list of file names to be read from.
     *
     * <p>
     * The default implementation provides only a single file name, <i>ClassName#collectionId.columnOrder.txt</i>.
     * </p>
     *
     * @param domainClass - the class in the standalone collection
     * @param addTo       - to be added to
     */
    protected void addResourceNames(
            final Class<?> domainClass,
            final List<String> addTo) {
        addTo.add(String.format("%s.columnOrder.txt", domainClass.getSimpleName()));
        addTo.add(String.format("%s.columnOrder.fallback.txt", domainClass.getSimpleName()));
    }

    /**
     * if contents is {@code null} returns {@code null}
     */
    @Nullable
    private static List<String> contentsMatching(
            final @Nullable String contents,
            final @NonNull List<String> associationIds) {
        return contents==null
                ? null
                : TextUtils.readLines(contents).stream()
                    .map(String::trim) // ignore any leading or trailing whitespace
                    .filter(line->!line.startsWith("#")) // speed up (not strictly required)
                    .filter(associationIds::contains)
                    .collect(Collectors.toList());
    }

}

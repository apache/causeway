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
package org.apache.isis.core.metamodel.services.tablecol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.tablecol.TableColumnOrderService;
import org.apache.isis.commons.internal.resources._Resources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * Provides a simple mechanism to order the columns of both parented and standalone collections by reading a flat
 * file containing the list of the properties in the desired order, one propertyId per line.
 *
 * <p>
 * The files are located relative to the class itself.  For parented collections, the file is named
 * <i>ParentClassName#collectionId.columnOrder.txt</i>, while for standalone collections, the file is named
 * <i>ClassName.columnOrder.txt</i>.
 * </p>
 *
 * <p>
 * Any properties omitted from the file will not be shown as columns of the table.  The propertyId must also
 * be an exact match, so can be ignored by commenting out, eg with &quot;#&quot;.
 * </p>
 *
 * <p>
 *     Also note that properties that have been explicitly hidden from tables using
 *     {@link PropertyLayout#hidden() @PropertyLayout#hidden} are never shown, irrespective of whether they are listed
 *     in the files.  You may therefore prefer to <i>not</i> hide properties with annotations, and then rely solely
 *     on these external <i>columnOrder.txt</i> files.  This has the further benefit that files can be modified at
 *     runtime and will be automatically picked up without requiring a restart of the application.
 * </p>
 *
 * @since 1.x {@index}
 */
@Service
@Named("isis.metamodel.TableColumnOrderServiceUsingTxtFile")
@Priority(PriorityPrecedence.LATE - 100) // before Default
@Qualifier("UsingFiles")
@Log4j2
public class TableColumnOrderServiceUsingTxtFile implements TableColumnOrderService {

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Reads propertyIds of the collection from a file named <i>ClassName#collectionId.columnOrder.txt</i>. relative
     * to the class itself.
     *
     * <p>
     * Additional files can be provided by overriding {@link #addResourceNames(Class, String, List)}.
     * </p>
     */
    @Override
    public List<String> orderParented(
            final Object domainObject,
            final String collectionId,
            final Class<?> collectionType,
            final List<String> propertyIds) {

        val domainClass = domainObject.getClass();
        val resourceNames = buildResourceNames(domainClass, collectionId);
        val contentsIfAny = tryLoad(domainClass, resourceNames);
        if(!contentsIfAny.isPresent()) {
            return null;
        }
        val s = contentsIfAny.get();
        return Arrays.stream(s.split(LINE_SEPARATOR))
                .filter(propertyIds::contains)
                .collect(Collectors.toList());
    }

    private List<String> buildResourceNames(Class<?> domainClass, String collectionId) {
        val resourceNames = new ArrayList<String>();
        addResourceNames(domainClass, collectionId, resourceNames);
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
            final List<String> addTo) {
        addTo.add(String.format("%s#%s.columnOrder.txt", domainClass.getSimpleName(), collectionId));
    }

    private static Optional<String> tryLoad(Class<?> domainClass, List<String> resourceNames) {
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
     * Reads propertyIds of the standalone collection from a file named <i>ClassName.columnOrder.txt</i>, relative
     * to the class itself.
     *
     * <p>
     * Additional files can be provided by overriding {@link #addResourceNames(Class, List)}.
     * </p>
     */
    @Override
    public List<String> orderStandalone(
            final Class<?> domainClass,
            final List<String> propertyIds) {
        val resourceNames = buildResourceNames(domainClass);
        val contentsIfAny = tryLoad(domainClass, resourceNames);
        if(!contentsIfAny.isPresent()) {
            return null;
        }
        val s = contentsIfAny.get();
        return Arrays.stream(s.split(LINE_SEPARATOR))
                .filter(propertyIds::contains)
                .collect(Collectors.toList());
    }

    private List<String> buildResourceNames(Class<?> domainClass) {
        val resourceNames = new ArrayList<String>();
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
    }

}

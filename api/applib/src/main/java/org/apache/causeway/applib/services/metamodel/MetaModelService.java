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

import java.util.Optional;
import java.util.function.BiPredicate;

import javax.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.metamodel.v2.MetamodelDto;

import lombok.NonNull;

/**
 * This service provides a formal API into the framework's metamodel.
 *
 * @apiNote This API is currently extremely limited, but the intention is to extend it
 * gradually as use cases emerge.
 *
 * @since 1.x {@index}
 */
public interface MetaModelService {

    /**
     * Provides a lookup by logicalTypeName of a domain class' object type, corresponding to
     * {@link Named#value()} or {@link DomainService#aliased()} or
     * {@link DomainObject#aliased()}.
     * Will return an empty result if there is no
     * such non-abstract class registered.
     * (interfaces and abstract types are never added to the lookup table).
     */
    Optional<LogicalType> lookupLogicalTypeByName(final String logicalTypeName);

    /**
     * Assuming that the {@link LogicalType} passed in actually represents a domain type, then
     * returns it along with any aliases defined as per
     * {@link DomainService#aliased()} or {@link DomainObject#aliased()}.
     *
     * <p>
     *     If there is no such domain type, then an empty {@link Can} will be returned.
     * </p>
     *
     * @param logicalType
     */
    Can<LogicalType> logicalTypeAndAliasesFor(final LogicalType logicalType);

    /**
     * Returns the {@link LogicalType} of a domain class' object type, corresponding to {@link Named#value()},
     * along with any aliases defined as per
     * {@link DomainService#aliased()} or {@link DomainObject#aliased()}.
     *
     * <p>
     *     If there is no such domain type, then an empty {@link Can} will be returned.
     * </p>
     */
    Can<LogicalType> logicalTypeAndAliasesFor(final String logicalTypeName);

    /**
     * Provides a lookup by class of a domain class' object type,corresponding to
     * {@link Named#value()} or {@link DomainService#aliased()} or
     * {@link DomainObject#aliased()}.
     */
    Optional<LogicalType> lookupLogicalTypeByClass(final Class<?> domainType);

    /**
     * Invalidates and rebuilds the internal metadata for the specified domain
     * type.
     */
    void rebuild(final Class<?> domainType);

    /**
     * Returns a list of representations of each of member of each domain class.
     *
     * <p>
     *     Used by {@link MetaModelServiceMenu} to return a downloadable CSV.
     * </p>
     *
     * <p>
     *     Note that {@link MetaModelService#exportMetaModel(Config)} provides a superset of
     *     the functionality provided by this method.
     * </p>
     *
     */
    DomainModel getDomainModel();

    /**
     * What sort of object the domain type represents.
     *
     * @param domainType
     * @param mode
     */
    BeanSort sortOf(@Nullable Class<?> domainType, Mode mode);

    /**
     * Override of {@link #sortOf(Class, Mode)}, extracting the domain type
     * from the provided {@link Bookmark} of a domain object instance.
     *
     * @param bookmark
     * @param mode
     */
    BeanSort sortOf(Bookmark bookmark, Mode mode);

    /**
     * Obtains the implementation of {@link CommandDtoProcessor} (if any) as
     * per {@link Action#commandDtoProcessor()} or
     * {@link Property#commandDtoProcessor()}.
     *
     * <p>
     *     This is used by framework-provided implementations of
     *     {@link org.apache.causeway.applib.services.conmap.ContentMappingService}.
     * </p>
     */
    CommandDtoProcessor commandDtoProcessorFor(
                            String logicalMemberIdentifier);

    /**
     * How {@link MetaModelService#sortOf(Class, Mode)} should act if an object
     * type is unknown.
     */
    enum Mode {
        /**
         * If the {@link #sortOf(Class, Mode) sort of} object type is unknown, then throw an exception.
         */
        STRICT,
        /**
         * If the {@link #sortOf(Class, Mode) sort of} object type is unknown, then return {@link BeanSort#UNKNOWN}.
         */
        RELAXED
    }

    /**
     * Exports the entire metamodel as a DTO, serializable into XML using JAXB.
     *
     * <p>
     *     The {@link Config} parameter can be used to restrict/filter the
     *     export to some subset of the metamodel; in particular to specific
     *     {@link Config#getNamespacePrefixes() namespace prefixes}.
     * </p>
     *
     * @param config - restricts/filters to a subsets of the metamodel.
     */
    MetamodelDto exportMetaModel(final Config config);

    /**
     * Can be used to create object relation diagrams (e.g. Plantuml).
     *
     * @param filter by {@link BeanSort} and {@link LogicalType} what to include in the resulting graph
     */
    ObjectGraph exportObjectGraph(final @NonNull BiPredicate<BeanSort, LogicalType> filter);

}

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

import java.util.Set;

import org.apache.causeway.commons.internal.collections._Sets;

import lombok.Builder;
import lombok.Getter;
import lombok.val;


/**
 * Describes what to include in the export from
 * {@link MetaModelService#exportMetaModel(Config)}.
 *
 * @since 1.x {@index}
 */
@Getter @Builder
public class Config {

    private final boolean ignoreFallbackFacets;
    private final boolean ignoreInterfaces;
    private final boolean ignoreAbstractClasses;
    private final boolean ignoreBuiltInValueTypes;
    private final boolean ignoreMixins;
    private final boolean includeShadowedFacets;
    private final boolean includeTitleAnnotations;

    @Builder.Default
    private final Set<String> namespacePrefixes = _Sets.newHashSet();

    public boolean isNamespacePrefixAny() {
        return namespacePrefixes.isEmpty()
                || namespacePrefixes.contains("*");
    }

    /**
     * Returns a copy of this config with given namespace added.
     */
    public Config withNamespacePrefix(final String namespace) {
        val newConfig = asBuilder().build();
        newConfig.namespacePrefixes.add(namespace);
        return newConfig;
    }

    public Config.ConfigBuilder asBuilder() {
        return Config.builder()
                .ignoreFallbackFacets(ignoreFallbackFacets)
                .ignoreInterfaces(ignoreInterfaces)
                .ignoreAbstractClasses(ignoreAbstractClasses)
                .ignoreBuiltInValueTypes(ignoreBuiltInValueTypes)
                .ignoreMixins(ignoreMixins)
                .includeShadowedFacets(includeShadowedFacets)
                .includeTitleAnnotations(includeTitleAnnotations)
                .namespacePrefixes(_Sets.newHashSet(namespacePrefixes));
    }


}

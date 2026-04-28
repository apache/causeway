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
package org.apache.causeway.applib.services.grid;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Strings;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Provides the ability to load the XML layout (grid) for a domain class.
 *
 * @since 1.x {@index}
 */
public interface GridService {
	
	@Getter @Accessors(fluent = true)
	@EqualsAndHashCode
	@ToString
    public static final class LayoutKey implements Serializable {
            private static final long serialVersionUID = 6793668843769895126L;
            
			private final @NonNull Class<?> domainClass;
            /**
             *<p>The optional layout name can for example be returned by the
             * domain object's <code>layout()</code> method, whereby - based on the
             * state of the domain object - it requests a different layout be used.
             */
			private final @Nullable String layoutIfAny; 

            public LayoutKey(Class<?> domainClass, String layoutIfAny) {
            	this.domainClass = domainClass;
                this.layoutIfAny = _Strings.emptyToNull(layoutIfAny);
            }
            public LayoutKey(final Class<?> domainClass) {
                this(domainClass, null);
            }

            public boolean isDefault() { return layoutIfAny==null; }
            public boolean isVariant() { return layoutIfAny!=null; }
            
            /**
             * Suggested resource name e.g. for export. (not strictly binding)
             */
            public String resourceName(CommonMimeType commonMimeType) {
            	return String.format("%s%s.%s",
            			domainClass.getSimpleName(),
        				isVariant()
        					? "-" + layoutIfAny()
        					: "",
    					commonMimeType.getProposedFileExtensions().getFirst().orElse("unknown"));
            }
	}

    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p> The default implementation enables reloading while prototyping, disables in production.
     */
    boolean supportsReloading();

    /**
     * To support metamodel invalidation/rebuilding of spec.
     *
     * <p> Acts as a no-op if not {@link #supportsReloading()}.
     */
    void invalidate(Class<?> domainClass);
    
    /**
     * Allows to replace or prime layout caches with a custom layout (as provided by given {@link LayoutResource}). 
     * Useful for prototyping.
     *  
     * <p>This patching is potentially in conflict with layout reloading. 
     * Patched layouts must stick around even when a layout reload attempt is made.
     * 
     * @since 4.0
     */
    void addPatchedLayout(LayoutKey layoutKey, LayoutResource layoutResource);

    /**
     * Returns a {@link BSGrid} for given {@link LayoutKey}.
     *
     * <p>The default implementation uses the layout name to search for a differently
     * named layout file, <code>[domainClass].layout.[layout].xml</code>.
     *
     * <p>When no specific grid layout is found returns a generic fallback.
     */
    BSGrid load(LayoutKey layoutKey);

    EnumSet<CommonMimeType> supportedFormats();
    Optional<GridMarshaller> marshaller(CommonMimeType format);

    /**
     * Clears any pre-calculated BSGrid instances from the cache. Useful to start with a clean slate,
     * after the MM was initialized.
     *
     * <p> The MM needs to be aware of all mixins, in order to reliably produce valid BSGrid instances.
     * During MM initialization incomplete BSGrid instances might be created and cached,
     * which are missing e.g. mixed-in Actions.
     */
	void clearCache();

}

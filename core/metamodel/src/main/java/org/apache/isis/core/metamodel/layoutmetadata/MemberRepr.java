/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layoutmetadata;

import java.util.Map;

/**
 * Representation of properties, collections or free-standing actions.
 * 
 * <p>
 * Actions associated with members are represented by {@link ActionRepr}. 
 */
public class MemberRepr {
    
    public Map<String,ActionRepr> actions;

    public PropertyLayoutFacetRepr propertyLayout;
    public CollectionLayoutFacetRepr collectionLayout;


    // properties or collections

    /**
     * @deprecated - use instead {@link #propertyLayout} or {@link #collectionLayout}
     */
    @Deprecated
    public CssClassFacetRepr cssClass;

    /**
     * @deprecated - use instead {@link #propertyLayout} or {@link #collectionLayout}
     */
    @Deprecated
    public DescribedAsFacetRepr describedAs;

    /**
     * @deprecated - use instead {@link #propertyLayout} or {@link #collectionLayout}
     */
    @Deprecated
    public NamedFacetRepr named;

    /**
     * @deprecated - use instead {@link #propertyLayout} or {@link #collectionLayout}
     */
    @Deprecated
    public DisabledFacetRepr disabled;

    /**
     * @deprecated - use instead {@link #propertyLayout} or {@link #collectionLayout}
     */
    @Deprecated
    public HiddenFacetRepr hidden;


    // properties

    /**
     * @deprecated - use instead {@link #propertyLayout}
     */
    @Deprecated
    public MultiLineFacetRepr multiLine;

    /**
     * @deprecated - use instead {@link #propertyLayout}
     */
    @Deprecated
    public TypicalLengthFacetRepr typicalLength;

    // collections

    /**
     * @deprecated - use instead {@link #collectionLayout}
     */
    @Deprecated
    public PagedFacetRepr paged;

    /**
     * @deprecated - use instead {@link #collectionLayout}
     */
    @Deprecated
    public RenderFacetRepr render;

}

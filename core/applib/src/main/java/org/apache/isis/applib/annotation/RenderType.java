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
package org.apache.isis.applib.annotation;

import javax.xml.bind.annotation.XmlType;

/**
 * @deprecated - use {@link CollectionLayout#defaultView()} set to &quot;table&quot; instead.
 */
@XmlType(
        namespace = "http://isis.apache.org/applib/layout/component"
)
@Deprecated
public enum RenderType {
    EAGERLY,
    LAZILY;

    @Deprecated
    public static Render.Type typeOf(final RenderType renderType) {
        if(renderType == null) return null;
        if(renderType == EAGERLY) return Render.Type.EAGERLY;
        if(renderType == LAZILY) return Render.Type.LAZILY;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized renderType: " + renderType);
    }

    @Deprecated
    public static RenderType typeOf(final Render.Type renderType) {
        if(renderType == null) return null;
        if (renderType == Render.Type.EAGERLY) return RenderType.EAGERLY;
        if (renderType == Render.Type.LAZILY) return RenderType.LAZILY;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized renderType: " + renderType);
    }
}

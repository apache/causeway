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

package org.apache.isis.viewer.html.image;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Factors out the {@link ImageLookup} responsibilities into an interface, with
 * a view to moving towards alternative mechanisms.
 */
public interface ImageProvider {

    public void debug(final DebugBuilder debug);

    /**
     * For an object, the icon name from the object is return if it is not null,
     * otherwise the specification is used to look up a suitable image name.
     * 
     * @see ObjectAdapter#getIconName()
     * @see #image(ObjectSpecification)
     */
    public String image(final ObjectAdapter object);

    public String image(final ObjectSpecification specification);

    public String image(final String name);

}

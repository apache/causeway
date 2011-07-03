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
package org.apache.isis.viewer.json.viewer;

import java.util.Arrays;

import org.apache.isis.viewer.json.viewer.representations.Representation;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * The context within which this representation is being requested.
 * 
 * <p>
 * Part of the context is the overall {@link ResourceContext}, which incorporates
 * such things as the current user, and HTTP headers.
 * 
 * <p>
 * The other part of the context is an indication of the attribute that this
 * representation will be keyed under.  This is required in order that
 * 'rel' links for attributes correctly concatenate.  It is also used to
 * infer whether member representations (which appear in summary form in
 * the {@link Representation} and in more detail in their own resources)
 * should include a _self attribute or not.
 */
public class RepContext {

    private final ResourceContext resourceContext;
    private final String attribute;

    public RepContext(ResourceContext resourceContext, String attribute) {
        this.resourceContext = resourceContext;
        this.attribute = attribute;
    }

    public String urlFor(String url) {
        return resourceContext.getUriInfo().getBaseUri().toString() + url;
    }

    public String relFor(String relSuffix) {
        return Joiner.on(".").join( 
            Iterables.filter(Arrays.asList(attribute, relSuffix), nonNulls())); 
    }

    private static <T> Predicate<T> nonNulls() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return input != null;
            }
        };
    }

    public boolean hasAttribute() {
        return attribute != null;
    }

    /**
     * Returns a new {@link RepContext} with a different attribute.
     */
    public RepContext underAttribute(String attribute) {
        return resourceContext.repContext(attribute);
    }
}

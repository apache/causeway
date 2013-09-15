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

package org.apache.isis.core.metamodel.adapter.oid;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;


/**
 * Defines a subtype of {@link Oid} specific to a root adapter.
 * 
 * <p>
 * The root adapter/pojo can be recreated with no further information; the
 * {@link #getObjectSpecId()} can be used to fetch the corresponding
 * {@link ObjectSpecification} using {@link SpecificationLoaderSpi#lookupBySpecId(String)}.
 * 
 * <p>
 * <p>
 * As such, is directly akin to the DSP's oid that is of the form 
 * <tt>CUS|1234567A</tt>, where the overall form is a simple string
 * and also identifies the type of the object.
 *
 * <p>
 * In addition, can be directly encoded/decoded into strings.  The {@link #enString(OidMarshaller)} interface
 * is defined in the interface; implementations must also provide a static 
 * <tt>deString(String)</tt> factory method.
 */
public interface RootOid extends TypedOid {

    String getIdentifier();
    
    void setVersion(Version version);


    /**
     * Returns a new RootOid for the same {@link #getObjectSpecId()}, but persistent and with the specified {@link #getIdentifier() identifier}.
     */
    RootOid asPersistent(String identifier);

    /**
     * The result of performing a {@link #compareTo(Comparison) comparison.}
     */
    public static enum Comparison {
        /**
         * The two {@link RootOid oid}s identify the same domain object,
         * (meaning {@link RootOid#equals(Object) equals} method returns <tt>true</tt>)
         * and the {@link RootOid#getVersion() version information} held within the 
         * oid is the same.
         */
        EQUIVALENT_AND_UNCHANGED,
        /**
         * The two {@link RootOid oid}s identify the same domain object,
         * (meaning {@link RootOid#equals(Object) equals} method returns <tt>true</tt>)
         * but the {@link RootOid#getVersion() version information} held within the 
         * oid is the different.
         */
        EQUIVALENT_BUT_CHANGED,
        /**
         * The two {@link RootOid oid}s identify the same domain object,
         * (meaning {@link RootOid#equals(Object) equals} method returns <tt>true</tt>)
         * but there is no {@link RootOid#getVersion() version information} so 
         * cannot determine whether the domain object has changed.
         */
        EQUIVALENT_BUT_NO_VERSION_INFO,
        /**
         * The two {@link RootOid oid}s identify the different domain objects,
         * (meaning {@link RootOid#equals(Object) equals} method returns <tt>false</tt>).
         * 
         * <p>
         */
        NOT_EQUIVALENT,
    }
    
    Comparison compareAgainst(RootOid oid2);

    Bookmark asBookmark();


}

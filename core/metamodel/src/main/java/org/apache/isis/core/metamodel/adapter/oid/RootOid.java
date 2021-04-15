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

import java.util.Optional;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.codec._UrlDecoderUtil;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.val;

public interface RootOid extends Oid {

    String getIdentifier();

    Bookmark asBookmark();

    // -- DECODE FROM STRING

    public static RootOid deStringEncoded(final String urlEncodedOidStr) {
        final String oidStr = _UrlDecoderUtil.urlDecode(urlEncodedOidStr);
        return deString(oidStr);
    }

    public static RootOid deString(final String oidStr) {
        return Oid.unmarshaller().unmarshal(oidStr, RootOid.class);
    }

    // -- OBJECT LOADING
    
    default public Optional<ManagedObject> loadObject(final @NonNull MetaModelContext mmc) {
        
        val objectId = this.getIdentifier();
        val specLoader = mmc.getSpecificationLoader(); 
        val objManager = mmc.getObjectManager();
        
        return specLoader
                .specForLogicalTypeName(this.getLogicalTypeName())
        .map(spec->objManager.loadObject(
                        ObjectLoader.Request.of(spec, objectId)));
        
    }


}

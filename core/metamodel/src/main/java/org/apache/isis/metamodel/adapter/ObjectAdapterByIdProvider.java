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
package org.apache.isis.metamodel.adapter;

import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.metamodel.adapter.oid.RootOid;

/**
 * 
 * @since 2.0
 *
 */
public interface ObjectAdapterByIdProvider {
    
    // -- INTERFACE

	/**
     * This method  will <i>always</i> return an object, possibly indicating it is persistent; so make sure that you
     * know that the oid does indeed represent an object you know exists.
     */
    ObjectAdapter adapterFor(RootOid rootOid);
    
    Map<RootOid, ObjectAdapter> adaptersFor(Stream<RootOid> rootOids);
    
    
    // -- FOR THOSE THAT IMPLEMENT THROUGH DELEGATION
    
    public static interface Delegating extends ObjectAdapterByIdProvider {
        
        @Programmatic
        ObjectAdapterByIdProvider getObjectAdapterByIdProvider();
        
        @Programmatic
        default ObjectAdapter adapterFor(RootOid rootOid) {
            return getObjectAdapterByIdProvider().adapterFor(rootOid);
        }
        
        @Programmatic
        default Map<RootOid, ObjectAdapter> adaptersFor(Stream<RootOid> rootOids) {
            return getObjectAdapterByIdProvider().adaptersFor(rootOids);
        }

        
    }
    

}

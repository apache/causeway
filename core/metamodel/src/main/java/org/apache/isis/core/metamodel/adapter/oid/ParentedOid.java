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

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.version.Version;


/**
 * Base type of the {@link Oid} for collections and for <tt>@Aggregated</tt>
 * types.
 * 
 * @see AggregatedOid
 * @see CollectionOid
 */
public abstract class ParentedOid implements Oid {

    private final TypedOid parentOid;
    
    public ParentedOid(TypedOid parentOid) {
        Assert.assertNotNull("parentOid required", parentOid);
        this.parentOid = parentOid;
    }

    public TypedOid getParentOid() {
        return parentOid;
    }

    @Override
    public Version getVersion() {
        return parentOid.getVersion();
    }

    @Override
    public void setVersion(Version version) {
        parentOid.setVersion(version);
    }


    @Override
    public boolean isTransient() {
        return getParentOid().isTransient();
    }
    
    @Override
    public boolean isViewModel() {
        return getParentOid().isViewModel();
    }
    
    @Override
    public boolean isPersistent() {
        return getParentOid().isPersistent();
    }


    // /////////////////////////////////////////////////////////
    // toString
    // /////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return enString(new OidMarshaller());
    }


}

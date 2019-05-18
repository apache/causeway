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
package org.apache.isis.core.runtime.memento;

import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.ioc.BeanSort;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.Getter;
import lombok.Value;

/**
 * 
 * @since 2.0.0
 * @deprecated FIXME[2112] yet an experiment, not working
 */
@Value(staticConstructor = "of")
public class ObjectAdapterMementoUsingSupport implements ObjectAdapterMemento {

    private static final long serialVersionUID = 1L;
    
    private transient _Lazy<ObjectAdapterMementoSupport> support = 
            _Lazy.threadSafe(ObjectAdapterMementoSupport::current);

    @Getter(onMethod = @__({@Override})) private final UUID storeKey;
    @Getter(onMethod = @__({@Override})) private final BeanSort beanSort;
    @Getter(onMethod = @__({@Override})) private final ObjectSpecId objectSpecId;
    @Getter(onMethod = @__({@Override})) private final RootOid rootOid;

    
    @Override
    public String asString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bookmark asBookmarkIfSupported() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bookmark asHintingBookmarkIfSupported() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectAdapter getObjectAdapter() {
        return support.get().reconstructObjectAdapter(this);
    }
    
    private final static _Probe probe = _Probe.unlimited().label("ObjectAdapterMementoUsingSupport");

    @Override
    public void resetVersion() {
        
        // was only ever required on entities 
        if(beanSort.isEntity()) {
            probe.warnNotImplementedYet("resetVersion() was removed with 2.0.0, its unclear whether still required");
        }
    }

}

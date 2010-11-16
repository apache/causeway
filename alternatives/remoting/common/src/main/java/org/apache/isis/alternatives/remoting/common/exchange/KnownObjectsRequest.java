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


package org.apache.isis.alternatives.remoting.common.exchange;

import java.util.Map;

import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

import com.google.inject.internal.Maps;


/**
 * A lookup of the objects that are part of a request or response. As only one instance of data per object
 * should be passed from node to node this object provides a way of ensuring this.
 */
public class KnownObjectsRequest {

    private final Map<ObjectAdapter, ObjectData> dataToObjectMap = Maps.newHashMap();
    private final Map<ObjectData, ObjectAdapter> objectToDataMap = Maps.newHashMap();

    public KnownObjectsRequest() {}

    public boolean containsKey(final ObjectAdapter object) {
        return dataToObjectMap.containsKey(object);
    }

    public boolean containsKey(final ObjectData data) {
        return objectToDataMap.containsKey(data);
    }

    public ObjectData get(final ObjectAdapter object) {
        return (ObjectData) dataToObjectMap.get(object);
    }

    public ObjectAdapter get(final ObjectData data) {
        return (ObjectAdapter) objectToDataMap.get(data);
    }

    public void put(final ObjectAdapter object, final ObjectData data) {
        dataToObjectMap.put(object, data);
        objectToDataMap.put(data, object);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KnownObjectsRequest other = (KnownObjectsRequest) obj;
        if (dataToObjectMap == null) {
            if (other.dataToObjectMap != null)
                return false;
        } else if (!dataToObjectMap.equals(other.dataToObjectMap))
            return false;
        if (objectToDataMap == null) {
            if (other.objectToDataMap != null)
                return false;
        } else if (!objectToDataMap.equals(other.objectToDataMap))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataToObjectMap == null) ? 0 : dataToObjectMap.hashCode());
        result = prime * result + ((objectToDataMap == null) ? 0 : objectToDataMap.hashCode());
        return result;
    }


//    @Override
//    public boolean equals(final Object obj) {
//        if (obj == this) {
//            return true;
//        }
//
//        if (obj instanceof KnownObjectsRequest) {
//            final KnownObjectsRequest other = (KnownObjectsRequest) obj;
//
//            return other.dataToObjectMap.equals(dataToObjectMap) && other.objectToDataMap.equals(objectToDataMap);
//        }
//
//        return false;
//    }


}


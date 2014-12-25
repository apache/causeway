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
package org.apache.isis.objectstore.jdo.datanucleus.valuetypes;

import org.datanucleus.store.rdbms.mapping.java.ObjectMapping;
import org.apache.isis.applib.value.Date;

public class IsisDateMapping extends ObjectMapping {

    private final IsisDateConverter dateConverter = new IsisDateConverter();
    
    public IsisDateMapping() {
        
    }
    
    @Override
    public Class<?> getJavaType() {
        return org.apache.isis.applib.value.Date.class;
    }

    // TODO: Need to check mapping
    
    protected Long objectToLong(Object object) {
        return dateConverter.toDatastoreType((Date) object);
    }

    protected Object longToObject(Long datastoreValue) {
        return dateConverter.toMemberType(datastoreValue);
    }

}

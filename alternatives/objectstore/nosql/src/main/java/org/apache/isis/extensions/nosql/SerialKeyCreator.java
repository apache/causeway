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


package org.apache.isis.extensions.nosql;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;

public class SerialKeyCreator implements KeyCreator {

    public String key(Oid oid) {
        long serialNo = ((SerialOid) oid).getSerialNo();
        return Long.toHexString(serialNo);
    }

    public String reference(ObjectAdapter object) {
        return object.getSpecification().getFullName() + "@" + key(object.getOid());
    }
    
    public SerialOid oid(String id) {
        return SerialOid.createPersistent(Long.valueOf(id, 16).longValue());
    }

    public Oid oidFromReference(String ref) {
        String id = ref.split("@")[1];
        return oid(id);
    }

    public ObjectSpecification specificationFromReference(String ref) {
        String name = ref.split("@")[0];
        return IsisContext.getSpecificationLoader().loadSpecification(name);
    }

}



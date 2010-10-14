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


package org.apache.isis.extensions.berkeley;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;


public class ObjectData {
    private final ObjectAdapter object;

    public ObjectData(ObjectAdapter object) {
        this.object = object;
    }

    public ObjectData(byte[] data) {

        try {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
            String className = inputStream.readUTF();
            ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(className);
            long id = inputStream.readLong();
            SerialOid oid = SerialOid.createPersistent(id);

            object = getAdapter(specification, oid);
            if (object.getResolveState().isResolved()) {
                // TODO - CHECK version and update
                return;
            }

            loadState(data, inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    private void loadState(byte[] data, DataInputStream inputStream) throws IOException {
        ResolveState resolveState = ResolveState.RESOLVING;
        object.changeState(resolveState);

        ObjectAssociationContainer specification = object.getSpecification();
        ObjectAssociation[] associations = specification.getAssociations();
        for (ObjectAssociation association : associations) {
            if (association.getSpecification().isValueOrIsAggregated()) {
                String fieldData = inputStream.readUTF();
                EncodableFacet encodeableFacet = association.getSpecification().getFacet(EncodableFacet.class);
                if (encodeableFacet != null) {
                    ObjectAdapter value = encodeableFacet.fromEncodedString(fieldData);
                    ((OneToOneAssociation) association).set(object, value);
                } else {
                    ((OneToOneAssociation) association).set(object, null);
                }
            } else {
                ObjectAdapter fieldObject;
                long id2 = inputStream.readLong();
                if (id2 == 0) {
                    fieldObject = null;
                } else {
                    SerialOid oid2 = SerialOid.createPersistent(id2);
                    fieldObject = getAdapter(association.getSpecification(), oid2);
                }
                ((OneToOneAssociation) association).set(object, fieldObject);
            }
        }
        object.changeState(resolveState.getEndState());
    }

    protected ObjectAdapter getAdapter(final ObjectSpecification specification, final Oid oid) {
        AdapterManager objectLoader = IsisContext.getPersistenceSession().getAdapterManager();
        ObjectAdapter adapter = objectLoader.getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        } else {
            return IsisContext.getPersistenceSession().recreateAdapter(oid, specification);
        }
    }

    public byte[] getData() {
        try {
            ObjectAssociation[] associations = object.getSpecification().getAssociations();
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

            DataOutputStream outputStream = new DataOutputStream(dataStream);
            String specName = object.getSpecification().getFullName();
            outputStream.writeUTF(specName);
            long serialNo = ((SerialOid) object.getOid()).getSerialNo();
            outputStream.writeLong(serialNo);

            for (ObjectAssociation association : associations) {
                ObjectAdapter field = association.get(object);
                if (association.getSpecification().isValueOrIsAggregated()) {
                    String data;
                    if (field == null) {
                        data = "";
                    } else {
                        EncodableFacet encodeableFacet = field.getSpecification().getFacet(EncodableFacet.class);
                        data = encodeableFacet.toEncodedString(field);
                    }
                    outputStream.writeUTF(data);

                } else {
                    if (field == null) {
                        outputStream.writeLong(0L);
                    } else {
                        long serialNo2 = ((SerialOid) field.getOid()).getSerialNo();
                        outputStream.writeLong(serialNo2);
                    }
                }
            }

            // TODO add version etc

            return dataStream.toByteArray();
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public String getKey() {
        Oid oid = object.getOid();
        return getKey(oid);
    }

    protected static String getKey(Oid oid) {
        long serialNo = ((SerialOid) oid).getSerialNo();
        return Long.toString(serialNo);
    }

    protected static SerialOid getOid(DataInputStream inputStream) throws IOException {
        long id = inputStream.readLong();
        SerialOid oid = SerialOid.createPersistent(id);
        return oid;
    }

    public static Oid getOid(byte[] data) {
        try {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
            return getOid(inputStream);
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public ObjectAdapter getObject() {
        return object;
    }

    public void update(byte[] data) {
        try {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));

            inputStream.readUTF();
            inputStream.readLong();

            loadState(data, inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

}


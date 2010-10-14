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


package org.apache.isis.webapp.context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.encoding.DataOutputExtended;
import org.apache.isis.metamodel.encoding.DataOutputStreamExtended;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.webapp.ScimpiException;
import org.apache.isis.webapp.context.RequestContext.Scope;
import org.apache.isis.webapp.debug.DebugView;
import org.apache.isis.webapp.processor.Request;


public class DefaultOidObjectMapping implements ObjectMapping {
    private static final Logger LOG = Logger.getLogger(DefaultOidObjectMapping.class);
    private final Map<String, TransientObjectMapping> requestTransients = new HashMap<String, TransientObjectMapping>();
    private final Map<String, TransientObjectMapping> sessionTransients = new HashMap<String, TransientObjectMapping>();
    private Class<? extends Oid> oidType;

    public void append(DebugView view) {
        append(view, requestTransients, "request");
        append(view, sessionTransients, "session");
    }

    protected void append(DebugView view, Map<String, TransientObjectMapping> transients, String type) {
        Iterator<String> ids = new HashSet(transients.keySet()).iterator();
        if (ids.hasNext()) {
            view.divider("Transient objects (" + type + ")");
            while (ids.hasNext()) {
                String key = ids.next();
                view.appendRow(key, transients.get(key).debug());
            }
        }
    }

    public void appendMappings(Request request) {}

    public void clear(Scope scope) {
        requestTransients.clear();
        
        List<String> remove = new ArrayList<String>();
        for (String id :sessionTransients.keySet()) {
            if (!sessionTransients.get(id).getOid().isTransient()) {
                remove.add(id);
                sessionTransients.put(id, null);
            }
        }
        for (String id : remove) {
            sessionTransients.remove(id);
        }
    }

    public void endSession() {
        sessionTransients.clear();
    }

    public String mapObject(ObjectAdapter inObject, Scope scope) {
        // TODO need to ensure that transient objects are remapped each time so that any changes are added to
        // session data
        // continue work here.....here

        ObjectAdapter object = inObject;
        try {
            Oid oid = object.getOid();
            if (oidType == null) {
                oidType = oid.getClass();
            }
            
            String encodedOid;
            if (oid instanceof AggregatedOid) {
                AggregatedOid aoid = (AggregatedOid) oid;
                Oid parentOid = aoid.getParentOid();
                String fieldName = aoid.getFieldName();
                int element = aoid.getElement();
                
                object = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(parentOid);
                encodedOid = Long.toHexString(((SerialOid) parentOid).getSerialNo()) + "@" + fieldName + (element == -1 ? "" : "@" + element);
            } else  if (oid instanceof SerialOid) {
                encodedOid = Long.toHexString(((SerialOid) oid).getSerialNo());
            } else {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DataOutputExtended outputStream = new DataOutputStreamExtended(output);
                oid.encode(outputStream);
                //  encodedOid = URLEncoder.encode(output.toString(), "UTF-8");
                encodedOid = IsisContext.getPersistenceSession().getOidGenerator().getOidStringifier().enString(oid);
                // outputStream.flush();
            }
            boolean isTransient = object.isTransient();
            String id = (isTransient ? "T" : "P") + object.getSpecification().getFullName() + "@" + encodedOid;
            LOG.debug("encoded " + oid + " as " + id + " ~ " + encodedOid);
            if (isTransient) {
                TransientObjectMapping mapping = new TransientObjectMapping((ObjectAdapter) object);
                if (scope == Scope.REQUEST) {
                    requestTransients.put(id, mapping);
                } else if (scope ==Scope.INTERACTION || scope == Scope.SESSION) {
                    sessionTransients.put(id, mapping);
                } else {
                    throw new ScimpiException("Can't hold globally transient object");
                }
            }
            return id;
        } catch (IOException e) {
            throw new IsisException(e);
        }
    }

    public ObjectAdapter mappedObject(String id) {
        char type = id.charAt(0);
        if ((type == 'T')) {
            TransientObjectMapping mapping = sessionTransients.get(id);
            if (mapping == null) {
                mapping = requestTransients.get(id);
            }
            if (mapping == null) {
                return null;
            }
            ObjectAdapter mappedTransientObject = mapping.getObject();
            LOG.debug("retrieved " + mappedTransientObject.getOid() + " for " + id);
            return mappedTransientObject;
        } else {
            String[] split = id.split("@");
            ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(split[0].substring(1));
            
           try {
               String oidData = split[1];
               LOG.debug("decoding " + oidData);
               
               ObjectAdapter loadObject;
               Oid oid;
               // HACK - to remove after fix!
               if (oidType == null) {
                   oidType = IsisContext.getPersistenceSession().getServices().get(0).getOid().getClass();
               }
               if (split.length > 2) {
                   SerialOid parentOid = SerialOid.createPersistent(Long.parseLong(oidData, 16));
                   int element = (split.length == 3) ? -1 : Integer.valueOf(split[3]);
                   oid = new AggregatedOid(parentOid, split[2], element);
                   IsisContext.getPersistenceSession().loadObject(parentOid, spec);
                   loadObject = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(oid); 
               } else if (oidType.isAssignableFrom(SerialOid.class)) {
                   oid = SerialOid.createPersistent(Long.parseLong(oidData, 16));
                   loadObject = IsisContext.getPersistenceSession().loadObject(oid, spec);
               } else {
                   oid = IsisContext.getPersistenceSession().getOidGenerator().getOidStringifier().deString(oidData);
/*                   String decodedOid = URLDecoder.decode(oidData, "UTF-8");
                   DataInputExtended stream = new DataInputStreamExtended(new ByteArrayInputStream(decodedOid.getBytes()));
                   
                    Constructor<? extends Oid> constructor = oidType.getConstructor(DataInputExtended.class);
                    oid = constructor.newInstance(stream);         
                    LOG.debug("decoded " + id + " as " + oid + ": " + oidData + " ~ " + decodedOid);
   */
                   loadObject = IsisContext.getPersistenceSession().loadObject(oid, spec);

               }

               
          
                    
                return loadObject;
            } catch (SecurityException e) {
                throw new IsisException(e);
                /*
            } catch (NoSuchMethodException e) {
                throw new IsisException(e);
            } catch (IllegalArgumentException e) {
                throw new IsisException(e);
            } catch (InstantiationException e) {
                throw new IsisException(e);
            } catch (IllegalAccessException e) {
                throw new IsisException(e);
            } catch (InvocationTargetException e) {
                throw new IsisException(e);
            } catch (UnsupportedEncodingException e) {
                throw new IsisException(e);
                */
            }
            
            /*
     //       long serialNumber = Long.parseLong(id.substring(pos + 1), 36);
   //         *** Oid oid = isTransient ? SerialOid.createTransient(serialNumber) : SerialOid.createPersistent(serialNumber);
            Oid oid = null;
            return IsisContext.getPersistenceSession().loadObject(oid, spec);
            */
        }
    }

    public void reloadIdentityMap() {
        Iterator<TransientObjectMapping> mappings = sessionTransients.values().iterator();
        while (mappings.hasNext()) {
            TransientObjectMapping mapping = mappings.next();
            mapping.reload();
        }
    }

    public void unmapObject(ObjectAdapter object, Scope scope) {
        sessionTransients.remove(object.getOid());
        requestTransients.remove(object.getOid());
    }

}

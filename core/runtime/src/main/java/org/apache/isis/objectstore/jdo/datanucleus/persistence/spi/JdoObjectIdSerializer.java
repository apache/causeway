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
package org.apache.isis.objectstore.jdo.datanucleus.persistence.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.jdo.annotations.IdentityType;
import javax.jdo.identity.ByteIdentity;
import javax.jdo.identity.IntIdentity;
import javax.jdo.identity.LongIdentity;
import javax.jdo.identity.ObjectIdentity;
import javax.jdo.identity.StringIdentity;

import org.datanucleus.identity.OID;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public final class JdoObjectIdSerializer {
    
    private static final char SEPARATOR = '_';

    private JdoObjectIdSerializer(){}

    public static class Exception extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public Exception(java.lang.Exception ex) {
            super(ex);
        }
    }

    public static String toOidIdentifier(Object jdoOid) {
        if(jdoOid instanceof javax.jdo.identity.IntIdentity) {
            return "i" + SEPARATOR + jdoOid; 
        }
        if(jdoOid instanceof javax.jdo.identity.StringIdentity) {
            return "s" + SEPARATOR + jdoOid; 
        }
        if(jdoOid instanceof javax.jdo.identity.LongIdentity) {
            return "l" + SEPARATOR + jdoOid; 
        }
        if(jdoOid instanceof javax.jdo.identity.ObjectIdentity) {
            javax.jdo.identity.ObjectIdentity id = (ObjectIdentity) jdoOid;
            Object keyAsObject = id.getKeyAsObject();
            // UUID support
            if(keyAsObject instanceof UUID) {
                UUID uuid = (UUID) keyAsObject;
                return "u" + SEPARATOR + uuid.toString(); 
            }
        }
        if(jdoOid instanceof OID) {
            OID dnOid = (OID) jdoOid;
            Object keyValue = dnOid.getKeyValue();
            
            // prettier handling of these common cases
            if(keyValue instanceof String) {
                return "S" + SEPARATOR + keyValue; 
            }

            if(keyValue instanceof Long) {
                return "L" + SEPARATOR + keyValue; 
            }

            if(keyValue instanceof BigInteger) {
                return "B" + SEPARATOR + keyValue; 
            }

            if(keyValue instanceof Integer) {
                return "I" + SEPARATOR + keyValue; 
            }

        }
        
        // the JDO spec (5.4.3) requires that OIDs are serializable toString and 
        // recreatable through the constructor
        return jdoOid.getClass().getName().toString() + SEPARATOR + jdoOid.toString();
    }

    private static List<String> dnPrefixes = Arrays.asList("S", "I", "L", "B");
    
    public static Object toJdoObjectId(RootOid oid) {

        String idStr = oid.getIdentifier();
        final int separatorIdx = idStr.indexOf(SEPARATOR);
        
        final String distinguisher = idStr.substring(0, separatorIdx);
        final String keyStr = idStr.substring(separatorIdx+1);

        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
        final JdoPersistenceCapableFacet jdoPcFacet = spec.getFacet(JdoPersistenceCapableFacet.class);

        if(isApplicationIdentity(jdoPcFacet)) {

            if("s".equals(distinguisher)) {
                return keyStr;
            }
            if("i".equals(distinguisher)) {
                return Integer.parseInt(keyStr);
            }
            if("l".equals(distinguisher)) {
                return Long.parseLong(keyStr);
            }
            if("b".equals(distinguisher)) {
                return Byte.parseByte(keyStr);
            }
            if("u".equals(distinguisher)) {
                return UUID.fromString(keyStr);
            }
            
        } else {

            if("s".equals(distinguisher)) {
                return new StringIdentity(objectTypeClassFor(oid), keyStr);
            }
            if("i".equals(distinguisher)) {
                return new IntIdentity(objectTypeClassFor(oid), keyStr);
            }
            if("l".equals(distinguisher)) {
                return new LongIdentity(objectTypeClassFor(oid), keyStr);
            }
            if("b".equals(distinguisher)) {
                return new ByteIdentity(objectTypeClassFor(oid), keyStr);
            }
            if("u".equals(distinguisher)) {
                return new ObjectIdentity(objectTypeClassFor(oid), UUID.fromString(keyStr));
            }
        }
        

        if(dnPrefixes.contains(distinguisher)) {
			return keyStr + "[OID]" + spec.getFullIdentifier(); 
        }
        
        final String clsName = distinguisher;
        try {
            final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(clsName);
            final Constructor<?> cons = cls.getConstructor(String.class);
            final Object dnOid = cons.newInstance(keyStr);
            return dnOid.toString();
        } catch (ClassNotFoundException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        } catch (IllegalArgumentException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        } catch (InstantiationException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        } catch (IllegalAccessException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        } catch (InvocationTargetException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        } catch (SecurityException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        } catch (NoSuchMethodException e) {
            throw new JdoObjectIdSerializer.Exception(e);
        }
    }

    protected static boolean isApplicationIdentity(final JdoPersistenceCapableFacet jdoPcFacet) {
        return jdoPcFacet != null && jdoPcFacet.getIdentityType() == IdentityType.APPLICATION;
    }

	private static Class<?> objectTypeClassFor(RootOid oid) {
		final ObjectSpecId objectSpecId = oid.getObjectSpecId();
		final ObjectSpecification spec = getSpecificationLoader().lookupBySpecId(objectSpecId);
		final Class<?> correspondingClass = spec.getCorrespondingClass();
		return correspondingClass;
	}

	private static SpecificationLoaderSpi getSpecificationLoader() {
		return IsisContext.getSpecificationLoader();
	}
}

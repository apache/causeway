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
package org.apache.isis.jdo.datanucleus.persistence.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
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

import org.datanucleus.identity.DatastoreId;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;

public final class JdoObjectIdSerializer {

    private static final char SEPARATOR = '_';

    private JdoObjectIdSerializer(){}

    public static class Exception extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public Exception(final java.lang.Exception ex) {
            super(ex);
        }
    }

    public static String toOidIdentifier(final Object jdoOid) {

        //
        // @javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.APPLICATION)
        //
        if(jdoOid instanceof javax.jdo.identity.ByteIdentity) {
            return "b" + SEPARATOR + jdoOid;
        }
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
            final javax.jdo.identity.ObjectIdentity id = (ObjectIdentity) jdoOid;
            final Object keyAsObject = id.getKeyAsObject();
            // UUID support
            if(keyAsObject instanceof UUID) {
                final UUID uuid = (UUID) keyAsObject;
                return "u" + SEPARATOR + uuid.toString();
            }
        }

        if(jdoOid instanceof DatastoreId) {

            //
            // prettier handling of common datatypes if possible
            //

            final DatastoreId dnOid = (DatastoreId) jdoOid;
            final Object keyValue = dnOid.getKeyAsObject();


            if( keyValue instanceof String ||
                    keyValue instanceof Long ||
                    keyValue instanceof BigDecimal || // 1.8.0 did not support BigDecimal
                    keyValue instanceof BigInteger ||
                    keyValue instanceof Integer) {

                // no separator
                return "" + keyValue;
            }

        }

        // the JDO spec (5.4.3) requires that OIDs are serializable toString and
        // recreatable through the constructor
        return jdoOid.getClass().getName() + SEPARATOR + jdoOid.toString();
    }

    private static List<String> dnPrefixes = Arrays.asList("S", "I", "L", "M", "B");

    public static Object toJdoObjectId(final RootOid oid) {

        final String idStr = oid.getIdentifier();
        final int separatorIdx = idStr.indexOf(SEPARATOR);

        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecIdElseLoad(oid.getObjectSpecId());
        final JdoPersistenceCapableFacet jdoPcFacet = spec.getFacet(JdoPersistenceCapableFacet.class);


        if(separatorIdx != -1) {

            // behaviour for OIDs as of 1.8.0 and previously

            final String distinguisher = idStr.substring(0, separatorIdx);
            final String keyStr = idStr.substring(separatorIdx + 1);

            final boolean isApplicationIdentity = isApplicationIdentity(jdoPcFacet);

            if("s".equals(distinguisher)) {
                if (isApplicationIdentity) {
                    return keyStr;
                } else {
                    return new StringIdentity(objectTypeClassFor(oid), keyStr);
                }
            } else if("i".equals(distinguisher)) {
                if(isApplicationIdentity) {
                    return Integer.parseInt(keyStr);
                } else {
                    return new IntIdentity(objectTypeClassFor(oid), keyStr);
                }
            } else if("l".equals(distinguisher)) {
                if(isApplicationIdentity) {
                    return Long.parseLong(keyStr);
                } else {
                    return new LongIdentity(objectTypeClassFor(oid), keyStr);
                }
            } else if("b".equals(distinguisher)) {
                if(isApplicationIdentity) {
                    return Byte.parseByte(keyStr);
                } else {
                    return new ByteIdentity(objectTypeClassFor(oid), keyStr);
                }
            } else if("u".equals(distinguisher)) {
                if(isApplicationIdentity) {
                    return UUID.fromString(keyStr);
                } else {
                    return new ObjectIdentity(objectTypeClassFor(oid), UUID.fromString(keyStr));
                }
            }

            if(dnPrefixes.contains(distinguisher)) {
                return keyStr + "[OID]" + spec.getFullIdentifier();
            }

            final String clsName = distinguisher;
            try {
                final Class<?> cls = _Context.loadClass(clsName);
                final Constructor<?> cons = cls.getConstructor(String.class);
                final Object dnOid = cons.newInstance(keyStr);
                return dnOid.toString();
            } catch (ClassNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
                throw new JdoObjectIdSerializer.Exception(e);
            }

        } else {

            // there was no separator, so this identifier must have been for
            // @javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE)
            // for one of the common types (prettier handling)

            // in DN 4.1, we did this...
            // return idStr + "[OID]" + spec.getFullIdentifier();

            // in DN 5.1, we simply do this...
            return idStr;
        }
    }

    protected static boolean isApplicationIdentity(final JdoPersistenceCapableFacet jdoPcFacet) {
        return jdoPcFacet != null && jdoPcFacet.getIdentityType() == IdentityType.APPLICATION;
    }

    private static Class<?> objectTypeClassFor(final RootOid oid) {
        final ObjectSpecId objectSpecId = oid.getObjectSpecId();
        final ObjectSpecification spec = getSpecificationLoader().lookupBySpecIdElseLoad(objectSpecId);
        final Class<?> correspondingClass = spec.getCorrespondingClass();
        return correspondingClass;
    }

    private static SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}

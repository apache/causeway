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
package org.apache.isis.persistence.jdo.integration.config.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.jdo.identity.ByteIdentity;
import javax.jdo.identity.IntIdentity;
import javax.jdo.identity.LongIdentity;
import javax.jdo.identity.ObjectIdentity;
import javax.jdo.identity.StringIdentity;

import org.datanucleus.identity.DatastoreId;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.persistence.jdo.integration.config.spi.JdoObjectIdDecoder.JdoObjectIdDecodingRequest;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JdoObjectIdSerializer {

    static final char SEPARATOR = '_';
    
    private static List<JdoObjectIdEncoder> encoders() {
        
        final List<String> nonSafeUrlChars = Arrays.asList("/", "\\");
        
        val encoders = Arrays.asList(
                // Byte
                JdoObjectIdEncoder.of(
                        JdoObjectIdEncoder.filter(ByteIdentity.class),   
                        JdoObjectIdEncoder.stringifier("b")),
                // Int
                JdoObjectIdEncoder.of(
                        JdoObjectIdEncoder.filter(IntIdentity.class),   
                        JdoObjectIdEncoder.stringifier("i")),
                // Long
                JdoObjectIdEncoder.of(
                        JdoObjectIdEncoder.filter(LongIdentity.class),   
                        JdoObjectIdEncoder.stringifier("l")),
                // String
                JdoObjectIdEncoder.of(
                        JdoObjectIdEncoder.filter(StringIdentity.class),   
                        jdoOid->{
                            val stringified = "" + jdoOid;
                            if(nonSafeUrlChars.stream()
                                    .anyMatch(stringified::contains)) {
                                return "base64" + SEPARATOR + _Strings.base64UrlEncode(stringified);
                            }
                            return "s" + SEPARATOR + stringified;
                        }),
                // UUID
                JdoObjectIdEncoder.of(
                        jdoOid->{
                            if(jdoOid instanceof javax.jdo.identity.ObjectIdentity) {
                                val id = (ObjectIdentity) jdoOid;
                                return id.getKeyAsObject() instanceof UUID;
                            }
                            return false;            
                        },
                        jdoOid->{
                            val id = (ObjectIdentity) jdoOid;
                            val uuid = (UUID) id.getKeyAsObject();
                            return "u" + SEPARATOR + uuid.toString();
                        }),
                // DatastoreId
                JdoObjectIdEncoder.of( 
                        jdoOid->{
                            if(jdoOid instanceof DatastoreId) {
                                final DatastoreId dnOid = (DatastoreId) jdoOid;
                                final Object keyValue = dnOid.getKeyAsObject();
                                // prettier handling of common datatypes if possible ?
                                if( keyValue instanceof String ||
                                        keyValue instanceof Long ||
                                        keyValue instanceof BigDecimal || // 1.8.0 did not support BigDecimal
                                        keyValue instanceof BigInteger ||
                                        keyValue instanceof Integer) {
                                    return true;
                                }
                            }
                            return false;            
                        },
                        jdoOid->{
                            final DatastoreId dnOid = (DatastoreId) jdoOid;
                            // no separator
                            return "" + dnOid.getKeyAsObject();
                        })
                );
        return encoders;
        
    }
    
    private static List<JdoObjectIdDecoder> decoders() {
        
        final List<String> dnPrefixes = Arrays.asList("S", "I", "L", "M", "B");
        
        val decoders = Arrays.asList(
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter("b"),
                        JdoObjectIdDecoder.parser(Byte::parseByte, ByteIdentity::new)),
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter("i"),
                        JdoObjectIdDecoder.parser(Integer::parseInt, IntIdentity::new)),
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter("l"),
                        JdoObjectIdDecoder.parser(Long::parseLong, LongIdentity::new)),
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter("s"),
                        JdoObjectIdDecoder.parser(Function.identity(), StringIdentity::new)),
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter("base64"),
                        JdoObjectIdDecoder.parser(
                                _Strings::base64UrlDecode, 
                                (type, keyStr)-> new StringIdentity(type, _Strings.base64UrlDecode(keyStr)) )),
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter("u"),
                        JdoObjectIdDecoder.parser(
                                UUID::fromString, 
                                (type, keyStr)-> new ObjectIdentity(type, UUID.fromString(keyStr)) )),
                // if there is no separator, the identifier is for
                // @javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE)
                JdoObjectIdDecoder.of(
                        JdoObjectIdDecoder.filter(""),
                        JdoObjectIdDecodingRequest::getKeyStr),
                JdoObjectIdDecoder.of(
                        request->dnPrefixes.contains(request.getDistinguisher()),
                        request->request.getKeyStr() + "[OID]" + request.getSpec().getFullIdentifier())
                
                );
        return decoders;
    }

    private final static ChainOfResponsibility<Object, String> 
        encodingChain = ChainOfResponsibility.of(encoders());
    
    private final static ChainOfResponsibility<JdoObjectIdDecodingRequest, Object> 
        decodingChain = ChainOfResponsibility.of(decoders());
    
    public static String toOidIdentifier(final Object jdoOid) {

        return encodingChain.handle(jdoOid)
        .orElseGet(()->
            // the JDO spec (5.4.3) requires that OIDs are serializable toString and
            // re-create-able through the constructor
            jdoOid.getClass().getName() + SEPARATOR + jdoOid.toString());
    }

   
    public static Object toJdoObjectId(SpecificationLoader specificationLoader, RootOid oid) {

        val spec = specificationLoader.lookupBySpecIdElseLoad(oid.getObjectSpecId());
        val request = JdoObjectIdDecodingRequest.parse(spec, oid.getIdentifier());
        
        return decodingChain.handle(request)
        .orElseGet(()->{

            val clsName = request.getDistinguisher();
            val keyStr = request.getKeyStr();         

            try {
                final Class<?> cls = _Context.loadClass(clsName);
                final Constructor<?> cons = cls.getConstructor(String.class);
                final Object dnOid = cons.newInstance(keyStr);
                return dnOid.toString();
            } catch (ClassNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
                throw _Exceptions.unrecoverableFormatted(
                        "failed to instantiate %s with arg %s", clsName, keyStr, e);
            }
            
        });

    }


}

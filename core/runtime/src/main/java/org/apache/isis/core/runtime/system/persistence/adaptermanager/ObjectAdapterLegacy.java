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
package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.schema.common.v1.CollectionDto;
import org.apache.isis.schema.common.v1.OidDto;
import org.apache.isis.schema.common.v1.ValueDto;
import org.apache.isis.schema.common.v1.ValueType;
import org.apache.isis.schema.utils.CommonDtoUtils;

/**
 * Interim class, expected to be removed with https://issues.apache.org/jira/browse/ISIS-1976 
 */
public class ObjectAdapterLegacy {
    
    static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterLegacy.class);
    
    // -- CommandExecutorServiceDefault --------------------------------------------------------
    
    public static class __CommandExecutorServiceDefault {

        public static ObjectAdapter adapterFor(Object targetObject) {
            if(targetObject instanceof OidDto) {
                final OidDto oidDto = (OidDto) targetObject;
                return adapterFor(oidDto);
            }
            if(targetObject instanceof CollectionDto) {
                final CollectionDto collectionDto = (CollectionDto) targetObject;
                final List<ValueDto> valueDtoList = collectionDto.getValue();
                final List<Object> pojoList = _Lists.newArrayList();
                for (final ValueDto valueDto : valueDtoList) {
                    ValueType valueType = collectionDto.getType();
                    final Object valueOrOidDto = CommonDtoUtils.getValue(valueDto, valueType);
                    // converting from adapter and back means we handle both
                    // collections of references and of values
                    final ObjectAdapter objectAdapter = adapterFor(valueOrOidDto);
                    Object pojo = objectAdapter != null ? objectAdapter.getObject() : null;
                    pojoList.add(pojo);
                }
                return adapterFor(pojoList);
            }
            if(targetObject instanceof Bookmark) {
                final Bookmark bookmark = (Bookmark) targetObject;
                return adapterFor(bookmark);
            }
            return getPersistenceSession().adapterFor(targetObject);
        }
        
        private static ObjectAdapter adapterFor(final OidDto oidDto) {
            final Bookmark bookmark = Bookmark.from(oidDto);
            return adapterFor(bookmark);
        }

        private static ObjectAdapter adapterFor(final Bookmark bookmark) {
            final RootOid rootOid = RootOid.create(bookmark);
            return adapterFor(rootOid);
        }

        private static ObjectAdapter adapterFor(final RootOid rootOid) {
            return getPersistenceSession().adapterFor(rootOid);
        }

        private static PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession().orElseThrow(_Exceptions::unexpectedCodeReach);
        }
        
    }
    
}

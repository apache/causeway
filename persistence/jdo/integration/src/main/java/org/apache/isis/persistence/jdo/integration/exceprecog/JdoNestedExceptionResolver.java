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
package org.apache.isis.persistence.jdo.integration.exceprecog;

import java.util.stream.Stream;

import javax.jdo.JDODataStoreException;

import org.apache.isis.commons.internal.base._NullSafe;

import lombok.val;

/**
 * @since 2.0
 */
final class JdoNestedExceptionResolver {

    static Stream<Throwable> streamNestedExceptionsOf(Throwable throwable) {
        
        if(throwable instanceof JDODataStoreException) {
            val jdoDataStoreException = (JDODataStoreException) throwable;
            return _NullSafe.stream(jdoDataStoreException.getNestedExceptions());
            
        }
        return Stream.empty();
        
    }
    
}

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
package org.apache.isis.jdo.exceprecog;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.config.internal._Config;

/**
 * Convenience implementation of the {@link ExceptionRecognizer} domain service that
 * recognizes a number of common and non-fatal exceptions (such as unique constraint
 * violations).
 *
 * <p>
 *     Unlike most other domain services, the framework will consult <i>all</i>
 *     registered implementations of this service (chain of responsibility pattern) (rather than
 *     the first one found).
 * </p>
 */
@DomainService(nature = NatureOfService.DOMAIN)
public class ExceptionRecognizerCompositeForJdoObjectStore extends ExceptionRecognizerComposite {

    public static final String KEY_DISABLE = 
            "isis.services.ExceptionRecognizerCompositeForJdoObjectStore.disable";

    @Override
    @PostConstruct
    public void init() {
        
        final boolean disabled = _Config.getConfiguration().getBoolean(KEY_DISABLE, false);
        if(disabled) {
            return;
        }

        addChildren();

        super.init();
    }

    protected void addChildren() {
        // most specific ones first
        add(new ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException());
        add(new ExceptionRecognizerForJDODataStoreExceptionIntegrityConstraintViolationForeignKeyNoActionException());
        add(new ExceptionRecognizerForJDOObjectNotFoundException());
        add(new ExceptionRecognizerForJDODataStoreException());
    }

}

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

package org.apache.isis.core.metamodel.services.exceprecog;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class ExceptionRecognizerDocDefault
implements ExceptionRecognizer {


    // -- init, shutdown

    @Programmatic
    @PostConstruct
    @Override
    public void init() {
        serviceRegistry.injectServicesInto(recognizer);
        recognizer.init();
    }

    @Programmatic
    @PreDestroy
    @Override
    public void shutdown() {
        recognizer.shutdown();
    }



    static class ExceptionRecognizerForConcurrencyException
    extends ExceptionRecognizerForType {
        public ExceptionRecognizerForConcurrencyException() {
            super(Category.CONCURRENCY, ConcurrencyException.class, prefix("Another user has just changed this data"));
        }
    }
    static class ExceptionRecognizerForRecoverableException
    extends ExceptionRecognizerForType {
        public ExceptionRecognizerForRecoverableException() {
            super(Category.CLIENT_ERROR, RecoverableException.class);
        }
    }


    private final ExceptionRecognizer recognizer =
            new ExceptionRecognizerComposite(
                    new ExceptionRecognizerForConcurrencyException(),
                    new ExceptionRecognizerForRecoverableException()
                    );

    /**
     * Framework-provided implementation of {@link ExceptionRecognizer},
     * which will automatically recognize any {@link org.apache.isis.applib.RecoverableException}s or
     * any {@link ConcurrencyException}s.
     */
    @Programmatic
    @Override
    public String recognize(Throwable ex) {
        return recognizer.recognize(ex);
    }

    @Programmatic
    @Override
    public Recognition recognize2(final Throwable ex) {
        return recognizer.recognize2(ex);
    }



    @javax.inject.Inject
    ServiceRegistry serviceRegistry;

}

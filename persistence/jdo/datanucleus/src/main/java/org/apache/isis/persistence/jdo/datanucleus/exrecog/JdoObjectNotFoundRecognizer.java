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
package org.apache.isis.persistence.jdo.datanucleus.exrecog;

import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.persistence.jdo.datanucleus.IsisModulePersistenceJdoDatanucleus;

@Service
@Named(IsisModulePersistenceJdoDatanucleus.NAMESPACE + ".JdoObjectNotFoundRecognizer")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("DN6")
public class JdoObjectNotFoundRecognizer implements ExceptionRecognizer {

    @Override
    public Optional<Recognition> recognize(final Throwable ex) {
        return _Exceptions.streamCausalChain(ex)
                .map(Throwable::getClass)
                .map(Class::getSimpleName)
                .anyMatch(exName->exName.endsWith("ObjectNotFoundException"))
                ? Recognition.of(Category.NOT_FOUND, _Strings.nullToEmpty(ex.getMessage()))
                : Optional.empty();
    }

}

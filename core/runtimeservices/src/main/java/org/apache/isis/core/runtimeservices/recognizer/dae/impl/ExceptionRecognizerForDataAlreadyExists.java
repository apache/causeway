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
package org.apache.isis.core.runtimeservices.recognizer.dae.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtimeservices.recognizer.dae.ExceptionRecognizerForDataAccessException;

@Service
@Named("isis.runtime.ExceptionRecognizerForDataAlreadyExists")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Default")
public class ExceptionRecognizerForDataAlreadyExists
extends ExceptionRecognizerForDataAccessException {

    @Inject
    public ExceptionRecognizerForDataAlreadyExists(IsisConfiguration conf) {
        super(conf, 
                Category.CONSTRAINT_VIOLATION,
                ofType(org.springframework.dao.DataAccessException.class)
                .and(including("unique constraint or index violation")), //TODO magic words might have changed since migration to Spring
                prefix("Data already exists"));
    }

}

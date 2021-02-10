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
import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtimeservices.recognizer.dae.ExceptionRecognizerForDataAccessException;

/**
 * Handles exceptions like {@literal org.springframework.dao.DataIntegrityViolationException: JDO operation: Insert of object "domainapp.modules.hello.dom.hwo.HelloWorldObject@6cad4834" using statement "INSERT INTO "hello"."HelloWorldObject" ("name","notes","version") VALUES (?,?,?)" failed : Unique index or primary key violation: "hello.HelloWorldObject_name_UNQ_INDEX_B ON hello.HelloWorldObject(name) VALUES 1"; SQL statement:}
 * <p>
 * TODO: should not be sensitive to 'NOT NULL check constraint' ... don't know whether this can ever happen
 * @since 2.0
 */
@Service
@Named("isis.runtime.ExceptionRecognizerForDataAlreadyExists")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Default")
public class ExceptionRecognizerForDataAlreadyExists
extends ExceptionRecognizerForDataAccessException {

    @Inject
    public ExceptionRecognizerForDataAlreadyExists(IsisConfiguration conf) {
        //XXX used prefix could be made a config option
        // under isis.core.runtimeservices.exception-recognizers.dae
        super(conf,
                Category.CONSTRAINT_VIOLATION,
                ofType(org.springframework.dao.DataIntegrityViolationException.class)
                .and(including("Unique index or primary key violation")),
                prefix("Data already exists"));
    }

}

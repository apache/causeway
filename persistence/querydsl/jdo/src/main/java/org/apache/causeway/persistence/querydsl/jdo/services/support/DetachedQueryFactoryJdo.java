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
 *
 */
package org.apache.causeway.persistence.querydsl.jdo.services.support;

import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;

import org.apache.causeway.persistence.querydsl.applib.services.support.DetachedQueryFactory;

import org.apache.causeway.persistence.querydsl.jdo.query.DslQueryJdo;

import org.springframework.stereotype.Service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jdo.JDOQuery;

@Service
public class DetachedQueryFactoryJdo implements DetachedQueryFactory {

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJdo.of(new JDOQuery<Void>().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJdo.of(new JDOQuery<Void>().select(exprs));
    }

}

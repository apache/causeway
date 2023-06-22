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
package org.apache.causeway.client.kroviz.handler

import org.apache.causeway.client.kroviz.core.event.LogEntry

/**
 * Delegates responses (logEntry.response) to handlers, acts as Facade.
 * @See: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 */
object ResponseHandler {
    private var delegate: BaseHandler

    //sequence of handlers follows frequency of invocation (demo execute all menu actions)
    //IMPROVE by dynamic lookup at runtime?
    private var _0 = RestfulHandler()
    private var _0a = UserHandler()
    private var _0b = VersionHandler()
    private var _0c = DomainTypesHandler()
    private var _0d = MenuBarsHandler()
    private var _0e = IconHandler()

    private var _1 = TObjectHandler()
    private var _2 = LayoutXmlHandler()
    private var _3 = CollectionHandler()
    private var _4 = ActionHandler()
    private var _5 = HttpErrorHandler()


    private var _10 = ServiceHandler()
    private var _11 = ResultListHandler()
    private var _12 = ResultObjectHandler()
    private var _13 = ResultValueHandler()
    private var _15 = PropertyHandler()
    private var _16 = MemberHandler()
    private var _17 = Http401ErrorHandler()
    private var _19 = DomainTypeHandler()
    private var _20 = DiagramHandler()
    private var last = DefaultHandler()

    init {
        delegate = _0
        _0.successor = _0a
        _0a.successor = _0b
        _0b.successor = _0c
        _0c.successor = _0d
        _0d.successor = _0e
        _0e.successor = _1
        _1.successor = _2
        _2.successor = _3
        _3.successor = _4
        _4.successor = _5
        _5.successor = _10

        _10.successor = _11
        _11.successor = _12
        _12.successor = _13
        _13.successor = _15
        _15.successor = _16
        _16.successor = _17
        _17.successor = _19

        _19.successor = _20
        _20.successor = last
    }

    fun handle(logEntry: LogEntry) {
        delegate.handle(logEntry)
    }

}

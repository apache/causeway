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
package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.ui.kv.Constants

/**
 * Handle responses to XmlHttpRequests asynchronously,
 * since they may arrive in arbitrary order.
 * @see: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 * COR simplifies implementation of Dispatcher.
 *
 * Implementing classes are responsible for:
 * @item creating Objects by parsing responses (JSON/XML),
 * @item creating/finding Aggregators (eg. ListAggregator, ObjectAggregator), and
 * @item setting Objects and Aggregators into LogEntry.
 */
abstract class BaseHandler {
    var successor:BaseHandler? = null
    protected var logEntry =LogEntry("")

    /**
     * @see https://en.wikipedia.org/wiki/Template_method_pattern
     */
    open fun handle(logEntry: LogEntry) {
        this.logEntry = logEntry
        val response: String? = logEntry.getResponse()
        if (null !== response) {
            if (canHandle(response)) {
                doHandle()
            } else {
                successor!!.handle(logEntry)
            }
        }
    }

    /**
     * Default implementation - may be overridden in subclasses.
     */
    open fun canHandle(response: String): Boolean {
        try {
            val obj = parse(response)
            logEntry.setTransferObject(obj)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    /**
     * May be overridden in subclasses
     */
    open fun doHandle() {
        update()
    }

    /**
     * Must be overridden in subclasses
     */
    open fun parse(response: String):TransferObject? {
        throw Exception("Subclass Responsibility")
    }

    protected fun update() {
        logEntry.getAggregator()!!.update(logEntry, Constants.subTypeJson)
    }

}

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

package org.apache.isis.runtimes.dflt.runtime.persistence;

import java.text.DateFormat;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class ConcurrencyException extends ObjectPersistenceException {
    private static final long serialVersionUID = 1L;
    private Oid source;

    public ConcurrencyException(final ObjectAdapter adapter, final Version updated) {
        this(adapter.getVersion().getUser() + " changed " + adapter.titleString() + " at " + DateFormat.getDateTimeInstance().format(adapter.getVersion().getTime()) + "\n\n" + adapter.getVersion() + " ~ " + updated + "", adapter.getOid());
    }

    public ConcurrencyException(final String message, final Oid source) {
        super(message);
        this.source = source;
    }

    public ConcurrencyException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public Oid getSource() {
        return source;
    }
}

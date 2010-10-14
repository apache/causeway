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


package org.apache.isis.remoting.client.transaction;

import org.apache.isis.metamodel.adapter.ObjectAdapter;


public class ClientTransactionEvent {
    private final ObjectAdapter object;
    private final int type;
    public static final int DELETE = 3;
    public static final int CHANGE = 2;
    public static final int ADD = 1;

    ClientTransactionEvent(final ObjectAdapter object, final int type) {
        this.object = object;
        this.type = type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ClientTransactionEvent) {
            return ((ClientTransactionEvent) obj).type == type && ((ClientTransactionEvent) obj).object.equals(object);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 37 * h + type;
        h = 37 * h + object.hashCode();
        return h;
    }

    public ObjectAdapter getObject() {
        return object;
    }

    public int getType() {
        return type;
    }
}


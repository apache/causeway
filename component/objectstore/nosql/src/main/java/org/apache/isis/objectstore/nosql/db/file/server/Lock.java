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

package org.apache.isis.objectstore.nosql.db.file.server;

import java.util.ArrayList;
import java.util.List;

class Lock {

    private Thread write;
    private final List<Thread> reads = new ArrayList<Thread>();

    public boolean isWriteLocked() {
        return write != null;
    }

    public void addRead(final Thread transaction) {
        reads.add(transaction);
    }

    public void setWrite(final Thread transaction) {
        write = transaction;
    }

    public void remove(final Thread transaction) {
        if (write == transaction) {
            write = null;
        } else {
            reads.remove(transaction);
        }
    }

    public boolean isEmpty() {
        return write == null && reads.isEmpty();
    }

}

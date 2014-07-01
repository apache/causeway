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

import java.util.HashMap;
import java.util.Map;

public class LockManager {

    private final Map<String, Lock> locks = new HashMap<String, Lock>();

    public synchronized void acquireRead(final String id, final Thread transaction) {
        final Lock lock = getLock(id);
        lock.addRead(transaction);
    }

    public boolean acquireWrite(final String id, final Thread transaction) {
        final Lock lock = getLock(id);
        if (lock.isWriteLocked()) {
            return false;
        }
        lock.setWrite(transaction);
        return true;
    }

    private Lock getLock(final String id) {
        Lock lock;
        synchronized (this) {
            lock = locks.get(id);
            if (lock == null) {
                lock = new Lock();
                locks.put(id, lock);
            }
        }
        return lock;
    }

    public synchronized void release(final String id, final Thread transaction) {
        final Lock lock = getLock(id);
        lock.remove(transaction);
        if (lock.isEmpty()) {
            locks.remove(id);
        }
    }

    public void waitUntilAllRealeased() {
    }

}

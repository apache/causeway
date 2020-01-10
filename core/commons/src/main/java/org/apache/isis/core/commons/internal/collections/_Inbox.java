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
package org.apache.isis.core.commons.internal.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.val;

/**
 * Allows for thread-safe producer consumer pattern backed by {@link ArrayList}. 
 *
 * @param <T>
 */
public class _Inbox<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Object $lock = new Object[0]; // serializable lock

    final List<T> list = _Lists.newArrayList();

    public void add(T element) {
        synchronized($lock) {
            list.add(element);
        }
    }

    public void remove(T element) {
        synchronized($lock) {
            list.remove(element);
        }
    }

    public List<T> snapshot() {
        synchronized($lock) {
            val defensiveCopy = _Lists.newArrayList(list);
            return defensiveCopy;
        }
    }

    public List<T> snapshotThenClear() {
        synchronized($lock) {
            val defensiveCopy = _Lists.newArrayList(list);
            list.clear();
            return defensiveCopy;
        }
    }

    public boolean isEmpty() {
        synchronized($lock) {
            return list.isEmpty();
        } 
    }

    public void clear() {
        synchronized($lock) {
            list.clear();
        } 
    }

    public int size() {
        synchronized($lock) {
            return list.size();
        } 
    }

}

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
package org.apache.causeway.commons.internal.collections.snapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

/**
 * Thread-safe pseudo list, that increments its version each time a snapshot is requested.
 * <p>
 * This allows to easily keep track of any additions to the list that occurred in between
 * snapshots.
 *
 * @since 2.0
 * @param <T>
 */
public final class _VersionedList<T> {

    private UUID uuid = UUID.randomUUID();
    private final List<List<T>> versions = new ArrayList<>();
    private List<T> currentVersion = new ArrayList<>();
    private int size;

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Snapshot<T> {
        private UUID ownerUuid;
        @SuppressWarnings("unused")
        private final int fromIndex; //low endpoint (inclusive) of the copy
        private final int toIndex; // high endpoint (exclusive) of the copy
        private final List<List<T>> versions;

        public boolean isEmpty() {
            return versions.isEmpty();
        }

        public Stream<T> stream() {
            return versions.stream()
                    .flatMap(List::stream);
        }

        public void forEach(final Consumer<T> action) {
            for(val ver : versions) {
                for(val element : ver) {
                    action.accept(element);
                }
            }
        }

        public void forEachParallel(final Consumer<T> action) {
            for(val ver : versions) {
                if(ver.size()>8) {
                    ver.parallelStream().forEach(action);
                } else {
                    for(val element : ver) {
                        action.accept(element);
                    }
                }
            }
        }


    }

    public Snapshot<T> snapshot() {
        synchronized(versions) {
            commit();
            return new Snapshot<>(uuid, 0, versions.size(), defensiveCopy());
        }
    }

    public Snapshot<T> deltaSince(final @NonNull Snapshot<T> snapshot) {

        if(snapshot.ownerUuid!=uuid) {
            throw new IllegalArgumentException("Snapshot's UUID is different from the VersionedList's.");
        }

        synchronized(versions) {
            commit();
            val from = snapshot.toIndex;
            val to = versions.size();
            return new Snapshot<>(uuid, from, to, defensiveCopy(from, to));
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size==0;
    }

    /** current implementation cannot handle concurrent additions that occur during traversal*/
    public Stream<T> stream() {
        final List<List<T>> defensiveCopy;
        synchronized(versions) {
            commit();
            defensiveCopy = defensiveCopy();
        }
        return defensiveCopy.stream()
                .flatMap(List::stream);
    }

    public boolean add(final T e) {
        synchronized(versions) {
            ++size;
            return currentVersion.add(e);
        }
    }

    public boolean addAll(final Collection<? extends T> c) {
        synchronized(versions) {
            size+=c.size();
            return currentVersion.addAll(c);
        }
    }

    public void clear() {
        synchronized(versions) {
            uuid = UUID.randomUUID();
            size=0;
            versions.clear();
            currentVersion.clear();
        }
    }

    /**
     * Also handles concurrent additions that occur during traversal.
     * @param action
     */
    public void forEach(final Consumer<T> action) {
        val snapshot = snapshot();
        snapshot.forEach(action);
        Snapshot<T> delta = deltaSince(snapshot);
        while(!delta.isEmpty()) {
            delta.forEach(action);
            delta = deltaSince(delta);
        }
    }

    /**
     * Also handles concurrent additions that occur during traversal.
     * @param action
     */
    public void forEachConcurrent(final Consumer<T> action) {
        val snapshot = snapshot();
        snapshot.forEachParallel(action);
        Snapshot<T> delta = deltaSince(snapshot);
        while(!delta.isEmpty()) {
            delta.forEachParallel(action);
            delta = deltaSince(delta);
        }
    }


    // -- HELPER


    /**
     * @implNote only call within synchronized block!
     * @param fromIndex low endpoint (inclusive) of the copy
     * @param toIndex high endpoint (exclusive) of the copy
     */
    private List<List<T>> defensiveCopy(final int fromIndex, final int toIndex) {
        if(fromIndex==toIndex) {
            return Collections.emptyList();
        }
        return new ArrayList<>(versions.subList(fromIndex, toIndex));
    }

    /** @implNote only call within synchronized block! */
    private List<List<T>> defensiveCopy() {
        if(versions.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(versions);
    }

    /** @implNote only call within synchronized block! */
    private void commit() {
        if(!currentVersion.isEmpty()) {
            versions.add(currentVersion);
            currentVersion = new ArrayList<>(); // create a new array for others to write to next
        }
    }


}

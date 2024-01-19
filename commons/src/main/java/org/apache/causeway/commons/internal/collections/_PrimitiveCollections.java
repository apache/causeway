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
package org.apache.causeway.commons.internal.collections;

import java.util.Iterator;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * Primitive int list implementation.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
@UtilityClass
public class _PrimitiveCollections {

    /**
     * Primitive int list implementation. Can also operate as a set,
     * if {@link IntList#addUnique(int)} is used over {@link IntList#add(int)}.
     * @implNote not thread-safe
     */
    public static class IntList implements Iterable<Integer> {

        private static final int DEFAULT_INITIAL_CAPACITY = 8;

        private int[] buf;
        private int size = 0;

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return size==0;
        }
        public boolean isNotEmpty() {
            return size!=0;
        }

        public IntList addUnique(final int v) {
            if(!contains(v)) return add(v);
            return this;
        }

        public IntList add(final int v) {
            if(buf==null) {
                this.buf = new int[DEFAULT_INITIAL_CAPACITY];
            } else if(size==buf.length) {
                var old = buf;
                this.buf = new int[buf.length * 2];
                System.arraycopy(old, 0, buf, 0, size);
            }
            buf[size++] = v;
            return this;
        }

        public int get(final int index) {
            if(index<0
                    || index>=size) {
                throw new IndexOutOfBoundsException(index);
            }
            return buf[index];
        }

        public OptionalInt indexOf(final int v) {
            if(isEmpty()) return OptionalInt.empty();
            for (int i = 0; i < size; i++) {
                if(buf[i]==v) return OptionalInt.of(i);
            }
            return OptionalInt.empty();
        }

        public boolean contains(final int v) {
            if(isEmpty()) return false;
            for (int i = 0; i < size; i++) {
                if(buf[i]==v) return true;
            }
            return false;
        }

        public IntStream stream() {
            return IntStream.of(toArray());
        }

        /**
         * @return a new array containing all the int(s) of this list
         */
        public int[] toArray() {
            var result = new int[size];
            if(!isEmpty()) {
                System.arraycopy(buf, 0, result, 0, size);
            }
            return result;
        }

        @Override
        public Iterator<Integer> iterator() {
            var defensiveCopy = toArray();
            return new PrimitiveIterator.OfInt() {
                int index = 0;
                @Override public boolean hasNext() {
                    return index<defensiveCopy.length;
                }
                @Override public int nextInt() {
                    return defensiveCopy[index++];
                }
            };
        }

    }

}

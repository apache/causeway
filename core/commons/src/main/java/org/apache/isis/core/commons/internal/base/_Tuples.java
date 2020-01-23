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

package org.apache.isis.core.commons.internal.base;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import lombok.Value;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 *      Provides Tuples of arity 2,
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Tuples {

    private _Tuples(){}
    
    // -- TUPLE 2

    public static class Tuple2<T1, T2>{
        private final T1 _1;
        private final T2 _2;

        private Tuple2(T1 _1, T2 _2) {
            this._1 = _1;
            this._2 = _2;
        }

        /**
         * @return first element of this tuple
         */
        public T1 get_1() { return _1; }
        /**
         * @return second element of this tuple
         */
        public T2 get_2() { return _2; }
    }

    public static <T1, T2> Tuple2<T1, T2> pair(T1 _1, T2 _2) {
        return new Tuple2<T1, T2>(_1, _2);
    }
    
    // -- INDEXED 
    
    @Value(staticConstructor = "of")
    public static class Indexed<T>{
        int index;
        T value;
    }
    
    public static <T> Indexed<T> indexed(int index, T value) {
        return Indexed.of(index, value);
    }

    // -- JAVAX - PERSISTENCE
    
    //TODO just a sketch yet
    @Value(staticConstructor = "of")
    private static final class TypedTuple implements Tuple {
        
        final Can<Class<? extends Serializable>> types; 
        final List<Serializable> values;

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            throw _Exceptions.notImplemented();
        }

        @Override
        public <X> X get(String alias, Class<X> type) {
            throw _Exceptions.notImplemented();
        }

        @Override
        public Object get(String alias) {
            throw _Exceptions.notImplemented();
        }

        @Override
        public <X> X get(int i, Class<X> requiredType) {
            val value = values.get(i);
            return _Casts.uncheckedCast(value);
        }

        @Override
        public Object get(int i) {
            return values.get(i);
        }

        @Override
        public Object[] toArray() {
            throw _Exceptions.notImplemented();
        }

        @Override
        public List<TupleElement<?>> getElements() {
            throw _Exceptions.notImplemented();
        }
        
    }
    
    public static Tuple of(Can<Class<? extends Serializable>> types, List<Serializable> values) {
        return of(types, values);
    }
    

}

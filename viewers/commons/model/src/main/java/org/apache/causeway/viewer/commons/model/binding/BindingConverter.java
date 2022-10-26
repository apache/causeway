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
package org.apache.causeway.viewer.commons.model.binding;

import lombok.val;

public interface BindingConverter<L, R> {

    L toLeft(R right);
    R toRight(L left);

    public static <T> BindingConverter<T, T> identity(final Class<T> type){
        return new BindingConverter<T, T>() {
            @Override public T toLeft(final T right) {
                return right;}
            @Override public T toRight(final T left) {
                return left;}
        };
    }

    public default BindingConverter<R, L> reverse() {
        val self = this;
        return new BindingConverter<R, L>() {
            @Override public R toLeft(final L right) {
                return self.toRight(right);}
            @Override public L toRight(final R left) {
                return self.toLeft(left);}
        };
    }

}





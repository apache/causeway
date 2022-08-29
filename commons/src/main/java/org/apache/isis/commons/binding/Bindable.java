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
package org.apache.isis.commons.binding;

import java.util.function.Function;

/**
 * @param <T>
 */
public interface Bindable<T> extends Observable<T>, Writable<T> {

    void bind(Observable<? extends T> observable);

    void unbind();

    boolean isBound();

    void bindBidirectional(Bindable<T> other);

    void unbindBidirectional(Bindable<T> other);

    <R> Bindable<R> mapToBindable(Function<T, R> forwardMapper, Function<R, T> reverseMapper);

}

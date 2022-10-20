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
package org.apache.causeway.core.metamodel._testing;

import java.util.List;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.callable.AsyncCallable;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.applib.services.wrapper.listeners.InteractionListener;

/** just a stub, not actually used */
class WrapperFactory_forTesting implements WrapperFactory {

    @Override
    public <T> T wrap(T domainObject, SyncControl syncControl) {
        return domainObject;
    }

    @Override
    public <T> T wrap(T domainObject) {
        return domainObject;
    }

    @Override
    public <T> T wrapMixin(Class<T> mixinClass, Object mixedIn, SyncControl syncControl) {
        return null;
    }

    @Override
    public <T> T wrapMixin(Class<T> mixinClass, Object mixedIn) {
        return null;
    }

    @Override
    public <T> T unwrap(T possibleWrappedDomainObject) {
        return null;
    }

    @Override
    public <T> boolean isWrapper(T possibleWrappedDomainObject) {
        return false;
    }

    @Override
    public <T, R> T asyncWrap(T domainObject, AsyncControl<R> asyncControl) {
        return null;
    }

    @Override
    public <T, R> T asyncWrapMixin(Class<T> mixinClass, Object mixedIn, AsyncControl<R> asyncControl) {
        return null;
    }

    @Override
    public List<InteractionListener> getListeners() {
        return null;
    }

    @Override
    public boolean addInteractionListener(InteractionListener listener) {
        return false;
    }

    @Override
    public boolean removeInteractionListener(InteractionListener listener) {
        return false;
    }

    @Override
    public void notifyListeners(InteractionEvent ev) {
    }

    @Override
    public <R> R execute(AsyncCallable<R> asyncCallable) {
        return null;
    }

}

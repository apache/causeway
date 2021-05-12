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
package org.apache.isis.commons.internal.binding;

import java.lang.ref.WeakReference;

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.binding.ChangeListener;
import org.apache.isis.commons.binding.InvalidationListener;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;


/**
 * <h1>- internal use only -</h1>
 *
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public abstract class _BindableAbstract<T> implements Bindable<T> {

    private T value;
    private Observable<? extends T> observable = null;;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private InternalUtil<T> util = null;

    public _BindableAbstract() {
    }

    public _BindableAbstract(T initialValue) {
        this.value = initialValue;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        util = InternalUtil.addListener(util, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        util = InternalUtil.removeListener(util, listener);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        util = InternalUtil.addListener(util, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        util = InternalUtil.removeListener(util, listener);
    }

    @Override
    public void bindBidirectional(Bindable<T> other) {
        InternalBidirectionalBinding.bind(this, other);
    }

    @Override
    public void unbindBidirectional(Bindable<T> other) {
        InternalBidirectionalBinding.unbind(this, other);
    }

    @Override
    public T getValue() {
        valid = true;
        return observable == null
                ? value
                : observable.getValue();
    }

    @Override
    public void setValue(T newValue) {
        if (isBound()) {
            throw _Exceptions.unrecoverable("Cannot set value on a bound bindable.");
        }
        if (value != newValue) {
            value = newValue;
            markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public void bind(@NonNull final Observable<? extends T> newObservable) {
        if (!newObservable.equals(this.observable)) {
            unbind();
            observable = newObservable;
            if (listener == null) {
                listener = new WeakInvalidationListener(this);
            }
            observable.addListener(listener);
            markInvalid();
        }
    }

    @Override
    public void unbind() {
        if (observable != null) {
            value = observable.getValue();
            observable.removeListener(listener);
            observable = null;
        }
    }

    protected void fireValueChanged() {
        InternalUtil.fireValueChanged(util);
    }

    protected void onInvalidated() {
    }

    // -- HELPER

    private void markInvalid() {
        if (valid) {
            valid = false;
            onInvalidated();
            fireValueChanged();
        }
    }

    private static class WeakInvalidationListener
    implements InvalidationListener, InternalUtil.WeakListener {

        private final WeakReference<_BindableAbstract<?>> wref;

        public WeakInvalidationListener(_BindableAbstract<?> ref) {
            this.wref = new WeakReference<_BindableAbstract<?>>(ref);
        }

        @Override
        public void invalidated(Observable<?> observable) {
            _BindableAbstract<?> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.markInvalid();
            }
        }

        @Override
        public boolean isNoLongerReferenced() {
            return wref.get() == null;
        }
    }
}

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.binding.ChangeListener;
import org.apache.isis.commons.binding.InvalidationListener;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.Setter;
import lombok.val;


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
    private InvalidationListener invalidationListener = null;
    private boolean valid = true;
    private InternalUtil<T> util = null;

    /**
     * Called within {@link #getValue()} to refine the internally stored value.
     * <p>
     * use-case: re-fetch detached entities
     */
    @Setter private @NonNull UnaryOperator<T> valueRefiner = UnaryOperator.identity();

    /**
     * Called within {@link #setValue()} to guard against invalid new values.
     * <p>
     * use-case: guard against incompatible types
     */
    @Setter private @NonNull UnaryOperator<T> valueGuard = UnaryOperator.identity();

    public _BindableAbstract() {
    }

    public _BindableAbstract(final T initialValue) {
        this.value = initialValue;
    }

    @Override
    public void addListener(final InvalidationListener listener) {
        util = InternalUtil.addListener(util, this, listener);
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
        util = InternalUtil.removeListener(util, listener);
    }

    @Override
    public void addListener(final ChangeListener<? super T> listener) {
        util = InternalUtil.addListener(util, this, listener);
    }

    @Override
    public void removeListener(final ChangeListener<? super T> listener) {
        util = InternalUtil.removeListener(util, listener);
    }

    @Override
    public void bindBidirectional(final Bindable<T> other) {
        InternalBidirectionalBinding.bind(this, other);
    }

    @Override
    public void unbindBidirectional(final Bindable<T> other) {
        InternalBidirectionalBinding.unbind(this, other);
    }

    @Override
    public T getValue() {
        valid = true;
        val val = observable == null
                ? value
                : observable.getValue();
        return valueRefiner.apply(val);
    }

    @Override
    public void setValue(final T proposedNewValue) {
        if (isBound()) {
            throw _Exceptions.unrecoverable("Cannot set value on a bound bindable.");
        }
        val newValue = valueGuard.apply(proposedNewValue);
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
    public void bind(final @NonNull Observable<? extends T> newObservable) {
        if (!newObservable.equals(this.observable)) {
            unbind();
            observable = newObservable;
            if (invalidationListener == null) {
                invalidationListener = new WeakInvalidationListener(this);
            }
            observable.addListener(invalidationListener);
            markInvalid();
        }
    }

    @Override
    public void unbind() {
        if (observable != null) {
            value = observable.getValue();
            observable.removeListener(invalidationListener);
            observable = null;
        }
    }

    protected void fireValueChanged() {
        InternalUtil.fireValueChanged(util);
    }

    protected void onInvalidated() {
    }

    // -- COMPOSITION

    @Override
    public <R> Observable<R> map(
            final Function<T, R> forwardMapper) {
        final var newBindable = _Observables.<R>lazy(()->forwardMapper.apply(getValue()));
        addListener((e,o,n)->{
            newBindable.setValue(forwardMapper.apply(n));
        });
        return newBindable;
    }

    @Override
    public <R> Bindable<R> mapToBindable(
            final Function<T, R> forwardMapper,
            final Function<R, T> reverseMapper) {

        val isForwardUpdating = new AtomicBoolean();
        val isReverseUpdating = new AtomicBoolean();

        val newBindable = _Bindables.<R>forValue(forwardMapper.apply(getValue()));
        addListener((e,o,n)->{
            if(isReverseUpdating.get()) {
                return;
            }
            try {
                isForwardUpdating.set(true);
                newBindable.setValue(forwardMapper.apply(n));
            } finally {
                isForwardUpdating.set(false);
            }
        });

        newBindable.addListener((e,o,n)->{
            if(isForwardUpdating.get()) {
                return;
            }
            try {
                isReverseUpdating.set(true);
                setValue(reverseMapper.apply(n));
            } finally {
                isReverseUpdating.set(false);
            }
        });

        return newBindable;
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

        public WeakInvalidationListener(final _BindableAbstract<?> ref) {
            this.wref = new WeakReference<_BindableAbstract<?>>(ref);
        }

        @Override
        public void invalidated(final Observable<?> observable) {
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

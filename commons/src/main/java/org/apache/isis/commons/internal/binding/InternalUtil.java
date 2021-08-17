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
import java.util.Arrays;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.binding.ChangeListener;
import org.apache.isis.commons.binding.InvalidationListener;
import org.apache.isis.commons.binding.Observable;

import lombok.NonNull;

abstract class InternalUtil<T> {

    // -- WEAK LISTENING STUFF

    static interface WeakListener {
        boolean isNoLongerReferenced();
    }

    final static class WeakChangeListener<T> implements ChangeListener<T>, WeakListener {

        private final WeakReference<ChangeListener<T>> ref;

        public WeakChangeListener(@NonNull ChangeListener<T> listener) {
            this.ref = new WeakReference<ChangeListener<T>>(listener);
        }

        @Override
        public boolean isNoLongerReferenced() {
            return (ref.get() == null);
        }

        @Override
        public void changed(Observable<? extends T> observable, T oldValue, T newValue) {
            ChangeListener<T> listener = ref.get();
            if (listener != null) {
                listener.changed(observable, oldValue, newValue);
            } else {
                observable.removeListener(this);
            }
        }

    }

    final static class WeakInvalidationListener implements InvalidationListener, WeakListener {

        private final WeakReference<InvalidationListener> ref;

        public WeakInvalidationListener(@NonNull InvalidationListener listener) {
            this.ref = new WeakReference<InvalidationListener>(listener);
        }

        @Override
        public boolean isNoLongerReferenced() {
            return (ref.get() == null);
        }

        @Override
        public void invalidated(Observable<?> observable) {
            InvalidationListener listener = ref.get();
            if (listener != null) {
                listener.invalidated(observable);
            } else {
                observable.removeListener(this);
            }
        }
    }


    // -- LISTENER ADD/REMOVE

    static <T> InternalUtil<T> addListener(
            @Nullable final InternalUtil<T> helper,
            @NonNull final Observable<T> observable,
            @NonNull final InvalidationListener listener) {
        observable.getValue(); // validate observable
        return (helper == null)
                ? new SingleInvalidation<T>(observable, listener)
                : helper.addListener(listener);
    }

    static <T> InternalUtil<T> removeListener(
            @Nullable final InternalUtil<T> helper,
            @NonNull final InvalidationListener listener) {
        return (helper == null)
                ? null
                : helper.removeListener(listener);
    }

    static <T> InternalUtil<T> addListener(
            @Nullable final InternalUtil<T> helper,
            @NonNull final Observable<T> observable,
            @NonNull final ChangeListener<? super T> listener) {
        return (helper == null)
                ? new SingleChange<T>(observable, listener)
                : helper.addListener(listener);
    }

    static <T> InternalUtil<T> removeListener(
            @Nullable final InternalUtil<T> helper,
            @NonNull ChangeListener<? super T> listener) {
        return (helper == null)
                ? null
                : helper.removeListener(listener);
    }

    static <T> void fireValueChanged(InternalUtil<T> helper) {
        if (helper != null) {
            helper.fireValueChanged();
        }
    }

    // -- COMMON IMPLEMENTATIONS

    protected final Observable<T> observable;

    private InternalUtil(Observable<T> observable) {
        this.observable = observable;
    }

    protected abstract InternalUtil<T> addListener(InvalidationListener listener);
    protected abstract InternalUtil<T> removeListener(InvalidationListener listener);

    protected abstract InternalUtil<T> addListener(ChangeListener<? super T> listener);
    protected abstract InternalUtil<T> removeListener(ChangeListener<? super T> listener);

    protected abstract void fireValueChanged();

    // -- IMPLEMENTATIONS

    private static class SingleInvalidation<T> extends InternalUtil<T> {

        private final InvalidationListener listener;

        private SingleInvalidation(Observable<T> observable, InvalidationListener listener) {
            super(observable);
            this.listener = listener;
        }

        @Override
        protected InternalUtil<T> addListener(InvalidationListener listener) {
            return new Generic<T>(observable, this.listener, listener);
        }

        @Override
        protected InternalUtil<T> removeListener(InvalidationListener listener) {
            return (listener.equals(this.listener))? null : this;
        }

        @Override
        protected InternalUtil<T> addListener(ChangeListener<? super T> listener) {
            return new Generic<T>(observable, this.listener, listener);
        }

        @Override
        protected InternalUtil<T> removeListener(ChangeListener<? super T> listener) {
            return this;
        }

        @Override
        protected void fireValueChanged() {
            try {
                listener.invalidated(observable);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleChange<T> extends InternalUtil<T> {

        private final ChangeListener<? super T> listener;
        private T currentValue;

        private SingleChange(Observable<T> observable, ChangeListener<? super T> listener) {
            super(observable);
            this.listener = listener;
            this.currentValue = observable.getValue();
        }

        @Override
        protected InternalUtil<T> addListener(InvalidationListener listener) {
            return new Generic<T>(observable, listener, this.listener);
        }

        @Override
        protected InternalUtil<T> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected InternalUtil<T> addListener(ChangeListener<? super T> listener) {
            return new Generic<T>(observable, this.listener, listener);
        }

        @Override
        protected InternalUtil<T> removeListener(ChangeListener<? super T> listener) {
            return (listener.equals(this.listener))? null : this;
        }

        @Override
        protected void fireValueChanged() {
            final T oldValue = currentValue;
            currentValue = observable.getValue();
            final boolean changed = (currentValue == null)? (oldValue != null) : !currentValue.equals(oldValue);
            if (changed) {
                try {
                    listener.changed(observable, oldValue, currentValue);
                } catch (Exception e) {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        }
    }

    private static class Generic<T> extends InternalUtil<T> {

        private InvalidationListener[] invalidationListeners;
        private ChangeListener<? super T>[] changeListeners;
        private int invalidationSize;
        private int changeSize;
        private boolean locked;
        private T currentValue;

        private Generic(Observable<T> observable, InvalidationListener listener0, InvalidationListener listener1) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
        }

        @SuppressWarnings("unchecked")
        private Generic(Observable<T> observable, ChangeListener<? super T> listener0, ChangeListener<? super T> listener1) {
            super(observable);
            this.changeListeners = new ChangeListener[] {listener0, listener1};
            this.changeSize = 2;
            this.currentValue = observable.getValue();
        }

        @SuppressWarnings("unchecked")
        private Generic(Observable<T> observable, InvalidationListener invalidationListener, ChangeListener<? super T> changeListener) {
            super(observable);
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ChangeListener[] {changeListener};
            this.changeSize = 1;
            this.currentValue = observable.getValue();
        }

        @Override
        protected Generic<T> addListener(InvalidationListener listener) {
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[] {listener};
                invalidationSize = 1;
            } else {
                final int oldCapacity = invalidationListeners.length;
                if (locked) {
                    final int newCapacity = (invalidationSize < oldCapacity)? oldCapacity : (oldCapacity * 3)/2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                } else if (invalidationSize == oldCapacity) {
                    invalidationSize = trimListeners(invalidationSize, invalidationListeners);
                    if (invalidationSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3)/2 + 1;
                        invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                    }
                }
                invalidationListeners[invalidationSize++] = listener;
            }
            return this;
        }

        @Override
        protected InternalUtil<T> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (listener.equals(invalidationListeners[index])) {
                        if (invalidationSize == 1) {
                            if (changeSize == 1) {
                                return new SingleChange<T>(observable, changeListeners[0]);
                            }
                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0)) {
                            return new SingleInvalidation<T>(observable, invalidationListeners[1-index]);
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            final InvalidationListener[] oldListeners = invalidationListeners;
                            if (locked) {
                                invalidationListeners = new InvalidationListener[invalidationListeners.length];
                                System.arraycopy(oldListeners, 0, invalidationListeners, 0, index);
                            }
                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index+1, invalidationListeners, index, numMoved);
                            }
                            invalidationSize--;
                            if (!locked) {
                                invalidationListeners[invalidationSize] = null; // Let gc do its work
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected InternalUtil<T> addListener(ChangeListener<? super T> listener) {
            if (changeListeners == null) {
                changeListeners = new ChangeListener[] {listener};
                changeSize = 1;
            } else {
                final int oldCapacity = changeListeners.length;
                if (locked) {
                    final int newCapacity = (changeSize < oldCapacity)? oldCapacity : (oldCapacity * 3)/2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                } else if (changeSize == oldCapacity) {
                    changeSize = trimListeners(changeSize, changeListeners);
                    if (changeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3)/2 + 1;
                        changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                    }
                }
                changeListeners[changeSize++] = listener;
            }
            if (changeSize == 1) {
                currentValue = observable.getValue();
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected InternalUtil<T> removeListener(ChangeListener<? super T> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (listener.equals(changeListeners[index])) {
                        if (changeSize == 1) {
                            if (invalidationSize == 1) {
                                return new SingleInvalidation<T>(observable, invalidationListeners[0]);
                            }
                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0)) {
                            return new SingleChange<T>(observable, changeListeners[1-index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ChangeListener<? super T>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ChangeListener[changeListeners.length];
                                System.arraycopy(oldListeners, 0, changeListeners, 0, index);
                            }
                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index+1, changeListeners, index, numMoved);
                            }
                            changeSize--;
                            if (!locked) {
                                changeListeners[changeSize] = null; // Let gc do its work
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @Override
        protected void fireValueChanged() {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ChangeListener<? super T>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    try {
                        curInvalidationList[i].invalidated(observable);
                    } catch (Exception e) {
                        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                    }
                }
                if (curChangeSize > 0) {
                    final T oldValue = currentValue;
                    currentValue = observable.getValue();
                    final boolean changed = (currentValue == null)? (oldValue != null) : !currentValue.equals(oldValue);
                    if (changed) {
                        for (int i = 0; i < curChangeSize; i++) {
                            try {
                                curChangeList[i].changed(observable, oldValue, currentValue);
                            } catch (Exception e) {
                                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                            }
                        }
                    }
                }
            } finally {
                locked = false;
            }
        }
    }

    private static int trimListeners(int size, Object[] listeners) {
        Predicate<Object> p = t -> t instanceof WeakListener
                && ((WeakListener)t).isNoLongerReferenced();
        int index = 0;
        for (; index < size; index++) {
            if (p.test(listeners[index])) {
                break;
            }
        }
        if (index < size) {
            for (int src = index + 1; src < size; src++) {
                if (!p.test(listeners[src])) {
                    listeners[index++] = listeners[src];
                }
            }
            int oldSize = size;
            size = index;
            for (; index < oldSize; index++) {
                listeners[index] = null;
            }
        }

        return size;
    }

}

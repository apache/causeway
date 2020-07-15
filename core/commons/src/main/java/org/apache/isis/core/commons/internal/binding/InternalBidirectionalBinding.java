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
package org.apache.isis.core.commons.internal.binding;

import java.lang.ref.WeakReference;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.ChangeListener;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

//@Log4j2
abstract class InternalBidirectionalBinding<T> 
implements ChangeListener<T>, InternalUtil.WeakListener {

    public static <T> InternalBidirectionalBinding<T> bind(Bindable<T> left, Bindable<T> right) {
        checkParameters(left, right);
        val binding = new GenericBidirectionalBinding<T>(left, right);
        left.setValue(right.getValue());
        left.addListener(binding);
        right.addListener(binding);
        return binding;
    }

    public static <T> void unbind(Bindable<T> left, Bindable<T> rigth) {
        checkParameters(left, rigth);
        val binding = new UntypedBidirectionalBinding(left, rigth);
        left.removeListener(binding);
        rigth.removeListener(binding);
    }

    public static void unbind(Object left, Object right) {
        checkParameters(left, right);
        val binding = new UntypedBidirectionalBinding(left, right);
        if (left instanceof Observable) {
            ((Observable<?>) left).removeListener(binding);
        }
        if (right instanceof Observable) {
            ((Observable<?>) right).removeListener(binding);
        }
    }

    protected abstract Object getLeft();

    protected abstract Object getRight();

    
    @Override
    public int hashCode() {
        return cachedHash;
    }

    @Override
    public boolean isNoLongerReferenced() {
        return (getLeft() == null) || (getRight() == null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        final Object left = getLeft();
        final Object right = getRight();
        if ((left == null) || (right == null)) {
            return false;
        }

        if (obj instanceof InternalBidirectionalBinding) {
            final InternalBidirectionalBinding<?> otherBinding = (InternalBidirectionalBinding<?>) obj;
            final Object otherLeft = otherBinding.getLeft();
            final Object otherRight = otherBinding.getRight();
            if ((otherLeft == null) || (otherRight == null)) {
                return false;
            }

            if (left == otherLeft && right == otherRight) {
                return true;
            }
            if (left == otherRight && right == otherLeft) {
                return true;
            }
        }
        return false;
    }
    
    // -- HELPER
    
    private static void checkParameters(@NonNull Object left, @NonNull Object right) {
        if (left == right) {
            throw _Exceptions.illegalArgument("Cannot bind to self");
        }
    }
    
    private final int cachedHash;

    private InternalBidirectionalBinding(Object left, Object right) {
        cachedHash = left.hashCode() * right.hashCode();
    }


    private static class GenericBidirectionalBinding<T> extends InternalBidirectionalBinding<T> {
        private final WeakReference<Bindable<T>> leftRef;
        private final WeakReference<Bindable<T>> rightRef;
        private boolean updating = false;

        private GenericBidirectionalBinding(Bindable<T> left, Bindable<T> right) {
            super(left, right);
            leftRef = new WeakReference<Bindable<T>>(left);
            rightRef = new WeakReference<Bindable<T>>(right);
        }

        @Override
        protected Bindable<T> getLeft() {
            return leftRef.get();
        }

        @Override
        protected Bindable<T> getRight() {
            return rightRef.get();
        }

        @Override
        public void changed(final Observable<? extends T> observable, final T oldValue, final T newValue) {
            if (!updating) {
                final Bindable<T> left = leftRef.get();
                final Bindable<T> right = rightRef.get();
                if ((left == null) || (right == null)) {
                    if (left != null) {
                        left.removeListener(this);
                    }
                    if (right != null) {
                        right.removeListener(this);
                    }
                } else {
                    try {
                        updating = true;
                        if (left == observable) {
                            right.setValue(newValue);
                        } else {
                            left.setValue(newValue);
                        }
                    } catch (RuntimeException e) {
                        try {
                            if (left == observable) {
                                left.setValue(oldValue);
                            } else {
                                right.setValue(oldValue);
                            }
                        } catch (Exception e2) {
                            e2.addSuppressed(e);
                            unbind(left, right);
                            throw _Exceptions.unrecoverableFormatted(
                                    "Bidirectional binding failed with an attempt to restore the "
                                    + "Observable to the previous value. "
                                    + "Removing the bidirectional binding from bindables %s and %s",
                                    ""+left,
                                    ""+right, 
                                    e2);
                        }
                        throw _Exceptions.unrecoverable(
                                "Bidirectional binding failed, setting to the previous value", e);
                    } finally {
                        updating = false;
                    }
                }
            }
        }
    }

    private static class UntypedBidirectionalBinding extends InternalBidirectionalBinding<Object> {

        @Getter private final Object left;
        @Getter private final Object right;

        public UntypedBidirectionalBinding(Object left, Object right) {
            super(left, right);
            this.left = left;
            this.right = right;
        }

        @Override
        public void changed(Observable<? extends Object> sourceProperty, Object oldValue, Object newValue) {
            throw _Exceptions.unexpectedCodeReach();
        }
    }


}

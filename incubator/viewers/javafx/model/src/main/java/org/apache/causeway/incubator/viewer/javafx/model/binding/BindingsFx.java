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
package org.apache.causeway.incubator.viewer.javafx.model.binding;

import java.lang.ref.WeakReference;
import java.util.Objects;

import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.ChangeListener;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.commons.model.binding.BindingConverter;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BindingsFx {

    public static <T> void bind(
            final @NonNull Property<T> leftProperty,
            final @NonNull Observable<T> rightObservable) {

        leftProperty.setValue(rightObservable.getValue());
        rightObservable.addListener((e,o,n)->{
            leftProperty.setValue(n);
        });
    }

    public static <L, R> void bind(
            final @NonNull Property<L> leftProperty,
            final @NonNull Observable<R> rightObservable,
            final @NonNull BindingConverter<L, R> converter) {

        leftProperty.setValue(converter.toLeft(rightObservable.getValue()));
        rightObservable.addListener((e,o,n)->{
            leftProperty.setValue(converter.toLeft(n));
        });
    }

    public static <L, R> void bindBidirectional(
            final @NonNull Property<L> leftProperty,
            final @NonNull Bindable<R> rightProperty,
            final @NonNull BindingConverter<L, R> converter) {
        val binding = new InternalBidirBinding<L, R>(leftProperty, rightProperty, converter);
        leftProperty.setValue(converter.toLeft(rightProperty.getValue()));
        leftProperty.addListener(binding);
        rightProperty.addListener(binding);
    }

    public static void bindParsableBidirectional(
            final @NonNull Property<String> leftProperty,
            final @NonNull Bindable<String> rightProperty) {
        bindBidirectional(leftProperty, rightProperty, BindingConverter.identity(String.class));
    }

    // -- VALIDATION

    public static void bindValidationFeeback(
            final @NonNull StringProperty textProperty,
            final @NonNull Property<Boolean> visibilityProperty,
            final @NonNull Observable<String> textObservable) {

        bind(textProperty, textObservable);
        visibilityProperty.bind(textProperty.isNotEmpty());
    }

    // -- INTERNAL

    private static class InternalBidirBinding<L, R>
    implements
        javafx.beans.value.ChangeListener<L>,
        ChangeListener<R>{

        private final WeakReference<Property<L>> leftRef;
        private final WeakReference<Bindable<R>> rightRef;
        private final BindingConverter<L, R> converter;
        private boolean updating = false;
        private final int cachedHash;

        private Property<L> getLeft() {
            return leftRef.get();
        }

        private Bindable<R> getRight() {
            return rightRef.get();
        }


        public InternalBidirBinding(
                final @NonNull Property<L> left,
                final @NonNull Bindable<R> right,
                final @NonNull BindingConverter<L, R> converter) {

            this.leftRef = new WeakReference<>(left);
            this.rightRef = new WeakReference<>(right);
            this.converter = converter;
            cachedHash = Objects.hash(left, right, converter);
        }

        @Override
        public void changed(final ObservableValue<? extends L> leftObservable, final L oldValue, final L newValue) {
            changed(oldValue, newValue, null, null);
        }

        @Override
        public void changed(
                final Observable<? extends R> rightObservable,
                final R oldValue,
                final R newValue) {
            changed(null, null, oldValue, newValue);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            final Object left = getLeft();
            final Object right = getRight();
            if ((left == null) || (right == null)) {
                return false;
            }

            if (obj instanceof InternalBidirBinding) {
                final InternalBidirBinding<?, ?> otherBinding = (InternalBidirBinding<?, ?>) obj;
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

        @Override
        public int hashCode() {
            return cachedHash;
        }

        // -- HELPER

        /**
         * @param oldPojo
         * @param newPojo
         * @param oldValue
         * @param newValue
         * @apiNote not pretty, but to not having to duplicate this logic:
         * either uses both pojos and ignores both managed objects (propagate changes left to right)
         * or vice versa.
         */
        private void changed(final L oldPojo, final L newPojo, final R oldValue, final R newValue) {
            if (updating) {
                return;
            }
            val left = getLeft();
            val right = getRight();
            if(!isStillBound(left, right)) {
                return;
            }

            try {
                updating = true;
                if(newValue!=null) { // direction
                    left.setValue(converter.toLeft(newValue)); // propagate changes right to left
                } else {
                    right.setValue(converter.toRight(newPojo)); // propagate changes left to right
                }
            } catch (RuntimeException e) {
                try {
                    if(newValue!=null) { // direction
                        left.setValue(converter.toLeft(oldValue)); // propagate changes right to left
                    } else {
                        right.setValue(converter.toRight(oldPojo)); // propagate changes left to right
                    }
                } catch (Exception e2) {
                    e2.addSuppressed(e);
                    left.removeListener(this);
                    right.removeListener(this);
                    throw _Exceptions.unrecoverable(e2,
                            "Bidirectional binding failed with an attempt to restore the "
                            + "Observable to the previous value. "
                            + "Removing the bidirectional binding from bindables %s and %s",
                            ""+left,
                            ""+right);
                }
                throw _Exceptions.unrecoverable(e,
                        "Bidirectional binding failed, setting to the previous value");
            } finally {
                updating = false;
            }
        }

        private boolean isStillBound(
                final Property<L> left,
                final Bindable<R> right) {

            if ((left == null) || (right == null)) {
                if (left != null) {
                    left.removeListener(this);
                }
                if (right != null) {
                    right.removeListener(this);
                }
                return false;
            }
            return true;
        }

    }


}

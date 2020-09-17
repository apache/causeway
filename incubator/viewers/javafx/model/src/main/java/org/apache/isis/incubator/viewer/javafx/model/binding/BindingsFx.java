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
package org.apache.isis.incubator.viewer.javafx.model.binding;

import java.lang.ref.WeakReference;
import java.util.Objects;

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.binding.ChangeListener;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.binding.BindingConverter;

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
    
    public static <L> void bind(
            final @NonNull Property<L> leftProperty, 
            final @NonNull Observable<ManagedObject> rightObservable, 
            final @NonNull BindingConverter<L> converter) {

        leftProperty.setValue(converter.unwrap(rightObservable.getValue()));
        rightObservable.addListener((e,o,n)->{
            leftProperty.setValue(converter.unwrap(n));
        });
    }

    public static <L> void bindBidirectional(
            final @NonNull Property<L> leftProperty, 
            final @NonNull Bindable<ManagedObject> rightProperty, 
            final @NonNull BindingConverter<L> converter) {
        final InternalBidirBinding<L> binding = new InternalBidirBinding<L>(leftProperty, rightProperty, converter);
        leftProperty.setValue(converter.unwrap(rightProperty.getValue()));
        leftProperty.addListener(binding);
        rightProperty.addListener(binding);
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
    
    private static class InternalBidirBinding<T> 
    implements 
        javafx.beans.value.ChangeListener<T>, 
        ChangeListener<ManagedObject>{

        private final WeakReference<Property<T>> leftRef;
        private final WeakReference<Bindable<ManagedObject>> rightRef;
        private final BindingConverter<T> converter;
        private boolean updating = false;
        private final int cachedHash;

        private Property<T> getLeft() {
            return leftRef.get();
        }

        private Bindable<ManagedObject> getRight() {
            return rightRef.get();
        }
        
        
        public InternalBidirBinding(
                final @NonNull Property<T> left, 
                final @NonNull Bindable<ManagedObject> right, 
                final @NonNull BindingConverter<T> converter) {

            this.leftRef = new WeakReference<>(left);
            this.rightRef = new WeakReference<>(right);
            this.converter = converter;
            cachedHash = Objects.hash(left, right, converter);
        }

        @Override
        public void changed(ObservableValue<? extends T> leftObservable, T oldValue, T newValue) {
            changed(oldValue, newValue, null, null);
        }

        @Override
        public void changed(
                Observable<? extends ManagedObject> rightObservable, 
                ManagedObject oldValue,
                ManagedObject newValue) {
            changed(null, null, oldValue, newValue);
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

            if (obj instanceof InternalBidirBinding) {
                final InternalBidirBinding<?> otherBinding = (InternalBidirBinding<?>) obj;
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
        private void changed(T oldPojo, T newPojo, ManagedObject oldValue, ManagedObject newValue) {
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
                    left.setValue(converter.unwrap(newValue)); // propagate changes right to left
                } else { 
                    right.setValue(converter.wrap(newPojo)); // propagate changes left to right
                }
            } catch (RuntimeException e) {
                try {
                    if(newValue!=null) { // direction
                        left.setValue(converter.unwrap(oldValue)); // propagate changes right to left
                    } else { 
                        right.setValue(converter.wrap(oldPojo)); // propagate changes left to right
                    }
                } catch (Exception e2) {
                    e2.addSuppressed(e);
                    left.removeListener(this);
                    right.removeListener(this);
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
        
        private boolean isStillBound(
                final Property<T> left, 
                final Bindable<ManagedObject> right) {
            
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

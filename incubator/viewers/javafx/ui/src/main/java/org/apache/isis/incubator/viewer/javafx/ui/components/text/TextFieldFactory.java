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
package org.apache.isis.incubator.viewer.javafx.ui.components.text;

import java.lang.ref.WeakReference;
import java.util.Objects;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.ChangeListener;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.value.string.StringValueFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.ComponentRequest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TextFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(ComponentRequest request) {
        return request.hasFeatureTypeFacet(StringValueFacet.class);
    }

    @Override
    public Node handle(ComponentRequest request) {

        val uiComponent = new TextField();
        val valueSpec = request.getFeatureTypeSpec();
        val converter = Converter.<String>of(valueSpec);

        if(request.getManagedFeature() instanceof ManagedParameter) {

            val managedParameter = (ManagedParameter)request.getManagedFeature();

            bindBidirectional(
                    uiComponent.textProperty(),
                    managedParameter.getValue(),
                    converter);

            //TODO bind parameter validation feedback

        } else if(request.getManagedFeature() instanceof ManagedProperty) {

            val managedProperty = (ManagedProperty)request.getManagedFeature();

            // readonly binding
            bind(
                    uiComponent.textProperty(),
                    managedProperty.getValue(),
                    converter);

            //TODO allow property editing
            //TODO bind property validation feedback
        }

        return uiComponent;
    }

    // -- TODO these binding helpers need to move (for reuse) ...
    
    @RequiredArgsConstructor(staticName = "of")
    private static final class Converter<T> {

        private final ObjectSpecification valueSpec;

        public T unwrap(ManagedObject object) {
            return _Casts.uncheckedCast(ManagedObjects.UnwrapUtil.single(object));
        }

        public ManagedObject wrap(T pojo) {
            return ManagedObject.of(valueSpec, pojo);
        }
    }
    
    private static class BidirBinding<T> 
    implements 
        javafx.beans.value.ChangeListener<T>, 
        ChangeListener<ManagedObject>{

        private final WeakReference<Property<T>> leftRef;
        private final WeakReference<Bindable<ManagedObject>> rightRef;
        private final Converter<T> converter;
        private boolean updating = false;
        private final int cachedHash;

        private Property<T> getLeft() {
            return leftRef.get();
        }

        private Bindable<ManagedObject> getRight() {
            return rightRef.get();
        }
        
        
        public BidirBinding(
                final @NonNull Property<T> left, 
                final @NonNull Bindable<ManagedObject> right, 
                final @NonNull Converter<T> converter) {

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

            if (obj instanceof BidirBinding) {
                final BidirBinding<?> otherBinding = (BidirBinding<?>) obj;
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
                if(newValue!=null) {
                    left.setValue(converter.unwrap(newValue));
                } else {
                    right.setValue(converter.wrap(newPojo));
                }
            } catch (RuntimeException e) {
                try {
                    if(newValue!=null) {
                        left.setValue(converter.unwrap(oldValue));
                    } else {
                        right.setValue(converter.wrap(oldPojo));
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

    public static <L, R> void bind(
            final @NonNull Property<L> leftProperty, 
            final @NonNull Observable<ManagedObject> rightObservable, 
            final @NonNull Converter<L> converter) {

        leftProperty.setValue(converter.unwrap(rightObservable.getValue()));
        rightObservable.addListener((e,o,n)->{
            leftProperty.setValue(converter.unwrap(n));
        });
    }

    public static <L, R> void bindBidirectional(
            final @NonNull Property<L> leftProperty, 
            final @NonNull Bindable<ManagedObject> rightProperty, 
            final @NonNull Converter<L> converter) {
        final BidirBinding<L> binding = new BidirBinding<L>(leftProperty, rightProperty, converter);
        leftProperty.setValue(converter.unwrap(rightProperty.getValue()));
        leftProperty.addListener(binding);
        rightProperty.addListener(binding);
    }

}

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
package org.apache.isis.testdomain.interact;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class SimulatedUiComponent extends HasValueValidation {
    
    private Bindable<ManagedObject> value = _Bindables.empty();

    private ObjectSpecification valueSpec;
    
    public void bind(ManagedValue managedValue) {
        valueSpec = managedValue.getSpecification();
        value.bindBidirectional(managedValue.getValue());
        super.bind(managedValue);
    }

    /** just listening, no validation */
    public void bind(ManagedProperty managedProperty) {
        value.bind(managedProperty.getValue());
    }
    
    public void simulateValueChange(Object newValue) {
        value.setValue(ManagedObject.of(valueSpec, newValue));
    }

    public ManagedObject getValue() {
        return value.getValue();
    }

    

    
}

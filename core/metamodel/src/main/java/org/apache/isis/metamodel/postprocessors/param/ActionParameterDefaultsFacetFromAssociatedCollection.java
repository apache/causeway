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

package org.apache.isis.metamodel.postprocessors.param;

import java.util.List;
import java.util.function.Function;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.param.defaults.ActionParameterDefaultsFacetAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;

public class ActionParameterDefaultsFacetFromAssociatedCollection extends ActionParameterDefaultsFacetAbstract {

    private static ThreadLocal<Can<Object>> _selectedPojos = ThreadLocal.withInitial(Can::empty);

    public static <T, R> R applyWithSelected(
            final Can<Object> selectedPojos, 
            final Function<T, R> function,
            final T argument) {
        
        try {
            _selectedPojos.set(selectedPojos);
            return function.apply(argument);
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            _selectedPojos.set(Can.empty());
        }
    }

    public ActionParameterDefaultsFacetFromAssociatedCollection(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public Object getDefault(
            final ManagedObject target,
            final List<ManagedObject> pendingArgs,
            final Integer paramNumUpdated) {
        
        return _selectedPojos.get();
    }


}

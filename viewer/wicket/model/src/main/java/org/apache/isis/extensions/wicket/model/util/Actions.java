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


package org.apache.isis.extensions.wicket.model.util;


import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;

import com.google.common.base.Predicate;

public final class Actions {

	private Actions(){}
	
	public static Predicate<ObjectAction> ofType(
			final ObjectActionType type) {
		return new Predicate<ObjectAction>(){
			public boolean apply(ObjectAction input) {
				return input.getType() == type;
			}};
	}

    public static String labelFor(final ObjectAction noAction) {
        String actionName = Actions.nameFor(noAction);
        String actionLabel = actionName;
        if (noAction.getParameterCount() > 0) {
            actionLabel += "...";
        }
        return actionLabel;
    }

    public static String labelFor(ObjectAction action,
            final ObjectAdapter contextAdapter) {
        if(action.getParameterCount() == 1) {
            final ObjectActionParameter actionParam = action.getParameters()[0];
            if (ActionParams.compatibleWith(contextAdapter, actionParam)) {
                return nameFor(action);
            }
        }
        return labelFor(action);
    }
    
    private static String nameFor(final ObjectAction noAction) {
        String actionName = noAction.getName();
        if (actionName != null) {
            return actionName;
        } 
        NamedFacet namedFacet = noAction.getFacet(NamedFacet.class);
        if (namedFacet != null) {
            return namedFacet.value();
        }
        return  "(no name)";
    }

}

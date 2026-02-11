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
package org.apache.causeway.core.metamodel.inspect.model;

import java.util.function.Supplier;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".MetamodelInspectMenu")
@DomainService
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class MetamodelInspectMenu {

    @Action(restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(
            named = "Inspect type",
            describedAs = "Opens a meta-model inspection view for given fully qualified domain class",
            cssClassFa = "solid shapes")
    public MetamodelInspectView inspect(final String fullyQualifiedClassName) {
    	return inspect(fullyQualifiedClassName, ()->null);
    }
    @MemberSupport
    public String validate0Inspect(final String fullyQualifiedClassName) {
    	return validateClassName(fullyQualifiedClassName);
    }

    // -- UTIL

    static MetamodelInspectView inspect(final String fullyQualifiedClassName, final Supplier<MetamodelInspectView> fallback) {
    	try {
    		var classOfInterest = _Context.loadClass(fullyQualifiedClassName);
    		return MetaModelContext.instance()
    	        	.map(MetaModelContext::getSpecificationLoader)
    	        	.map(specLoader->specLoader.loadSpecification(classOfInterest))
    	        	.map(MetamodelInspectView::root)
    	        	.orElseGet(fallback);
    	} catch (ClassNotFoundException e) {
    		// unexpected, as covered by validate
		}
    	return null;
    }

    static String validateClassName(final String fullyQualifiedClassName) {
    	try {
    		_Context.loadClass(fullyQualifiedClassName);
    	} catch (ClassNotFoundException e) {
			return e.getMessage();
		}
    	return null;
    }

}

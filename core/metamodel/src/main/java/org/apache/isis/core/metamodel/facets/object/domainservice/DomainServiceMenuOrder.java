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

package org.apache.isis.core.metamodel.facets.object.domainservice;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.core.commons.compare.SequenceCompare;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class DomainServiceMenuOrder {

	public static int compare(ObjectAdapter serviceAdapter1, ObjectAdapter serviceAdapter2) {
		
		return SequenceCompare.compareNullLast(
				orderOf(serviceAdapter1.getSpecification().getCorrespondingClass()),
				orderOf(serviceAdapter2.getSpecification().getCorrespondingClass()) );
	}
	
	// -- HELPER
    
    public static String orderOf(final Class<?> cls) {
        final DomainServiceLayout domainServiceLayout = cls.getAnnotation(DomainServiceLayout.class);
        String dslayoutOrder = domainServiceLayout != null ? domainServiceLayout.menuOrder(): null;
        final DomainService domainService = cls.getAnnotation(DomainService.class);
        String dsOrder = domainService != null ? domainService.menuOrder() : "" + Integer.MAX_VALUE;

        return minimumOf(dslayoutOrder, dsOrder);
    }

    private static String minimumOf(final String dslayoutOrder, final String dsOrder) {
        if(isUndefined(dslayoutOrder)) {
            return dsOrder;
        }
        if(isUndefined(dsOrder)) {
            return dslayoutOrder;
        }
        return dslayoutOrder.compareTo(dsOrder) < 0 ? dslayoutOrder : dsOrder;
    }

    private static boolean isUndefined(final String str) {
        return str == null || str.equals("" + Integer.MAX_VALUE);
    }

}

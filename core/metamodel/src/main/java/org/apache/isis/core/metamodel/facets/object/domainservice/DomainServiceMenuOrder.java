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

import javax.annotation.Priority;

import org.apache.isis.applib.annotation.Constants;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.core.commons.compare.SequenceCompare;

public class DomainServiceMenuOrder {

    private DomainServiceMenuOrder(){}
	
    public static String orderOf(final Class<?> cls) {
        final Priority priority = cls.getAnnotation(Priority.class);
        final DomainService domainService = cls.getAnnotation(DomainService.class);
        final DomainServiceLayout domainServiceLayout = cls.getAnnotation(DomainServiceLayout.class);

        String dsOrder = priority !=null ? new Integer(priority.value()).toString() :
                domainService != null ? domainService.menuOrder() : null;
        String dslayoutOrder = domainServiceLayout != null ? domainServiceLayout.menuOrder(): null;

        String min = minimumOf(dslayoutOrder, dsOrder);
        return min!=null ? min : Constants.MENU_ORDER_DEFAULT;
    }

	// -- HELPER

    private static String minimumOf(final String dslayoutOrder, final String dsOrder) {
        if(isUndefined(dslayoutOrder))
            return dsOrder;
        if(isUndefined(dsOrder))
            return dslayoutOrder;
        
        //XXX ISIS-1715 honor member order (use Dewey Decimal format)
        return SequenceCompare.compareNullLast(dslayoutOrder, dsOrder) < 0
        		? dslayoutOrder 
        		: dsOrder;
    }

    private static boolean isUndefined(final String str) {
        return str == null || str.equals(Constants.MENU_ORDER_DEFAULT);
    }

}

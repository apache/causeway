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

package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceMenuOrder;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;

class ServiceAndActionOrder {

	private final static MemberOrderFacetComparator memberOrder = 
			new MemberOrderFacetComparator(false);
	
	public static int compare(ServiceAndAction a, ServiceAndAction b) {
		
		int c  = a.serviceName.compareTo(b.serviceName);
		if(c!=0)
			return c;
		
		c = DomainServiceMenuOrder.compare(
				a.serviceEntityModel.getObject(),
				b.serviceEntityModel.getObject() );
		if(c!=0)
			return c;
		
		return memberOrder.compare(
				a.objectAction.getFacet(MemberOrderFacet.class), 
				b.objectAction.getFacet(MemberOrderFacet.class) );
		
	}

}

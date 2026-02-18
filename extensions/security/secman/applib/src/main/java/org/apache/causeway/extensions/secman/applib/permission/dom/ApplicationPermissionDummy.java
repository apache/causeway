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
package org.apache.causeway.extensions.secman.applib.permission.dom;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;

/**
 * Just a marker, to have the static-weaving processor pickup inherited abstract classes.
 * @deprecated not required in 4.x (as abstract classes were refactored into interfaces) 
 */
@Deprecated
@Domain.Exclude
@Entity
public class ApplicationPermissionDummy extends org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission {

	@Id
    @GeneratedValue
    private Long id;

	@Override
	public ApplicationRole getRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRole(ApplicationRole applicationRole) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApplicationPermissionRule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRule(ApplicationPermissionRule rule) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApplicationPermissionMode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMode(ApplicationPermissionMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApplicationFeatureSort getFeatureSort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFeatureSort(ApplicationFeatureSort featureSort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFeatureFqn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFeatureFqn(String featureFqn) {
		// TODO Auto-generated method stub
		
	}

}

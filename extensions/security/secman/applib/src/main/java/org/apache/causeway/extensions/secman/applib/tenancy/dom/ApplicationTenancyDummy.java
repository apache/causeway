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
package org.apache.causeway.extensions.secman.applib.tenancy.dom;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.causeway.applib.annotation.Domain;

/**
 * Just a marker, to have the static-weaving processor pickup inherited abstract classes.
 * @deprecated not required in 4.x (as abstract classes were refactored into interfaces) 
 */
@Deprecated
@Domain.Exclude
@Entity
public class ApplicationTenancyDummy extends ApplicationTenancy {

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApplicationTenancy getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(ApplicationTenancy parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<ApplicationTenancy> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}
}

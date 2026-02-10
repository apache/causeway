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
package org.apache.causeway.testdomain.model.bad;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InvalidDomainObjectOnInterface {
	
	// -- ABSTRACT TYPES
	
	@DomainObject(nature = Nature.VIEW_MODEL)
	public interface Interface1 {
		
	}
	
	@DomainObject(nature = Nature.VIEW_MODEL)
	public interface Interface2 {
		
	}
	
	@DomainObject(nature = Nature.VIEW_MODEL)
	public abstract class Abstract {
		
	}
	
	// -- CONCRETE TYPES
	
	@DomainObject(nature = Nature.VIEW_MODEL)
	public class Good implements Interface1 {
		
	}
	
	// not allowed, should fail MM validation CAUSEWAY-3965
	@DomainObject(nature = Nature.VIEW_MODEL)
	public class BadlyInterfaced implements Interface1, Interface2 {
		
	}
	
	// not allowed, should fail MM validation CAUSEWAY-3965
	@DomainObject(nature = Nature.VIEW_MODEL)
	public class BadlyMixed extends Abstract implements Interface1 {
		
	}

}

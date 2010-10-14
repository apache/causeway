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


package org.apache.isis.commons.jmock;


import org.jmock.Mockery;
import org.apache.isis.commons.jmock.MockFixture.Builder;

public class FixtureMockery extends DelegatingMockery {
	
	public FixtureMockery(Mockery underlying) {
		super(underlying); 
	}
	
	public FixtureMockery() {
		super(); 
	}
	
	public <T extends MockFixture<?>> T fixture(Class<T> fixtureClass) {
		final Builder<T> fixtureBuilder = fixtureBuilder(fixtureClass);
		return fixtureBuilder.build();
	}
	
	public <T extends MockFixture<?>> T fixture(Class<T> fixtureClass, String name) {
		final Builder<T> fixtureBuilder = fixtureBuilder(fixtureClass);
		fixtureBuilder.named(name);
		return fixtureBuilder.build();
	}

	public <T extends MockFixture<?>> MockFixture.Builder<T> fixtureBuilder(Class<T> fixtureClass) {
		return new MockFixture.Builder<T>(this, fixtureClass);
	}
	

}

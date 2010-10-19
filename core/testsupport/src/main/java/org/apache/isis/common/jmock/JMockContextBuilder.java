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


package org.apache.isis.common.jmock;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

public final class JMockContextBuilder {

	public static enum Imposterising {
		INTERFACE_ONLY {
			@Override
			public ConvenienceMockery setUp(ConvenienceMockery mockery) {
				return mockery;
			}
		},
		INTERFACES_AND_CLASSES {
			@Override
			public ConvenienceMockery setUp(ConvenienceMockery mockery) {
				mockery.setImposteriser(ClassImposteriser.INSTANCE);
				return mockery;
			}
		};

		public abstract ConvenienceMockery setUp(ConvenienceMockery mockery);
	}

	public static enum MockFixtures {
		SUPPORTED {
			@Override
			public ConvenienceMockery create(Imposterising imposterising,
					Mockery underlying) {
				FixtureMockery mockery = new FixtureMockery(underlyingOrDefault(underlying));
				return imposterising.setUp(mockery);
			}

		},
		NOT_SUPPORTED {
			@Override
			public ConvenienceMockery create(Imposterising imposterising,
					Mockery underlying) {
				DelegatingMockery mockery = new DelegatingMockery(underlyingOrDefault(underlying));
				return imposterising.setUp(mockery);
			}
		};

		abstract ConvenienceMockery create(Imposterising imposterising, Mockery underlying);
		
		private static Mockery underlyingOrDefault(Mockery underlying) {
			return underlying != null ? underlying : new JUnit4Mockery();
		}

	}

	public static JMockContextBuilder context() {
		return new JMockContextBuilder();
	}
	
	/**
	 * Convenience factory for commonly-trod path.
	 */
	public static ConvenienceMockery interfaceMockingContext() {
		return context().build();
	}

	/**
	 * Convenience factory for commonly-trod path.
	 */
	public static ConvenienceMockery classMockingContext() {
		return context().with(Imposterising.INTERFACES_AND_CLASSES).build();
	}
	
	private Imposterising imposterising = Imposterising.INTERFACE_ONLY;
	private MockFixtures mockFixtures = MockFixtures.NOT_SUPPORTED;
	private Mockery underlying = null;
	
	public JMockContextBuilder with(Imposterising imposterising) {
		this.imposterising = imposterising;
		return this;
	}

	public JMockContextBuilder with(MockFixtures mockFixtures) {
		this.mockFixtures = mockFixtures;
		return this;
	}

	public JMockContextBuilder delegatingTo(Mockery underlying) {
		this.underlying = underlying;
		return this;
	}

	public ConvenienceMockery build() {
		return mockFixtures.create(imposterising, underlying);
	}
}

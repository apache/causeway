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

import org.jmock.Expectations;
import org.jmock.Mockery;

/**
 * Adds some convenience methods to {@link Mockery}.
 */
public abstract class ConvenienceMockery extends Mockery {
	
    /**
     * Ignoring any interaction with the mock; an allowing/ignoring mock will be
     * returned in turn.
     */
    public void ignoring(final Object mock) {
        checking(new Expectations() {
        {
                ignoring(mock);
            }
        });
    }

    /**
     * Allow any interaction with the mock; an allowing mock will be
     * returned in turn.
     */
    public void allowing(final Object mock) {
        checking(new Expectations() {
        {
                allowing(mock);
            }
        });
    }

    /**
     * Prohibit any interaction with the mock.
     */
    public void never(final Object mock) {
        checking(new Expectations() {
        {
                never(mock);
            }
        });
    }

    /**
     * Same as {@link #never(Object)}.
     */
    public void prohibit(final Object mock) {
    	never(mock);
    }

	public <T> T mockAndIgnoreAnyInteraction(Class<T> typeToMock) {
		final T mock = mock(typeToMock);
		checking(new Expectations(){{
			ignoring(mock);
		}});
		return mock;
	}
	
	public <T> T mockAndIgnoreAnyInteraction(Class<T> typeToMock, String name) {
		final T mock = mock(typeToMock, name);
		checking(new Expectations(){{
			ignoring(mock);
		}});
		return mock;
	}
	
	public <T> T mockAndAllowAnyInteraction(Class<T> typeToMock) {
		final T mock = mock(typeToMock);
		checking(new Expectations(){{
			allowing(mock);
		}});
		return mock;
	}
	
	public <T> T mockAndAllowAnyInteraction(Class<T> typeToMock, String name) {
		final T mock = mock(typeToMock, name);
		checking(new Expectations(){{
			allowing(mock);
		}});
		return mock;
	}
	
	public <T> T mockAndNeverAnyInteraction(Class<T> typeToMock) {
		final T mock = mock(typeToMock);
		checking(new Expectations(){{
			never(mock);
		}});
		return mock;
	}
	
	public <T> T mockAndNeverAnyInteraction(Class<T> typeToMock, String name) {
		final T mock = mock(typeToMock, name);
		checking(new Expectations(){{
			never(mock);
		}});
		return mock;
	}
	

	/**
	 * Same as {@link #mockAndNeverAnyInteraction(Class)}.
	 */
	public <T> T mockAndProhibitAnyInteraction(Class<T> typeToMock) {
		return mockAndNeverAnyInteraction(typeToMock);
	}
	
	/**
	 * Same as {@link #mockAndNeverAnyInteraction(Class, String)}.
	 */
	public <T> T mockAndProhibitAnyInteraction(Class<T> typeToMock, String name) {
		return mockAndNeverAnyInteraction(typeToMock, name);
	}
}

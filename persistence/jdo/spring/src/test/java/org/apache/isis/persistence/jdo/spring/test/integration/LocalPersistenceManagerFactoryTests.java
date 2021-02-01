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
package org.apache.isis.persistence.jdo.spring.test.integration;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManagerFactory;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;

class LocalPersistenceManagerFactoryTests {

	@Test
	void testLocalPersistenceManagerFactoryBean() throws IOException {
		final PersistenceManagerFactory pmf = mock(PersistenceManagerFactory.class);
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			@Override
			protected PersistenceManagerFactory newPersistenceManagerFactory(Map<?, ?> props) {
				return pmf;
			}
		};
		pmfb.setJdoProperties(new Properties());
		pmfb.afterPropertiesSet();
		assertSame(pmf, pmfb.getObject());
	}

	@Test
	void testLocalPersistenceManagerFactoryBeanWithInvalidSettings() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean();
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown JDOFatalUserException");
		}
		catch (JDOFatalUserException ex) {
			// expected
		}
	}

	@Test
	void testLocalPersistenceManagerFactoryBeanWithIncompleteProperties() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean();
		Properties props = new Properties();
		props.setProperty("myKey", "myValue");
		pmfb.setJdoProperties(props);
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown JDOFatalUserException");
		}
		catch (JDOFatalUserException ex) {
			// expected
		}
	}

	@Test
	void testLocalPersistenceManagerFactoryBeanWithInvalidProperty() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			@Override
			protected PersistenceManagerFactory newPersistenceManagerFactory(Map<?, ?> props) {
				throw new IllegalArgumentException((String) props.get("myKey"));
			}
		};
		Properties props = new Properties();
		props.setProperty("myKey", "myValue");
		pmfb.setJdoProperties(props);
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("myValue".equals(ex.getMessage()), "Correct exception");
		}
	}

	@Test
	void testLocalPersistenceManagerFactoryBeanWithFile() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			@Override
			protected PersistenceManagerFactory newPersistenceManagerFactory(Map<?, ?> props) {
				throw new IllegalArgumentException((String) props.get("myKey"));
			}
		};
		pmfb.setConfigLocation(new ClassPathResource("test.properties", getClass()));
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("myValue".equals(ex.getMessage()), "Correct exception");
		}
	}

	@Test
	public void testLocalPersistenceManagerFactoryBeanWithName() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			@Override
			protected PersistenceManagerFactory newPersistenceManagerFactory(String name) {
				throw new IllegalArgumentException(name);
			}
		};
		pmfb.setPersistenceManagerFactoryName("myName");
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("myName".equals(ex.getMessage()), "Correct exception");
		}
	}

	@Test
	public void testLocalPersistenceManagerFactoryBeanWithNameAndProperties() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			@Override
			protected PersistenceManagerFactory newPersistenceManagerFactory(String name) {
				throw new IllegalArgumentException(name);
			}
		};
		pmfb.setPersistenceManagerFactoryName("myName");
		pmfb.getJdoPropertyMap().put("myKey", "myValue");
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("persistenceManagerFactoryName") != -1);
			assertTrue(ex.getMessage().indexOf("jdoProp") != -1);
		}
	}

}

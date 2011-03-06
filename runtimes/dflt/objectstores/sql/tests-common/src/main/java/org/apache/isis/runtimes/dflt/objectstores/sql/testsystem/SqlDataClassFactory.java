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

/**
 * 
 */
package org.apache.isis.runtimes.dflt.objecstores.sql.testsystem;

import java.util.List;

import org.apache.isis.runtimes.dflt.objecstores.sql.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtimes.dflt.objecstores.sql.testsystem.dataclasses.SimpleClassTwo;
import org.apache.isis.runtimes.dflt.objecstores.sql.testsystem.dataclasses.SqlDataClass;
import org.apache.isis.applib.AbstractFactoryAndRepository;

/**
 * @author Kevin
 * 
 */
public class SqlDataClassFactory extends AbstractFactoryAndRepository {
	public List<SqlDataClass> allDataClasses() {
		return allInstances(SqlDataClass.class);
	}

	public SqlDataClass newDataClass() {
		SqlDataClass object = newTransientInstance(SqlDataClass.class);
		return object;
	}

	public void save(SqlDataClass sqlDataClass) {
		persist(sqlDataClass);
	}

	public void delete(SqlDataClass sqlDataClass) {
		remove(sqlDataClass);
	}

	// SimpleClass
	public List<SimpleClass> allSimpleClasses() {
		return allInstances(SimpleClass.class);
	}

	public SimpleClass newSimpleClass() {
		SimpleClass object = newTransientInstance(SimpleClass.class);
		return object;
	}

	public void save(SimpleClass simpleClass) {
		persist(simpleClass);
	}

	public void delete(SimpleClass simpleClass) {
		remove(simpleClass);
	}

	// SimpleClassTwo
	public List<SimpleClassTwo> allSimpleClassTwos() {
		return allInstances(SimpleClassTwo.class);
	}

	public SimpleClassTwo newSimpleClassTwo() {
		SimpleClassTwo object = newTransientInstance(SimpleClassTwo.class);
		return object;
	}

	public void save(SimpleClassTwo simpleClassTwo) {
		persistIfNotAlready(simpleClassTwo);
	}

	public void update(SimpleClassTwo simpleClassTwo) {
		getContainer().objectChanged(simpleClassTwo);
	}

	public void delete(SimpleClassTwo simpleClassTwo) {
		remove(simpleClassTwo);
	}

	public void resolve(final Object domainObject) {
		getContainer().resolve(domainObject);
	}

}

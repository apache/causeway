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
package org.apache.isis.persistence.jdo.spring.integration;

import javax.jdo.PersistenceManager;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

/**
 * Holder wrapping a JDO PersistenceManager.
 * JdoTransactionManager binds instances of this class
 * to the thread, for a given PersistenceManagerFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @see JdoTransactionManager
 * @see PersistenceManagerFactoryUtils
 */
public class PersistenceManagerHolder extends ResourceHolderSupport {

	private final PersistenceManager persistenceManager;

	private boolean transactionActive;


	public PersistenceManagerHolder(PersistenceManager persistenceManager) {
		Assert.notNull(persistenceManager, "PersistenceManager must not be null");
		this.persistenceManager = persistenceManager;
	}


	public PersistenceManager getPersistenceManager() {
		return this.persistenceManager;
	}

	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	protected boolean isTransactionActive() {
		return this.transactionActive;
	}

	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
	}

}

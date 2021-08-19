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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;

import javax.jdo.Constants;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.persistence.jdo.spring.exceptions.JdoResourceFailureException;
import org.apache.isis.persistence.jdo.spring.integration.JdoDialect;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.persistence.jdo.spring.integration.PersistenceManagerFactoryUtils;
import org.apache.isis.persistence.jdo.spring.integration.PersistenceManagerHolder;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;
import org.apache.isis.persistence.jdo.spring.support.SpringPersistenceManagerProxyBean;
import org.apache.isis.persistence.jdo.spring.support.StandardPersistenceManagerProxyBean;

import lombok.val;

class JdoTransactionManagerTests {

    private MetaModelContext mmc;
	private PersistenceManagerFactory pmf;
	private PersistenceManager pm;
	private Transaction tx;


	@BeforeEach
	void setUp() {
	    mmc = mock(MetaModelContext.class);
		pmf = mock(PersistenceManagerFactory.class);
		pm = mock(PersistenceManager.class);
		tx = mock(Transaction.class);
	}

	@AfterEach
	void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
		assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
	}

	@Test
	void testTransactionCommit() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pmf.getPersistenceManagerProxy()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");

				TransactionAwarePersistenceManagerFactoryProxy proxyFactory =
						new TransactionAwarePersistenceManagerFactoryProxy(mmc);
				proxyFactory.setTargetPersistenceManagerFactory(pmf);
				PersistenceManagerFactory pmfProxy = proxyFactory.getObject();
				assertEquals(pm.toString(), pmfProxy.getPersistenceManager().toString());
				pmfProxy.getPersistenceManager().flush();
				pmfProxy.getPersistenceManager().close();

				SpringPersistenceManagerProxyBean proxyBean = new SpringPersistenceManagerProxyBean();
				proxyBean.setPersistenceManagerFactory(pmf);
				proxyBean.afterPropertiesSet();
				PersistenceManager pmProxy = proxyBean.getObject();
				assertSame(pmf, pmProxy.getPersistenceManagerFactory());
				pmProxy.flush();
				pmProxy.close();

				StandardPersistenceManagerProxyBean stdProxyBean = new StandardPersistenceManagerProxyBean();
				stdProxyBean.setPersistenceManagerFactory(pmf);
				PersistenceManager stdPmProxy = stdProxyBean.getObject();
				stdPmProxy.flush();

				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(pm, times(4)).flush();
		verify(pm).close();
		verify(tx).begin();
		verify(tx).commit();
	}

	@Test
	public void testTransactionRollback() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		try {
			tt.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(final TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
					PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
					throw new RuntimeException("application exception");
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(pm).close();
		verify(tx).begin();
		verify(tx).rollback();
	}

	@Test
	public void testTransactionRollbackWithAlreadyRolledBack() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		try {
			tt.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(final TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
					PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
					throw new RuntimeException("application exception");
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(pm).close();
		verify(tx).begin();
	}

	@Test
	public void testTransactionRollbackOnly() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
				status.setRollbackOnly();
				return null;
			}
		});

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		verify(pm).flush();
		verify(pm).close();
		verify(tx).begin();
		verify(tx).rollback();
	}

	@Test
	public void testParticipatingTransactionWithCommit() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		val l = new ArrayList<Object>();
		l.add("test");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {

				return tt.execute(new TransactionCallback<Object>() {
					@Override
					public Object doInTransaction(final TransactionStatus status) {
						PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
						return l;
					}
				});
			}
		});
		assertTrue(result == l, "Correct result list");

		verify(pm).flush();
		verify(pm).close();
		verify(tx).begin();
	}

	@Test
	public void testParticipatingTransactionWithRollback() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(final TransactionStatus status) {
					return tt.execute(new TransactionCallback<Object>() {
						@Override
						public Object doInTransaction(final TransactionStatus status) {
							PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
							throw new RuntimeException("application exception");
						}
					});
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}
		verify(pm).close();
		verify(tx).begin();
		verify(tx).setRollbackOnly();
		verify(tx).rollback();
	}

	@Test
	public void testParticipatingTransactionWithRollbackOnly() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);
		given(tx.getRollbackOnly()).willReturn(true);
		willThrow(new JDOFatalDataStoreException()).given(tx).commit();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		val l = new ArrayList<Object>();
		l.add("test");

		try {
			tt.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(final TransactionStatus status) {
					return tt.execute(new TransactionCallback<Object>() {
						@Override
						public Object doInTransaction(final TransactionStatus status) {
							PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
							status.setRollbackOnly();
							return null;
						}
					});
				}
			});
			fail("Should have thrown JdoResourceFailureException");
		}
		catch (JdoResourceFailureException ex) {
			// expected
		}
		verify(pm).flush();
		verify(pm).close();
		verify(tx).begin();
		verify(tx).setRollbackOnly();
	}

	@Test
	public void testParticipatingTransactionWithWithRequiresNew() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		val l = new ArrayList<Object>();
		l.add("test");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				return tt.execute(new TransactionCallback<Object>() {
					@Override
					public Object doInTransaction(final TransactionStatus status) {
						PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
						return l;
					}
				});
			}
		});
		assertTrue(result == l, "Correct result list");
		verify(tx, times(2)).begin();
		verify(tx, times(2)).commit();
		verify(pm).flush();
		verify(pm, times(2)).close();
	}

	@Test
	public void testParticipatingTransactionWithWithRequiresNewAndPrebound() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(tx.isActive()).willReturn(true);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);

				return tt.execute(new TransactionCallback<Object>() {
					@Override
					public Object doInTransaction(final TransactionStatus status) {
						PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
						return l;
					}
				});
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
		TransactionSynchronizationManager.unbindResource(pmf);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(tx, times(2)).begin();
		verify(tx, times(2)).commit();
		verify(pm).flush();
		verify(pm).close();
	}

	@Test
	public void testJtaTransactionCommit() throws Exception {
		UserTransaction ut = mock(UserTransaction.class);
		given(ut.getStatus()).willReturn(Status.STATUS_NO_TRANSACTION, Status.STATUS_ACTIVE);
		given(pmf.getPersistenceManager()).willReturn(pm);

		JtaTransactionManager ptm = new JtaTransactionManager(ut);
		TransactionTemplate tt = new TransactionTemplate(ptm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations active");
				assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				return l;
			}
		});
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(ut).begin();
		verify(ut).commit();
		verify(pm, times(2)).flush();
		verify(pm, times(2)).close();
	}

	@Test
	public void testParticipatingJtaTransactionWithWithRequiresNewAndPrebound() throws Exception {
		final UserTransaction ut = mock(UserTransaction.class);
		final TransactionManager tm = mock(TransactionManager.class);

		given(ut.getStatus()).willReturn(Status.STATUS_NO_TRANSACTION,
				Status.STATUS_ACTIVE, Status.STATUS_ACTIVE, Status.STATUS_ACTIVE,
				Status.STATUS_ACTIVE, Status.STATUS_ACTIVE);
		given(pmf.getPersistenceManager()).willReturn(pm);

		JtaTransactionManager ptm = new JtaTransactionManager(ut, tm);
		final TransactionTemplate tt = new TransactionTemplate(ptm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				try {
					MockJtaTransaction transaction = new MockJtaTransaction();
					given(tm.suspend()).willReturn(transaction);
				}
				catch (Exception ex) {
				}

				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);

				return tt.execute(new TransactionCallback<Object>() {
					@Override
					public Object doInTransaction(final TransactionStatus status) {
						PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
						return l;
					}
				});
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
		TransactionSynchronizationManager.unbindResource(pmf);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(ut, times(2)).begin();
		verify(pm).flush();
		verify(pm, times(2)).close();
	}

	@Test
	public void testTransactionCommitWithPropagationSupports() {
		given(pmf.getPersistenceManager()).willReturn(pm);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
				assertTrue(!status.isNewTransaction(), "Is not new transaction");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		verify(pm, times(2)).close();
	}

	@Test
	public void testIsolationLevel() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		tt.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
			}
		});
		verify(tx).setIsolationLevel(Constants.TX_SERIALIZABLE);
		verify(pm).close();
	}

	@Test
	public void testTransactionCommitWithPrebound() {
		given(pm.currentTransaction()).willReturn(tx);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
		TransactionSynchronizationManager.unbindResource(pmf);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		verify(tx).begin();
		verify(tx).commit();
	}

	@Test
	public void testTransactionCommitWithDataSource() throws SQLException {
		final DataSource ds = mock(DataSource.class);
		JdoDialect dialect = mock(JdoDialect.class);
		final Connection con = mock(Connection.class);
		ConnectionHandle conHandle = new SimpleConnectionHandle(con);

		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		TransactionTemplate tt = new TransactionTemplate();
		given(dialect.getJdbcConnection(pm, false)).willReturn(conHandle);

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setPersistenceManagerFactory(pmf);
		tm.setDataSource(ds);
		tm.setJdoDialect(dialect);
		tt.setTransactionManager(tm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				assertTrue(TransactionSynchronizationManager.hasResource(ds), "Has thread con");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.hasResource(ds), "Hasn't thread con");

		verify(pm).close();
		verify(dialect).beginTransaction(tx, tt);
		verify(dialect).releaseJdbcConnection(conHandle, pm);
		verify(dialect).cleanupTransaction(null);
		verify(tx).commit();
	}

	@Test
	public void testTransactionCommitWithAutoDetectedDataSource() throws SQLException {
		final DataSource ds = mock(DataSource.class);
		JdoDialect dialect = mock(JdoDialect.class);
		final Connection con = mock(Connection.class);
		ConnectionHandle conHandle = new SimpleConnectionHandle(con);

		given(pmf.getConnectionFactory()).willReturn(ds);
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		TransactionTemplate tt = new TransactionTemplate();
		given(dialect.getJdbcConnection(pm, false)).willReturn(conHandle);

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setPersistenceManagerFactory(pmf);
		tm.setJdoDialect(dialect);
		tm.afterPropertiesSet();
		tt.setTransactionManager(tm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				assertTrue(TransactionSynchronizationManager.hasResource(ds), "Has thread con");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true);
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.hasResource(ds), "Hasn't thread con");

		verify(pm).close();
		verify(dialect).beginTransaction(tx, tt);
		verify(dialect).releaseJdbcConnection(conHandle, pm);
		verify(dialect).cleanupTransaction(null);
		verify(tx).commit();
	}

	@Test
	public void testTransactionCommitWithAutoDetectedDataSourceAndNoConnection() throws SQLException {
		final DataSource ds = mock(DataSource.class);
		final JdoDialect dialect = mock(JdoDialect.class);

		given(pmf.getConnectionFactory()).willReturn(ds);
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		TransactionTemplate tt = new TransactionTemplate();
		given(dialect.getJdbcConnection(pm, false)).willReturn(null);

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setPersistenceManagerFactory(pmf);
		tm.setJdoDialect(dialect);
		tm.afterPropertiesSet();
		tt.setTransactionManager(tm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				assertTrue(!TransactionSynchronizationManager.hasResource(ds), "Hasn't thread con");
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.hasResource(ds), "Hasn't thread con");

		verify(pm).flush();
		verify(pm).close();
		verify(dialect).beginTransaction(tx, tt);
		verify(dialect).cleanupTransaction(null);
		verify(tx).commit();
	}

	@Test
	public void testExistingTransactionWithPropagationNestedAndRollback() throws SQLException {
		doTestExistingTransactionWithPropagationNestedAndRollback(false);
	}

	@Test
	public void testExistingTransactionWithManualSavepointAndRollback() throws SQLException {
		doTestExistingTransactionWithPropagationNestedAndRollback(true);
	}

	private void doTestExistingTransactionWithPropagationNestedAndRollback(final boolean manualSavepoint)
			throws SQLException {

		final DataSource ds = mock(DataSource.class);
		JdoDialect dialect = mock(JdoDialect.class);
		final Connection con = mock(Connection.class);
		DatabaseMetaData md = mock(DatabaseMetaData.class);
		Savepoint sp = mock(Savepoint.class);

		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);
		given(md.supportsSavepoints()).willReturn(true);
		given(con.getMetaData()).willReturn(md);
		given(con.setSavepoint(ConnectionHolder.SAVEPOINT_NAME_PREFIX + 1)).willReturn(sp);
		final TransactionTemplate tt = new TransactionTemplate();
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		ConnectionHandle conHandle = new SimpleConnectionHandle(con);
		given(dialect.getJdbcConnection(pm, false)).willReturn(conHandle);
		given(tx.isActive()).willReturn(!manualSavepoint);

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setNestedTransactionAllowed(true);
		tm.setPersistenceManagerFactory(pmf);
		tm.setDataSource(ds);
		tm.setJdoDialect(dialect);
		tt.setTransactionManager(tm);
		val l = new ArrayList<Object>();
		l.add("test");
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");

		Object result = tt.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				assertTrue(TransactionSynchronizationManager.hasResource(ds), "Has thread con");
				if (manualSavepoint) {
					Object savepoint = status.createSavepoint();
					status.rollbackToSavepoint(savepoint);
				}
				else {
					tt.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(final TransactionStatus status) {
							assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread session");
							assertTrue(TransactionSynchronizationManager.hasResource(ds), "Has thread connection");
							status.setRollbackOnly();
						}
					});
				}
				PersistenceManagerFactoryUtils.getPersistenceManager(pmf, true).flush();
				return l;
			}
		});
		assertTrue(result == l, "Correct result list");

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.hasResource(ds), "Hasn't thread con");
		verify(pm).flush();
		verify(pm).close();
		verify(con).setSavepoint(ConnectionHolder.SAVEPOINT_NAME_PREFIX + 1);
		verify(con).rollback(sp);
		verify(dialect).beginTransaction(tx, tt);
		verify(dialect).releaseJdbcConnection(conHandle, pm);
		verify(dialect).cleanupTransaction(null);
		verify(tx).commit();
	}

	@Test
	public void testTransactionFlush() {
		given(pmf.getPersistenceManager()).willReturn(pm);
		given(pm.currentTransaction()).willReturn(tx);

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");

		tt.execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(final TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(pmf), "Has thread pm");
				status.flush();
			}
		});

		assertTrue(!TransactionSynchronizationManager.hasResource(pmf), "Hasn't thread pm");
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive(), "JTA synchronizations not active");
		verify(pm).flush();
		verify(pm).close();
		verify(tx).begin();
		verify(tx).commit();
	}

}

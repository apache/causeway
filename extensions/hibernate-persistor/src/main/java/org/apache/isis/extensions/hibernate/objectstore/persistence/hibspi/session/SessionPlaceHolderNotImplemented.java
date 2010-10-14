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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.session;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.query.HQLQueryPlan;
import org.hibernate.engine.query.sql.NativeSQLQuerySpecification;
import org.hibernate.event.EventListeners;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.jdbc.Batcher;
import org.hibernate.jdbc.JDBCContext;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.type.Type;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;


/**
 * An implementation of {@link Session} and also {@link SessionImplementor} where
 * every method just throws a {@link NotYetImplementedException}.
 */
public class SessionPlaceHolderNotImplemented implements Session, SessionImplementor {

    private static final long serialVersionUID = 1L;

    
    @SuppressWarnings("unchecked")
    public Criteria createCriteria(final Class persistentClass, final String alias) {
        notImplemented();
        return null;
    }

    public Criteria createCriteria(final String entityName, final String alias) {
        notImplemented();
        return null;
    }

    @SuppressWarnings("unchecked")
    public Criteria createCriteria(final Class persistentClass) {
        notImplemented();
        return null;
    }

    public Criteria createCriteria(final String entityName) {
        notImplemented();
        return null;
    }


    
    public Query createQuery(final String queryString) throws HibernateException {
        notImplemented();
        return null;
    }

    
    
    
    private void notImplemented() {
        throw new NotYetImplementedException("Not Implemented");
    }

    public void afterScrollOperation() {
        notImplemented();
    }

    public void afterTransactionCompletion(final boolean arg0, final Transaction arg1) {
        notImplemented();
    }

    public void beforeTransactionCompletion(final Transaction arg0) {
        notImplemented();
    }

    public String bestGuessEntityName(final Object arg0) {
        notImplemented();
        return null;
    }

    public Connection connection() {
        notImplemented();
        return null;
    }

    public int executeNativeUpdate(final NativeSQLQuerySpecification arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return 0;
    }

    public int executeUpdate(final String arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return 0;
    }

    public void flush() {
        notImplemented();
    }

    public Batcher getBatcher() {
        notImplemented();
        return null;
    }

    public CacheMode getCacheMode() {
        notImplemented();
        return null;
    }

    public Serializable getContextEntityIdentifier(final Object arg0) {
        notImplemented();
        return null;
    }

    public int getDontFlushFromFind() {
        notImplemented();
        return 0;
    }

    public Map<?, ?> getEnabledFilters() {
        notImplemented();
        return null;
    }

    public EntityMode getEntityMode() {
        notImplemented();
        return null;
    }

    public EntityPersister getEntityPersister(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object getEntityUsingInterceptor(final EntityKey arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public SessionFactoryImplementor getFactory() {
        notImplemented();
        return null;
    }

    public String getFetchProfile() {
        notImplemented();
        return null;
    }

    public Type getFilterParameterType(final String arg0) {
        notImplemented();
        return null;
    }

    public Object getFilterParameterValue(final String arg0) {
        notImplemented();
        return null;
    }

    public FlushMode getFlushMode() {
        notImplemented();
        return null;
    }

    public Interceptor getInterceptor() {
        notImplemented();
        return null;
    }

    public JDBCContext getJDBCContext() {
        notImplemented();
        return null;
    }

    public EventListeners getListeners() {
        notImplemented();
        return null;
    }

    public Query getNamedQuery(final String arg0) {
        notImplemented();
        return null;
    }

    public Query getNamedSQLQuery(final String arg0) {
        notImplemented();
        return null;
    }

    public PersistenceContext getPersistenceContext() {
        notImplemented();
        return null;
    }

    public long getTimestamp() {
        notImplemented();
        return 0;
    }

    public String guessEntityName(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object immediateLoad(final String arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public void initializeCollection(final PersistentCollection arg0, final boolean arg1) throws HibernateException {
        notImplemented();
    }

    public Object instantiate(final String arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object internalLoad(final String arg0, final Serializable arg1, final boolean arg2, final boolean arg3)
            throws HibernateException {
        notImplemented();
        return null;
    }

    public boolean isClosed() {
        notImplemented();
        return false;
    }

    public boolean isConnected() {
        notImplemented();
        return false;
    }

    public boolean isEventSource() {
        notImplemented();
        return false;
    }

    public boolean isOpen() {
        notImplemented();
        return false;
    }

    public boolean isTransactionInProgress() {
        notImplemented();
        return false;
    }

    public Iterator<?> iterate(final String arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public Iterator<?> iterateFilter(final Object arg0, final String arg1, final QueryParameters arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public List<?> list(final CriteriaImpl arg0) {
        notImplemented();
        return null;
    }

    public List<?> list(final String arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public List<?> list(final NativeSQLQuerySpecification arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public List<?> listCustomQuery(final CustomQuery arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public List<?> listFilter(final Object arg0, final String arg1, final QueryParameters arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public ScrollableResults scroll(final String arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public ScrollableResults scroll(final CriteriaImpl arg0, final ScrollMode arg1) {
        notImplemented();
        return null;
    }

    public ScrollableResults scroll(final NativeSQLQuerySpecification arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public ScrollableResults scrollCustomQuery(final CustomQuery arg0, final QueryParameters arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public void setAutoClear(final boolean arg0) {
        notImplemented();
    }

    public void setCacheMode(final CacheMode arg0) {
        notImplemented();
    }

    public void setFetchProfile(final String arg0) {
        notImplemented();
    }

    public void setFlushMode(final FlushMode arg0) {
        notImplemented();
    }

    public Transaction beginTransaction() throws HibernateException {
        notImplemented();
        return null;
    }

    public void cancelQuery() throws HibernateException {
        notImplemented();
    }

    public void clear() {
        notImplemented();
    }

    public Connection close() throws HibernateException {
        notImplemented();
        return null;
    }

    public boolean contains(final Object arg0) {
        notImplemented();
        return false;
    }

    public Query createFilter(final Object arg0, final String arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    protected HQLQueryPlan getHQLQueryPlan(final String query, final boolean shallow) throws HibernateException {
        notImplemented();
        return null;
    }

    public SQLQuery createSQLQuery(final String arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public void delete(final Object arg0) throws HibernateException {
        notImplemented();
    }

    public void delete(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
    }

    public void disableFilter(final String arg0) {
        notImplemented();
    }

    public Connection disconnect() throws HibernateException {
        notImplemented();
        return null;
    }

    public Filter enableFilter(final String arg0) {
        notImplemented();
        return null;
    }

    public void evict(final Object arg0) throws HibernateException {
        notImplemented();
    }

    @SuppressWarnings("unchecked")
    public Object get(final Class arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object get(final String arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object get(final Class arg0, final Serializable arg1, final LockMode arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object get(final String arg0, final Serializable arg1, final LockMode arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public LockMode getCurrentLockMode(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Filter getEnabledFilter(final String arg0) {
        notImplemented();
        return null;
    }

    public String getEntityName(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Serializable getIdentifier(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Session getSession(final EntityMode arg0) {
        notImplemented();
        return null;
    }

    public SessionFactory getSessionFactory() {
        notImplemented();
        return null;
    }

    public SessionStatistics getStatistics() {
        notImplemented();
        return null;
    }

    public Transaction getTransaction() {
        notImplemented();
        return null;
    }

    public boolean isDirty() throws HibernateException {
        notImplemented();
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object load(final Class arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object load(final String arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public void load(final Object arg0, final Serializable arg1) throws HibernateException {
        notImplemented();
    }

    @SuppressWarnings("unchecked")
    public Object load(final Class arg0, final Serializable arg1, final LockMode arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object load(final String arg0, final Serializable arg1, final LockMode arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public void lock(final Object arg0, final LockMode arg1) throws HibernateException {
        notImplemented();
    }

    public void lock(final String arg0, final Object arg1, final LockMode arg2) throws HibernateException {
        notImplemented();
    }

    public Object merge(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Object merge(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public void persist(final Object arg0) throws HibernateException {
        notImplemented();
    }

    public void persist(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
    }

    public void reconnect() throws HibernateException {
        notImplemented();
    }

    public void reconnect(final Connection arg0) throws HibernateException {
        notImplemented();
    }

    public void refresh(final Object arg0) throws HibernateException {
        notImplemented();
    }

    public void refresh(final Object arg0, final LockMode arg1) throws HibernateException {
        notImplemented();
    }

    public void replicate(final Object arg0, final ReplicationMode arg1) throws HibernateException {
        notImplemented();
    }

    public void replicate(final String arg0, final Object arg1, final ReplicationMode arg2) throws HibernateException {
        notImplemented();
    }

    public Serializable save(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Serializable save(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public void saveOrUpdate(final Object arg0) throws HibernateException {
        notImplemented();
    }

    public void saveOrUpdate(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
    }

    public void setReadOnly(final Object arg0, final boolean arg1) {
        notImplemented();
    }

    public void update(final Object arg0) throws HibernateException {
        notImplemented();
    }

    public void update(final String arg0, final Object arg1) throws HibernateException {
        notImplemented();
    }

}

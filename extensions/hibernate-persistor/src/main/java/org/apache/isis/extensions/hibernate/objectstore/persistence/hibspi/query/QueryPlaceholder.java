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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;


public class QueryPlaceholder implements Query, Serializable {

    private static final long serialVersionUID = 1L;

    static DetermineType DETERMINE = new DetermineType();

    private transient Session session;
    private transient Query wrappedQuery;
    
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    private final Map<String, LockMode> lockModes = new HashMap<String, LockMode>();
    private final String queryString;
    
    private String comment;
    private CacheMode cacheMode;
    private String cacheRegion;
    private Boolean cacheable;
    private Integer firstResult;
    private Integer maxResults;
    private Integer timeout;
    private Integer fetchSize;
    private ResultTransformer resultTransformer;
    private Boolean readOnly;
    private FlushMode flushMode;

    public QueryPlaceholder(final String queryString) {
        this.queryString = queryString;
    }

    public int executeUpdate() throws HibernateException {
        return getOrCreateRealQuery().executeUpdate();
    }

    public String[] getNamedParameters() throws HibernateException {
        notImplemented();
        return null;
    }

    public String getQueryString() {
        return queryString;
    }

    public String[] getReturnAliases() throws HibernateException {
        notImplemented();
        return null;
    }

    public Type[] getReturnTypes() throws HibernateException {
        notImplemented();
        return null;
    }

    public Iterator<?> iterate() throws HibernateException {
        return getOrCreateRealQuery().iterate();
    }

    public List<?> list() throws HibernateException {
        return getOrCreateRealQuery().list();
    }

    public ScrollableResults scroll() throws HibernateException {
        return getOrCreateRealQuery().scroll();
    }

    public ScrollableResults scroll(final ScrollMode scrollMode) throws HibernateException {
        return getOrCreateRealQuery().scroll(scrollMode);
    }

    public Query setBigDecimal(final int index, final BigDecimal value) {
        return setIndexedParameter(index, value, Hibernate.BIG_DECIMAL);
    }

    public Query setBigDecimal(final String name, final BigDecimal value) {
        return setNamedParameter(name, value, Hibernate.BIG_DECIMAL);
    }

    public Query setBigInteger(final int index, final BigInteger value) {
        return setIndexedParameter(index, value, Hibernate.BIG_INTEGER);
    }

    public Query setBigInteger(final String name, final BigInteger value) {
        return setNamedParameter(name, value, Hibernate.BIG_INTEGER);
    }

    public Query setBinary(final int index, final byte[] value) {
        return setIndexedParameter(index, value, Hibernate.BINARY);
    }

    public Query setBinary(final String name, final byte[] value) {
        return setNamedParameter(name, value, Hibernate.BINARY);
    }

    public Query setBoolean(final int index, final boolean value) {
        return setIndexedParameter(index, Boolean.valueOf(value), Hibernate.BOOLEAN);
    }

    public Query setBoolean(final String name, final boolean value) {
        return setNamedParameter(name, Boolean.valueOf(value), Hibernate.BOOLEAN);
    }

    public Query setByte(final int index, final byte value) {
        return setIndexedParameter(index, new Byte(value), Hibernate.BYTE);
    }

    public Query setByte(final String name, final byte value) {
        return setNamedParameter(name, new Byte(value), Hibernate.BYTE);
    }

    public Query setCacheMode(final CacheMode cacheMode) {
        this.cacheMode = cacheMode;
        return this;
    }

    public Query setCacheRegion(final String cacheRegion) {
        this.cacheRegion = cacheRegion;
        return this;
    }

    public Query setCacheable(final boolean cacheable) {
        this.cacheable = Boolean.valueOf(cacheable);
        return this;
    }

    public Query setCalendar(final int index, final Calendar value) {
        return setIndexedParameter(index, value, Hibernate.CALENDAR);
    }

    public Query setCalendar(final String name, final Calendar value) {
        return setNamedParameter(name, value, Hibernate.CALENDAR);
    }

    public Query setCalendarDate(final int index, final Calendar value) {
        return setIndexedParameter(index, value, Hibernate.CALENDAR_DATE);
    }

    public Query setCalendarDate(final String name, final Calendar value) {
        return setNamedParameter(name, value, Hibernate.CALENDAR_DATE);
    }

    public Query setCharacter(final int index, final char value) {
        return setIndexedParameter(index, new Character(value), Hibernate.CHARACTER);
    }

    public Query setCharacter(final String name, final char value) {
        return setNamedParameter(name, new Character(value), Hibernate.CHARACTER);
    }

    public Query setComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public Query setDate(final int index, final Date value) {
        return setIndexedParameter(index, value, Hibernate.DATE);
    }

    public Query setDate(final String name, final Date value) {
        return setNamedParameter(name, value, Hibernate.DATE);
    }

    public Query setDouble(final int index, final double value) {
        return setIndexedParameter(index, Double.valueOf(value), Hibernate.DOUBLE);
    }

    public Query setDouble(final String name, final double value) {
        return setNamedParameter(name, Double.valueOf(value), Hibernate.DOUBLE);
    }

    public Query setEntity(final int index, final Object value) {
        return setIndexedParameter(index, value, Hibernate.OBJECT);
    }

    public Query setEntity(final String name, final Object value) {
        return setNamedParameter(name, value, Hibernate.OBJECT);
    }

    public Query setFetchSize(final int value) {
        this.fetchSize = Integer.valueOf(value);
        return this;
    }

    public Query setFirstResult(final int value) {
        this.firstResult = Integer.valueOf(value);
        return this;
    }

    public Query setFloat(final int index, final float value) {
        return setIndexedParameter(index, Float.valueOf(value), Hibernate.FLOAT);
    }

    public Query setFloat(final String name, final float value) {
        return setNamedParameter(name, Float.valueOf(value), Hibernate.FLOAT);
    }

    public Query setFlushMode(final FlushMode value) {
        this.flushMode = value;
        return this;
    }

    public Query setInteger(final int index, final int value) {
        return setIndexedParameter(index, Integer.valueOf(value), Hibernate.INTEGER);
    }

    public Query setInteger(final String name, final int value) {
        return setNamedParameter(name, Integer.valueOf(value), Hibernate.INTEGER);
    }

    public Query setLocale(final int index, final Locale value) {
        return setIndexedParameter(index, value, Hibernate.LOCALE);
    }

    public Query setLocale(final String name, final Locale value) {
        return setNamedParameter(name, value, Hibernate.LOCALE);
    }

    public Query setLockMode(final String key, final LockMode value) {
        lockModes.put(key, value);
        return this;
    }

    public Query setLong(final int index, final long value) {
        return setIndexedParameter(index, Long.valueOf(value), Hibernate.LONG);
    }

    public Query setLong(final String name, final long value) {
        return setNamedParameter(name, Long.valueOf(value), Hibernate.LONG);
    }

    public Query setMaxResults(final int value) {
        this.maxResults = Integer.valueOf(value);
        return this;
    }

    public Query setParameter(final int index, final Object value) throws HibernateException {
        return setIndexedParameter(index, value, DETERMINE);
    }

    public Query setParameter(final String name, final Object value) throws HibernateException {
        return setNamedParameter(name, value, DETERMINE);
    }

    public Query setParameter(final int index, final Object value, final Type type) {
        return setIndexedParameter(index, value, type);
    }

    public Query setParameter(final String name, final Object value, final Type type) {
        return setNamedParameter(name, value, type);
    }

    @SuppressWarnings("unchecked")
    public Query setParameterList(final String arg0, final Collection arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    public Query setParameterList(final String arg0, final Object[] arg1) throws HibernateException {
        notImplemented();
        return null;
    }

    @SuppressWarnings("unchecked")
    public Query setParameterList(final String arg0, final Collection arg1, final Type arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public Query setParameterList(final String arg0, final Object[] arg1, final Type arg2) throws HibernateException {
        notImplemented();
        return null;
    }

    public Query setParameters(final Object[] values, final Type[] types) throws HibernateException {
        Assert.assertTrue(values.length == types.length);
        for (int i = 0; i < values.length; i++) {
            setIndexedParameter(i, values[i], types[i]);
        }
        return this;
    }

    public Query setProperties(final Object arg0) throws HibernateException {
        notImplemented();
        return null;
    }

    public Query setReadOnly(final boolean value) {
        this.readOnly = Boolean.valueOf(value);
        return this;
    }

    public Query setResultTransformer(final ResultTransformer value) {
        this.resultTransformer = value;
        return this;
    }

    public Query setSerializable(final int index, final Serializable value) {
        return setIndexedParameter(index, value, Hibernate.SERIALIZABLE);
    }

    public Query setSerializable(final String name, final Serializable value) {
        return setNamedParameter(name, value, Hibernate.SERIALIZABLE);
    }

    public Query setShort(final int index, final short value) {
        return setIndexedParameter(index, Short.valueOf(value), Hibernate.SHORT);
    }

    public Query setShort(final String name, final short value) {
        return setNamedParameter(name, Short.valueOf(value), Hibernate.SHORT);
    }

    public Query setString(final int index, final String value) {
        return setIndexedParameter(index, value, Hibernate.STRING);
    }

    public Query setString(final String name, final String value) {
        return setNamedParameter(name, value, Hibernate.STRING);
    }

    public Query setText(final int index, final String value) {
        return setIndexedParameter(index, value, Hibernate.TEXT);
    }

    public Query setText(final String name, final String value) {
        return setNamedParameter(name, value, Hibernate.TEXT);
    }

    public Query setTime(final int index, final Date value) {
        return setIndexedParameter(index, value, Hibernate.TIME);
    }

    public Query setTime(final String name, final Date value) {
        return setNamedParameter(name, value, Hibernate.TIME);
    }

    public Query setTimeout(final int value) {
        this.timeout = Integer.valueOf(value);
        return this;
    }

    public Query setTimestamp(final int index, final Date value) {
        return setIndexedParameter(index, value, Hibernate.TIMESTAMP);
    }

    public Query setTimestamp(final String name, final Date value) {
        return setNamedParameter(name, value, Hibernate.TIMESTAMP);
    }

    public Object uniqueResult() throws HibernateException {
        return getOrCreateRealQuery().uniqueResult();
    }

    public void setSession(final Session session) {
        this.session = session;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @SuppressWarnings("unchecked")
    public Query setProperties(final Map arg0) throws HibernateException {
        notImplemented();
        return null;
    }
    
    
    
    private Query setIndexedParameter(final int index, final Object value, final Type type) {
        parameters.add(new IndexedParameter(index, value, type));
        return this;
    }

    private Query setNamedParameter(final String name, final Object value, final Type type) {
        parameters.add(new NamedParameter(name, value, type));
        return this;
    }

    private void setOptions(final Query query) {
        if (comment != null) {
            query.setComment(comment);
        }
        if (cacheMode != null) {
            query.setCacheMode(cacheMode);
        }
        if (cacheRegion != null) {
            query.setCacheRegion(cacheRegion);
        }
        if (cacheable != null) {
            query.setCacheable(cacheable.booleanValue());
        }
        if (firstResult != null) {
            query.setFirstResult(firstResult.intValue());
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults.intValue());
        }
        if (timeout != null) {
            query.setTimeout(timeout.intValue());
        }
        if (fetchSize != null) {
            query.setFetchSize(fetchSize.intValue());
        }
        if (resultTransformer != null) {
            query.setResultTransformer(resultTransformer);
        }
        if (readOnly != null) {
            query.setReadOnly(readOnly.booleanValue());
        }
        if (flushMode != null) {
            query.setFlushMode(flushMode);
        }
        for (final Map.Entry<String, LockMode> entry : lockModes.entrySet()) {
            query.setLockMode(entry.getKey(), entry.getValue());
        }
    }

    private Query getOrCreateRealQuery() {
        if (wrappedQuery == null) {
            Assert.assertNotNull(session);
            wrappedQuery = session.createQuery(queryString);
            setOptions(wrappedQuery);
            for (final Parameter param : parameters) {
                param.setParameterInto(wrappedQuery);
            }
        }
        return wrappedQuery;
    }


    private void notImplemented() {
        throw new NotYetImplementedException("Not Implemented");
    }

}

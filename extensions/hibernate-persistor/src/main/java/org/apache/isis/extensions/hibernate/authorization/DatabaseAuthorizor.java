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


package org.apache.isis.extensions.hibernate.authorization;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.authorization.standard.AuthorizorAbstract;


public class DatabaseAuthorizor extends AuthorizorAbstract {

	@SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DatabaseAuthorizor.class);
    
    private static final int READ_WRITE = 0;
    @SuppressWarnings("unused")
    private static final int READ_ONLY = 1;
    
    private static final String LOOKUP_ROLE_ID_BY_ROLENAME_SQL = "select id from role  where rolename = ?";
    private static final String GET_MAX_PERMISSIONS_ID_SQL = "select id from permissions order by id desc limit 1";

    private static final String NO_FLAG_MATCH = "select count(p.permission) from permissions p, role r where p.role = r.id  and r.rolename = ? and p.permission = ?";
    private static final String FLAG_MATCH = NO_FLAG_MATCH + " and p.flags is NULL or p.flags = ?";

    private static final String NO_FLAG_INSERT = "insert into permissions values (?, ?, ?)";
    private static final String FLAG_INSERT = "insert into permissions values (?, ?, ?, ?)";
    
    private final boolean learn;

    public DatabaseAuthorizor(IsisConfiguration configuration) {
    	super(configuration);
    	learn = getConfiguration().getBoolean(DatabaseAuthorizationConstants.AUTH_LEARN, false);
    }

    
    /////////////////////////////////////////////////////////////
    // init, shutdown
    /////////////////////////////////////////////////////////////
    
    public void init() {
    }

    public void shutdown() {
        // do nothing
    }

    
    /////////////////////////////////////////////////////////////
    // API
    /////////////////////////////////////////////////////////////
    
    public boolean isUsableInRole(final String role, final Identifier member) {
        // only match if flag is either null or READ_WRITE
        return isAuthorised(role, member, Integer.valueOf(READ_WRITE));
    }

    public boolean isVisibleInRole(final String role, final Identifier member) {
        // match ignoring flag
        return isAuthorised(role, member, null);
    }


    private int getNextId() {
        try {
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(
                    GET_MAX_PERMISSIONS_ID_SQL);
            final Integer id = (Integer) sq.uniqueResult();
            HibernateUtil.commitTransaction();
            return id != null ? id.intValue() + 1 : 0;
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new IsisException(e);
        }
    }

    private int getRoleId(final String role) {
        try {
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(LOOKUP_ROLE_ID_BY_ROLENAME_SQL);
            sq.setString(0, role);
            final Integer id = (Integer) sq.uniqueResult();
            HibernateUtil.commitTransaction();
            return id.intValue();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new IsisException(e);
        }
    }

    private boolean learn(final String role, final String key, final Integer flag) {
        if (isMatch(role, key, flag)) {
            return true;
        }
        try {
            final int permissionId = getNextId();
            final int roleId = getRoleId(role);

            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(flag == null ? NO_FLAG_INSERT : FLAG_INSERT);
            sq.setInteger(0, permissionId);
            sq.setInteger(1, roleId);
            sq.setString(2, key);
            if (flag != null) {
                sq.setInteger(3, flag.intValue());
            }
            sq.executeUpdate();
            HibernateUtil.commitTransaction();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new IsisException(e);
        }
        return true;
    }

    private boolean isMatch(final String role, final String key, final Integer flag) {
        try {
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(flag == null ? NO_FLAG_MATCH : FLAG_MATCH);
            sq.setString(0, role);
            sq.setString(1, key);
            if (flag != null) {
                sq.setInteger(2, flag.intValue());
            }
            final Number count = (Number) sq.uniqueResult();
            HibernateUtil.commitTransaction();
            return count != null && count.intValue() > 0;
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new IsisException(e);
        }
    }

    public boolean isAuthorised(final String role, final Identifier member, final Integer flag) {
        if (learn) {
            return learn(role, member.toIdentityString(Identifier.CLASS_MEMBERNAME_PARMS), flag);
        }

        if (isMatch(role, member.toIdentityString(Identifier.CLASS), flag)) {
        	return true;
        }
        if (isMatch(role, member.toIdentityString(Identifier.CLASS_MEMBERNAME), flag)) {
        	return true;
        }
        if (isMatch(role, member.toIdentityString(Identifier.CLASS_MEMBERNAME_PARMS), flag)) {
        	return true;
        }
        return false;
    }

}

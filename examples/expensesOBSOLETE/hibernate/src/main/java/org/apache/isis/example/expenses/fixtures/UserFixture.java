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


package org.apache.isis.example.expenses.fixtures;

import java.sql.DatabaseMetaData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.metamodel.commons.exceptions.IsisException;
import org.apache.isis.extensions.hibernate.authentication.DatabaseAuthenticator;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;


// special fixture to insert users 

public class UserFixture extends AbstractFixture {

    private static final int PERMISSIONS = 0;
    private static final int USER_ROLE = 1;
    private static final int ROLE = 2;
    private static final int USER = 3;
    private static final int GET_ID = 4;
    private static final int INSERT_USER = 5;
    private static final int INSERT_ROLE = 6;
    private static final int INSERT_USER_ROLE = 7;
    private static final int INSERT_PERMISSION = 8;

    // works just for mySQL
    private static final String[] MYSQL_SQL = {
            "CREATE TABLE `permissions` (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, `role` INTEGER UNSIGNED NOT NULL,`permission` VARCHAR(255) NOT NULL, `flags` INTEGER UNSIGNED, PRIMARY KEY (`id`)) ENGINE = InnoDB;",
            "CREATE TABLE `user_role` (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, `user` INTEGER UNSIGNED NOT NULL,`role` INTEGER UNSIGNED NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;",
            "CREATE TABLE `role` (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, `rolename` VARCHAR(255) NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;",
            "CREATE TABLE `expenses`.`user` (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT, `username` VARCHAR(255) NOT NULL,`password` VARCHAR(255) NOT NULL, `emailupdates` TINYINT(1) NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;",
            "SELECT `id` FROM `?` ORDER BY `id` DESC LIMIT 1",
            "INSERT INTO `user` VALUES (?, ?, ?, ?)",
            "INSERT INTO `role` VALUES (?, ?)",
            "INSERT INTO `user_role` VALUES (?, (SELECT `id` FROM `user` WHERE `username` = ?), (SELECT `id` FROM `role` WHERE `rolename` = ?))",
            "INSERT INTO `permissions` VALUES (?, (SELECT `id` FROM `role` WHERE `rolename` = ?), ?, ?)" };

    // works for both mySQL and hsql-db (but not postgresql)
    private static final String[] HSQLDB_SQL = {
            "CREATE TABLE permissions (id INTEGER  NOT NULL , role INTEGER  NOT NULL,permission VARCHAR(255) NOT NULL, flags INTEGER, PRIMARY KEY (id));",
            "CREATE TABLE user_role (id INTEGER  NOT NULL , user INTEGER  NOT NULL,role INTEGER  NOT NULL, PRIMARY KEY (id));",
            "CREATE TABLE role (id INTEGER  NOT NULL , rolename VARCHAR(255) NOT NULL, PRIMARY KEY (id)) ;",
            "CREATE TABLE user (id INTEGER  NOT NULL , username VARCHAR(255) NOT NULL,password VARCHAR(255) NOT NULL, emailupdates TINYINT, PRIMARY KEY (id));",
            "SELECT id FROM ? ORDER BY id DESC LIMIT 1",
            "INSERT INTO user VALUES (?, ?, ?, ?)",
            "INSERT INTO role VALUES (?, ?)",
            "INSERT INTO user_role VALUES (?, (SELECT id FROM user WHERE username = ?), (SELECT id FROM role WHERE rolename = ?))",
            "INSERT INTO permissions VALUES (?, (SELECT id FROM role WHERE rolename = ?), ?, ?)" };

    // works for postgreSQL
    private static final String[] POSTGRESQL_SQL = {
            "CREATE TABLE \"permissions\" (\"id\" INTEGER  NOT NULL , \"role\" INTEGER  NOT NULL,\"permission\" VARCHAR(255) NOT NULL, \"flags\" INTEGER, PRIMARY KEY (\"id\"));",
            "CREATE TABLE \"user_role\" (\"id\" INTEGER  NOT NULL , \"user\" INTEGER  NOT NULL,\"role\" INTEGER  NOT NULL, PRIMARY KEY (\"id\"));",
            "CREATE TABLE \"role\" (\"id\" INTEGER  NOT NULL , \"rolename\" VARCHAR(255) NOT NULL, PRIMARY KEY (\"id\")) ;",
            "CREATE TABLE \"user\" (\"id\" INTEGER  NOT NULL , \"username\" VARCHAR(255) NOT NULL,\"password\" VARCHAR(255) NOT NULL, \"emailupdates\" BOOLEAN, PRIMARY KEY (\"id\"));",
            "SELECT id FROM \"?\" ORDER BY id DESC LIMIT 1",
            "INSERT INTO \"user\" VALUES (?, ?, ?, ?)",
            "INSERT INTO \"role\" VALUES (?, ?)",
            "INSERT INTO \"user_role\" VALUES (?, (SELECT id FROM \"user\" WHERE username = ?), (SELECT id FROM \"role\" WHERE rolename = ?))",
            "INSERT INTO \"permissions\" VALUES (?, (SELECT id FROM \"role\" WHERE rolename = ?), ?, ?)" };

    private static final String[] SQL;
    private DatabaseAuthenticator databaseAuthenticator;

    static {
        final String databaseType = getDatabaseType();
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            SQL = POSTGRESQL_SQL;
        } else if ("mysql".equalsIgnoreCase(databaseType)) {
            SQL = MYSQL_SQL;
        } else {
            SQL = HSQLDB_SQL;
        }
    }

    private static String getDatabaseType() {
        try {
            HibernateUtil.startTransaction();
            final DatabaseMetaData dbm = HibernateUtil.getCurrentSession().connection().getMetaData();
            HibernateUtil.commitTransaction();
            final String dbURL = dbm.getURL();
            final Pattern pattern = Pattern.compile("(.*jdbc\\:)([^\\:]*)(\\:.*)");
            final Matcher matcher = pattern.matcher(dbURL);
            if (matcher.matches()) {
                return matcher.group(2);
            }
        } catch (final Exception e) {
            throw new IsisException(e);
        }

        return null;
    }

    private void createTable(final String createSQL) {
        try {
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(createSQL);
            sq.executeUpdate();
            HibernateUtil.commitTransaction();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
        }
    }

    private void createPermissionsTable() {
        final String createTable = SQL[PERMISSIONS];
        createTable(createTable);
    }

    private void createUserRoleTable() {
        final String createTable = SQL[USER_ROLE];
        createTable(createTable);
    }

    private void createRoleTable() {
        final String createTable = SQL[ROLE];
        createTable(createTable);
    }

    private void createUserTable() {
        final String createTable = SQL[USER];
        createTable(createTable);
    }

    private int getNextId(final String fromTable) {
        try {
            HibernateUtil.startTransaction();
            final String tableSQL = SQL[GET_ID].replace("?", fromTable);
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(tableSQL);

            final Integer id = (Integer) sq.uniqueResult();
            HibernateUtil.commitTransaction();
            return id != null ? id.intValue() + 1 : 0;
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
        }
        return 0;
    }

    private void insertUser(final String username, final String password) {
        try {
            final int nextId = getNextId("user");
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(SQL[INSERT_USER]);
            sq.setInteger(0, nextId);
            sq.setString(1, username);
            sq.setString(2, databaseAuthenticator.generateHash(password));
            sq.setBoolean(3, false);
            sq.executeUpdate();
            HibernateUtil.commitTransaction();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
        }
    }

    private void insertRole(final String role) {
        try {
            final int nextId = getNextId("role");
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(SQL[INSERT_ROLE]);
            sq.setInteger(0, nextId);
            sq.setString(1, role);
            sq.executeUpdate();
            HibernateUtil.commitTransaction();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
        }
    }

    private void insertUserRole(final String user, final String role) {
        try {
            final int nextId = getNextId("user_role");
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(SQL[INSERT_USER_ROLE]);
            sq.setInteger(0, nextId);
            sq.setString(1, user);
            sq.setString(2, role);
            sq.executeUpdate();
            HibernateUtil.commitTransaction();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
        }
    }

    private void insertPermission(final String role, final String permission) {
        insertPermission(role, permission, null);
    }

    private void insertPermission(final String role, final String permission, final Integer flag) {
        try {
            final int nextId = getNextId("permissions");
            HibernateUtil.startTransaction();
            final SQLQuery sq = HibernateUtil.getCurrentSession().createSQLQuery(SQL[INSERT_PERMISSION]);
            sq.setInteger(0, nextId);
            sq.setString(1, role);
            sq.setString(2, permission);
            if (flag == null) {
                sq.setParameter(3, null, Hibernate.INTEGER);
            } else {
                sq.setInteger(3, flag.intValue());
            }
            sq.executeUpdate();
            HibernateUtil.commitTransaction();
        } catch (final Exception e) {
            HibernateUtil.rollbackTransaction();
        }

    }

    private void insertPermissions() {
        // actions
        // common
        insertPermission("claimant", "org.apache.isis.example.expenses.employee.EmployeeStartPoints");
        insertPermission("claimant", "org.apache.isis.example.expenses.employee.Employee");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#returnToClaimant(java.lang.String)");
        insertPermission("claimant", "org.apache.isis.example.expenses.recordedAction.impl.RecordedAction");
        insertPermission("approver", "org.apache.isis.example.expenses.employee.EmployeeStartPoints");
        insertPermission("approver", "org.apache.isis.example.expenses.employee.Employee");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#returnToClaimant(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.recordedAction.impl.RecordedAction");

        // approver
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ClaimStartPoints#claimsAwaitingMyApproval()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#approveItems(boolean)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#queryItems(java.lang.String,boolean)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#rejectItems(java.lang.String,boolean)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#approve()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#query(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#reject(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#approve()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#query(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#reject(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#approve()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#query(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#reject(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#approve()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#query(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#reject(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#approve()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#query(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#reject(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#approve()");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#query(java.lang.String)");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#reject(java.lang.String)");

        // claimant

        insertPermission("claimant", "org.apache.isis.example.expenses.services.hibernate.EmployeeRepositoryHibernate");

        insertPermission("claimant", "org.apache.isis.example.expenses.recordedAction.impl.RecordedActionContributedActions");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ClaimStartPoints#myRecentClaims()");
        insertPermission(
                "claimant",
                "org.apache.isis.example.expenses.claims.ClaimStartPoints#findMyClaims(org.apache.isis.example.expenses.claims.ClaimStatus,java.lang.String)");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ClaimStartPoints#createNewClaim(java.lang.String)");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.Claim#createNewExpenseItem(org.apache.isis.example.expenses.claims.ExpenseType)");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.Claim#copyAnExistingExpenseItem(org.apache.isis.example.expenses.claims.ExpenseItem)");
        insertPermission(
                "claimant",
                "org.apache.isis.example.expenses.claims.Claim#copyAllExpenseItemsFromAnotherClaim(org.apache.isis.example.expenses.claims.Claim,org.apache.isis.applib.value.Date)");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.Claim#createNewClaimFromThis(java.lang.String,org.apache.isis.applib.value.Date)");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.Claim#submit(org.apache.isis.example.expenses.employee.Employee,boolean)");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#findSimilarExpenseItems()");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#copyFrom(org.apache.isis.example.expenses.claims.ExpenseItem)");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#findSimilarExpenseItems()");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.items.GeneralExpense#copyFrom(org.apache.isis.example.expenses.claims.ExpenseItem)");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#findSimilarExpenseItems()");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.items.Airfare#copyFrom(org.apache.isis.example.expenses.claims.ExpenseItem)");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#findSimilarExpenseItems()");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.items.Taxi#copyFrom(org.apache.isis.example.expenses.claims.ExpenseItem)");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#findSimilarExpenseItems()");
        insertPermission("claimant",
                "org.apache.isis.example.expenses.claims.items.Hotel#copyFrom(org.apache.isis.example.expenses.claims.ExpenseItem)");

        // fields
        // common

        insertPermission("claimant", "org.apache.isis.example.expenses.currency.Currency");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ProjectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#dateCreated");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#claimant");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#approver");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#total");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.Claim#expenseItems");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ExpenseItem#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ExpenseItem#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ExpenseItem#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ExpenseItem#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.ExpenseItem#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#origin");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#destination");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#returnJourney");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#totalMiles");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#mileageRate");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#comment");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.GeneralExpense#comment");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#origin");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#destination");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#returnJourney");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#airlineAndFlight");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#comment");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Airfare#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#origin");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#destination");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#returnJourney");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#comment");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Taxi#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#hotelURL");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#numberOfNights");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#accommodation");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#food");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#other");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.Hotel#comment");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#dateIncurred");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#description");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#amount");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#projectCode");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#status");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#comment");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#rentalCompany");
        insertPermission("claimant", "org.apache.isis.example.expenses.claims.items.CarRental#numberOfDays");
        insertPermission("approver", "org.apache.isis.example.expenses.currency.Currency");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ProjectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#dateCreated");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#approver");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#approver");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#total");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.Claim#expenseItems");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ExpenseItem#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ExpenseItem#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ExpenseItem#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ExpenseItem#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.ExpenseItem#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#origin");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#destination");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#returnJourney");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#totalMiles");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#mileageRate");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#comment");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.PrivateCarJourney#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.GeneralExpense#comment");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#origin");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#destination");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#returnJourney");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#airlineAndFlight");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#comment");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Airfare#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#origin");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#destination");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#returnJourney");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#comment");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Taxi#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#hotelURL");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#numberOfNights");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#accommodation");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#food");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#other");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.Hotel#comment");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#dateIncurred");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#description");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#amount");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#projectCode");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#status");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#comment");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#rentalCompany");
        insertPermission("approver", "org.apache.isis.example.expenses.claims.items.CarRental#numberOfDays");

    }

    public void install() {

        // needed to generate hashes of passwords shortly
        databaseAuthenticator = new DatabaseAuthenticator(IsisContext.getConfiguration());

        createUserTable();
        createRoleTable();
        createUserRoleTable();
        createPermissionsTable();

        insertUser("sven", "pass");
        insertUser("dick", "pass");
        insertUser("bob", "pass");
        insertUser("joe", "pass");

        insertRole("claimant");
        insertRole("approver");

        insertUserRole("sven", "claimant");
        insertUserRole("dick", "claimant");
        insertUserRole("bob", "claimant");
        insertUserRole("joe", "claimant");

        insertUserRole("dick", "approver");

        insertPermissions();
    }
}

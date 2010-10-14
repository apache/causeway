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


package org.apache.isis.extensions.hibernate.objectstore.tools;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.apache.isis.extensions.hibernate.objectstore.tools.internal.Nof2HbmXml;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;


public class HibernateTools {

    /**
     * Export schema to database - will also drop tables first
     * 
     * @param script
     *            to write DDL script to System.out
     * @param export
     *            to export updates to the database
     */
    public static void exportSchema(final boolean script, final boolean export) {
        final SchemaExport schemaExport = new SchemaExport(HibernateUtil.getConfiguration());
        // schemaExport.edrop(script, export);
        schemaExport.create(script, export);
    }

    /**
     * Drop schema from database
     * 
     * @param script
     *            to write DDL script to System.out
     * @param export
     *            to export updates to the database
     */
    public static void dropSchema(final boolean script, final boolean export) {
        final SchemaExport schemaExport = new SchemaExport(HibernateUtil.getConfiguration());
        schemaExport.drop(script, export);
    }

    /**
     * Update Schema in the database
     * 
     * @param script
     *            to write DDL script to System.out
     * @param export
     *            to export updates to the database
     */
    public static void updateSchema(final boolean script, final boolean export) {
        updateSchema(HibernateUtil.getConfiguration(), script, export);
    }

    /**
     * Update Schema in the database
     * 
     * @param script
     *            to write DDL script to System.out
     * @param export
     *            to export updates to the database
     */
    public static void updateSchema(final Configuration cfg, final boolean script, final boolean export) {
        new SchemaUpdate(cfg).execute(script, export);
    }

    /**
     * Export Hibernate mapping files for all [[NAME]] currently in Isis.
     * 
     * @param outDir
     */
    public static void exportHbmXml(final String basedir) {
        new Nof2HbmXml().exportHbmXml(basedir);
    }

}

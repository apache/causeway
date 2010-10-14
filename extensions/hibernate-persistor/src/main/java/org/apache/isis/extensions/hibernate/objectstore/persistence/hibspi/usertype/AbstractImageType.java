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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.value.ImageValueSemanticsProviderAbstract;


public abstract class AbstractImageType extends ImmutableUserType {
    // protected abstract AbstractImageAdapter getImageAdapter(Object value);
    protected abstract ImageValueSemanticsProviderAbstract getImageAdapter();

    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException,
            SQLException {
        final Blob blob = rs.getBlob(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        // final AbstractImageAdapter imageAdapter = getImageAdapter(null);
        final ImageValueSemanticsProviderAbstract imageAdapter = getImageAdapter();
        return imageAdapter.restoreFromByteArray(blob.getBytes(1, (int) blob.length()));
    }

    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException,
            SQLException {
        if (value == null) {
            st.setNull(index, Types.BLOB);
        } else {
            // AbstractImageAdapter imageAdapter = getImageAdapter(value);
            // st.setBlob(index, Hibernate.createBlob(imageAdapter.getAsByteArray()));
            final ImageValueSemanticsProviderAbstract imageAdapter = getImageAdapter();
            final ObjectAdapter valueAdapter = lookupObjectAdapter(value);
            st.setBlob(index, Hibernate.createBlob(imageAdapter.getAsByteArray(valueAdapter)));
        }
    }

    /**
     * TODO: need to lookup the NO that wraps the provided value
     */
    private ObjectAdapter lookupObjectAdapter(final Object value) {
        return null;
    }

    public abstract Class<?> returnedClass();

    public int[] sqlTypes() {
        return new int[] { Types.BLOB };
    }
}

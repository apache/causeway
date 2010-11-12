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

package org.apache.isis.extensions.sql.objectstore.jdbc;

import org.apache.isis.applib.value.Time;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.sql.objectstore.DatabaseConnector;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMappingFactory;

public class JdbcTimeMapper extends AbstractJdbcFieldMapping {

	public static class Factory implements FieldMappingFactory {
		@Override
		public FieldMapping createFieldMapping(final ObjectAssociation field) {
			return new JdbcTimeMapper(field);
		}
	}

	protected JdbcTimeMapper(ObjectAssociation field) {
		super(field);
	}

	// TODO:KAM:here XY
	@Override
	public String valueAsDBString(final ObjectAdapter value,
			DatabaseConnector connector) {
		Time asDate = (Time) value.getObject();
		java.sql.Time xxx = java.sql.Time.valueOf(asDate.toString() + ":00");
		connector.addToQueryValues(xxx);
		return "?";
		/*
		 * EncodableFacet encodeableFacet =
		 * value.getSpecification().getFacet(EncodableFacet.class); String
		 * encodedString = encodeableFacet.toEncodedString(value); String minute
		 * = encodedString.substring(2, 4); String hour =
		 * encodedString.substring(0, 2); String encodedWithAdaptions = hour +
		 * ":" + minute + ":00"; return "'" + encodedWithAdaptions + "'";
		 */
	}

	@Override
	public ObjectAdapter setFromDBColumn(final String encodedValue,
			final ObjectAssociation field) {
		Long hour = Long.decode(encodedValue.substring(0, 2));
		Long minute = Long.decode(encodedValue.substring(3, 5));
		Long millis = (minute + hour * 60) * 60 * 1000;
		String valueString = "T" + Long.toString(millis);
		return field.getSpecification().getFacet(EncodableFacet.class)
				.fromEncodedString(valueString);
	}

	@Override
	public String columnType() {
		return "TIME";
	}

}

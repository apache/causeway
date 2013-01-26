/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.sql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.value.Image;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;
import org.apache.isis.objectstore.sql.mapping.FieldMappingFactory;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcImageValueMapper extends AbstractJdbcMultiFieldMapping {

    public static class Factory implements FieldMappingFactory {
        private final String type_string; // A reference, e.g. file name
        private final String type_blob; // The BLOB data

        public Factory(final String string_type, final String type_blob) {
            this.type_string = string_type;
            this.type_blob = type_blob;
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            return new JdbcImageValueMapper(field, type_string, type_blob);
        }
    }

    public JdbcImageValueMapper(final ObjectAssociation field, final String type1, final String type2) {
        super(field, 2, type1, type2);
    }

    @Override
    protected Object preparedStatementObject(int index, Object o) {
        if (o instanceof Image) {
            if (index == 0) {
                return "Image";
            } else {
                return getOutputStreamFromImage((Image) o);
            }
        } else {
            throw new PersistFailedException("Invalid object type " + o.getClass().getCanonicalName()
                + " for JdbcImageValueMapper");
        }
    }

    private Object getOutputStreamFromImage(Image o) {
        int[][] intArray = o.getImage();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final int height = o.getHeight();
        final int width = o.getWidth();

        stream.write(height);
        stream.write(width);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                // stream.write(o.getImage());
                stream.write(intArray[j][i]);
            }
        }
        return new ByteArrayInputStream(stream.toByteArray());
    }

    @Override
    protected Object getObjectFromResults(Results results) {
        // final String name = results.getString(columnName(0));
        InputStream binaryStream = results.getStream(columnName(1));

        int[][] intArray;
        try {
            int width = binaryStream.read();
            int height = binaryStream.read();

            intArray = new int[height][width];
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    intArray[j][i] = binaryStream.read();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            intArray = new int[0][0];
        }

        final Image object = new Image(intArray);

        return object;
    }

}

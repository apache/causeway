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


package org.apache.isis.extensions.berkeley;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;


public class InstanceTypeKeyCreator implements SecondaryKeyCreator {

    public boolean createSecondaryKey(SecondaryDatabase database, DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
            throws DatabaseException {
        try {

            String keyData = new String(key.getData());
            if (Character.isDigit(keyData.charAt(0))) {
                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data.getData()));
                String className = inputStream.readUTF();
                ObjectSpecification specification = IsisContext.getSpecificationLoader()
                        .loadSpecification(className);
                String secondaryKey = specification.getShortName();
                result.setData(secondaryKey.getBytes("UTF-8"));
                return true;
            } else {
                return false;
            }
        } catch (IOException willNeverOccur) {
            return false;
        }
    }

}


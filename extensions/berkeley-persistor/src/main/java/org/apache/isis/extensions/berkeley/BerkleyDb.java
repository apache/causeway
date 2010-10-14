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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.persistence.ObjectNotFoundException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;


public class BerkleyDb {
    private Environment myDbEnvironment;
    private Database myDatabase;
    private SecondaryDatabase mySecDb;
    private Transaction txn;

    public static void main(String[] args) {

        BerkleyDb db = new BerkleyDb();
        db.open();
        db.read("one");
        db.read("two");

        db.write("two", "data".getBytes());
        db.close();
    }

    public void open() {
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setTransactional(true);
            envConfig.setAllowCreate(true);
            File file = new File("berkeley");
            if (!file.exists()) {
                file.mkdirs();
            }
            myDbEnvironment = new Environment(file, envConfig);

            DatabaseConfig dbConfig = new DatabaseConfig();
            Transaction txn = null; // myDbEnvironment.beginTransaction(null, null);
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);
            myDatabase = myDbEnvironment.openDatabase(txn, "sampleDatabase", dbConfig);

            SecondaryConfig mySecConfig = new SecondaryConfig();
            mySecConfig.setAllowCreate(true);
            mySecConfig.setTransactional(true);
            mySecConfig.setSortedDuplicates(true);

            InstanceTypeKeyCreator keyCreator = new InstanceTypeKeyCreator();
            mySecConfig.setKeyCreator(keyCreator);
            mySecDb = myDbEnvironment.openSecondaryDatabase(txn, "mySecondaryDatabase", myDatabase, mySecConfig);

        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void close() {
        try {
            if (mySecDb != null) {
                mySecDb.close();
            }
            if (myDatabase != null) {
                myDatabase.close();
            }
            if (myDbEnvironment != null) {
                myDbEnvironment.close();
            }
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void startTransaction() {
        try {
            txn = myDbEnvironment.beginTransaction(null, null);
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void endTransaction() {
        try {
            txn.commit();
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public byte[] read(String key) {
        try {
            DatabaseEntry theKey;
            theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry();
            if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                byte[] retData = theData.getData();
                return retData;
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            throw new BerkeleyObjectStoreException(e);
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void update(ObjectData objectData) {
        byte[] data = read(objectData.getKey());
        objectData.update(data);
    }

    public void write(ObjectData objectData) {
        write(objectData.getKey(), objectData.getData());
    }

    public void write(String key, String data) {
        write(key, data.getBytes());
    }

    public void write(String key, byte[] data) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry(data);
            OperationStatus status = myDatabase.put(txn, theKey, theData);

            /*
             * Note that put will throw a DatabaseException when error conditions are found such as deadlock.
             * However, the status return conveys a variety of information. For example, the put might
             * succeed, or it might not succeed if the record exists and duplicates were not.
             */
            if (status != OperationStatus.SUCCESS) {
                throw new BerkeleyObjectStoreException("Data insertion got status " + status);
            }
        } catch (UnsupportedEncodingException e) {
            throw new BerkeleyObjectStoreException(e);
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void delete(ObjectData objectData) {
        delete(objectData.getKey());
    }

    public void delete(String key) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            OperationStatus status = myDatabase.delete(txn, theKey);

            /*
             * Note that put will throw a DatabaseException when error conditions are found such as deadlock.
             * However, the status return conveys a variety of information. For example, the put might
             * succeed, or it might not succeed if the record exists and duplicates were not.
             */
            if (status != OperationStatus.SUCCESS) {
                throw new BerkeleyObjectStoreException("Data deletion got status " + status);
            }
        } catch (UnsupportedEncodingException e) {
            throw new BerkeleyObjectStoreException(e);
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public ObjectData[] getAll(ObjectSpecification specification) {
        List<ObjectData> list = new ArrayList<ObjectData>();
        try {
            DatabaseEntry secondaryKey = new DatabaseEntry(className(specification));
            DatabaseEntry foundData = new DatabaseEntry();
            SecondaryCursor mySecCursor = mySecDb.openSecondaryCursor(null, null);
            OperationStatus status = mySecCursor.getSearchKey(secondaryKey, foundData, LockMode.DEFAULT);
            while (status == OperationStatus.SUCCESS) {
                // String key = new String(foundData.getData());
                list.add(new ObjectData(foundData.getData()));
                status = mySecCursor.getNextDup(secondaryKey, foundData, LockMode.DEFAULT);
            }
            mySecCursor.close();

            return list.toArray(new ObjectData[list.size()]);
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public ObjectData[] getAll() {
        List<ObjectData> list = new ArrayList<ObjectData>();
        try {
            Cursor cursor = myDatabase.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                String key = new String(foundKey.getData());
                if (Character.isDigit(key.charAt(0))) {
                    list.add(new ObjectData(foundData.getData()));
                }
            }
            cursor.close();

            return list.toArray(new ObjectData[list.size()]);
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public boolean containsData() {
        try {
            return myDatabase.count() > 0;
        } catch (DatabaseException e) {

            throw new BerkeleyObjectStoreException(e);
        }
    }

    public Oid getService(String name) {
        try {
            byte[] data = getData(name.getBytes("UTF-8"));
            return ObjectData.getOid(data);
        } catch (UnsupportedEncodingException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void addService(String name, Oid oid) {
        write("service:" + name, ObjectData.getKey(oid));
    }

    public ObjectData getObject(Oid oid, ObjectSpecification hint) {
        try {
            byte[] data;
            data = getData(ObjectData.getKey(oid).getBytes("UTF-8"));
            if (data == null) {
                throw new ObjectNotFoundException("Not found object for " + oid);
            }
            ObjectData objectData = new ObjectData(data);
            return objectData;
        } catch (UnsupportedEncodingException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public byte[] getData(byte[] key) {
        try {
            Cursor cursor = myDatabase.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry(key);
            DatabaseEntry foundData = new DatabaseEntry();
            if (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                byte[] data = foundData.getData();
                cursor.close();
                return data;
            }
            cursor.close();
            return null;
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public boolean hasInstances(ObjectSpecification specification) {
        try {
            DatabaseEntry secondaryKey = new DatabaseEntry(className(specification));
            DatabaseEntry foundData = new DatabaseEntry();
            SecondaryCursor mySecCursor = mySecDb.openSecondaryCursor(null, null);
            OperationStatus status = mySecCursor.getSearchKey(secondaryKey, foundData, LockMode.DEFAULT);
            boolean hasInstances = status == OperationStatus.SUCCESS;
            mySecCursor.close();
            return hasInstances;
        } catch (DatabaseException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    private byte[] className(ObjectSpecification specification) {
        try {
            return specification.getShortName().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

}


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


package org.apache.isis.extensions.hibernate.objectstore.tools.internal;

/**
 * TODO combine with other versions of TestSystem
 */
public class TestSystem {

    // private IsisConfiguration configuration;
    // private IsisContext isis;
    // private TestObjectLoader objectLoader;
    // private ObjectAdapterPersistor objectPersistor;
    // private ObjectReflector specificationLoader;
    //
    // public TestSystem() {
    // }
    //
    // public void addAdapter(final Object object, final DummyObjectAdapter adapter) {
    // objectLoader.addAdapter(object, adapter);
    // }
    //
    // public void addConfiguration(String name, String value) {
    // configuration.add(name, value);
    // }
    //
    // public void addLoadedIdentity(final DummyOid oid, final ObjectAdapter adapter) {
    // objectLoader.addIdentity(oid, adapter);
    // }
    //
    // // public void addCollectionAdapterAdapter(final CollectionAdapter collection) {
    // // objectLoader.addAdapter(collection.getObject(), collection);
    // // }
    //
    // public void addRecreated(final DummyOid oid, final DummyObjectAdapter adapter) {
    // objectLoader.addRecreated(oid, adapter);
    //
    // }
    //
    // public void addRecreatedTransient(final DummyObjectAdapter adapter) {
    // objectLoader.addRecreatedTransient(adapter);
    //
    // }
    //
    // public void addSpecification(final String name) {
    // specificationLoader.loadSpecification(name);
    // }
    //
    // public void addValue(final Object object, final EntryTextParser adapter) {
    // objectLoader.addAdapter(object, adapter);
    //
    // }
    //
    // public ObjectAdapter createAdapterForTransient(final Object associate) {
    // ObjectAdapter createAdapterForTransient = PersistorUtil.createAdapterForTransient(associate, false);
    // objectLoader.addAdapter(associate, createAdapterForTransient);
    // return createAdapterForTransient;
    // }
    //
    // public void init() {
    // isis = StaticContext.createInstance();
    // configuration = new PropertiesConfiguration();
    // IsisContext.setConfiguration(configuration);
    //        
    // specificationLoader = new JavaReflector();
    // // ((JavaReflector) specificationLoader).setReflectionPeerFactories(new ReflectionPeerFactory[0]);
    // isis.setReflector(specificationLoader);
    // objectLoader = new TestObjectLoader();
    // isis.setObjectLoader(objectLoader);
    // objectPersistor = new TestPersistor();
    // isis.setObjectPersistor(objectPersistor);
    // isis.setSession(new TestProxySession());
    //
    // specificationLoader.init();
    // objectLoader.init();
    // objectPersistor.init();
    // }
    //
    // public void setObjectPersistor(final ObjectAdapterPersistor objectManager) {
    // this.objectPersistor = objectManager;
    // }
    //
    // public void setupLoadedObject(final Object forObject, final ObjectAdapter adapter) {
    // ((TestObjectLoader) objectLoader).addAdapter(forObject, adapter);
    // }
    //
    // public void shutdown() {
    // IsisContext.closeSession();
    // }
}

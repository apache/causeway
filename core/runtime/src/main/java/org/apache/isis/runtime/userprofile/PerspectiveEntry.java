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


package org.apache.isis.runtime.userprofile;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.services.ServiceUtil;

public class PerspectiveEntry implements DebugInfo {
        private final List<Object> objects = new ArrayList<Object>();
        private final List<Object> services = new ArrayList<Object>();
        private String name;

        public PerspectiveEntry() {}
/*
        public PerspectiveEntry(XElement doc) {
            name = doc.Element("name").Value;

            XElement servicesElement = doc.Element("services");
            foreach (XElement serviceElement in servicesElement.Elements()) {
                XAttribute id = serviceElement.Attribute("id");
                IObjectAdapter s = IsisContext.ObjectPersistor.GetService(id.Value);
                if (s != null) {
                    services.Add(s.Object);
                }
                Console.WriteLine(id + "  " + s);
            }

            XElement objectsElement = doc.Element("objects");
            foreach (XElement objectElement in objectsElement.Elements()) {
                IObjectSpecification specification =
                    IsisContext.Reflector.LoadSpecification(
                        objectElement.Attribute("specification").Value);

                List<String> data = new List<string>();
                foreach (XElement dataElement in objectElement.Elements("data")) {
                    data.Add(dataElement.Value);
                }
                string[] dataArray = data.ToArray();

                Type oidType = TypeUtils.GetType(objectElement.Attribute("oid").Value);
                ConstructorInfo contructor = oidType.GetConstructor(new Type[] { typeof(String[]) });
                IOid oid = (IOid)contructor.Invoke(new object[] { dataArray });


                IObjectAdapter s = IsisContext.ObjectPersistor.LoadObject(oid, specification);
                if (s != null) {
                    objects.Add(s.Object);
                }
                Console.WriteLine(oid + "  " + s);

            }
        }


        public void SaveAsXml(XElement element) {
            element.Add(new XElement("name", name));

            XElement servicesElement = new XElement("services");
            foreach (object service in Services) {
                XElement serviceElement = new XElement("service");
                serviceElement.Add(new XAttribute("id", ServiceUtils.GetId(service)));
                servicesElement.Add(serviceElement);
            }
            element.Add(servicesElement);

            XElement objectsElement = new XElement("objects");
            foreach (object pojo in Objects) {
                IObjectAdapter obj = IsisContext.ObjectPersistor.GetAdapterFor(pojo);
                if (obj.Oid is IEncodedToStrings) {
                    XElement objectElement = new XElement("object");
                    objectElement.Add(new XAttribute("specification", obj.Specification.FullName));
                    objectElement.Add(new XAttribute("oid", obj.Oid.GetType().FullName));
                    string[] oidData = ((IEncodedToStrings)obj.Oid).ToEncodedStrings();
                    foreach (string data in oidData) {
                        objectElement.Add(new XElement("data", data));
                    }
                    objectsElement.Add(objectElement);
                }
            }
            element.Add(objectsElement);
        }
*/

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        // REVIEW should this deal with Isis, and the services with IDs (or Isis)
        public List<Object> getObjects() {
            return objects;
        }
        
        public List<Object> getServices() {
            return services;
        }

        public String getTitle() {
            return name + " (" + services.size() + " classes)";
        }

        public void addToObjects(Object obj) {
            if (!objects.contains(obj)) {
                objects.add(obj);
            }
        }

        public Object addToServices(Class serviceType) {
            Object service = findService(serviceType);
            addToServices(service);
            return service;
        }

        private Object findService(Class serviceType) {
            for (Object service : IsisContext.getServices()) {
                if (service.getClass().isAssignableFrom(serviceType)) {
                    return service;
                }
            }
            throw new IsisException("No service of type " + serviceType.getName());
        }

        public void addToServices(Object service) {
            if (service != null && !services.contains(service)) {
                services.add(service);
            }
        }

        public void addGenericRepository(Class type) {
            Object service = IsisContext.getPersistenceSession().getService("repository#" + type.getName()).getObject();
            addToServices(service);
        }

        public void removeFromObjects(Object obj) {
            objects.remove(obj);
        }

        public void removeServices(Class serviceType) {
            Object service = findService(serviceType);
            if (!services.contains(service)) {
                services.remove(service);
            }
        }

        public void removeFromServices(Object service) {
            services.remove(service);
        }

        public void copy(PerspectiveEntry template) {
            name = template.getName();
            for (Object service : template.getServices()) {
                addToServices(service);
            }
            for (Object obj : template.getObjects()) {
                addToObjects(obj);
            }
        }

        public void save(List<ObjectAdapter> objects) {
            this.objects.clear();
            for (ObjectAdapter obj : objects) {
                addToObjects(obj.getObject());
            }
        }

        public void debugData(DebugString debug) {
            debug.appendln("Name", getName());
            debug.blankLine();
            debug.appendTitle("Services (Ids)");
            debug.indent();
            for (Object service : getServices()) {
                debug.appendln(ServiceUtil.id(service));
            }
            debug.unindent();
            
            debug.blankLine();
            debug.appendTitle("Objects");
            debug.indent();
            AdapterManager adapterManager = IsisContext.getPersistenceSession().getAdapterManager();
            for (Object obj : getObjects()) {
                debug.appendln(adapterManager.adapterFor(obj).toString());
            }
            debug.unindent();
        }

        public String debugTitle() {
            return "Perspective";
        }
}



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

package dom;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;


public class Repository extends AbstractFactoryAndRepository {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "repository";
    }

    // }}

    // {{ allInstances (action)
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public List<MyObject> allInstances() {
        return allInstances(MyObject.class);
    }

    // }}

    // {{ newToDo (action)
    @MemberOrder(sequence = "2")
    public MyObject newInstance(@Named("Name") String name) {
        final MyObject newInstance = newTransientInstance(MyObject.class);
        newInstance.setName(name);
        persist(newInstance);
        return newInstance;
    }
    // }}
}

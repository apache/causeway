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
package org.apache.isis.viewer.json.viewer.resources.objects;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.json.viewer.RepContext;
import org.apache.isis.viewer.json.viewer.representations.Representation;

import com.google.common.collect.Lists;

public class ActionRepBuilder extends AbstractMemberRepBuilder<ObjectAction> {

    public static ActionRepBuilder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, ObjectAction oa) {
        return new ActionRepBuilder(repContext, objectAdapter, oa);
    }

    public ActionRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, ObjectAction oa) {
        super(repContext, objectAdapter, MemberType.ACTION, oa);
    }

    public Representation build() {
        putSelfIfRequired();
        putTypeRep();
        putMemberTypeRep();
        representation.put("actionType", objectMember.getType());
        representation.put("numParameters", objectMember.getParameterCount());
        putValueIfRequired();
        putDisabledReason();
        putMutatorsIfRequired();
        putDetailsIfRequired();
        return representation;
    }
    
    protected List<String> mutatorArgValues(MutatorSpec mutatorSpec) {
        List<String> values = Lists.newArrayList();
        for(int i=0; i<objectMember.getParameterCount(); i++) {
            values.add(argValueFor(i)); 
        }
        return values;
    }

    // TODO: expect in future to provide values for contributed actions.
    private String argValueFor(int i) {
        return "{arg" + i + "}";
    }

}
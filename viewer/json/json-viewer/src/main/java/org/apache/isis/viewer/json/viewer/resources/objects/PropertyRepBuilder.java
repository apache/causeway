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
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.RepContext;

import com.google.common.collect.Lists;

public class PropertyRepBuilder extends AbstractMemberRepBuilder<OneToOneAssociation> {

    public static PropertyRepBuilder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        return new PropertyRepBuilder(repContext, objectAdapter, otoa);
    }

    public PropertyRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        super(repContext, objectAdapter, MemberType.PROPERTY, otoa);
    }

    public JsonRepresentation build() {
        putSelfIfRequired();
        putTypeRep();
        putIdRep();
        putMemberTypeRep();
        putValueIfRequired();
        putDisabledReason();
        putChoices();
        putMutatorsIfRequired();
        putDetailsIfRequired();
        return representation;
    }


	@Override
    protected Object valueRep() {
        ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if(valueAdapter == null) {
		    return null;
		}
        return DomainObjectRepBuilder.valueOrRef(repContext, valueAdapter, objectMember.getSpecification(), getOidStringifier(), getLocalization());
    }

    private void putChoices() {
		Object propertyChoices = propertyChoices();
		if(propertyChoices != null) {
			representation.put("choices", propertyChoices);
		}
	}

	private Object propertyChoices() {
		ObjectAdapter[] choiceAdapters = objectMember.getChoices(objectAdapter);
		if(choiceAdapters == null || choiceAdapters.length == 0) {
			return null;
		}
        List<Object> list = Lists.newArrayList();
        for (final ObjectAdapter choiceAdapter : choiceAdapters) {
        	ObjectSpecification objectSpec = objectMember.getSpecification();
        	list.add(DomainObjectRepBuilder.valueOrRef(repContext, choiceAdapter, objectSpec, getOidStringifier(), getLocalization()));
        }
        return list;
	}

}
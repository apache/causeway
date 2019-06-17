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
package domainapp.dom.types.blob;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.runtime.system.context.IsisContext;

import lombok.val;

@Singleton
public class DemoBlobStore {
	
	@Inject HttpSession session;
	
	public void put(UUID uuid, Blob blob) {
		if(blob==null) {
			return;
		}
		session.setAttribute(uuid.toString(), blob);
	}

	public Blob get(UUID uuid) {
		if(uuid==null) {
			return null;
		}
		return (Blob) session.getAttribute(uuid.toString());
	}
	
	private static DemoBlobStore current() {
		return IsisContext.getServiceRegistry().lookupServiceElseFail(DemoBlobStore.class);
	}
	
	// -- JAXB ADAPTER
	
    public static final class BlobAdapter extends XmlAdapter<String, Blob> {

    	
		@Override
		public Blob unmarshal(String data) throws Exception {
		    if(data==null) {
                return null;
            }
		    val uuid = UUID.fromString(data);
		    return DemoBlobStore.current().get(uuid);
		}
		
		@Override
		public String marshal(Blob blob) throws Exception {
			if(blob==null) {
                return null;
            }
		    val uuid = UUID.randomUUID();
			DemoBlobStore.current().put(uuid, blob);
			return uuid.toString();
		}
    	
    }
	
	
}

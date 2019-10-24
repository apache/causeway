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

import javax.servlet.http.HttpSession;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.apache.isis.applib.value.Blob;

import lombok.val;

public class DemoBlobStore {

    // -- JAXB ADAPTER

    public static final class BlobAdapter extends XmlAdapter<String, Blob> {
        
        @Override
        public Blob unmarshal(String data) throws Exception {
            if(data==null) {
                return null;
            }
            val uuid = UUID.fromString(data);
            return get(uuid);
        }

        @Override
        public String marshal(Blob blob) throws Exception {
            if(blob==null) {
                return null;
            }
            val uuid = UUID.randomUUID();
            put(uuid, blob);
            return uuid.toString();
        }
        
        private void put(UUID uuid, Blob blob) {
            if(blob==null) {
                return;
            }
            session().setAttribute(uuid.toString(), blob);
        }

        private Blob get(UUID uuid) {
            if(uuid==null) {
                return null;
            }
            return (Blob) session().getAttribute(uuid.toString());
        }
        
        public static HttpSession session() {
            ServletRequestAttributes attr = 
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attr.getRequest().getSession(false); // false == don't allow create
        }

    }


}

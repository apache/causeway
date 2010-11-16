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


package org.apache.isis.extensions.xml.profilestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.encoding.DataOutputStreamExtended;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.extensions.xml.objectstore.internal.data.xml.ContentWriter;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.services.ServiceUtil;

public class UserProfileContentWriter  implements ContentWriter {
    private final UserProfile userProfile;

    public UserProfileContentWriter(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public void write(Writer writer) throws IOException {
        final StringBuffer xml = new StringBuffer();
        xml.append("<profile>\n");
        
        Options options = userProfile.getOptions();
        writeOptions(xml, options, null, 0);
        
        xml.append("  <perspectives>\n");
        for (String perspectiveName : userProfile.list()) {
            PerspectiveEntry perspective = userProfile.getPerspective(perspectiveName);
            
            xml.append("    <perspective" + attribute("name", perspectiveName)+ ">\n");
            xml.append("      <services>\n");
            for (Object service : perspective.getServices()) {
                xml.append("        <service " + attribute("id", ServiceUtil.id(service))+ "/>\n"); 
            }
            xml.append("      </services>\n");
            xml.append("      <objects>\n");
            for (Object object : perspective.getObjects()){
                ObjectAdapter adapter = getPersistenceSession().getAdapterManager().adapterFor(object);
                OutputStream out = new ByteArrayOutputStream();
                DataOutputStreamExtended outputImpl   = new DataOutputStreamExtended(out);
                adapter.getOid().encode(outputImpl);
                // FIX need to sort out encoding
                //xml.append("      <object>" + out.toString() + "</object>\n");
                xml.append("        <object>" + "not yet encoding properly" + "</object>\n");
            }
            xml.append("      </objects>\n");
            xml.append("    </perspective>\n");
        }
        xml.append("  </perspectives>\n");
        
        xml.append("</profile>\n");
        
        writer.write(xml.toString());
    }

    private void writeOptions(final StringBuffer xml, Options options, String name1, int level) {
        String spaces = StringUtils.repeat("  ", level);
        
        Iterator<String> names = options.names();
        if (level == 0 || names.hasNext()) {
            xml.append(spaces + "  <options");
            if (name1 != null) {
                xml.append(" id=\""+ name1 + "\"");
            }
            xml.append(">\n");
            while (names.hasNext()) {
                String name = names.next();
                if (options.isOptions(name)) {
                    writeOptions(xml, options.getOptions(name), name, level + 1);
                } else {
                    xml.append(spaces + "    <option" + attribute("id", name)+ ">"+ options.getString(name) + "</option>\n");
                }
            }
            xml.append(spaces + "  </options>\n");
        }
    }

    private String attribute(final String name, final String value) {
        return " " + name + "=\"" + value + "\"";
    }

    /////////////////////////////////////////////////////
    // Dependencies (from context)
    /////////////////////////////////////////////////////
    
    
    protected static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
    

}



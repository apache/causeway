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

import org.apache.isis.metamodel.config.ConfigurationConstants;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.extensions.xml.objectstore.internal.data.xml.XmlFile;
import org.apache.isis.runtime.userprofile.UserProfile;
import org.apache.isis.runtime.userprofile.UserProfileStore;

import com.google.inject.Inject;


public class XmlUserProfileStore implements UserProfileStore {

    private static final String XML_DIR = ConfigurationConstants.ROOT + "xmluserprofile.dir";
    private final XmlFile xmlFile;

    @Inject
    public XmlUserProfileStore(IsisConfiguration configuration) {
        String directory = configuration.getString(XML_DIR, "xml/profiles");
        xmlFile = new XmlFile(configuration, directory);
    }

    public UserProfile getUserProfile(String userName) {
        final UserProfileDataHandler handler = new UserProfileDataHandler();
        if (xmlFile.parse(handler, userName)) {
            return handler.getUserProfile();
        } else {
            return null;
        }
    }

    public boolean isFixturesInstalled() {
        return xmlFile.isFixturesInstalled();
    }

    public void save(final String userName, final UserProfile userProfile) {
        xmlFile.writeXml(userName, new UserProfileContentWriter(userProfile));
    }

}


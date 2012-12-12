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

package org.apache.isis.profilestore.xml.internal;

import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.core.runtime.userprofile.UserProfile;

public class UserProfileDataHandler extends DefaultHandler {
    private final StringBuffer data = new StringBuffer();
    private final UserProfile userProfile = new UserProfile();
    private final Stack<Options> options = new Stack<Options>();
    private String optionName;
    private boolean isProfile;
    private boolean isOptions;
    private boolean isPerspectives;
    private PerspectiveEntry perspective;
    private boolean isServices;
    private boolean isObjects;

    public UserProfile getUserProfile() {
        return userProfile;
    }

    @Override
    public void characters(final char[] ch, final int start, final int end) throws SAXException {
        data.append(new String(ch, start, end));
    }

    @Override
    public void endElement(final String ns, final String name, final String tagName) throws SAXException {
        if (tagName.equals("options")) {
            options.pop();
            isOptions = options.size() > 0;
        } else if (tagName.equals("perspectives")) {
            isPerspectives = false;
        } else if (tagName.equals("perspective")) {
            // TODO add perspective to profile

            perspective = null;
        } else if (tagName.equals("services")) {
            isServices = false;
        } else if (tagName.equals("objects")) {
            isObjects = false;
        } else if (tagName.equals("option")) {
            final String value = data.toString();
            options.peek().addOption(optionName, value);
        } else if (tagName.equals("name")) {
            final String value = data.toString();
            System.out.println(value);
        }
    }

    @Override
    public void startElement(final String ns, final String name, final String tagName, final Attributes attributes) throws SAXException {

        if (isProfile) {
            if (isOptions) {
                if (tagName.equals("option")) {
                    optionName = attributes.getValue("id");
                    data.setLength(0);
                } else if (tagName.equals("options")) {
                    final String optionsName = attributes.getValue("id");
                    final Options newOptions = new Options();
                    options.peek().addOptions(optionsName, newOptions);
                    options.push(newOptions);
                } else {
                    throw new SAXException("Invalid element in options: " + tagName);
                }
            } else if (isPerspectives) {
                if (perspective != null) {
                    if (isServices) {
                        if (tagName.equals("service")) {
                            final String serviceId = attributes.getValue("id");
                            final List<Object> serviceObjects = IsisContext.getServices();
                            for (final Object service : serviceObjects) {
                                if (ServiceUtil.id(service).equals(serviceId)) {
                                    perspective.addToServices(service);
                                    break;
                                }
                            }
                        } else {
                            throw new SAXException("Invalid element in services: " + tagName);
                        }
                    } else if (isObjects) {
                        // TODO reload objects
                    } else if (tagName.equals("services")) {
                        isServices = true;
                    } else if (tagName.equals("objects")) {
                        isObjects = true;
                    } else {
                        throw new SAXException("Invalid element in perspective: " + tagName);
                    }
                } else if (tagName.equals("perspective")) {
                    perspective = userProfile.newPerspective(attributes.getValue("name"));
                } else {
                    throw new SAXException("Invalid element in perspectives: " + tagName);
                }
            } else if (tagName.equals("options")) {
                isOptions = true;
                options.push(userProfile.getOptions());
            } else if (tagName.equals("perspectives") && !isOptions) {
                isPerspectives = true;
            } else {
                throw new SAXException("Invalid element in profile: " + tagName);
            }

        }
        /*
         * else { throw new SAXException("Invalid data"); } }
         */

        if (tagName.equals("profile")) {
            isProfile = true;
        }

    }

}

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
package org.apache.isis.viewer.restful.viewer.resources.user;

import java.util.List;

import javax.ws.rs.Path;

import org.apache.isis.viewer.restful.applib.resources.UserResource;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.html.XhtmlTemplate;
import org.apache.isis.viewer.restful.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.restful.viewer.tree.Element;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/user")
public class UserResourceImpl extends ResourceAbstract implements UserResource {

    @Override
    public String user() {
        init();

        final XhtmlTemplate xhtml = new XhtmlTemplate("User", getServletRequest());
        xhtml.appendToBody(asDivIsisSession());
        xhtml.appendToBody(resourcesDiv());

        final Element div = xhtmlRenderer.div_p("Roles", HtmlClass.SECTION);

        final Element ul = xhtmlRenderer.ul(HtmlClass.ROLES);
        final List<String> roles = getSession().getRoles();
        for (final String role : roles) {
            ul.appendChild(xhtmlRenderer.li_p(role, HtmlClass.ROLE));
        }
        div.appendChild(ul);

        xhtml.appendToBody(div);

        return xhtml.toXML();
    }
}

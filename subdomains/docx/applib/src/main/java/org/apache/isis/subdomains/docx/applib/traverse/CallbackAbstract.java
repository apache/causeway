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
package org.apache.isis.subdomains.docx.applib.traverse;

import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;

public abstract class CallbackAbstract implements Callback {
    public void walkJAXBElements(Object parent) {
        List<Object> children = getChildren(parent);
        if (children == null) {
            return;
        }
        for (Object o : children) {
            // if wrapped in javax.xml.bind.JAXBElement, get its value
            o = XmlUtils.unwrap(o);
            apply(o);
            if (shouldTraverse(o)) {
                walkJAXBElements(o);
            }
        }
    }

    public List<Object> getChildren(Object o) {
        return TraversalUtil.getChildrenImpl(o);
    }

    public boolean shouldTraverse(Object o) {
        return true;
    }
}

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
import java.util.function.Predicate;

import org.apache.commons.compress.utils.Lists;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;

public class AllMatches<T> extends CallbackAbstract {

    public static <T> List<T> matching(Object docxObject, Predicate<Object> predicate) {
        return new AllMatches<T>(docxObject, predicate).getResult();
    }

    private final Object parent;
    private final Predicate<Object> predicate;

    private final List<T> result = Lists.newArrayList();

    AllMatches(Object parent, Predicate<Object> predicate) {
        this.parent = parent;
        this.predicate = predicate;
    }

    @SuppressWarnings("unchecked")
    public List<Object> apply(Object o) {
        o = XmlUtils.unwrap(o);

        if(predicate.test(o)) {
            this.result.add((T) o);
            return null;
        }
        return null;
    }

    public List<T> getResult() {
        new TraversalUtil(parent, this);
        return this.result;
    }

}

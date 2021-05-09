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
package org.apache.isis.extensions.secman.jdo.user.dom;

import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepositoryAbstract;

import lombok.val;

@Component
@Named("isis.ext.secman.RegexReplacer")
public class RegexReplacer implements org.apache.isis.extensions.secman.api.util.RegexReplacer {

    @Override
    public String asRegex(String str) {
        val search = str.replace("*", ".*").replace("?", ".");
        return String.format("(?i).*%s.*", search);
    }

}

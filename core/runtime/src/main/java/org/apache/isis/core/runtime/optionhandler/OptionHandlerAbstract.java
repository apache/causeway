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


package org.apache.isis.core.runtime.optionhandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.lang.ListUtils;


public abstract class OptionHandlerAbstract implements OptionHandler {
	
	public OptionHandlerAbstract() {
	}

    protected StringBuffer availableInstallers(final Object[] factories) {
        final StringBuffer types = new StringBuffer();
        for (int i = 0; i < factories.length; i++) {
            if (i > 0) {
                types.append("; ");
            }
            types.append(((Installer) factories[i]).getName());
        }
        return types;
    }

    protected List<String> getOptionValues(CommandLine commandLine, String opt) {
        List<String> list = new ArrayList<String>();
        String[] optionValues = commandLine.getOptionValues(opt);
        if (optionValues != null) {
            for (String optionValue : optionValues) {
                ListUtils.appendDelimitedStringToList(optionValue, list);
            }
        }
        return list;
    }



}

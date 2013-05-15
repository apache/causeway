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

package org.apache.isis.objectstore.jdo.datanucleus;

import java.lang.reflect.Field;
import java.util.Map;

import org.datanucleus.NucleusContext;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.state.ObjectProviderFactory;

public class NucleusContextForIsis extends NucleusContext {

	public NucleusContextForIsis(String apiName, ContextType type,
			Map startupProps, PluginManager pluginMgr) {
		super(apiName, type, startupProps, pluginMgr);
	}

	public NucleusContextForIsis(String apiName, ContextType type,
			Map startupProps) {
		super(apiName, type, startupProps);
	}

	public NucleusContextForIsis(String apiName, Map startupProps,
			PluginManager pluginMgr) {
		super(apiName, startupProps, pluginMgr);
	}

	public NucleusContextForIsis(String apiName, Map startupProps) {
		super(apiName, startupProps);
	}

	/**
	 * Horrendous code...
	 */
	@Override
	public ObjectProviderFactory getObjectProviderFactory() {
		try {
			Field declaredField = NucleusContext.class.getDeclaredField("opFactory");
			declaredField.setAccessible(true);
			ObjectProviderFactory opf = (ObjectProviderFactory) declaredField.get(this);
			if(opf == null) {
				opf = newObjectProviderFactory();
				declaredField.set(this, opf);
			}
			return opf;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ObjectProviderFactoryForIsis newObjectProviderFactory() {
		return new ObjectProviderFactoryForIsis(this);
	}
	
}

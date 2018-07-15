/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.swagger.plugins;

import org.apache.isis.core.metamodel.services.swagger.internal.ValuePropertyPlugin;

import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;

public class IsisTimeValuePropertyPlugin implements ValuePropertyPlugin {

	@Override
	public void plugin(ValuePropertyCollector collector) {

		collector.addValueProperty(org.apache.isis.applib.value.DateTime.class, DateTimeProperty::new);
		collector.addValueProperty(org.apache.isis.applib.value.Date.class, DateProperty::new);
		
	}

}

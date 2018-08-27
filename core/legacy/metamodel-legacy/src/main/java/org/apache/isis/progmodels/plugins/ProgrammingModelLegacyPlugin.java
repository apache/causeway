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
package org.apache.isis.progmodels.plugins;

import org.apache.isis.core.metamodel.facets.object.encodeable.annotcfg.EncodableFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.membergroups.annotprop.MemberGroupLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.parseable.annotcfg.ParseableFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.value.date.DateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetime.DateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.time.TimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestamp.TimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelPlugin;

public class ProgrammingModelLegacyPlugin implements ProgrammingModelPlugin {

	@Override
	public void plugin(FactoryCollector collector) {
	    
	    collector.addFactory(new MemberGroupLayoutFacetFactory(), FacetFactoryCategory.VALUE);
		
		collector.addFactory(new DateValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);
		collector.addFactory(new DateTimeValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);
        
		collector.addFactory(new TimeStampValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);
		collector.addFactory(new TimeValueFacetUsingSemanticsProviderFactory(), FacetFactoryCategory.VALUE);

		collector.addFactory(new EncodableFacetAnnotationElseConfigurationFactory(), FacetFactoryCategory.VALUE);
		collector.addFactory(new ParseableFacetAnnotationElseConfigurationFactory(), FacetFactoryCategory.VALUE);
		
	}

}

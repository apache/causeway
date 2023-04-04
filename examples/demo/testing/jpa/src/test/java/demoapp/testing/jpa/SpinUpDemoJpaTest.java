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
package demoapp.testing.jpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.val;

@SpringBootTest(
        classes = {
                DemoDomainJpa_forTesting.class
        },
        properties = {
                //"spring.jpa.show-sql=true",
                //"logging.level.org.springframework.orm.jpa=DEBUG"
        })
@ActiveProfiles(profiles = "demo-jpa")
class SpinUpDemoJpaTest {
    
    @Autowired MetaModelContext mmc;
    @Autowired CausewayBeanTypeRegistry causewayBeanTypeRegistry; 
    
    @Test
    @DisplayName("verifyAllSpecificationsDiscovered")
    @UseReporter(DiffReporter.class)
    void verify() {
        
        var specsBySort = _Multimaps.<String, String>newListMultimap(LinkedHashMap<String, List<String>>::new, ArrayList::new);

        // collect all ObjectSpecifications into a list-multi-map, where BeanSort is the key
        mmc.getSpecificationLoader().snapshotSpecifications()
                .stream()
                .sorted()
                .forEach(spec->specsBySort.putElement(spec.getBeanSort().name(), spec.getLogicalTypeName()));
        
        // export the list-multi-map to YAML format
        val sb = new StringBuilder();
        sb.append("ObjectSpecifications:\n");
        specsBySort
            .forEach((key, list)->{
                sb.append(String.format("  %s:\n", key));
                list.forEach(logicalTypeName->{
                    sb.append(String.format("  - %s\n", logicalTypeName));
                });
            });
        
        //debug
        //System.err.printf("%s%n", sb.toString());
        
        // verify against approved run
        Approvals.verify(sb.toString(), new Options()
                .forFile()
                .withExtension(".yaml"));
    }

}

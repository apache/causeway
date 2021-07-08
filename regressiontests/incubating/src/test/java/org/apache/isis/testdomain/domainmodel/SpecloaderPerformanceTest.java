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
package org.apache.isis.testdomain.domainmodel;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class
        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.core.meta-model.validator.explicit-logical-type-names=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel,
    //IsisPresets.DebugProgrammingModel,
})
//XXX not a real test, just for performance tuning
@Log4j2
class SpecloaderPerformanceTest {

    @Inject private IsisConfiguration config;
    @Inject private SpecificationLoader specificationLoader;
    //@Inject private MetaModelServiceMenu metaModelServiceMenu; //XXX could use ascii diff utilizing metaModelServiceMenu

    private final _Lazy<Set<String>> referenceMetamodelSummary = _Lazy.threadSafe(()->
        _MetamodelUtil.featuresSummarized(specificationLoader.snapshotSpecifications()));

    @BeforeEach
    void setup() {
        config.getCore().getMetaModel().getIntrospector().setParallelize(false);
        referenceMetamodelSummary.get(); // memoize
        config.getCore().getMetaModel().getIntrospector().setParallelize(true);

        _Probe.errOut("========================== SETUP DONE ===================================");

    }

    static long ITERATIONS = 100; /* should typically run in ~10s */
    static long EXPECTED_MILLIS_PER_ITERATION = 100;

    @Test
    void concurrentSpecloading_shouldYieldSameMetamodelAsSequential() {
        _Annotations.clearCache();
        specificationLoader.disposeMetaModel();
        specificationLoader.createMetaModel();
        val mmSummary = _MetamodelUtil.featuresSummarized(specificationLoader.snapshotSpecifications());

        val missingFeatures = _Sets.minus(referenceMetamodelSummary.get(), mmSummary);
        if(!missingFeatures.isEmpty()) {
            System.err.println(String.format("%d missing features", missingFeatures.size()));
            missingFeatures.forEach(f->{
                System.err.println(String.format(" - %s", f));
            });
        }
        assertEquals(Collections.<String>emptySet(), missingFeatures);
    }

    @Test @Tag("LongRunning")
    void repeatedConcurrentSpecloading_shouldNotDeadlock() {



        val timeOutMillis = ITERATIONS * EXPECTED_MILLIS_PER_ITERATION;
        val goodUntilMillis = System.currentTimeMillis() + timeOutMillis;

        val repeatedRun = (Runnable)()->{

            for(int i=0; i<ITERATIONS; ++i) {
                _Annotations.clearCache();
                specificationLoader.disposeMetaModel();
                specificationLoader.createMetaModel();

                if(System.currentTimeMillis() > goodUntilMillis) {
                    fail("timed out");
                }
            }

        };

        _Timing.runVerbose(log, "Repeated Concurrent Specloading", repeatedRun);
    }


}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.html.domain;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.xactn.TransactionService;

@Configuration
public class SampleDataConfiguration {

    @Bean
    ApplicationRunner loadSampleData(
            final TransactionService transactionService,
            final SampleObjectRepository repository) {
        return args -> transactionService.runTransactional(Propagation.REQUIRED, () -> {
            if (repository.findById(SampleObject.SAMPLE_ID) == null) {
                final SampleObject sample = new SampleObject(
                        SampleObject.SAMPLE_ID,
                        SampleObject.SAMPLE_NAME,
                        SampleObject.SAMPLE_CODE,
                        SampleObject.SAMPLE_SECRET);
                sample.addRelatedObject(
                        SampleRelatedObject.FIRST_ID,
                        SampleRelatedObject.FIRST_NAME,
                        SampleRelatedObject.FIRST_CODE);
                sample.addRelatedObject(
                        SampleRelatedObject.SECOND_ID,
                        SampleRelatedObject.SECOND_NAME,
                        SampleRelatedObject.SECOND_CODE);
                repository.persist(sample);
            }
        });
    }
}

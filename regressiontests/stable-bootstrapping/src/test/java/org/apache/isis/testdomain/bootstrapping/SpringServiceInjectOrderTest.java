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
package org.apache.isis.testdomain.bootstrapping;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.message.MessageServiceDefault;
import org.apache.isis.testdomain.conf.Configuration_headless;

import lombok.Getter;
import lombok.val;

=======
        >>>>>>>4dd5fb2460(ISIS-2751:fixes ModuleWithFixturesService)

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                SpringServiceInjectOrderTest.TestConfig.class,

                SpringServiceInjectOrderTest.Average.class,
                SpringServiceInjectOrderTest.Excellent.class,
                SpringServiceInjectOrderTest.Good.class,
                SpringServiceInjectOrderTest.DummyService.class
        },
        properties = {
                // "isis.core.meta-model.introspector.parallelize=false",
                // "logging.level.ObjectSpecificationAbstract=TRACE"
        })
@TestPropertySource({
        IsisPresets.SilenceMetaModel,
        IsisPresets.SilenceProgrammingModel,
        IsisPresets.UseLog4j2Test
})
class SpringServiceInjectOrderTest {

    @Inject
    DummyService dummyService;
    @Inject
    ServiceInjector serviceInjector;
    @Inject
    OrderComparator orderComparator;

    @BeforeEach
    void beforeEach() {

    }

    @Test
    void defaultOrdering_shouldConsiderAnnotations() throws IOException {
        assertTrue(orderComparator instanceof AnnotationAwareOrderComparator);
    }

    @Test
    void injectionOnServices_shouldFollowOrder() throws IOException {

        val messageService = dummyService.getMessageService();
        assertNotNull(messageService);
        assertTrue(messageService instanceof MessageServiceDefault);

        // injected as per @Order
        val ratings = dummyService.getRatings();
        assertThat(ratings.get(0).getRating(), is(equalTo(1)));
        assertThat(ratings.get(1).getRating(), is(equalTo(2)));
        assertThat(ratings.get(2).getRating(), is(equalTo(3)));

        // uses the @Primary
        assertThat(dummyService.getSomeArbitraryRating().getRating(), is(equalTo(2)));

        // does match @Qualifier to @Qualifier
        assertThat(dummyService.getQualifiedRating1().getRating(), is(equalTo(1)));
        assertThat(dummyService.getQualifiedRating2().getRating(), is(equalTo(2)));
        assertThat(dummyService.getQualifiedRating3().getRating(), is(equalTo(3)));

        // does NOT match field name to @Qualifier
        assertThat(dummyService.getTallest().getRating(), is(equalTo(2))); // rather than '1'... so defaulted to @Primary

        // does NOT match field name to @Qualifier
        assertThat(dummyService.getMostExcellentName().getRating(), is(equalTo(2)));  // rather than '1'... so defaulted to @Primary
    }

    @Test
    void injectionOnObjects_shouldFollowOrder() throws IOException {

        val dummyObject = new DummyObject();
        serviceInjector.injectServicesInto(dummyObject);

        val messageService = dummyObject.getMessageService();
        assertNotNull(messageService);
        assertTrue(messageService instanceof MessageServiceDefault);

        // injected as per @Order
        val ratings = dummyObject.getRatings();
        assertThat(ratings.get(0).getRating(), is(equalTo(1)));
        assertThat(ratings.get(1).getRating(), is(equalTo(2)));
        assertThat(ratings.get(2).getRating(), is(equalTo(3)));

        // uses the @Primary
        assertThat(dummyObject.getSomeArbitraryRating().getRating(), is(equalTo(2)));

        // does match @Qualifier to @Qualifier
        assertThat(dummyObject.getQualifiedRating1().getRating(), is(equalTo(1)));
        assertThat(dummyObject.getQualifiedRating2().getRating(), is(equalTo(2)));
        assertThat(dummyObject.getQualifiedRating3().getRating(), is(equalTo(3)));

        // does NOT match field name to @Qualifier
        assertThat(dummyObject.getTallest().getRating(), is(equalTo(2))); // rather than '1'... so defaulted to @Primary

        // does NOT match field name to @Qualifier
        assertThat(dummyObject.getMostExcellentName().getRating(), is(equalTo(2)));  // rather than '1'... so defaulted to @Primary

    }


    interface Rating {
        int getRating();
    }

    @Configuration
    static class TestConfig {
    }

    @Service
    @Order(PriorityPrecedence.EARLY)
    @Qualifier("tallest")
    @Named("withExcellentName")
    static class Excellent implements Rating {

        @Override
        public int getRating() {
            return 1;
        }
    }

    @Service
    @Priority(PriorityPrecedence.MIDPOINT)
    @Qualifier("tall")
    @Named("withGoodName")
    static class Good implements Rating {

        @Override
        public int getRating() {
            return 2;
        }
    }

    @Service
    @Order(PriorityPrecedence.LAST)
    @Qualifier("middle")
    @Named("withAverageName")
    static class Average implements Rating {

        @Override
        public int getRating() {
            return 3;
        }
    }

    @Service
    static class DummyService {
        @Inject
        @Getter
        MessageService messageService;
        @Inject
        @Getter
        List<Rating> ratings;
        @Inject
        @Getter
        Rating primaryRating;
        @Inject
        @Getter
        Rating someArbitraryRating;
        @Inject
        @Getter
        @Qualifier("tallest")
        Rating qualifiedRating1;
        @Inject
        @Getter
        @Qualifier("tall")
        Rating qualifiedRating2;
        @Inject
        @Getter
        @Qualifier("middle")
        Rating qualifiedRating3;
        @Inject
        @Getter
        Rating tallest;
        @Inject
        @Getter
        Rating mostExcellentName;

        // this doesn't bootstrap, because matching is done using the service's @Qualifier, not the service's @Name
        // @Inject @Getter @Qualifier("mostExcellentName") Rating namedRating1;
    }

    @DomainObject
    static class DummyObject {
        @Inject
        @Getter
        MessageService messageService;
        @Inject
        @Getter
        List<Rating> ratings;
        @Inject
        @Getter
        Rating someArbitraryRating;
        @Inject
        @Getter
        @Qualifier("tallest")
        Rating qualifiedRating1;
        @Inject
        @Getter
        @Qualifier("tall")
        Rating qualifiedRating2;
        @Inject
        @Getter
        @Qualifier("middle")
        Rating qualifiedRating3;
        @Inject
        @Getter
        Rating tallest;
        @Inject
        @Getter
        Rating mostExcellentName;
    }

}

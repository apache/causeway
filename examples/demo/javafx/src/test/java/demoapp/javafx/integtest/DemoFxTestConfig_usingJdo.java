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
package demoapp.javafx.integtest;

import javax.sql.DataSource;

import org.datanucleus.store.rdbms.datasource.dbcp2.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.incubator.viewer.javafx.model.events.JavaFxViewerConfig;

import demoapp.javafx.DemoAppJavaFx;
import javafx.application.HostServices;
import lombok.val;

@Configuration
@PropertySources({
    @PropertySource(CausewayPresets.DatanucleusAutocreateNoValidate),
    @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema)
})
public class DemoFxTestConfig_usingJdo {

    //XXX why is the H2InMemory_withUniqueSchema preset not working?
    @Bean(destroyMethod = "close")
    public DataSource getDataSource() {
        val dataSourceBuilder = DataSourceBuilder.create().type(BasicDataSource.class);
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:mem:test");
        dataSourceBuilder.username("sa");
        dataSourceBuilder.password("");
        return dataSourceBuilder.build();
    }

    @Bean
    public JavaFxViewerConfig viewerConfig() {
        return new DemoAppJavaFx().viewerConfig();
    }

    @Bean
    public HostServices hostServices() {
        return  null;
    }



}

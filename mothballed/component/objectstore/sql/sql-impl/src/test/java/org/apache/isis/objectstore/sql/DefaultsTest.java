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

package org.apache.isis.objectstore.sql;

import static org.hamcrest.Matchers.is;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;

public class DefaultsTest {
    @Test
    public void tablePrefixDefaultsTo_Isis() {
        final String prefix = "isis.persistor.sql";
        final IsisConfiguration config = new IsisConfigurationDefault();
        Defaults.initialise(prefix, config);
        Assert.assertThat(Defaults.getTablePrefix(), is("isis_"));
    }

    @Test
    public void tablePrefixCanBeReplaced() {
        final String prefix = "isis.persistor.sql";
        final String key = "isis.persistor.sql.default.tableprefix";
        final IsisConfigurationDefault config = new IsisConfigurationDefault();
        config.add(key, "");
        Defaults.initialise(prefix, config);
        Assert.assertThat(Defaults.getTablePrefix(), is(""));
    }
    
    @Test 
    public void checkLimitStatement(){
        final String prefix = "isis.persistor.sql";
        final IsisConfigurationDefault config = new IsisConfigurationDefault();
        Defaults.initialise(prefix, config);
        
        final long startIndex=0;
        final long rowCount=0;
        
        Assert.assertThat(Defaults.getLimitsClause(startIndex, rowCount), is("LIMIT 0, 0"));
    }
}

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
package org.apache.isis.applib.services.settings;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SettingAbstractTest {

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    private static final class SettingAbstractForTesting extends SettingAbstract {
        private final String key;
        private final String valueRaw;
        private final SettingType type;
        
        public SettingAbstractForTesting(String key, String valueRaw, SettingType type) {
            this.key = key;
            this.valueRaw = valueRaw;
            this.type = type;
        }
        
        public String getKey() {
            return key;
        }

        public String getValueRaw() {
            return valueRaw;
        }

        public SettingType getType() {
            return type;
        }

        public String getDescription() {
            return null;
        }
    }
    
    private SettingAbstract strSetting;
    private SettingAbstract intSetting;
    private SettingAbstract localDateSetting;
    private SettingAbstract longSetting;
    private SettingAbstract boolSetting;
    
    private LocalDate someLocalDate;
    
    @Before
    public void setUp() throws Exception {
        someLocalDate = new LocalDate(2012,4,1);
        
        strSetting = new SettingAbstractForTesting("strSetting", "ABC", SettingType.STRING);
        intSetting = new SettingAbstractForTesting("intSetting", "" + Integer.MAX_VALUE, SettingType.INT);
        localDateSetting = new SettingAbstractForTesting("localDateSetting", someLocalDate.toString(SettingAbstract.DATE_FORMATTER), SettingType.LOCAL_DATE);
        longSetting = new SettingAbstractForTesting("longSetting", ""+Long.MAX_VALUE, SettingType.LONG);
        boolSetting = new SettingAbstractForTesting("boolSetting", Boolean.TRUE.toString(), SettingType.BOOLEAN);
    }
    
    @Test
    public void happyCases() {
        assertThat(strSetting.valueAsString(), is("ABC"));
        assertThat(intSetting.valueAsInt(), is(Integer.MAX_VALUE));
        assertThat(localDateSetting.valueAsLocalDate(), is(someLocalDate));
        assertThat(longSetting.valueAsLong(), is(Long.MAX_VALUE));
        assertThat(boolSetting.valueAsBoolean(), is(true));
    }

    @Test
    public void sadCases() {
        expectedExceptions.expectMessage("Setting 'strSetting' is of type STRING, not of type INT");
        strSetting.valueAsInt();
        expectedExceptions.expectMessage("Setting 'strSetting' is of type STRING, not of type LONG");
        strSetting.valueAsLong();
        expectedExceptions.expectMessage("Setting 'strSetting' is of type STRING, not of type LOCAL_DATE");
        strSetting.valueAsLocalDate();
        expectedExceptions.expectMessage("Setting 'strSetting' is of type STRING, not of type BOOLEAN");
        strSetting.valueAsBoolean();
        expectedExceptions.expectMessage("Setting 'intSetting' is of type INT, not of type STRING");
        intSetting.valueAsString();
    }

}

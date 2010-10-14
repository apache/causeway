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


package org.apache.isis.example.expenses.claims;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.When;


@Bounded
@Immutable(When.ONCE_PERSISTED)
public class ExpenseType extends AbstractDomainObject {

    // {{ Title

    @Override
    public String toString() {
        final StringBuffer t = new StringBuffer();
        t.append(titleString);
        return t.toString();
    }

    // }}

    // {{ TitleString field
    private String titleString;

    public String getTitleString() {
        return this.titleString;
    }

    public void setTitleString(final String titleString) {
        this.titleString = titleString;
    }

    // }}

    /**
     * This method potentially allows each instance of ExpenseType to have the same icon as its corresponding
     * classname.
     */
    public String iconName() {
        return getTitleString();
    }

    // {{ Corresponding Class
    private String correspondingClass;

    /**
     * The fully-qualified path name for the class that this instance of ApplicationType corresponds to -
     * typically a sub-class of Application
     */
    @Hidden
    public String getCorrespondingClassName() {
        return correspondingClass;
    }

    /**
     * @see #geTitleString
     */
    public void setCorrespondingClassName(final String correspondingClass) {
        this.correspondingClass = correspondingClass;
    }

    /**
     * Converts the correspondingClassName into a java.lang.class.
     * 
     * @return
     */
    public Class<?> correspondingClass() {
        try {
            return Class.forName(getCorrespondingClassName());
        } catch (final ClassNotFoundException e) {
            throw new ApplicationException("Not a valid class " + getCorrespondingClassName());
        }
    }
    // }}
}

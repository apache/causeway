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

package org.apache.isis.core.runtime.system;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.apache.isis.applib.security.UserMemento;

public class JavaObjectWithBasicProgramConventions implements Interface1, Interface2 {
    public static String classActionValid;
    public String objectActionValid;
    private final List<Object> collection = new ArrayList<Object>();

    public static String pluralName() {
        return "Plural";
    }

    public static String singularName() {
        return "Singular";
    }

    public static String getFour() {
        return "";
    }

    public static void setFour(final String value) {
    }

    public String getOne() {
        return "";
    }

    public String defaultOne() {
        return "default value";
    }

    public String[] choicesOne() {
        return new String[] { "four", "five", "six" };
    }

    public void setOne(final String value) {
    }

    public static boolean optionalOne() {
        return true;

    }

    public static String disableOne(final UserMemento user) {
        return "no edits";
    }

    public boolean hideFieldTwo() {
        return true;
    }

    private JavaReferencedObject object;

    public JavaReferencedObject getFieldTwo() {
        return object;
    }

    public void setFieldTwo(final JavaReferencedObject object) {
        this.object = object;
    }

    // field two should be hidden for the user
    public static boolean hideFieldTwo(final UserMemento user) {
        Assert.assertEquals("unit tester", user.getName());
        return true;
    }

    // this field should not be persisted as it has no setter
    public JavaReferencedObject getThree() {
        return null;
    }

    public static boolean alwaysHideSix() {
        return true;
    }

    public String[] choicesSix() {
        return new String[] { "one", "two" };
    }

    public String getSix() {
        return "";
    }

    public void setSix(final String value) {
    }

    public String disableSeven() {
        return "no changes";
    }

    public String getSeven() {
        return "";
    }

    public void setSeven(final String value) {
    }

    public static boolean protectEight() {
        return true;
    }

    public String getEight() {
        return "";
    }

    public void setEight(final String value) {
    }

    public void setValue(final String value) {
    }

    public static String nameStop() {
        return "object action name";
    }

    public static String descriptionStop() {
        return "object action description";
    }

    public String validateStart(final String param) {
        return objectActionValid;
    }

    public void stop() {
    }

    public static boolean[] optionalStart() {
        return new boolean[] { true };
    }

    public String[] defaultStart() {
        return new String[] { "default param" };
    }

    public String[][] choicesStart() {
        return new String[][] { { "one", "two", "three" } };
    }

    public void start2(final String name) {
    }

    public Object[] choicesStart2() {
        return new Object[] { new String[] { "three", "two", "one" } };
    }

    public static String validateTop() {
        return classActionValid;
    }

    public static String[] namesStart() {
        return new String[] { "parameter name" };
    }

    public int start(final String param) {
        return 1;
    }

    public static String nameTop() {
        return "class action name";
    }

    public static String descriptionTop() {
        return "class action description";
    }

    public static void top() {
    }

    public static void bottom(final String param) {
    }

    public static String actionOrder() {
        // make sure there is an action which doesn't exist
        return "missing, start, stop";
    }

    public static String classActionOrder() {
        return "top, bottom";
    }

    public static String fieldOrder() {
        return "one, field two ,three, five";
    }

    // tests the hide method with same set of paramaters
    public static boolean alwaysHideHiddenAction(final String param) {
        return true;
    }

    public void hiddenAction(final String param) {
    }

    // tests the hide method with no paramaters
    public static boolean alwaysHideHiddenAction2() {
        return true;
    }

    public void hiddenAction2(final String param1, final String param2) {
    }

    public static boolean hideHiddenToUser(final UserMemento user) {
        Assert.assertEquals("unit tester", user.getName());
        return true;
    }

    public void hiddenToUser() {
    }

    public List<Object> getFive() {
        return collection;
    }

    public void addToFive(final JavaReferencedObject person) {
    }

    public void removeFromFive(final JavaReferencedObject person) {
    }

    public static String nameFive() {
        return "five";
    }

    public List<?> getNine() {
        return collection;
    }

    // method that would be run on client
    public void localRunOnClient() {
    }

    // method that would be run on the server
    public void remoteRunOnServer() {
    }

    // method for debug access
    public String debugTwo(final String parameter) {
        return "action two";
    }
    
}

interface Interface2 {
}

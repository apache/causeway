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
package org.apache.isis.applib.annotation;

/**
 * Whether the property or parameter is optional or is required (aka mandatory).
 * @since 1.x {@index}
 */
public enum Optionality {

    /**
     * Default, usually meaning mandatory for properties and always meaning mandatory for parameters.
     *
     * <p>
     * For properties, will be false unless JDO <code>javax.jdo.annotations.Column</code> has also specified with
     * <code>javax.jdo.annotations.Column#allowsNull()</code> set to <code>true</code>.
     * </p>
     */
    DEFAULT,

    /**
     * Indicates that the property or parameter is not required.
     */
    OPTIONAL,

    /**
     * Indicates that the property is required (even if the JDO <code>javax.jdo.annotations.Column</code> annotation
     * says otherwise).
     *
     * <p>
     * When using the JDO/DataNucleus objectstore, it is sometimes necessary to annotate a property as optional
     * (using <code>javax.jdo.annotations.Column#allowsNull()</code> set to <code>true</code>), even if the property is
     * logically mandatory.  For example, this can occur when the property is in a subtype class that has been
     * "rolled up" to the superclass table using <code>javax.jdo.annotations.Inheritance</code>> with the
     * <code>javax.jdo.annotations.InheritanceStrategy#SUPERCLASS_TABLE</code> superclass strategy.
     * </p>
     *
     * <p>
     * This annotation, therefore, is intended to override any objectstore-specific
     * annotation, so that Isis can apply the constraint even though the objectstore
     * is unable to do so.
     * </p>
     */
    MANDATORY,

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    NOT_SPECIFIED;

    public boolean isOptional() { return this == OPTIONAL; }

}

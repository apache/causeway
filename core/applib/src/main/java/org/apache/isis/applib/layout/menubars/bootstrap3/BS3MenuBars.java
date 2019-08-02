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
package org.apache.isis.applib.layout.menubars.bootstrap3;

import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information within {@link org.apache.isis.applib.annotation.DomainServiceLayout}.
 */
@XmlRootElement(
        name = "menuBars"
        )
@XmlType(
        name = "menuBars"
        , propOrder = {
                "primary",
                "secondary",
                "tertiary",
                "metadataError"
        }
        )
public class BS3MenuBars extends org.apache.isis.applib.layout.menubars.MenuBarsAbstract {

    private static final long serialVersionUID = 1L;

    public BS3MenuBars() {
    }

    private BS3MenuBar primary = new BS3MenuBar();

    public BS3MenuBar getPrimary() {
        return primary;
    }

    public void setPrimary(final BS3MenuBar primary) {
        this.primary = primary;
    }

    private BS3MenuBar secondary = new BS3MenuBar();

    public BS3MenuBar getSecondary() {
        return secondary;
    }

    public void setSecondary(final BS3MenuBar secondary) {
        this.secondary = secondary;
    }

    private BS3MenuBar tertiary = new BS3MenuBar();

    public BS3MenuBar getTertiary() {
        return tertiary;
    }

    public void setTertiary(final BS3MenuBar tertiary) {
        this.tertiary = tertiary;
    }

    @Override
    public BS3MenuBar menuBarFor(final DomainServiceLayout.MenuBar menuBar) {
        switch (menuBar) {
        case PRIMARY:
            return getPrimary();
        case SECONDARY:
            return getSecondary();
        case TERTIARY:
            return getTertiary();
        case NOT_SPECIFIED:
            break;
        default:
            throw _Exceptions.unmatchedCase(menuBar);
        }
        return null;
    }

    public interface Visitor extends MenuBars.Visitor {
        void preVisit(final BS3MenuBar menuBar);
        void visit(final BS3MenuBar menuBar);
        void postVisit(final BS3MenuBar menuBar);

        void preVisit(final BS3Menu menu);
        void visit(final BS3Menu menu);
        void postVisit(final BS3Menu menu);

        void preVisit(final BS3MenuSection menuSection);
        void visit(final BS3MenuSection section);
        void postVisit(final BS3MenuSection menuSection);
    }

    public static class VisitorAdapter implements BS3MenuBars.Visitor {
        @Override public void preVisit(final BS3MenuBar menuBar) { }
        @Override public void visit(final BS3MenuBar menuBar) { }
        @Override public void postVisit(final BS3MenuBar menuBar) { }

        @Override public void preVisit(final BS3Menu menu) { }
        @Override public void visit(final BS3Menu menu) { }
        @Override public void postVisit(final BS3Menu menu) { }

        @Override public void preVisit(final BS3MenuSection menuSection) { }
        @Override public void visit(final BS3MenuSection section) { }
        @Override public void postVisit(final BS3MenuSection menuSection) { }

        @Override public void visit(final ServiceActionLayoutData serviceActionLayoutData) { }

        // -- PREDEFINED SHORTCUTS

        public static VisitorAdapter visitingMenuSections(Consumer<BS3MenuSection> onVisit) {
            return new VisitorAdapter() {
                @Override public void visit(final BS3MenuSection section) { 
                    onVisit.accept(section);
                }
            };
        }

        public static VisitorAdapter visitingMenus(Consumer<BS3Menu> onVisit) {
            return new VisitorAdapter() {
                @Override public void visit(final BS3Menu menu) { 
                    onVisit.accept(menu);
                }
            };
        }

    }

    @Override
    public void visit(final MenuBars.Visitor visitor) {
        traverseMenuBar(getPrimary(), visitor);
        traverseMenuBar(getSecondary(), visitor);
        traverseMenuBar(getTertiary(), visitor);
    }

    private void traverseMenuBar(final BS3MenuBar menuBar, final MenuBars.Visitor visitor) {

        final Visitor bs3Visitor = visitor instanceof Visitor ? (Visitor) visitor : null;

        if(bs3Visitor != null) {
            bs3Visitor.preVisit(menuBar);
            bs3Visitor.visit(menuBar);
        }

        for (BS3Menu menu : menuBar.getMenus()) {
            traverseMenu(menu, visitor);
        }

        if(bs3Visitor != null) {
            bs3Visitor.postVisit(menuBar);
        }
    }

    private void traverseMenu(final BS3Menu menu, final MenuBars.Visitor visitor) {

        final Visitor bs3Visitor = visitor instanceof Visitor ? (Visitor) visitor : null;

        if(bs3Visitor != null) {
            bs3Visitor.preVisit(menu);
            bs3Visitor.visit(menu);
        }

        final List<BS3MenuSection> sections = menu.getSections();
        for (BS3MenuSection section : sections) {
            traverseSection(section, visitor);
        }

        if(bs3Visitor != null) {
            bs3Visitor.postVisit(menu);
        }
    }

    private void traverseSection(final BS3MenuSection section, final MenuBars.Visitor visitor) {

        final Visitor bs3Visitor = visitor instanceof Visitor ? (Visitor) visitor : null;
        if(bs3Visitor != null) {
            bs3Visitor.preVisit(section);
            bs3Visitor.visit(section);
        }

        final List<ServiceActionLayoutData> actions = section.getServiceActions();
        for (ServiceActionLayoutData action : actions) {
            visitor.visit(action);
        }

        if(bs3Visitor != null) {
            bs3Visitor.postVisit(section);
        }
    }

    private String metadataError;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    public String getMetadataError() {
        return metadataError;
    }

    public void setMetadataError(final String metadataError) {
        this.metadataError = metadataError;
    }

}

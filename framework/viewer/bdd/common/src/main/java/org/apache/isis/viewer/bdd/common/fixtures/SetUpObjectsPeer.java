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
package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;

public class SetUpObjectsPeer extends AbstractFixturePeer {

    public static enum Mode {
        PERSIST, DO_NOT_PERSIST;

        public boolean isPersist() {
            return this == PERSIST;
        }
    }

    public static enum PropertyResult {
        ALIAS, NO_SUCH_PROPERTY, NOT_A_PROPERTY, OK;
        public boolean isOk() {
            return this == OK;
        }
    }

    public static enum SetUpObjectResult {
        OK, NO_ASSOCIATION, CLEARED, CANNOT_CLEAR("(cannot clear)"), CANNOT_SET("(cannot set"), CANNOT_PARSE("(cannot parse)"), UNKNOWN_REFERENCE("(unknown reference)");
        private String errorMessage;

        private SetUpObjectResult() {
            this(null);
        }

        private SetUpObjectResult(final String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public boolean isHandled() {
            return !isError();
        }

        public boolean isError() {
            return errorMessage != null;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public interface AssociationVisitor {
        void visit(OneToOneAssociation association, int colNum);
    }

    private final ObjectSpecification spec;

    private final List<OneToOneAssociation> properties = new ArrayList<OneToOneAssociation>();
    private final CellBinding aliasBinding;
    private final SetUpObjectsPeer.Mode mode;

    private final List<String> cellTextList = new ArrayList<String>();
    private String alias;

    // ///////////////////////////////////////////////////////////////////////
    // constructor
    // ///////////////////////////////////////////////////////////////////////

    public SetUpObjectsPeer(final AliasRegistry aliasRegistry, final String className, final SetUpObjectsPeer.Mode mode, final CellBinding aliasBinding) {
        super(aliasRegistry, aliasBinding);

        this.spec = loadSpecIfValid(className);

        this.mode = mode;
        this.aliasBinding = aliasBinding;
    }

    private ObjectSpecification loadSpecIfValid(final String className) {
        try {
            return getSpecificationLoader().loadSpecification(className);
        } catch (final IsisException ex) {
            return null;
        }
    }

    public boolean isSpecOk() {
        return spec != null;
    }

    public List<OneToOneAssociation> getProperties() {
        return properties;
    }

    public CellBinding getAliasBinding() {
        return aliasBinding;
    }

    // ///////////////////////////////////////////////////////////////////////
    // header
    // ///////////////////////////////////////////////////////////////////////

    public PropertyResult definePropertyOrAlias(final String heading, final int colNum) {

        OneToOneAssociation otoa = null;

        try {
            final int aliasColumn = getAliasBinding().getColumn();
            if (colNum == aliasColumn) {
                return PropertyResult.ALIAS;
            }

            ObjectAssociation association = null;
            try {
                final String memberName = StringUtils.memberIdFor(heading);
                association = spec.getAssociation(memberName);
            } catch (final Exception ex) {
                return PropertyResult.NO_SUCH_PROPERTY;
            }

            if (!association.isOneToOneAssociation()) {
                return PropertyResult.NOT_A_PROPERTY;
            }

            otoa = (OneToOneAssociation) association;

            return PropertyResult.OK;
        } finally {
            // add an association if OK, add null otherwise
            getProperties().add(otoa);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // each row
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Used by Concordion only.
     * 
     * <p>
     * FitNesse, on the other hand, uses a more fine-grained approach, calling
     * the underlying methods.
     */
    public void createObject() throws ScenarioBoundValueException {
        final ObjectAdapter adapter = createInstance();

        for (int colNum = 0; colNum < getProperties().size(); colNum++) {
            final SetUpObjectResult result = setUpProperty(adapter, colNum);

            if (!result.isHandled()) {
                final CellBinding cellBinding = getCellBindings().get(colNum);
                throw ScenarioBoundValueException.current(cellBinding, result.getErrorMessage());
            }
        }

        persistIfNecessary(adapter);
        alias(adapter);
        resetForNextObject();
    }

    public void resetForNextObject() {
        cellTextList.clear();
        this.alias = null;
    }

    public ObjectAdapter createInstance() {
        if (spec == null) {
            return null;
        }
        return getPersistenceSession().createTransientInstance(spec);
    }

    public SetUpObjectResult setUpProperty(final ObjectAdapter adapter, final int colNum) {

        final OneToOneAssociation association = getProperties().get(colNum);
        if (association == null) {
            return SetUpObjectResult.NO_ASSOCIATION;
        }

        final String cellText = cellTextList.get(colNum);

        // handle empty cell as null
        if (cellText == null || cellText.length() == 0) {

            // use clear facet if available
            final PropertyClearFacet clearFacet = association.getFacet(PropertyClearFacet.class);

            if (clearFacet != null) {
                clearFacet.clearProperty(adapter);
                return SetUpObjectResult.CLEARED;
            }

            // use setter facet otherwise
            final PropertySetterFacet setterFacet = association.getFacet(PropertySetterFacet.class);

            if (setterFacet != null) {
                setterFacet.setProperty(adapter, null);
                return SetUpObjectResult.CLEARED;
            }

            return SetUpObjectResult.CANNOT_CLEAR;
        }

        // non-empty, will need a setter
        final PropertySetterFacet setterFacet = association.getFacet(PropertySetterFacet.class);
        if (setterFacet == null) {
            return SetUpObjectResult.CANNOT_SET;
        }

        final ObjectSpecification fieldSpecification = association.getSpecification();
        final ParseableFacet parseableFacet = fieldSpecification.getFacet(ParseableFacet.class);

        ObjectAdapter referencedAdapter = null;
        if (parseableFacet != null) {
            // handle as parseable value
            try {
                referencedAdapter = parseableFacet.parseTextEntry(adapter, cellText, null);
            } catch (final IllegalArgumentException ex) {
                return SetUpObjectResult.CANNOT_PARSE;
            }

        } else {
            // handle as reference to known object
            referencedAdapter = getAliasRegistry().getAliased(cellText);
            if (referencedAdapter == null) {
                return SetUpObjectResult.UNKNOWN_REFERENCE;
            }
        }

        setterFacet.setProperty(adapter, referencedAdapter);
        return SetUpObjectResult.OK;
    }

    public void persistIfNecessary(final ObjectAdapter adapter) {
        if (mode.isPersist()) {
            // xactn mgmt now done by PersistenceSession#makePersistent()
            // getTransactionManager().startTransaction();
            getPersistenceSession().makePersistent(adapter);
            // getTransactionManager().endTransaction();
        }
    }

    public void alias(final ObjectAdapter adapter) {
        final String alias = aliasFor(adapter);
        getAliasRegistry().aliasAs(alias, adapter);
    }

    public String aliasFor(final ObjectAdapter adapter) {
        if (alias != null) {
            return alias;
        } else {
            final String specShortName = StringUtils.lowerLeading(spec.getShortIdentifier());
            return getAliasRegistry().aliasPrefixedAs(specShortName, adapter);
        }
    }

    public void forEachAssociation(final AssociationVisitor visitor) {
        for (int colNum = 0; colNum < getProperties().size(); colNum++) {
            final OneToOneAssociation association = getProperties().get(colNum);
            if (association != null) {
                visitor.visit(association, colNum);
            }
        }
    }

    public boolean addPropertyValueOrAlias(final String propertyValue) {
        cellTextList.add(propertyValue);

        // capture alias if just added
        final int aliasColumn1based = getAliasBinding().getColumn() + 1;
        if (cellTextList.size() == aliasColumn1based) {
            alias = propertyValue;
        }

        return true;
    }

}

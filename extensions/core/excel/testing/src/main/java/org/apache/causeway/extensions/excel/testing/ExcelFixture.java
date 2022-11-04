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
package org.apache.causeway.extensions.excel.testing;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.excel.applib.CausewayModuleExtExcelApplib;
import org.apache.causeway.extensions.excel.applib.ExcelService;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureResultList;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

/**
 *
 * @since 2.0 {@index}
 */
@Named(ExcelFixture.LOGICAL_TYPE_NAME)
@DomainObject
public class ExcelFixture extends FixtureScript implements FixtureScriptWithExecutionStrategy {

    public final static String LOGICAL_TYPE_NAME = CausewayModuleExtExcelApplib.NAMESPACE + ".ExcelFixture";

    private final List<Class<?>> classes;


    @Inject SpecificationLoader specLoader;
    @Inject ExcelService excelService;
    @Inject RepositoryService repositoryService;


    public ExcelFixture(final URL excelResource, final Class<?>... classes) {
        this(excelResource, Arrays.asList(classes));
    }

    public ExcelFixture(final URL excelResource, final List<Class<?>> classes) {
        this(classes);
        setExcelResource(excelResource);
    }

    public ExcelFixture(final Blob upload, final Class<?>... classes) {
        this(upload, Arrays.asList(classes));
    }

    public ExcelFixture(final Blob blob, final List<Class<?>> classes) {
        this(classes);
        setBlob(blob);
    }

    private ExcelFixture(final List<Class<?>> classes) {
        for (Class<?> cls : classes) {

            val beanSort = Optional.ofNullable(specLoader)
            .flatMap(sl->sl.specForType(cls))
            .filter(_NullSafe::isPresent)
            .map(ObjectSpecification::getBeanSort)
            .orElse(BeanSort.UNKNOWN);

            if (!beanSort.isViewModel() && !beanSort.isEntity()) {
                throw new IllegalArgumentException(String.format(
                        "Class '%s' does not implement '%s', nor is it persistable",
                        cls.getSimpleName(), ExcelFixtureRowHandler.class.getSimpleName()));
            }
        }
        this.classes = classes;
    }


    /**
     * Input, optional: defines the name of the resource, used as a suffix to override {@link #getQualifiedName()}
     * (disambiguate items when added to {@link FixtureResultList} if multiple instances of {@link ExcelFixture} are
     * used with different excel spreadsheets).
     */
    @Getter @Setter
    @PropertyLayout(sequence = "1.1")
    private String excelResourceName;

    @Programmatic
    @Override
    public String getQualifiedName() {
        return super.getQualifiedName() + (getExcelResourceName() != null ? "-" + getExcelResourceName() : "");
    }


    /**
     * Input, mandatory ... the Excel spreadsheet to read.
     */
    @Getter @Setter
    @PropertyLayout(sequence = "1.2")
    private URL excelResource;


    /**
     * Input, mandatory ... the Excel spreadsheet to read.
     */
    @Getter @Setter
    private Blob blob;


    /**
     * Output: all the objects created by this fixture.
     */
    @Getter
    private final List objects = _Lists.newArrayList();

    /**
     * Output: the objects created by this fixture, for a specific persistable/row handler class.
     */
    @Getter
    private final Map<Class<?>, List<Object>> objectsByClass = _Maps.newHashMap();




    @Override
    protected void execute(final ExecutionContext ec) {

        if (blob == null){
            byte[] bytes = getBytes();
            blob = new Blob("unused", ExcelService.XSLX_MIME_TYPE, bytes);
        }

        for (Class<?> cls : classes) {
            final List<?> rowObjects = excelService.fromExcel(blob, cls, cls.getSimpleName());
            Object previousRow = null;
            for (final Object rowObj : rowObjects) {
                final List<Object> createdObjects = create(rowObj, ec, previousRow);
                if (createdObjects != null) {
                    addToMap(cls, createdObjects);
                    addToCombined(createdObjects);
                }
                previousRow = rowObj;
            }
        }
    }

    private List<Object> create(
            final Object rowObj,
            final ExecutionContext ec,
            final Object previousRow) {
        if (rowObj instanceof ExcelFixtureRowHandler) {
            final ExcelFixtureRowHandler rowHandler = (ExcelFixtureRowHandler) rowObj;
            return rowHandler.handleRow(ec, this, previousRow);
        } else {
            repositoryService.persist(rowObj);
            ec.addResult(this, rowObj);
            return Collections.singletonList(rowObj);
        }
    }

    private void addToMap(final Class<?> cls, final List<Object> createdObjects) {
        List<Object> objectList = objectsByClass.get(cls);
        if (objectList == null) {
            objectList = _Lists.newArrayList();
            this.objectsByClass.put(cls, objectList);
        }
        objectList.addAll(createdObjects);
    }

    private void addToCombined(final List<Object> createdObjects) {
        this.objects.addAll(createdObjects);
    }



    private byte[] bytes;
    private byte[] getBytes() {
        if (bytes == null) {
            if (blob != null){
                bytes = blob.getBytes();
            } else {
                bytes = readBytes();
            }
        }
        return bytes;
    }

    private byte[] readBytes() {
        try(final InputStream is = getExcelResource().openStream()) {
            return _Bytes.of(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read from resource: " + getExcelResource());
        }
    }

    @Override
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_VALUE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ExcelFixture that = (ExcelFixture) o;

        return Arrays.equals(getBytes(), that.getBytes());

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }



}

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
package org.apache.isis.subdomains.excel.testing;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.applib.dom.util.ExcelServiceImpl;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureResultList;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * This class should be executed using 
 * {@link org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_VALUE} 
 * (it has value semantics).
 *
 * @since 2.0 {@index}
 */
@DomainObject(
        objectType = "isis.sub.excel.ExcelFixture"
)
public class ExcelFixture extends FixtureScript {

    @Inject SpecificationLoader specLoader;

    private final List<Class<?>> classes;

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
            .map(sl->sl.loadSpecification(cls))
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
     * Output: the objects created by this fixture, for a specific persistable/row handler class.
     */
    @Getter
    private final Map<Class<?>, List<Object>> objectsByClass = _Maps.newHashMap();

    /**
     * Output: all the objects created by this fixture.
     */
    @Getter
    private final List objects = _Lists.newArrayList();

    @Programmatic
    @Override
    public String getQualifiedName() {
        return super.getQualifiedName() + (getExcelResourceName() != null ? "-" + getExcelResourceName() : "");
    }

    @Override
    protected void execute(final ExecutionContext ec) {

        final ExcelServiceImpl excelServiceImpl = new ExcelServiceImpl();
        serviceInjector.injectServicesInto(excelServiceImpl);

        if (blob == null){
            byte[] bytes = getBytes();
            blob = new Blob("unused", ExcelService.XSLX_MIME_TYPE, bytes);
        }

        for (Class<?> cls : classes) {
            final List<?> rowObjects = excelServiceImpl.fromExcel(blob, cls, cls.getSimpleName());
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
    //region > bytes
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
        try(val is = getExcelResource().openStream()) {
            return _Bytes.of(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read from resource: " + excelResource);
        }
    }
    //endregion

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

    //region > hashCode, equals

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

    //endregion

    @javax.inject.Inject
    private RepositoryService repositoryService;
//    @javax.inject.Inject
//    private BookmarkService bookmarkService;
    @javax.inject.Inject
    private ServiceInjector serviceInjector;

}

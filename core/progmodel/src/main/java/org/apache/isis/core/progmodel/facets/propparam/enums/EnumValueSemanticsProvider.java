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


package org.apache.isis.core.progmodel.facets.propparam.enums;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.TextEntryParseException;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.progmodel.value.ValueSemanticsProviderAbstract;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class EnumValueSemanticsProvider extends ValueSemanticsProviderAbstract implements EnumFacet {

  private static final boolean IMMUTABLE = true;
  private static final boolean EQUAL_BY_CONTENT = true;
  private static final int TYPICAL_LENGTH = 8;

  private static Class<? extends Facet> type() {
    return EnumFacet.class;
}

  /**
   * Required because {@link Parser} and {@link EncoderDecoder}.
   */
  @SuppressWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
  public EnumValueSemanticsProvider() {
      this(null, null, null, null, null);
  }

  public EnumValueSemanticsProvider(
      FacetHolder holder,
      Class<?> adaptedClass,
      IsisConfiguration configuration, SpecificationLoader specificationLoader, RuntimeContext runtimeContext) {
    this(type(), holder, adaptedClass, TYPICAL_LENGTH, IMMUTABLE, EQUAL_BY_CONTENT, adaptedClass.getEnumConstants()[0], configuration, specificationLoader, runtimeContext);
  }

  private EnumValueSemanticsProvider(
      Class<? extends Facet> adapterFacetType,
      FacetHolder holder,
      Class<?> adaptedClass,
      int typicalLength,
      boolean immutable,
      boolean equalByContent,
      Object defaultValue,
      IsisConfiguration configuration,
      SpecificationLoader specificationLoader,
      RuntimeContext runtimeContext) {
    super(adapterFacetType, holder, adaptedClass, typicalLength, immutable,
        equalByContent, defaultValue, configuration, specificationLoader,
        runtimeContext);
  }


  @Override
  protected Object doParse(Object original, String entry) {
    Object[] enumConstants=getAdaptedClass().getEnumConstants();
    for (Object enumConstant : enumConstants) {
      if (enumConstant.toString().equals(entry))
        return enumConstant;
    }
    throw new TextEntryParseException("Unknown enum constant '" + entry + "'");
  }

  @Override
  protected String doEncode(Object object) {
    return titleString(object);
  }

  @Override
  protected Object doRestore(String data) {
    return doParse(null, data);
  }

  @Override
  protected String titleString(Object object) {
    return object.toString();
  }

  @Override
  public String titleStringWithMask(Object value, String usingMask) {
    return titleString(value);
  }

}

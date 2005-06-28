/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */

package org.apache.log4j.config;

import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.LogLog;


/**
   Used for inferring configuration information for a log4j's component.

   @author  Anders Kristensen
 */
public class PropertyGetter {
  protected static final Object[] NULL_ARG = new Object[] {};
  protected Object obj;
  protected PropertyDescriptor[] props;

  public interface PropertyCallback {
    void foundProperty(Object obj, String prefix, String name, Object value);
  }

  /**
    Create a new PropertyGetter for the specified Object. This is done
    in prepartion for invoking {@link
    #getProperties(PropertyGetter.PropertyCallback, String)} one or
    more times.

    @param obj the object for which to set properties */
  public
  PropertyGetter(Object obj) throws IntrospectionException {
    BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
    props = bi.getPropertyDescriptors();
    this.obj = obj;
  }

  public
  static
  void getProperties(Object obj, PropertyCallback callback, String prefix) {
    try {
      new PropertyGetter(obj).getProperties(callback, prefix);
    } catch (IntrospectionException ex) {
      LogLog.error("Failed to introspect object " + obj, ex);
    }
  }

  public
  void getProperties(PropertyCallback callback, String prefix) {
    for (int i = 0; i < props.length; i++) {
      Method getter = props[i].getReadMethod();
      if (getter == null) continue;
      if (!isHandledType(getter.getReturnType())) {
	//System.err.println("Ignoring " + props[i].getName() +" " + getter.getReturnType());
	continue;
      }
      String name = props[i].getName();
      try {
	Object result = getter.invoke(obj, NULL_ARG);
	//System.err.println("PROP " + name +": " + result);
	if (result != null) {
	  callback.foundProperty(obj, prefix, name, result);
	}
      } catch (Exception ex) {
	LogLog.warn("Failed to get value of property " + name);
      }
    }
  }

  protected
  boolean isHandledType(Class type) {
    return String.class.isAssignableFrom(type) ||
      Integer.TYPE.isAssignableFrom(type) ||
      Long.TYPE.isAssignableFrom(type)    ||
      Boolean.TYPE.isAssignableFrom(type) ||
      Priority.class.isAssignableFrom(type);
  }
}

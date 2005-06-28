/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.jmx;

import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.OptionHandler;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.reflect.Method;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanInfo;
import javax.management.Attribute;

import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

public class LayoutDynamicMBean extends AbstractDynamicMBean {

  private MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
  private Vector dAttributes = new Vector();
  private String dClassName = this.getClass().getName();

  private Hashtable dynamicProps = new Hashtable(5);
  private MBeanOperationInfo[] dOperations = new MBeanOperationInfo[1];
  private String dDescription =
     "This MBean acts as a management facade for log4j layouts.";

  // This category instance is for logging.
  private static Logger cat = Logger.getLogger(LayoutDynamicMBean.class);

  // We wrap this layout instance.
  private Layout layout;

  public  LayoutDynamicMBean(Layout layout) throws IntrospectionException {
    this.layout = layout;
    buildDynamicMBeanInfo();
  }

  private
  void buildDynamicMBeanInfo() throws IntrospectionException {
    Constructor[] constructors = this.getClass().getConstructors();
    dConstructors[0] = new MBeanConstructorInfo(
             "LayoutDynamicMBean(): Constructs a LayoutDynamicMBean instance",
	     constructors[0]);


    BeanInfo bi = Introspector.getBeanInfo(layout.getClass());
    PropertyDescriptor[] pd = bi.getPropertyDescriptors();

    int size = pd.length;

    for(int i = 0; i < size; i++) {
      String name = pd[i].getName();
      Method readMethod =  pd[i].getReadMethod();
      Method writeMethod =  pd[i].getWriteMethod();
      if(readMethod != null) {
	Class returnClass = readMethod.getReturnType();
	if(isSupportedType(returnClass)) {
	  String returnClassName;
	  if(returnClass.isAssignableFrom(Level.class)) {
	    returnClassName = "java.lang.String";
	  } else {
	    returnClassName = returnClass.getName();
	  }

	  dAttributes.add(new MBeanAttributeInfo(name,
						 returnClassName,
						 "Dynamic",
						 true,
						 writeMethod != null,
						 false));
	  dynamicProps.put(name, new MethodUnion(readMethod, writeMethod));
	}
      }
    }

    MBeanParameterInfo[] params = new MBeanParameterInfo[0];

    dOperations[0] = new MBeanOperationInfo("activateOptions",
					    "activateOptions(): add an layout",
					    params,
					    "void",
					    MBeanOperationInfo.ACTION);
  }

  private
  boolean isSupportedType(Class clazz) {
    if(clazz.isPrimitive()) {
      return true;
    }

    if(clazz == String.class) {
      return true;
    }
    if(clazz.isAssignableFrom(Level.class)) {
      return true;
    }

    return false;
  }



  public
  MBeanInfo getMBeanInfo() {
    cat.debug("getMBeanInfo called.");

    MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[dAttributes.size()];
    dAttributes.toArray(attribs);

    return new MBeanInfo(dClassName,
			 dDescription,
			 attribs,
			 dConstructors,
			 dOperations,
			 new MBeanNotificationInfo[0]);
  }

  public
  Object invoke(String operationName, Object params[], String signature[])
    throws MBeanException,
    ReflectionException {

    if(operationName.equals("activateOptions") &&
                     layout instanceof OptionHandler) {
      OptionHandler oh = (OptionHandler) layout;
      oh.activateOptions();
      return "Options activated.";
    }
    return null;
  }

  protected
  Logger  getLogger() {
    return cat;
  }


  public
  Object getAttribute(String attributeName) throws AttributeNotFoundException,
                                                   MBeanException,
                                                   ReflectionException {

       // Check attributeName is not null to avoid NullPointerException later on
    if (attributeName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException(
			"Attribute name cannot be null"),
       "Cannot invoke a getter of " + dClassName + " with null attribute name");
    }


    MethodUnion mu = (MethodUnion) dynamicProps.get(attributeName);

    cat.debug("----name="+attributeName+", mu="+mu);

    if(mu != null && mu.readMethod != null) {
      try {
	return mu.readMethod.invoke(layout, null);
      } catch(Exception e) {
	return null;
      }
    }



    // If attributeName has not been recognized throw an AttributeNotFoundException
    throw(new AttributeNotFoundException("Cannot find " + attributeName +
					 " attribute in " + dClassName));

  }


  public
  void setAttribute(Attribute attribute) throws AttributeNotFoundException,
                                                InvalidAttributeValueException,
                                                MBeanException,
                                                ReflectionException {

    // Check attribute is not null to avoid NullPointerException later on
    if (attribute == null) {
      throw new RuntimeOperationsException(
                  new IllegalArgumentException("Attribute cannot be null"),
		  "Cannot invoke a setter of " + dClassName +
		  " with null attribute");
    }
    String name = attribute.getName();
    Object value = attribute.getValue();

    if (name == null) {
      throw new RuntimeOperationsException(
                    new IllegalArgumentException("Attribute name cannot be null"),
		    "Cannot invoke the setter of "+dClassName+
		    " with null attribute name");
    }



    MethodUnion mu = (MethodUnion) dynamicProps.get(name);

    if(mu != null && mu.writeMethod != null) {
      Object[] o = new Object[1];

      Class[] params = mu.writeMethod.getParameterTypes();
      if(params[0] == org.apache.log4j.Priority.class) {
	value = OptionConverter.toLevel((String) value,
					(Level) getAttribute(name));
      }
      o[0] = value;

      try {
	mu.writeMethod.invoke(layout,  o);

      } catch(Exception e) {
	cat.error("FIXME", e);
      }
    } else {
      throw(new AttributeNotFoundException("Attribute " + name +
					   " not found in " +
					   this.getClass().getName()));
    }
  }
}



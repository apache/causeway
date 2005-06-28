
package org.apache.log4j.jmx;

import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.Appender;
import org.apache.log4j.helpers.OptionConverter;

import java.util.Vector;
import java.util.Enumeration;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.Attribute;

import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.NotificationListener;
import javax.management.Notification;

public class LoggerDynamicMBean extends AbstractDynamicMBean
                                  implements NotificationListener {

  private MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
  private MBeanOperationInfo[] dOperations = new MBeanOperationInfo[1];

  private Vector dAttributes = new Vector();
  private String dClassName = this.getClass().getName();

  private String dDescription =
     "This MBean acts as a management facade for a org.apache.log4j.Logger instance.";

  // This Logger instance is for logging.
  private static Logger cat = Logger.getLogger(LoggerDynamicMBean.class);

  // We wrap this Logger instance.
  private Logger logger;

  public LoggerDynamicMBean(Logger logger) {
    this.logger = logger;
    buildDynamicMBeanInfo();
  }

  public
  void handleNotification(Notification notification, Object handback) {
    cat.debug("Received notification: "+notification.getType());
    registerAppenderMBean((Appender) notification.getUserData() );


  }

  private
  void buildDynamicMBeanInfo() {
    Constructor[] constructors = this.getClass().getConstructors();
    dConstructors[0] = new MBeanConstructorInfo(
             "HierarchyDynamicMBean(): Constructs a HierarchyDynamicMBean instance",
	     constructors[0]);

    dAttributes.add(new MBeanAttributeInfo("name",
					   "java.lang.String",
					   "The name of this Logger.",
					   true,
					   false,
					   false));

    dAttributes.add(new MBeanAttributeInfo("priority",
					   "java.lang.String",
					   "The priority of this logger.",
					   true,
					   true,
					   false));





    MBeanParameterInfo[] params = new MBeanParameterInfo[2];
    params[0] = new MBeanParameterInfo("class name", "java.lang.String",
				       "add an appender to this logger");
    params[1] = new MBeanParameterInfo("appender name", "java.lang.String",
				       "name of the appender");

    dOperations[0] = new MBeanOperationInfo("addAppender",
					    "addAppender(): add an appender",
					    params,
					    "void",
					    MBeanOperationInfo.ACTION);
  }

  protected
  Logger getLogger() {
    return logger;
  }


  public
  MBeanInfo getMBeanInfo() {
    //cat.debug("getMBeanInfo called.");

    MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[dAttributes.size()];
    dAttributes.toArray(attribs);

    MBeanInfo mb = new MBeanInfo(dClassName,
			 dDescription,
			 attribs,
			 dConstructors,
			 dOperations,
			 new MBeanNotificationInfo[0]);
    //cat.debug("getMBeanInfo exit.");
    return mb;
  }

  public
  Object invoke(String operationName, Object params[], String signature[])
    throws MBeanException,
    ReflectionException {

    if(operationName.equals("addAppender")) {
      addAppender((String) params[0], (String) params[1]);
      return "Hello world.";
    }

    return null;
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

    // Check for a recognized attributeName and call the corresponding getter
    if (attributeName.equals("name")) {
      return logger.getName();
    }  else if(attributeName.equals("priority")) {
      Level l = logger.getLevel();
      if(l == null) {
	return null;
      } else {
	return l.toString();
      }
    } else if(attributeName.startsWith("appender=")) {
      try {
	return new ObjectName("log4j:"+attributeName );
      } catch(Exception e) {
	cat.error("Could not create ObjectName" + attributeName);
      }
    }


    // If attributeName has not been recognized throw an AttributeNotFoundException
    throw(new AttributeNotFoundException("Cannot find " + attributeName +
					 " attribute in " + dClassName));

  }


  void addAppender(String appenderClass, String appenderName) {
    cat.debug("addAppender called with "+appenderClass+", "+appenderName);
    Appender appender = (Appender)
       OptionConverter.instantiateByClassName(appenderClass,
					      org.apache.log4j.Appender.class,
					      null);
    appender.setName(appenderName);
    logger.addAppender(appender);

    //appenderMBeanRegistration();

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


    if(name.equals("priority")) {
      if (value instanceof String) {
	String s = (String) value;
	Level p = logger.getLevel();
	if(s.equalsIgnoreCase("NULL")) {
	  p = null;
	} else {
	  p = OptionConverter.toLevel(s, p);
	}
	logger.setPriority(p);
      }
    } else {
      throw(new AttributeNotFoundException("Attribute " + name +
					   " not found in " +
					   this.getClass().getName()));
    }
  }

  void appenderMBeanRegistration() {
    Enumeration enum = logger.getAllAppenders();
    while(enum.hasMoreElements()) {
      Appender appender = (Appender) enum.nextElement();
      registerAppenderMBean(appender);
    }
  }

  void registerAppenderMBean(Appender appender) {
    String name = appender.getName();
    cat.debug("Adding AppenderMBean for appender named "+name);
    ObjectName objectName = null;
    try {
      AppenderDynamicMBean appenderMBean = new AppenderDynamicMBean(appender);
      objectName = new ObjectName("log4j", "appender", name);
      server.registerMBean(appenderMBean, objectName);

      dAttributes.add(new MBeanAttributeInfo("appender="+name,
					     "javax.management.ObjectName",
					     "The "+name+" appender.",
					     true,
					     true,
					     false));

    } catch(Exception e) {
      cat.error("Could not add appenderMBean for ["+name+"].", e);
    }
  }

  public
  void postRegister(java.lang.Boolean registrationDone) {
    appenderMBeanRegistration();
  }
}

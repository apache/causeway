/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.spi;

import org.apache.log4j.*;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.Loader;
import java.lang.reflect.Method;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;

// Contributors:   Nelson Minar <nelson@monkey.org>
//                 Wolf Siberski
//                 Anders Kristensen <akristensen@dynamicsoft.com>

/**
   The internal representation of logging events. When an affirmative
   decision is made to log then a <code>LoggingEvent</code> instance
   is created. This instance is passed around to the different log4j
   components.

   <p>This class is of concern to those wishing to extend log4j.

   @author Ceki G&uuml;lc&uuml;
   @author James P. Cakalic

   @since 0.8.2 */
public class LoggingEvent implements java.io.Serializable {

  private static long startTime = System.currentTimeMillis();

  /** Fully qualified name of the calling category class. */
  transient public final String fqnOfCategoryClass;

  /** 
   * The category of the logging event. This field is not serialized
   * for performance reasons.
   *
   * <p>It is set by the LoggingEvent constructor or set by a remote
   * entity after deserialization.
   * 
   * @deprecated This field will be marked as private or be completely
   * removed in future releases. Please do not use it.
   * */
  transient private Category logger;

  /** 
   * <p>The category (logger) name.
   *   
   * @deprecated This field will be marked as private in future
   * releases. Please do not access it directly. Use the {@link
   * #getLoggerName} method instead.

   * */
  final public String categoryName;

  /** 
   * Level of logging event. Level cannot be serializable because it
   * is a flyweight.  Due to its special seralization it cannot be
   * declared final either.
   *   
   * <p> This field should not be accessed directly. You shoud use the
   * {@link #getLevel} method instead.
   *
   * @deprecated This field will be marked as private in future
   * releases. Please do not access it directly. Use the {@link
   * #getLevel} method instead.
   * */
  transient public Priority level;

  /** The nested diagnostic context (NDC) of logging event. */
  private String ndc;

  /** The mapped diagnostic context (MDC) of logging event. */
  private Hashtable mdcCopy;


  /** Have we tried to do an NDC lookup? If we did, there is no need
   *  to do it again.  Note that its value is always false when
   *  serialized. Thus, a receiving SocketNode will never use it's own
   *  (incorrect) NDC. See also writeObject method. */
  private boolean ndcLookupRequired = true;


  /** Have we tried to do an MDC lookup? If we did, there is no need
   *  to do it again.  Note that its value is always false when
   *  serialized. See also the getMDC and getMDCCopy methods.  */
  private boolean mdcCopyLookupRequired = true;

  /** The application supplied message of logging event. */
  transient private Object message;

  /** The application supplied message rendered through the log4j
      objet rendering mechanism.*/
  private String renderedMessage;

  /** The name of thread in which this logging event was generated. */
  private String threadName;


  /** This
      variable contains information about this event's throwable
  */
  private ThrowableInformation throwableInfo;

  /** The number of milliseconds elapsed from 1/1/1970 until logging event
      was created. */
  public final long timeStamp;
  /** Location information for the caller. */
  private LocationInfo locationInfo;

  // Serialization
  static final long serialVersionUID = -868428216207166145L;

  static final Integer[] PARAM_ARRAY = new Integer[1];
  static final String TO_LEVEL = "toLevel";
  static final Class[] TO_LEVEL_PARAMS = new Class[] {int.class};
  static final Hashtable methodCache = new Hashtable(3); // use a tiny table

  /**
     Instantiate a LoggingEvent from the supplied parameters.

     <p>Except {@link #timeStamp} all the other fields of
     <code>LoggingEvent</code> are filled when actually needed.
     <p>
     @param category The category of this event.
     @param level The level of this event.
     @param message  The message of this event.
     @param throwable The throwable of this event.  */
  public LoggingEvent(String fqnOfCategoryClass, Category logger,
		      Priority priority, Object message, Throwable throwable) {
    this.fqnOfCategoryClass = fqnOfCategoryClass;
    this.logger = logger;
    this.categoryName = logger.getName();
    this.level = priority;
    this.message = message;
    if(throwable != null) {
      this.throwableInfo = new ThrowableInformation(throwable);
    }
    timeStamp = System.currentTimeMillis();
  }

  /**
     Instantiate a LoggingEvent from the supplied parameters.

     <p>Except {@link #timeStamp} all the other fields of
     <code>LoggingEvent</code> are filled when actually needed.
     <p>
     @param category The category of this event.
     @param timeStamp the timestamp of this logging event
     @param level The level of this event.
     @param message  The message of this event.
     @param throwable The throwable of this event.  */
  public LoggingEvent(String fqnOfCategoryClass, Category logger,
		      long timeStamp, Priority priority, Object message,
		      Throwable throwable) {
    this.fqnOfCategoryClass = fqnOfCategoryClass;
    this.logger = logger;
    this.categoryName = logger.getName();
    this.level = priority;
    this.message = message;
    if(throwable != null) {
      this.throwableInfo = new ThrowableInformation(throwable);
    }

    this.timeStamp = timeStamp;
  }

  /**
     Set the location information for this logging event. The collected
     information is cached for future use.
   */
  public LocationInfo getLocationInformation() {
    if(locationInfo == null) {
      locationInfo = new LocationInfo(new Throwable(), fqnOfCategoryClass);
    }
    return locationInfo;
  }

  /**
   * Return the level of this event. Use this form instead of directly
   * accessing the <code>level</code> field.  */
  public Level getLevel() {
    return (Level) level;
  }

  /**
   * Return the name of the logger. Use this form instead of directly
   * accessing the <code>categoryName</code> field.  
   */
  public String getLoggerName() {
    return categoryName;
  }

  /**
     Return the message for this logging event.

     <p>Before serialization, the returned object is the message
     passed by the user to generate the logging event. After
     serialization, the returned value equals the String form of the
     message possibly after object rendering.

     @since 1.1 */
  public
  Object getMessage() {
    if(message != null) {
      return message;
    } else {
      return getRenderedMessage();
    }
  }

  /**
   * This method returns the NDC for this event. It will return the
   * correct content even if the event was generated in a different
   * thread or even on a different machine. The {@link NDC#get} method
   * should <em>never</em> be called directly.  */
  public
  String getNDC() {
    if(ndcLookupRequired) {
      ndcLookupRequired = false;
      ndc = NDC.get();
    }
    return ndc;
  }


// .NET port
//  /**
//    Returns the the context corresponding to the <code>key</code>
//    parameter. If there is a local MDC copy, possibly because we are
//    in a logging server or running inside AsyncAppender, then we
//      search for the key in MDC copy, if a value is found it is
//      returned. Otherwise, if the search in MDC copy returns a null
//      result, then the current thread's <code>MDC</code> is used.
//      
//      <p>Note that <em>both</em> the local MDC copy and the current
//      thread's MDC are searched.
//
//  */
//  public
//  Object getMDC(String key) {
//    Object r;
//    // Note the mdcCopy is used if it exists. Otherwise we use the MDC
//    // that is associated with the thread.
//    if(mdcCopy != null) {
//      r = mdcCopy.get(key);
//      if(r != null) {
//        return r;
//      }
//    }
//    return MDC.get(key);
//  }

  /**
     Obtain a copy of this thread's MDC prior to serialization or
     asynchronous logging.  
  */
  public
  void getMDCCopy() {
	  // .NET port
//    if(mdcCopyLookupRequired) {
//      mdcCopyLookupRequired = false;
//      // the clone call is required for asynchronous logging.
//      // See also bug #5932.
//      Hashtable t = (Hashtable) MDC.getContext();
//      if(t != null) {
//         mdcCopy = (Hashtable) t.clone();
//      }
//    }
  }

  public
  String getRenderedMessage() {
     if(renderedMessage == null && message != null) {
       if(message instanceof String)
	 renderedMessage = (String) message;
       else {
	 LoggerRepository repository = logger.getHierarchy();

	 if(repository instanceof RendererSupport) {
	   RendererSupport rs = (RendererSupport) repository;
	   renderedMessage= rs.getRendererMap().findAndRender(message);
	 } else {
	   renderedMessage = message.toString();
	 }
       }
     }
     return renderedMessage;
  }

  /**
     Returns the time when the application started, in milliseconds
     elapsed since 01.01.1970.  */
  public static long getStartTime() {
    return startTime;
  }

  public
  String getThreadName() {
    if(threadName == null)
      threadName = (Thread.currentThread()).getName();
    return threadName;
  }

  /**
     Returns the throwable information contained within this
     event. May be <code>null</code> if there is no such information.

     <p>Note that the {@link Throwable} object contained within a
     {@link ThrowableInformation} does not survive serialization.

     @since 1.1 */
  public
  ThrowableInformation getThrowableInformation() {
    return throwableInfo;
  }

  /**
     Return this event's throwable's string[] representaion.
  */
  public
  String[] getThrowableStrRep() {

    if(throwableInfo ==  null)
      return null;
    else
      return throwableInfo.getThrowableStrRep();
  }


  private
  void readLevel(ObjectInputStream ois)
                      throws java.io.IOException, ClassNotFoundException {

    int p = ois.readInt();
    try {
      String className = (String) ois.readObject();
      if(className == null) {
	level = Level.toLevel(p);
      } else {
	Method m = (Method) methodCache.get(className);
	if(m == null) {
	  Class clazz = Loader.loadClass(className);
	  // Note that we use Class.getDeclaredMethod instead of
	  // Class.getMethod. This assumes that the Level subclass
	  // implements the toLevel(int) method which is a
	  // requirement. Actually, it does not make sense for Level
	  // subclasses NOT to implement this method. Also note that
	  // only Level can be subclassed and not Priority.
	  m = clazz.getDeclaredMethod(TO_LEVEL, TO_LEVEL_PARAMS);
	  methodCache.put(className, m);
	}
	PARAM_ARRAY[0] = new Integer(p);
	level = (Level) m.invoke(null,  PARAM_ARRAY);
      }
    } catch(Exception e) {
	LogLog.warn("Level deserialization failed, reverting to default.", e);
	level = Level.toLevel(p);
    }
  }

  private void readObject(ObjectInputStream ois)
                        throws java.io.IOException, ClassNotFoundException {
    ois.defaultReadObject();
    readLevel(ois);

    // Make sure that no location info is available to Layouts
    if(locationInfo == null)
      locationInfo = new LocationInfo(null, null);
  }

  private
  void writeObject(ObjectOutputStream oos) throws java.io.IOException {
    // Aside from returning the current thread name the wgetThreadName
    // method sets the threadName variable.
    this.getThreadName();

    // This sets the renders the message in case it wasn't up to now.
    this.getRenderedMessage();

    // This call has a side effect of setting this.ndc and
    // setting ndcLookupRequired to false if not already false.
    this.getNDC();

    // This call has a side effect of setting this.mdcCopy and
    // setting mdcLookupRequired to false if not already false.
    this.getMDCCopy();

    // This sets the throwable sting representation of the event throwable.
    this.getThrowableStrRep();

    oos.defaultWriteObject();

    // serialize this event's level
    writeLevel(oos);
  }

  private
  void writeLevel(ObjectOutputStream oos) throws java.io.IOException {

    oos.writeInt(level.toInt());

    Class clazz = level.getClass();
    if(clazz == Level.class) {
      oos.writeObject(null);
    } else {
      // writing directly the Class object would be nicer, except that
      // serialized a Class object can not be read back by JDK
      // 1.1.x. We have to resort to this hack instead.
      oos.writeObject(clazz.getName());
    }
  }

}

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.or.jms;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.or.ObjectRenderer;

import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.DeliveryMode;

/**
   Render <code>javax.jms.Message</code> objects.

   @author Ceki G&uuml;lc&uuml;
   @since 1.0 */
public class MessageRenderer implements ObjectRenderer {

  public
  MessageRenderer() {
  }


  /**
     Render a {@link javax.jms.Message}.
  */
  public
  String  doRender(Object o) {
    if(o instanceof Message) {
      StringBuffer sbuf = new StringBuffer();
      Message m = (Message) o;
      try {
	sbuf.append("DeliveryMode=");
	switch(m.getJMSDeliveryMode()) {
	case DeliveryMode.NON_PERSISTENT :
	  sbuf.append("NON_PERSISTENT");
	  break;
	case DeliveryMode.PERSISTENT :
	  sbuf.append("PERSISTENT");
	  break;
	default: sbuf.append("UNKNOWN");
	}
	sbuf.append(", CorrelationID=");
	sbuf.append(m.getJMSCorrelationID());

	sbuf.append(", Destination=");
	sbuf.append(m.getJMSDestination());

	sbuf.append(", Expiration=");
	sbuf.append(m.getJMSExpiration());

	sbuf.append(", MessageID=");
	sbuf.append(m.getJMSMessageID());

	sbuf.append(", Priority=");
	sbuf.append(m.getJMSPriority());

	sbuf.append(", Redelivered=");
	sbuf.append(m.getJMSRedelivered());

	sbuf.append(", ReplyTo=");
	sbuf.append(m.getJMSReplyTo());

	sbuf.append(", Timestamp=");
	sbuf.append(m.getJMSTimestamp());

	sbuf.append(", Type=");
	sbuf.append(m.getJMSType());

	//Enumeration enum = m.getPropertyNames();
	//while(enum.hasMoreElements()) {
	//  String key = (String) enum.nextElement();
	//  sbuf.append("; "+key+"=");
	//  sbuf.append(m.getStringProperty(key));
	//}

      } catch(JMSException e) {
	LogLog.error("Could not parse Message.", e);
      }
      return sbuf.toString();
    } else {
      return o.toString();
    }
  }
}

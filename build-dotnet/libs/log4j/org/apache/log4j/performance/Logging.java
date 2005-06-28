//      Copyright 1996-1999, International Business Machines
//      Corporation. All Rights Reserved.

//      Copyright 2000, Ceki Gulcu. All Rights Reserved.

//      See the LICENCE file for the terms of distribution.

package org.apache.log4j.performance;

import org.apache.log4j.Category;
import org.apache.log4j.xml.DOMConfigurator;

import org.apache.log4j.NDC;
import org.apache.log4j.performance.NOPWriter;


/**
   Measure the performance of logging.

   <p>Experimental results are listed below in units of
   <b>micro-seconds</b>. Measurements were done on a AMD Duron clocked
   at 800Mhz running Windows 2000 and Sun's 1.3 JDK.

<p><table border=1>

<tr>
<th>Layout
<th>NullAppender
<th>FileAppender
<th>FileAppender (no flush)
<th>AsyncAppender (no flush)

<tr>
<td>SimpleLayout
<td>4
<td>21
<td>16
<td>33

<tr>
<td>PatternLayout "%p - %m%n"
<td>4
<td>21
<td>16
<td>32

<tr>
<td>PatternLayout "%-5p - %m%n"
<td>4
<td>NA
<td>NA
<td>NA

<tr>
<td>TTCCLayout/RELATIVE
<td>10
<td>37
<td>31
<td>45

<tr>
<td>PatternLayout "%r [%t] %-5p %c{2} %x - %m%n"
<td>11
<td>NA
<td>NA
<td>NA

<tr>
<td>PatternLayout "%r [%t] %-5p %.10c %x - %m%n"
<td>11
<td>NA
<td>NA
<td>NA

<tr>
<td>PatternLayout "%r [%t] %-5p %.20c %x - %m%n"
<td>11
<td>NA
<td>NA
<td>NA

<tr>
<td>PatternLayout "%r [%t] %-5p %c - %m%n"
<td>9
<td>36
<td>29
<td>45

<tr>
<td>TTCCLayout/ISO8601
<td>25
<td>58
<td>51
<td>71

<tr>
<td>PatternLayout "%d{ISO8601} [%t] %-5p %c %x - %m%n"
<td>28
<td>62
<td>55
<td>76

<tr>
<td>PatternLayout "%d{yyyy-MM-dd HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n"
<td>46
<td>82
<td>72
<td>94

<tr>
<td>PatternLayout "%l - %m%n"
<td> 241
<td> 317
<td> 299
<td> 251

<tr>
<td>PatternLayout "%C.%M.%L - %m%n"
<td>267
<td>NA
<td>NA
<td>NA

</table>

   <p>The results of the measurements (should) show that:

   <ol>

   <li><b>The PatternLayout perforance is very close to the performance of
   a dedicated layout of equivalent format.</b>

   <p><li>Format specifiers in conversion patterns have almost no impact
   on performance.

   <p><li>Formating time and date information is costly. Using relative
   time has the least impact on performance. It is recommended that to
   use log4j specific date formatters such as the {@link
   org.apache.log4j.helpers.ISO8601DateFormat} instead of the standard {@link
   java.text.SimpleDateFormat} because of its poor performance. See
   the <b>%d</b> conversion character in {@link
   org.apache.log4j.PatternLayout}.

   <p><li>Avoiding the flush operation at the end of each append
   results in a performance gain of 10 to 20 percent. However, there
   is safety tradeoff invloved in skipping flushing. Indeed, when
   flushing is skipped, then it is likely that the last few log events
   will not be recorded on disk when the application exits. This is a
   high price to pay even for a 20% performance gain.

   <p><li>The <code>AsyncAppender</code> does not automatically
   increase performance. On the contrary, it significantly degrades
   performance. The performance tests done here very quickly fill up
   the bounded buffer of the <code>AsyncAppender</code> and there is
   cosiderable overhead in managing this bounded buffer.

   <p>On the other hand, had we interleaved logging operations with
   long blocking and non CPU-intensive operations, such as I/O,
   network access, sleeping threads, then the
   <code>AsyncAppender</code> would have tremendously reduced the cost
   of logging in terms of overall application runtime.

   <p>In a variant of this test, we have inserted a short sleep
   operation between every 10 log operations. When the total slept
   time is substracted, logging with the <code>AsyncLogger</code>
   takes no time at all. In other words, logging is done for "free".

   <p><li>Extracting location information is comparatively slow. It
   implies at least a ten fold increase in logging time!  It should be
   avoided unless performace is not a concern. It has been reported
   from a trusted source that logging with location information on AIX
   machines will <em>kill</em> your application's performance.

   </ol>

   @author Ceki G&uuml;lc&uuml;

 */
public class Logging {

  static int runLength;
  static int delay = -1;
  /**
     A delay is applied after every <code>burstLen</code> log
     requests.  The default value of this constant is 100.  */
  static  int burstLen = 100;
  static int DELAY_MULT = 1000/burstLen;

  static Category cat = Category.getInstance("A0123456789.B0123456789.C0123456789");

  static
  void  Usage(String msg) {
    System.err.println(msg);
    System.err.println(
      "Usage: java "+Logging.class.getName()+" confFile runLength [delay] [burstLen]\n"+
      "        confFile is an XML configuration file and\n"+
      "        runLength (integer) is the length of test loop.\n"+
      "        delay is the time in millisecs to wait every bustLen log requests.");
    System.exit(1);
  }

  /**
     <b>Usage:</b> <code>java org.apache.log4j.performance.Logging confFile runLength [delay] [burstLen]</code>

     <p><code>confFile</code> is an XML configuration file and
      <code>runLength</code> (integer) is the length of test loop,
      <code>delay</code> is the time in millisecs to sleep every
      <code>bustLen</code> log requests.

      <p>This application just prints the average time it took to log.


   */
  public static void main(String argv[]) {

    if(argv.length == 2)
      init(argv[0], argv[1], null, null);
    else if( argv.length == 4)
      init(argv[0], argv[1], argv[2], argv[3]);
    else
      Usage("Wrong number of arguments.");


    NDC.push("some context");

    double delta;
    String msg = "ABCDEGHIJKLMNOPQRSTUVWXYZabcdeghijklmnopqrstuvwxyz1234567890";
     if(delay <= 0)
      delta = NoDelayLoop(cat, msg);
    else
      delta = DelayedLoop(cat, msg);

    System.out.print((int)delta);

    Category.shutdown();

  }

  /**
    Program wide initialization method.
    */
  static
  void init(String configFile, String runLengthStr, String delayStr,
	    String burstLenStr) {
    try {
      runLength   = Integer.parseInt(runLengthStr);
      if(delayStr != null) {
	delay = Integer.parseInt(delayStr);
      }
      if(delayStr != null) {
	burstLen = Integer.parseInt(burstLenStr);
	DELAY_MULT = 1000/burstLen;
      }
    }
    catch(java.lang.NumberFormatException e) {
      e.printStackTrace();
    }
    DOMConfigurator.configure(configFile);
  }

  static
  double NoDelayLoop(Category category, String msg) {
    long before = System.currentTimeMillis();
    for(int i = 0; i < runLength; i++) {
      category.info(msg);
    }
    return ((System.currentTimeMillis() - before)*1000.0)/runLength;
  }

  static
  double DelayedLoop(Category category, String msg) {
    long before = System.currentTimeMillis();
    int j = 0;
    Thread currentThread = Thread.currentThread();
    for(int i = 0; i < runLength; i++) {
      category.info(msg);
      if(j++ == burstLen) {
	j = 0;
	try{currentThread.sleep(delay);}catch(Exception e){}
      }

    }
    double actualTime = ((System.currentTimeMillis()-before)*1000.0/runLength);
    System.out.println("actual time: "+actualTime);
    return (actualTime - delay*DELAY_MULT);
  }
}

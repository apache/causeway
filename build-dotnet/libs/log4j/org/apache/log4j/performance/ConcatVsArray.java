//      Copyright 1996-1999, International Business Machines 
//      Corporation. All Rights Reserved.

package org.apache.log4j.performance;

import java.util.Date;

/**
   Measure difference in performance of string concatenation versus
   creating an anonymous string array.

   <p>You should be able to see that anonymous string array
   construction is significatnly faster than string concatenation. The
   difference increases proportionally with the length of the strings
   to be concatanated.

   @author Ceki G&uuml;lc&uuml;
 */
public class ConcatVsArray {


  static
  void  Usage() {
    System.err.println("Usage: java org.apache.log4j.performance.ConcatVsArray " +
		       "string1 string2 runLength\n" +
		       "       where runLength is an integer.");
    System.exit(1);
  }

  public static void main(String args[]) {

    if(args.length != 3) {
      Usage();
    }    

    String s1 = args[0];
    String s2 = args[1];
    int runLength = 0;
    try {
      runLength = Integer.parseInt(args[2]);      
    }
    catch(java.lang.NumberFormatException e) {
      System.err.println(e);
      Usage();
    }      

    double micros;

    String[] sa;
    long before = new Date().getTime();
    for(int i = 0; i < runLength; i++) {
      sa = new String[]{s1, s2};
    }
    micros = (new Date().getTime() - before)*1000.0/runLength;
    System.out.println("The anonymous array loop took around " + micros + " microseconds.");

    String s;    
    before = new Date().getTime();
    for(int i = 0; i < runLength; i++) {
      s = s1 + s2;
    }
    micros = (new Date().getTime() - before)*1000.0/runLength;
    System.out.println("The append loop took around " + micros + " microseconds.");

  }
}

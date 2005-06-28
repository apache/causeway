/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */



package org.apache.log4j;

import java.io.IOException;
import java.io.Writer;
import java.io.File;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.spi.LoggingEvent;

/**
   RollingFileAppender extends FileAppender to backup the log files when
   they reach a certain size.

   @author Heinz Richter
   @author Ceki G&uuml;lc&uuml;

*/
public class RollingFileAppender extends FileAppender {

  /**
     The default maximum file size is 10MB.
  */
  protected long maxFileSize = 10*1024*1024;

  /**
     There is one backup file by default.
   */
  protected int  maxBackupIndex  = 1;

  /**
     The default constructor simply calls its {@link
     FileAppender#FileAppender parents constructor}.  */
  public
  RollingFileAppender() {
    super();
  }

  /**
    Instantiate a RollingFileAppender and open the file designated by
    <code>filename</code>. The opened filename will become the ouput
    destination for this appender.

    <p>If the <code>append</code> parameter is true, the file will be
    appended to. Otherwise, the file desginated by
    <code>filename</code> will be truncated before being opened.
  */
  public
  RollingFileAppender(Layout layout, String filename, boolean append)
                                      throws IOException {
    super(layout, filename, append);
  }

  /**
     Instantiate a FileAppender and open the file designated by
    <code>filename</code>. The opened filename will become the output
    destination for this appender.

    <p>The file will be appended to.  */
  public
  RollingFileAppender(Layout layout, String filename) throws IOException {
    super(layout, filename);
  }

  /**
     Returns the value of the <b>MaxBackupIndex</b> option.
   */
  public
  int getMaxBackupIndex() {
    return maxBackupIndex;
  }

 /**
    Get the maximum size that the output file is allowed to reach
    before being rolled over to backup files.

    @since 1.1
 */
  public
  long getMaximumFileSize() {
    return maxFileSize;
  }

  /**
     Implements the usual roll over behaviour.

     <p>If <code>MaxBackupIndex</code> is positive, then files
     {<code>File.1</code>, ..., <code>File.MaxBackupIndex -1</code>}
     are renamed to {<code>File.2</code>, ...,
     <code>File.MaxBackupIndex</code>}. Moreover, <code>File</code> is
     renamed <code>File.1</code> and closed. A new <code>File</code> is
     created to receive further log output.

     <p>If <code>MaxBackupIndex</code> is equal to zero, then the
     <code>File</code> is truncated with no backup files created.

   */
  public // synchronization not necessary since doAppend is alreasy synched
  void rollOver() {
    File target;
    File file;

    LogLog.debug("rolling over count=" + ((CountingQuietWriter) qw).getCount());
    LogLog.debug("maxBackupIndex="+maxBackupIndex);

    // If maxBackups <= 0, then there is no file renaming to be done.
    if(maxBackupIndex > 0) {
      // Delete the oldest file, to keep Windows happy.
      file = new File(fileName + '.' + maxBackupIndex);
      if (file.exists())
       file.delete();

      // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
      for (int i = maxBackupIndex - 1; i >= 1; i--) {
	file = new File(fileName + "." + i);
	if (file.exists()) {
	  target = new File(fileName + '.' + (i + 1));
	  LogLog.debug("Renaming file " + file + " to " + target);
	  file.renameTo(target);
	}
      }

      // Rename fileName to fileName.1
      target = new File(fileName + "." + 1);

      this.closeFile(); // keep windows happy.

      file = new File(fileName);
      LogLog.debug("Renaming file " + file + " to " + target);
      file.renameTo(target);
    }

    try {
      // This will also close the file. This is OK since multiple
      // close operations are safe.
      this.setFile(fileName, false, bufferedIO, bufferSize);
    }
    catch(IOException e) {
      LogLog.error("setFile("+fileName+", false) call failed.", e);
    }
  }

  public
  synchronized
  void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
                                                                 throws IOException {
    super.setFile(fileName, append, this.bufferedIO, this.bufferSize);
    if(append) {
      File f = new File(fileName);
      ((CountingQuietWriter) qw).setCount(f.length());
    }
  }


  /**
     Set the maximum number of backup files to keep around.

     <p>The <b>MaxBackupIndex</b> option determines how many backup
     files are kept before the oldest is erased. This option takes
     a positive integer value. If set to zero, then there will be no
     backup files and the log file will be truncated when it reaches
     <code>MaxFileSize</code>.
   */
  public
  void setMaxBackupIndex(int maxBackups) {
    this.maxBackupIndex = maxBackups;
  }

  /**
     Set the maximum size that the output file is allowed to reach
     before being rolled over to backup files.

     <p>This method is equivalent to {@link #setMaxFileSize} except
     that it is required for differentiating the setter taking a
     <code>long</code> argument from the setter taking a
     <code>String</code> argument by the JavaBeans {@link
     java.beans.Introspector Introspector}.

     @see #setMaxFileSize(String)
 */
  public
  void setMaximumFileSize(long maxFileSize) {
    this.maxFileSize = maxFileSize;
  }


  /**
     Set the maximum size that the output file is allowed to reach
     before being rolled over to backup files.

     <p>In configuration files, the <b>MaxFileSize</b> option takes an
     long integer in the range 0 - 2^63. You can specify the value
     with the suffixes "KB", "MB" or "GB" so that the integer is
     interpreted being expressed respectively in kilobytes, megabytes
     or gigabytes. For example, the value "10KB" will be interpreted
     as 10240.
   */
  public
  void setMaxFileSize(String value) {
    maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
  }

  protected
  void setQWForFiles(Writer writer) {
     this.qw = new CountingQuietWriter(writer, errorHandler);
  }

  /**
     This method differentiates RollingFileAppender from its super
     class.

     @since 0.9.0
  */
  protected
  void subAppend(LoggingEvent event) {
    super.subAppend(event);
    if((fileName != null) &&
                     ((CountingQuietWriter) qw).getCount() >= maxFileSize)
      this.rollOver();
   }
}

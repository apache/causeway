/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j;

import java.io.IOException;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.BufferedWriter;

import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.helpers.LogLog;

// Contibutors: Jens Uwe Pipka <jens.pipka@gmx.de>
//              Ben Sandee

/**
 *  FileAppender appends log events to a file.
 *
 *  <p>Support for <code>java.io.Writer</code> and console appending
 *  has been deprecated and then removed. See the replacement
 *  solutions: {@link WriterAppender} and {@link ConsoleAppender}.
 *
 * @author Ceki G&uuml;lc&uuml; 
 * */
public class FileAppender extends WriterAppender {

  /** Append to or truncate the file? The default value for this
      variable is <code>true</code>, meaning that by default a
      <code>FileAppender</code> will append to an existing file and
      not truncate it.

      <p>This option is meaningful only if the FileAppender opens the
      file.
  */
  protected boolean fileAppend = true;

  /**
     The name of the log file. */
  protected String fileName = null;

  /**
     Do we do bufferedIO? */
  protected boolean bufferedIO = false;

  /**
     How big should the IO buffer be? Default is 8K. */
  protected int bufferSize = 8*1024;


  /**
     The default constructor does not do anything.
  */
  public
  FileAppender() {
  }

  /**
    Instantiate a <code>FileAppender</code> and open the file
    designated by <code>filename</code>. The opened filename will
    become the output destination for this appender.

    <p>If the <code>append</code> parameter is true, the file will be
    appended to. Otherwise, the file designated by
    <code>filename</code> will be truncated before being opened.

    <p>If the <code>bufferedIO</code> parameter is <code>true</code>,
    then buffered IO will be used to write to the output file.

  */
  public
  FileAppender(Layout layout, String filename, boolean append, boolean bufferedIO,
	       int bufferSize) throws IOException {
    this.layout = layout;
    this.setFile(filename, append, bufferedIO, bufferSize);
  }

  /**
    Instantiate a FileAppender and open the file designated by
    <code>filename</code>. The opened filename will become the output
    destination for this appender.

    <p>If the <code>append</code> parameter is true, the file will be
    appended to. Otherwise, the file designated by
    <code>filename</code> will be truncated before being opened.
  */
  public
  FileAppender(Layout layout, String filename, boolean append)
                                                             throws IOException {
    this.layout = layout;
    this.setFile(filename, append, false, bufferSize);
  }

  /**
     Instantiate a FileAppender and open the file designated by
    <code>filename</code>. The opened filename will become the output
    destination for this appender.

    <p>The file will be appended to.  */
  public
  FileAppender(Layout layout, String filename) throws IOException {
    this(layout, filename, true);
  }

  /**
     The <b>File</b> property takes a string value which should be the
     name of the file to append to.

     <p><font color="#DD0044"><b>Note that the special values
     "System.out" or "System.err" are no longer honored.</b></font>

     <p>Note: Actual opening of the file is made when {@link
     #activateOptions} is called, not when the options are set.  */
  public void setFile(String file) {
    // Trim spaces from both ends. The users probably does not want
    // trailing spaces in file names.
    String val = file.trim();
    fileName = val;
  }

  /**
      Returns the value of the <b>Append</b> option.
   */
  public
  boolean getAppend() {
    return fileAppend;
  }


  /** Returns the value of the <b>File</b> option. */
  public
  String getFile() {
    return fileName;
  }

  /**
     If the value of <b>File</b> is not <code>null</code>, then {@link
     #setFile} is called with the values of <b>File</b>  and
     <b>Append</b> properties.

     @since 0.8.1 */
  public
  void activateOptions() {
    if(fileName != null) {
      try {
	setFile(fileName, fileAppend, bufferedIO, bufferSize);
      }
      catch(java.io.IOException e) {
	errorHandler.error("setFile("+fileName+","+fileAppend+") call failed.",
			   e, ErrorCode.FILE_OPEN_FAILURE);
      }
    } else {
      //LogLog.error("File option not set for appender ["+name+"].");
      LogLog.warn("File option not set for appender ["+name+"].");
      LogLog.warn("Are you using FileAppender instead of ConsoleAppender?");
    }
  }

 /**
     Closes the previously opened file.
  */
  protected
  void closeFile() {
    if(this.qw != null) {
      try {
	this.qw.close();
      }
      catch(java.io.IOException e) {
	// Exceptionally, it does not make sense to delegate to an
	// ErrorHandler. Since a closed appender is basically dead.
	LogLog.error("Could not close " + qw, e);
      }
    }
  }

  /**
     Get the value of the <b>BufferedIO</b> option.

     <p>BufferedIO will significatnly increase performance on heavily
     loaded systems.

  */
  public
  boolean getBufferedIO() {
    return this.bufferedIO;
  }


  /**
     Get the size of the IO buffer.
  */
  public
  int getBufferSize() {
    return this.bufferSize;
  }



  /**
     The <b>Append</b> option takes a boolean value. It is set to
     <code>true</code> by default. If true, then <code>File</code>
     will be opened in append mode by {@link #setFile setFile} (see
     above). Otherwise, {@link #setFile setFile} will open
     <code>File</code> in truncate mode.

     <p>Note: Actual opening of the file is made when {@link
     #activateOptions} is called, not when the options are set.
   */
  public
  void setAppend(boolean flag) {
    fileAppend = flag;
  }

  /**
     The <b>BufferedIO</b> option takes a boolean value. It is set to
     <code>false</code> by default. If true, then <code>File</code>
     will be opened and the resulting {@link java.io.Writer} wrapped
     around a {@link BufferedWriter}.

     BufferedIO will significatnly increase performance on heavily
     loaded systems.

  */
  public
  void setBufferedIO(boolean bufferedIO) {
    this.bufferedIO = bufferedIO;
    if(bufferedIO) {
      immediateFlush = false;
    }
  }


  /**
     Set the size of the IO buffer.
  */
  public
  void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
    <p>Sets and <i>opens</i> the file where the log output will
    go. The specified file must be writable.

    <p>If there was already an opened file, then the previous file
    is closed first.

    <p><b>Do not use this method directly. To configure a FileAppender
    or one of its subclasses, set its properties one by one and then
    call activateOptions.</b>

    @param fileName The path to the log file.
    @param append   If true will append to fileName. Otherwise will
        truncate fileName.  */
  public
  synchronized
  void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
                                                            throws IOException {
    LogLog.debug("setFile called: "+fileName+", "+append);

    // It does not make sense to have immediate flush and bufferedIO.
    if(bufferedIO) {
      setImmediateFlush(false);
    }

    reset();
    Writer fw = createWriter(new FileOutputStream(fileName, append));
    if(bufferedIO) {
      fw = new BufferedWriter(fw, bufferSize);
    }
    this.setQWForFiles(fw);
    this.fileName = fileName;
    this.fileAppend = append;
    this.bufferedIO = bufferedIO;
    this.bufferSize = bufferSize;
    writeHeader();
    LogLog.debug("setFile ended");
  }


  /**
     Sets the quiet writer being used.

     This method is overriden by {@link RollingFileAppender}.
   */
  protected
  void setQWForFiles(Writer writer) {
     this.qw = new QuietWriter(writer, errorHandler);
  }


  /**
     Close any previously opened file and call the parent's
     <code>reset</code>.  */
  protected
  void reset() {
    closeFile();
    this.fileName = null;
    super.reset();
  }
}


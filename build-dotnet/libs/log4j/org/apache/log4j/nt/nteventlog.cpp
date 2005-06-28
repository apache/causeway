/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

#ifndef NtEventLogAppender_h
#define NtEventLogAppender_h
#include "org_apache_log4j_Priority.h"
#include "org_apache_log4j_nt_NTEventLogAppender.h"
#include <windows.h>

// Borrowed unabashedly from the JNI Programmer's Guide
void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg) {

  jclass classForName = env->FindClass(name);
  // If cls is null, an exception has already been thrown.
  if (classForName != (jclass)0) {
    env->ThrowNew(classForName, msg);
  }
  // cleanup
  env->DeleteLocalRef(classForName);
  return;
}

// Borrowed unabashedly from the JNI Programmer's Guide
char *JNU_GetStringNativeChars(JNIEnv *env, jstring jstr) {
  static jmethodID midStringGetBytes = 0;
  jbyteArray bytes = 0;
  jthrowable exc;
  char *nstr = 0;
  
  //if (env->EnsureLocalCapacity(2) < 0) {
  // out of memory
  //return 0;
  //}

  if (midStringGetBytes == 0) {
    // Lookup and cache the String.getBytes() method id.
    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == 0) {
      // An exception will have been thrown.
      return 0;
    }
    midStringGetBytes = env->GetMethodID(stringClass, "getBytes", "()[B");
    env->DeleteLocalRef(stringClass);
    if (midStringGetBytes == 0) {
      // An exception will have been thrown.
      return 0;
    }
  }

  bytes = (jbyteArray)env->CallObjectMethod(jstr, midStringGetBytes);
  exc = env->ExceptionOccurred();
  if (exc == 0) {
    // Attempt to malloc enough room for the length of the Java
    // string plus one byte for the 0-terminator.
    jint len = env->GetArrayLength(bytes);
    nstr = (char *)malloc(len + 1);
    if (nstr == 0) {
      // malloc failed -- throw an OutOfMemoryError
      JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
      env->DeleteLocalRef(bytes);
      return 0;
    }
    // copy to the malloc'd array and 0-terminate
    env->GetByteArrayRegion(bytes, 0, len, (jbyte *)nstr);
    nstr[len] = 0;
  } else {
    // cleanup
    env->DeleteLocalRef(exc);
  }
  // cleanup
  env->DeleteLocalRef(bytes);
  return nstr;
}

/*
 * Convert log4j Priority to an EventLog category. Each category is
 * backed by a message resource so that proper category names will
 * be displayed in the NT Event Viewer.
 */
WORD getCategory(jint priority) {
  // Priority values map directly to EventLog category values
  return (WORD)(priority + 1);
}

/*
 * Convert log4j Priority to an EventLog type. The log4j package
 * supports 8 defined priorites, but the NT EventLog only knows
 * 3 event types of interest to us: ERROR, WARNING, and INFO.
 */
WORD getType(jint priority) {
  WORD ret_val;
  
  switch (priority) {
  case org_apache_log4j_Priority_FATAL_INT:
  case org_apache_log4j_Priority_ERROR_INT:
    ret_val = EVENTLOG_ERROR_TYPE;
    break;
  case org_apache_log4j_Priority_WARN_INT:
    ret_val = EVENTLOG_WARNING_TYPE;
    break;
  case org_apache_log4j_Priority_INFO_INT:
  case org_apache_log4j_Priority_DEBUG_INT:
  default:
    ret_val = EVENTLOG_INFORMATION_TYPE;
    break;
  }
  return ret_val;
}

HKEY regGetKey(TCHAR *subkey, DWORD *disposition) {
  HKEY hkey = 0;
  RegCreateKeyEx(HKEY_LOCAL_MACHINE, subkey, 0, NULL, 
		 REG_OPTION_NON_VOLATILE, KEY_SET_VALUE, NULL, 
		 &hkey, disposition);
  return hkey;
}

void regSetString(HKEY hkey, TCHAR *name, TCHAR *value) {
  RegSetValueEx(hkey, name, 0, REG_SZ, (LPBYTE)value, lstrlen(value));
}

void regSetDword(HKEY hkey, TCHAR *name, DWORD value) {
  RegSetValueEx(hkey, name, 0, REG_DWORD, (LPBYTE)&value, sizeof(DWORD));
}

/*
 * Add this source with appropriate configuration keys to the registry.
 */
void addRegistryInfo(char *source) {
  const TCHAR *prefix = "SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\";
  DWORD disposition;
  HKEY hkey = 0;
  TCHAR subkey[256];
  
  lstrcpy(subkey, prefix);
  lstrcat(subkey, source);
  hkey = regGetKey(subkey, &disposition);
  if (disposition == REG_CREATED_NEW_KEY) {
    regSetString(hkey, "EventMessageFile", "NTEventLogAppender.dll");
    regSetString(hkey, "CategoryMessageFile", "NTEventLogAppender.dll");
    regSetDword(hkey, "TypesSupported", (DWORD)7);
    regSetDword(hkey, "CategoryCount", (DWORD)8);
  }
	//RegSetValueEx(hkey, "EventMessageFile", 0, REG_SZ, (LPBYTE)dllname, lstrlen(dllname));
	//RegSetValueEx(hkey, "CategoryMessageFile", 0, REG_SZ, (LPBYTE)dllname, lstrlen(dllname));
	//RegSetValueEx(hkey, "TypesSupported", 0, REG_DWORD, (LPBYTE)&whichTypes, sizeof(DWORD));
	//RegSetValueEx(hkey, "CategoryCount", 0, REG_DWORD, (LPBYTE)&numCategories, sizeof(DWORD));
  RegCloseKey(hkey);
  return;
}

/*
 * Class:     org.apache.log4j.nt.NTEventLogAppender
 * Method:    registerEventSource
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_log4j_nt_NTEventLogAppender_registerEventSource(
   JNIEnv *env, jobject java_this, jstring server, jstring source) {
  
  char *nserver = 0;
  char *nsource = 0;

  if (server != 0) {
    nserver = JNU_GetStringNativeChars(env, server);
  }
  if (source != 0) {
    nsource = JNU_GetStringNativeChars(env, source);
  }
  addRegistryInfo(nsource);
  jint handle = (jint)RegisterEventSource(nserver, nsource);
  free((void *)nserver);
  free((void *)nsource);
  return handle;
}

/*
 * Class:     org_apache_log4j_nt_NTEventLogAppender
 * Method:    reportEvent
 * Signature: (ILjava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_org_apache_log4j_nt_NTEventLogAppender_reportEvent(
   JNIEnv *env, jobject java_this, jint handle, jstring jstr, jint priority) {
  
  jboolean localHandle = JNI_FALSE;
  if (handle == 0) {
    // Client didn't give us a handle so make a local one.
    handle = (jint)RegisterEventSource(NULL, "Log4j");
    localHandle = JNI_TRUE;
  }
  
  // convert Java String to character array
  const int numStrings = 1;
  LPCTSTR array[numStrings];
  char *nstr = JNU_GetStringNativeChars(env, jstr);
  array[0] = nstr;
  
  // This is the only message supported by the package. It is backed by
  // a message resource which consists of just '%1' which is replaced
  // by the string we just created.
  const DWORD messageID = 0x1000;
  ReportEvent((HANDLE)handle, getType(priority), 
	      getCategory(priority), 
	      messageID, NULL, 1, 0, array, NULL);
  
  free((void *)nstr);
  if (localHandle == JNI_TRUE) {
    // Created the handle here so free it here too.
    DeregisterEventSource((HANDLE)handle);
  }
  return;
}

/*
 * Class:     org_apache_log4j_nt_NTEventLogAppender
 * Method:    deregisterEventSource
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_apache_log4j_nt_NTEventLogAppender_deregisterEventSource(
JNIEnv *env, 
jobject java_this, 
jint handle)
{
  DeregisterEventSource((HANDLE)handle);
}


#endif

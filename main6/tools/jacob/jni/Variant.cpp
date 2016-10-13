/*
 * Copyright (c) 1999-2004 Sourceforge JACOB Project.
 * All rights reserved. Originator: Dan Adler (http://danadler.com).
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution. 
 * 3. Redistributions in any form must be accompanied by information on
 *    how to obtain complete source code for the JACOB software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#include "stdafx.h"
#include "Variant.h"
// Win32 support for Ole Automation
#include <wchar.h>
#include <string.h>
#include <atlbase.h>
#include <objbase.h>
#include <oleauto.h>
#include <olectl.h>
#include "util.h"

extern "C" 
{

#define VARIANT_FLD "m_pVariant"

// extract a VARIANT from a Variant object
VARIANT *extractVariant(JNIEnv *env, jobject arg)
{
  jclass argClass = env->GetObjectClass(arg);
  jfieldID ajf = env->GetFieldID( argClass, VARIANT_FLD, "I");
  jint anum = env->GetIntField(arg, ajf);
  VARIANT *v = (VARIANT *)anum;
  return v;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_release
  (JNIEnv *env, jobject _this)
{
  jclass clazz = env->GetObjectClass(_this);
  jfieldID jf = env->GetFieldID(clazz, VARIANT_FLD, "I");
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    // fix byref leak 
    if (V_VT(v) & VT_BYREF)        // is this a reference
    {
      void  *pMem = V_BSTRREF(v);    // get allocated memory
      if (pMem)
      {
        if (V_VT(v) == (VT_BYREF|VT_BSTR))
        {
          BSTR  *pBstr = (BSTR*)pMem;
          if (*pBstr)
            SysFreeString(*pBstr);// release bstr
        }
        CoTaskMemFree(pMem);
      }
    }
    VariantClear(v);
    delete v;
    env->SetIntField(_this, jf, (unsigned int)0);
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_init
  (JNIEnv *env, jobject _this)
{
  jclass clazz = env->GetObjectClass(_this);
  jfieldID jf = env->GetFieldID( clazz, VARIANT_FLD, "I");
  VARIANT *v = new VARIANT();
  VariantInit(v);
  env->SetIntField(_this, jf, (unsigned int)v);
}


/*
 * Class:     com_jacob_com_Variant
 * Method:    zeroVariant
 * Signature: ()V
 *
 * This should only be used on variant objects created by teh
 * com layer as part of a call through EventProxy.
 * This zeros out the variant pointer in the Variant object
 * so that the calling COM program can free the memory.
 * instead of both the COM program and the Java GC doing it.
 */
void zeroVariant(JNIEnv *env, jobject _this)
{
  jclass clazz = env->GetObjectClass(_this);
  jfieldID jf = env->GetFieldID(clazz, VARIANT_FLD, "I");
  env->SetIntField(_this, jf, (unsigned int)0);
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_Save
  (JNIEnv *env, jobject _this, jobject outStream)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) 
  {
    DWORD flags = MSHCTX_LOCAL;
    jint size = VARIANT_UserSize(&flags, 0L, v);
    // allocate a byte array of the right length
    jbyte* pBuf = new jbyte[size];
    // clear it out
    ZeroMemory(pBuf, size);
    // marshall the Variant into the buffer
    VARIANT_UserMarshal(&flags, (unsigned char *)pBuf, v);
    // need to convert the buffer to a java byte ba[]
    jbyteArray ba = env->NewByteArray(size);
    env->SetByteArrayRegion(ba, 0, size, pBuf);
    // and delete the original memory
    delete [] pBuf;

    //java code: DataOutputStream dos = new DataOutputStream(outStream);
    jclass dosCls = env->FindClass("java/io/DataOutputStream");
    jmethodID dosCons =
      env->GetMethodID(dosCls, "<init>", "(Ljava/io/OutputStream;)V");
    jmethodID dosWriteInt =
      env->GetMethodID(dosCls, "writeInt", "(I)V");
    jmethodID dosWriteBytes =
      env->GetMethodID(dosCls, "write", "([B)V");
    jobject dos = env->NewObject(dosCls, dosCons, outStream);
    // write the size into the stream
    env->CallVoidMethod(dos, dosWriteInt, size);
    // write the buffer into the stream
    env->CallVoidMethod(dos, dosWriteBytes, ba);
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_Load
  (JNIEnv *env, jobject _this, jobject inStream)
{

  VARIANT *v = extractVariant(env, _this);
  if (!v) 
  {
    // since the default constructor is not called when serializing in
    // I need to init the underlying VARIANT
    jclass clazz = env->GetObjectClass(_this);
    jfieldID jf = env->GetFieldID( clazz, VARIANT_FLD, "I");
    v = new VARIANT();
    VariantInit(v);
    env->SetIntField(_this, jf, (unsigned int)v);
  }
  if (v) 
  {
    //java code: DataInputStream dis = new DataInputStream(outStream);
    jclass disCls = env->FindClass("java/io/DataInputStream");
    jmethodID disCons =
      env->GetMethodID(disCls, "<init>", "(Ljava/io/InputStream;)V");
    jmethodID disReadInt =
      env->GetMethodID(disCls, "readInt", "()I");
    jmethodID disReadBytes =
      env->GetMethodID(disCls, "readFully", "([B)V");
    jobject dis = env->NewObject(disCls, disCons, inStream);

    // read in the size from the input stream
    jint size = env->CallIntMethod(dis, disReadInt);
    // allocate a byte array of this size
    jbyteArray ba = env->NewByteArray(size);
    // read it in from the input stream
    env->CallVoidMethod(dis, disReadBytes, ba);
	if ( size > 0 )
	{
		// get a buffer from it
		jbyte *pBuf = env->GetByteArrayElements(ba, 0);
		// unmarshall the Variant from the buffer
		DWORD flags = MSHCTX_LOCAL;
		VARIANT_UserUnmarshal(&flags, (unsigned char *)pBuf, v);
		// release the byte array
		env->ReleaseByteArrayElements(ba, pBuf, 0);
	}
  }
}

JNIEXPORT jint JNICALL Java_com_jacob_com_Variant_toInt
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_I4))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jint)V_I4(v);
  }
  return NULL;
}

JNIEXPORT jdouble JNICALL Java_com_jacob_com_Variant_toDate
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_DATE))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jdouble)V_DATE(v);
  }
  return NULL;
}

JNIEXPORT jboolean JNICALL Java_com_jacob_com_Variant_toBoolean
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_BOOL))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jboolean)V_BOOL(v);
  }
  return NULL;
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_toEnumVariant
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) 
  {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_UNKNOWN))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    jclass autoClass = env->FindClass("com/jacob/com/EnumVariant");
    jmethodID autoCons =
    env->GetMethodID(autoClass, "<init>", "(I)V");
    // construct an Unknown object to return
    IUnknown *unk = V_UNKNOWN(v);
    IEnumVARIANT *ie;
    hr = unk->QueryInterface(IID_IEnumVARIANT, (void **)&ie);
    if (FAILED(hr)) {
      ThrowComFail(env, "[toEnumVariant]: Object does not implement IEnumVariant", hr);
      return NULL;
    }
    // I am copying the pointer to java
    if (ie) ie->AddRef();
    jobject newAuto = env->NewObject(autoClass, autoCons, ie);
    return newAuto;
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_getNull
  (JNIEnv *env, jobject _this)
{
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putNull
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_NULL;
  }
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_cloneIndirect
  (JNIEnv *env, jobject _this)
{
  return NULL;
}

JNIEXPORT jdouble JNICALL Java_com_jacob_com_Variant_toDouble
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_R8))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jdouble)V_R8(v);
  }
  return NULL;
}

JNIEXPORT jlong JNICALL Java_com_jacob_com_Variant_toCurrency
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_CY))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    CY cy = V_CY(v);
    return (jlong)cy.int64;
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putShortRef
  (JNIEnv *env, jobject _this, jshort s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    short *ps = (short *)CoTaskMemAlloc(sizeof(short));
    *ps = s;
    V_VT(v) = VT_I2|VT_BYREF;
    V_I2REF(v) = ps;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putIntRef
  (JNIEnv *env, jobject _this, jint s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    long *ps = (long *)CoTaskMemAlloc(sizeof(long));
    *ps = s;
    V_VT(v) = VT_I4|VT_BYREF;
    V_I4REF(v) = ps;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putDoubleRef
  (JNIEnv *env, jobject _this, jdouble s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    double *ps = (double *)CoTaskMemAlloc(sizeof(double));
    *ps = s;
    V_VT(v) = VT_R8|VT_BYREF;
    V_R8REF(v) = ps;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putDateRef
  (JNIEnv *env, jobject _this, jdouble s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    double *ps = (double *)CoTaskMemAlloc(sizeof(double));
    *ps = s;
    V_VT(v) = VT_DATE|VT_BYREF;
    V_DATEREF(v) = ps;
  }
}

// SF 1065533  added unicode support
JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putStringRef
  (JNIEnv *env, jobject _this, jstring s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before

	const jchar *cStr = env->GetStringChars(s,NULL);
    CComBSTR bs(cStr);

    BSTR *pbs = (BSTR *)CoTaskMemAlloc(sizeof(BSTR));
    bs.CopyTo(pbs);
    V_VT(v) = VT_BSTR|VT_BYREF;
    V_BSTRREF(v) = pbs;

    env->ReleaseStringChars(s,cStr);  }
}

JNIEXPORT jshort JNICALL Java_com_jacob_com_Variant_getShortRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_I2|VT_BYREF)) {
      return NULL;
    }
    return (jshort)*V_I2REF(v);
  }
  return NULL;
}

JNIEXPORT jint JNICALL Java_com_jacob_com_Variant_getIntRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_I4|VT_BYREF)) {
      return NULL;
    }
    return (jint)*V_I4REF(v);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putShort
  (JNIEnv *env, jobject _this, jshort s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_I2;
    V_I2(v) = (short)s;
  }
}

JNIEXPORT jshort JNICALL Java_com_jacob_com_Variant_getShort
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_I2) {
      return NULL;
    }
    return (jshort)V_I2(v);
  }
  return NULL;
}

JNIEXPORT jdouble JNICALL Java_com_jacob_com_Variant_getDoubleRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_R8|VT_BYREF)) {
      return NULL;
    }
    return (jdouble)*V_R8REF(v);
  }
  return NULL;
}

JNIEXPORT jdouble JNICALL Java_com_jacob_com_Variant_getDateRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_DATE|VT_BYREF)) {
      return NULL;
    }
    return (jdouble)*V_DATEREF(v);
  }
  return NULL;
}

JNIEXPORT jstring JNICALL Java_com_jacob_com_Variant_getStringRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_BSTR|VT_BYREF)) {
      return NULL;
    }
    BSTR *bs = V_BSTRREF(v);
    jstring js = env->NewString(*bs, SysStringLen(*bs));
    return js;
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_VariantClear
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v);
  }
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_toDispatch
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_DISPATCH))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    jclass autoClass = env->FindClass("com/jacob/com/Dispatch");
    jmethodID autoCons =
    env->GetMethodID(autoClass, "<init>", "(I)V");
    // construct a Dispatch object to return
    IDispatch *disp = V_DISPATCH(v);
    // I am copying the pointer to java
    if (disp) disp->AddRef();
    jobject newAuto = env->NewObject(autoClass, autoCons, disp);
    return newAuto;
  }
  return NULL;
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_clone
  (JNIEnv *env, jobject _this)
{
  return NULL;
}

JNIEXPORT jstring JNICALL Java_com_jacob_com_Variant_toString
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    switch (V_VT(v))
    {
      case VT_EMPTY:
      case VT_NULL:
      case VT_ERROR:
         // causes VariantChangeType to bomb
         return env->NewStringUTF("null");
    }
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_BSTR))) {
      // cannot change type to a string
      return env->NewStringUTF("???");
    }
    BSTR bs = V_BSTR(v);
    jstring js = env->NewString(bs, SysStringLen(bs));
    return js;
  }
  return NULL;
}

JNIEXPORT jint JNICALL Java_com_jacob_com_Variant_getInt
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_I4) {
      return NULL;
    }
    return (jint)V_I4(v);
  }
  return NULL;
}

JNIEXPORT jdouble JNICALL Java_com_jacob_com_Variant_getDate
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_DATE) {
      return NULL;
    }
    return (jdouble)V_DATE(v);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putInt
  (JNIEnv *env, jobject _this, jint i)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_I4;
    V_I4(v) = (int)i;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putDate
  (JNIEnv *env, jobject _this, jdouble date)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_DATE;
    V_DATE(v) = date;
  }
}

JNIEXPORT jbyte JNICALL Java_com_jacob_com_Variant_toByte
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_UI1))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jbyte)V_UI1(v);
  }
  return NULL;
}

JNIEXPORT jboolean JNICALL Java_com_jacob_com_Variant_getBoolean
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_BOOL)) {
      return NULL;
    }
    return (jboolean)V_BOOL(v);
  }
  return NULL;
}

JNIEXPORT jbyte JNICALL Java_com_jacob_com_Variant_getByte
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_UI1)) {
      return NULL;
    }
    return (jbyte)V_UI1(v);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putBoolean
  (JNIEnv *env, jobject _this, jboolean b)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_BOOL;
    V_BOOL(v) = b == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
  }
  else ThrowComFail(env, "putBoolean failed", -1);
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putByte
  (JNIEnv *env, jobject _this, jbyte b)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_UI1;
    V_UI1(v) = b;
  }
  else ThrowComFail(env, "putByte failed", -1);
}

JNIEXPORT jint JNICALL Java_com_jacob_com_Variant_toError
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_ERROR))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jint)V_ERROR(v);
  }
  return NULL;
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_toObject
  (JNIEnv *env, jobject _this)
{
  // not supported
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_getEmpty
  (JNIEnv *env, jobject _this)
{
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putEmpty
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_EMPTY;
  }
}

JNIEXPORT jint JNICALL Java_com_jacob_com_Variant_getError
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_ERROR) {
      return NULL;
    }
    return (jint)V_ERROR(v);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putError
  (JNIEnv *env, jobject _this, jint i)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_ERROR;
    V_ERROR(v) = (int)i;
  }
}


JNIEXPORT jdouble JNICALL Java_com_jacob_com_Variant_getDouble
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_R8) {
      return NULL;
    }
    return (jdouble)V_R8(v);
  }
  return NULL;
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_getObject
  (JNIEnv *env, jobject _this)
{
  // not supported
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putCurrency
  (JNIEnv *env, jobject _this, jlong cur)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    CY pf;
    pf.int64 = (LONGLONG)cur;
    V_VT(v) = VT_CY;
    V_CY(v) = pf;
  } else ThrowComFail(env, "putCurrency failed", -1);
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putObject
  (JNIEnv *env, jobject _this, jobject _that)
{
  VARIANT *v = extractVariant(env, _this);
  IDispatch *disp = extractDispatch(env, _that);
  if (disp && v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_DISPATCH;
    V_DISPATCH(v) = disp;
    // I am handing the pointer to COM
    disp->AddRef();
  } else ThrowComFail(env, "putObject failed", -1);
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putDouble
  (JNIEnv *env, jobject _this, jdouble d)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_R8;
    V_R8(v) = (double)d;
  }
}

JNIEXPORT jlong JNICALL Java_com_jacob_com_Variant_getCurrency
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_CY) {
      return NULL;
    }
    CY cy;
    cy = V_CY(v);
    jlong jl;
    memcpy(&jl, &cy, 64);
    return jl;
  }
  return NULL;
}


JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putFloatRef
  (JNIEnv *env, jobject _this, jfloat val)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    float *pf = (float *)CoTaskMemAlloc(sizeof(float));
    *pf = val;
    V_VT(v) = VT_R4|VT_BYREF;
    V_R4REF(v) = pf;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putCurrencyRef
  (JNIEnv *env, jobject _this, jlong cur)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    CY *pf = (CY *)CoTaskMemAlloc(sizeof(CY));
    memcpy(pf, &cur, 64);
    V_VT(v) = VT_BYREF|VT_CY;
    V_CYREF(v) = pf;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putErrorRef
  (JNIEnv *env, jobject _this, jint i)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_ERROR|VT_BYREF;
    V_ERROR(v) = (int)i;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putBooleanRef
  (JNIEnv *env, jobject _this, jboolean b)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    VARIANT_BOOL *br = (VARIANT_BOOL *)CoTaskMemAlloc(sizeof(VARIANT_BOOL));
    *br = b ? VARIANT_TRUE : VARIANT_FALSE;
    V_VT(v) = VT_BOOL|VT_BYREF;
    V_BOOLREF(v) = br;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putByteRef
  (JNIEnv *env, jobject _this, jbyte b)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    unsigned char *br = (unsigned char *)CoTaskMemAlloc(sizeof(char));
    *br = b;
    V_VT(v) = VT_UI1|VT_BYREF;
    V_UI1REF(v) = br;
  }
}

JNIEXPORT jstring JNICALL Java_com_jacob_com_Variant_getString
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  VT_BSTR) {
      return NULL;
    }
    BSTR bs = V_BSTR(v);
    jstring js = env->NewString(bs, SysStringLen(bs));
    return js;
  }
  return NULL;
}

// SF 1065533  added unicode support
JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putString
  (JNIEnv *env, jobject _this, jstring s)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_BSTR;

    const jchar *cStr = env->GetStringChars(s,NULL);
    CComBSTR bs(cStr);

    V_VT(v) = VT_BSTR;
    V_BSTR(v) = bs.Copy();
    
    env->ReleaseStringChars(s,cStr);
  }
}

JNIEXPORT jfloat JNICALL Java_com_jacob_com_Variant_getFloatRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_R4|VT_BYREF)) {
      return NULL;
    }
    return (jfloat)*V_R4REF(v);
  }
  return NULL;
}

JNIEXPORT jlong JNICALL Java_com_jacob_com_Variant_getCurrencyRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_CY|VT_BYREF)) {
      return NULL;
    }
    CY *cy;
    cy = V_CYREF(v);
    jlong jl;
    memcpy(&jl, cy, 64);
    return jl;
  }
  return NULL;
}

JNIEXPORT jint JNICALL Java_com_jacob_com_Variant_getErrorRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_ERROR|VT_BYREF)) {
      return NULL;
    }
    return (jint)V_ERROR(v);
  }
  return NULL;
}

JNIEXPORT jboolean JNICALL Java_com_jacob_com_Variant_getBooleanRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_BOOL|VT_BYREF)) {
      return NULL;
    }
    return (jboolean)*V_BOOLREF(v);
  }
  return NULL;
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_getObjectRef
  (JNIEnv *env, jobject _this)
{
  return NULL;
}


JNIEXPORT jbyte JNICALL Java_com_jacob_com_Variant_getByteRef
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_UI1|VT_BYREF)) {
      return NULL;
    }
    return (jbyte)*V_UI1REF(v);
  }
  return NULL;
}

JNIEXPORT jfloat JNICALL Java_com_jacob_com_Variant_toFloat
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_R4))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jfloat)V_R4(v);
  }
  return NULL;
}

JNIEXPORT jobject JNICALL Java_com_jacob_com_Variant_toSafeArray
  (JNIEnv *env, jobject _this, jboolean deepCopy)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if ((V_VT(v) & VT_ARRAY) == 0) 
    {
      ThrowComFail(env, "Variant not array", -1);
      return NULL;
    }
    // prepare a new sa obj
    jclass saClass = env->FindClass("com/jacob/com/SafeArray");
    jmethodID saCons = env->GetMethodID(saClass, "<init>", "()V");
    // construct an SA to return
    jobject newSA = env->NewObject(saClass, saCons);
    // pass in the deep copy indicator
    setSA(env, newSA, V_ARRAY(v), deepCopy == JNI_TRUE ? 1 : 0);
    return newSA;
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putSafeArrayRef
  (JNIEnv *env, jobject _this, jobject sa)
{
  SAFEARRAY *psa = extractSA(env, sa);
  if (psa) 
  {
    VARIANT *v = extractVariant(env, _this);
    if (v) {
      VARTYPE vt;
      SAFEARRAY **sa = (SAFEARRAY **)CoTaskMemAlloc(sizeof(SAFEARRAY*));
      *sa = psa;
      SafeArrayGetVartype(psa, &vt);
      V_VT(v) = VT_ARRAY | vt | VT_BYREF;
      V_ARRAYREF(v) = sa;
      return;
    }
    ThrowComFail(env, "Can't get variant pointer", -1);
    return;
  }
  ThrowComFail(env, "Can't get sa pointer", -1);
  return;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putSafeArray
  (JNIEnv *env, jobject _this, jobject sa)
{
  SAFEARRAY *psa = extractSA(env, sa);
  if (psa) 
  {
    VARIANT *v = extractVariant(env, _this);
    if (v) {
      VARTYPE vt;
      SafeArrayGetVartype(psa, &vt);
      V_VT(v) = VT_ARRAY | vt;
      V_ARRAY(v) = copySA(psa);
      return;
    }
    ThrowComFail(env, "Can't get variant pointer", -1);
    return;
  }
  ThrowComFail(env, "Can't get sa pointer", -1);
  return;
}


JNIEXPORT void JNICALL Java_com_jacob_com_Variant_noParam
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    V_VT(v) = VT_ERROR;
    V_ERROR(v) = DISP_E_PARAMNOTFOUND;
  }
}

JNIEXPORT jfloat JNICALL Java_com_jacob_com_Variant_getFloat
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    if (V_VT(v) !=  (VT_R4)) {
      return NULL;
    }
    return (jfloat)V_R4(v);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putFloat
  (JNIEnv *env, jobject _this, jfloat val)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_R4;
    V_R4(v) = val;
  }
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_changeType
  (JNIEnv *env, jobject _this, jshort t)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantChangeType(v, v, 0, t);
  }
}

JNIEXPORT jshort JNICALL Java_com_jacob_com_Variant_getvt
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    return (jshort)V_VT(v);
  }
  return NULL;
}

JNIEXPORT jshort JNICALL Java_com_jacob_com_Variant_toShort
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    HRESULT hr;
    if (FAILED(hr = VariantChangeType(v, v, 0, VT_I2))) {
      ThrowComFail(env, "VariantChangeType failed", hr);
      return NULL;
    }
    return (jshort)V_I2(v);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_com_jacob_com_Variant_putSafeArrayRefHelper
  (JNIEnv *env, jobject _this, jint pSA)
{
  VARIANT *v = extractVariant(env, _this);
  if (v) {
    VariantClear(v); // whatever was there before
  }
}

JNIEXPORT jboolean JNICALL Java_com_jacob_com_Variant_isNull
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractVariant(env, _this);
  if (!v) return JNI_TRUE;
  if ((V_VT(v) & VT_ARRAY)) 
  {
    // is it a null safearray
    if ((V_VT(v) & VT_BYREF)) 
       if (!V_ARRAYREF(v)) return JNI_TRUE;
    else
       if (!V_ARRAY(v)) return JNI_TRUE;
  }
  switch (V_VT(v))
  {
    case VT_EMPTY:
    case VT_NULL:
    case VT_ERROR:
            return JNI_TRUE;
    // is it a null dispatch (Nothing in VB)
    case VT_DISPATCH:
            if (!V_DISPATCH(v)) return JNI_TRUE;
  }
  return JNI_FALSE;
}

}

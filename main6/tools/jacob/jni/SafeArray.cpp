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
#include "SafeArray.h"
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

#define V_FLD   "m_pV"

static SAFEARRAY *makeArray(int vt, int nDims, long *lb, long *cel)
{
  if (nDims == 1) {
    // no need to alloc
    SAFEARRAYBOUND sab;
    sab.lLbound = lb[0];
    sab.cElements = cel[0];
    SAFEARRAY *sa = SafeArrayCreate(vt,1,&sab);
    return sa;
  } else {
    SAFEARRAYBOUND* rgsaBounds = new SAFEARRAYBOUND[nDims];
    for(int i=0;i<nDims;i++) 
    {
      rgsaBounds[i].lLbound = lb[i];
      rgsaBounds[i].cElements = cel[i];
    }
    SAFEARRAY *sa = SafeArrayCreate(vt,nDims,rgsaBounds);
    delete[] rgsaBounds;
    return sa;
  }
}

VARIANT *extractWrapper(JNIEnv *env, jobject arg)
{
  jclass argClass = env->GetObjectClass(arg);
  jfieldID vf = env->GetFieldID( argClass, V_FLD, "I");
  jint vnum = env->GetIntField(arg, vf);
  if (vnum != NULL)
  {
      // if vnum is not NULL, then there is a Variant wrapper present
      VARIANT *v = (VARIANT *)vnum;
      return v;
  }
  return NULL;
}

// extract a SAFEARRAY from a SafeArray object
SAFEARRAY *extractSA(JNIEnv *env, jobject arg)
{
  VARIANT *v = extractWrapper(env, arg);
  if (v != NULL)
  {
      SAFEARRAY *sa = V_ARRAY(v);
      return sa;
  }
  return NULL;
}

// deep copy a SAFEARRAY
SAFEARRAY *copySA(SAFEARRAY *psa)
{
    // easiest way to make a deep copy is to use VariantCopy
    VARTYPE vt;
    SafeArrayGetVartype(psa, &vt);
    VARIANT v1, v2;

    VariantInit(&v1);
	VariantInit(&v2);
    V_VT(&v1) = VT_ARRAY | vt;
    V_ARRAY(&v1) = psa;
    VariantCopy(&v2, &v1);
    SAFEARRAY *sa = V_ARRAY(&v2);
    VariantInit(&v2); // make sure it's not owned by this variant
    VariantClear(&v2);
    VariantInit(&v1);
    VariantClear(&v1);
    
    return sa;
}

// create a VARIANT wrapper for the safearray
void setSA(JNIEnv *env, jobject arg, SAFEARRAY *sa, int copy)
{
    // construct a variant to hold the result
    // the variant then owns the array
    jclass argClass = env->GetObjectClass(arg);
    jfieldID ajf = env->GetFieldID( argClass, V_FLD, "I");
    jint vnum = env->GetIntField(arg, ajf);
    VARIANT *v = (VARIANT *)vnum;
    if (v == NULL)
    {
      v = new VARIANT();
      VariantInit(v);
    }
    VARTYPE vt;
    SafeArrayGetVartype(sa, &vt);
    V_VT(v) = VT_ARRAY | vt;
    V_ARRAY(v) = copy ? copySA(sa) : sa;
    env->SetIntField(arg, ajf, (unsigned int)v);
}

/*
 * Class:     SafeArray
 * Method:    init
 * Signature: (I[I[I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_init
  (JNIEnv *env, jobject _this, jint vt, jintArray lb, jintArray cel)
{
  jint *lbounds = env->GetIntArrayElements(lb, NULL);
  jint *celems = env->GetIntArrayElements(cel, NULL);
  int nDims = env->GetArrayLength(lb);
  // array lengths must match
  if (nDims != env->GetArrayLength(cel)) return;
  // build the sa according to params
  if ( nDims > 0 )
  {
	  SAFEARRAY *sa = makeArray(vt, nDims, lbounds, celems);
	  env->ReleaseIntArrayElements(lb, lbounds, 0);
	  env->ReleaseIntArrayElements(cel, celems, 0);
	  jclass clazz = env->GetObjectClass(_this);
	  setSA(env, _this, sa, 0);
  }
}

/*
 * Class:     SafeArray
 * Method:    clone
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_jacob_com_SafeArray_clone
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) 
  {
    // prepare a new return value
    jclass saClass = env->GetObjectClass(_this);
    jmethodID saCons = env->GetMethodID(saClass, "<init>", "()V");
    // construct an SA to return
    jobject newSA = env->NewObject(saClass, saCons);
    // wrap in a Variant
    setSA(env, newSA, psa, 1);
    return newSA;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_destroy
  (JNIEnv *env, jobject _this)
{
  VARIANT *v = extractWrapper(env, _this);
  if (v) {
    // this is the case where a Variant wrapper exists, in that
    // case free the variant, but if there is just a raw SA, then
    // the owning variant will free it
    jclass saClass = env->GetObjectClass(_this);
    jfieldID jf = env->GetFieldID(saClass, V_FLD, "I");
    VariantClear(v);
    delete v;
    env->SetIntField(_this, jf, (unsigned int)0);
  }
}

/*
 * Class:     SafeArray
 * Method:    getvt
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getvt
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
    VARTYPE vt;
    SafeArrayGetVartype(psa, &vt);
    return (jint)vt;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    reinit
 * Signature: (LSafeArray;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_reinit
  (JNIEnv *env, jobject _this, jobject sa)
{
  // what to do here?
}

/*
 * Class:     SafeArray
 * Method:    reinterpretType
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_reinterpretType
  (JNIEnv *env, jobject _this, jint vt)
{
}

/*
 * Class:     SafeArray
 * Method:    getLBound
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getLBound__
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
     jint lb;
     SafeArrayGetLBound(psa, 1, &lb);
     return lb;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getLBound
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getLBound__I
  (JNIEnv *env, jobject _this, jint dim)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
     jint lb;
     SafeArrayGetLBound(psa, dim, &lb);
     return lb;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getUBound
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getUBound__
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
     jint lb;
     SafeArrayGetUBound(psa, 1, &lb);
     return lb;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getUBound
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getUBound__I
  (JNIEnv *env, jobject _this, jint dim)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
     jint lb;
     SafeArrayGetUBound(psa, dim, &lb);
     return lb;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getNumDim
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getNumDim
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
     return SafeArrayGetDim(psa);
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getFeatures
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getFeatures
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
   SafeArrayLock(psa);
   jint features = psa->fFeatures;
   SafeArrayUnlock(psa);
   return features;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getElemSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getElemSize
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (psa) {
   jint siz = SafeArrayGetElemsize(psa);
   return siz;
  }
  return NULL;
}

static int numElements(SAFEARRAY *psa)
{
  int nDims = SafeArrayGetDim(psa);
  int elems = 0;
  for(int i=1;i<=nDims;i++) {
    long lb, ub;
    SafeArrayGetLBound(psa, i, &lb);
    SafeArrayGetUBound(psa, i, &ub);
    elems += ub - lb + 1;
  }
  return elems;
}

/*
 * Class:     SafeArray
 * Method:    fromCharArray
 * Signature: ([C)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromCharArray
  (JNIEnv *env, jobject _this, jcharArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the double array - don't make a copy
  jchar *iarr = env->GetCharArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_UI2;
    for(int i=0;i<len;i++) {
      V_UI2(&v) = iarr[i];
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_UI2 || vt == VT_I2) {
    void *pData;
    SafeArrayAccessData(psa, &pData);
    memcpy(pData, iarr, len*sizeof(jchar));
    SafeArrayUnaccessData(psa);
  } else {
    ThrowComFail(env, "safearray cannot be assigned from char", 0);
  }
  env->ReleaseCharArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromIntArray
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromIntArray
  (JNIEnv *env, jobject _this, jintArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the int array - don't make a copy
  jint *iarr = env->GetIntArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_I4;
    for(int i=0;i<len;i++) {
      V_I4(&v) = iarr[i];
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_I4) {
    void *pData;
    SafeArrayAccessData(psa, &pData);
    memcpy(pData, iarr, len*sizeof(int));
    SafeArrayUnaccessData(psa);
  } else {
    ThrowComFail(env, "safearray cannot be assigned from int", -1);
  }
  env->ReleaseIntArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromShortArray
 * Signature: ([S)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromShortArray
  (JNIEnv *env, jobject _this, jshortArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the short array - don't make a copy
  jshort *iarr = env->GetShortArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_I2;
    for(int i=0;i<len;i++) {
      V_I2(&v) = iarr[i];
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_I2) {
    void *pData;
    SafeArrayAccessData(psa, &pData);
    memcpy(pData, iarr, len*sizeof(short));
    SafeArrayUnaccessData(psa);
  } else {
    ThrowComFail(env, "safearray cannot be assigned from short", -1);
  }
  env->ReleaseShortArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromDoubleArray
 * Signature: ([D)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromDoubleArray
  (JNIEnv *env, jobject _this, jdoubleArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the double array - don't make a copy
  jdouble *iarr = env->GetDoubleArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_R8;
    for(int i=0;i<len;i++) {
      V_R8(&v) = iarr[i];
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_R8 || vt == VT_DATE) {
    void *pData;
    SafeArrayAccessData(psa, &pData);
    memcpy(pData, iarr, len*sizeof(double));
    SafeArrayUnaccessData(psa);
  } else {
    ThrowComFail(env, "safearray cannot be assigned from double", -1);
  }
  env->ReleaseDoubleArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromStringArray
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromStringArray
  (JNIEnv *env, jobject _this, jobjectArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_BSTR;
    for(int i=0;i<len;i++) {
      jstring s = (jstring)env->GetObjectArrayElement(a, i);
      const char *str = env->GetStringUTFChars(s, NULL);
      CComBSTR bs(str);
      V_VT(&v) = VT_BSTR;
      V_BSTR(&v) = bs.Copy();
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
      env->ReleaseStringUTFChars(s, str);
      VariantClear(&v);
    }
  } else if (vt == VT_BSTR) {
    for(int i=0;i<len;i++) {
      jstring s = (jstring)env->GetObjectArrayElement(a, i);
      const char *str = env->GetStringUTFChars(s, NULL);
      CComBSTR bs(str);
      BSTR bstr = bs.Detach();
      long x = i;
      SafeArrayPutElement(psa,&x,bstr);
      env->ReleaseStringUTFChars(s, str);
    }
  } else {
    ThrowComFail(env, "safearray cannot be assigned from string\n", 0);
  }
}

/*
 * Class:     SafeArray
 * Method:    fromByteArray
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromByteArray
  (JNIEnv *env, jobject _this, jbyteArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the byte array - don't make a copy
  jbyte *iarr = env->GetByteArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_UI1;
    for(int i=0;i<len;i++) {
      V_UI1(&v) = iarr[i];
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_UI1 || vt == VT_I1) {
    jbyte *pData;
    SafeArrayAccessData(psa, (void **)&pData);
    memcpy(pData, iarr, len*sizeof(jbyte));
    SafeArrayUnaccessData(psa);
  } else {
    ThrowComFail(env, "safearray cannot be assigned from byte", -1);
  }
  env->ReleaseByteArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromFloatArray
 * Signature: ([F)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromFloatArray
  (JNIEnv *env, jobject _this, jfloatArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the float array - don't make a copy
  jfloat *iarr = env->GetFloatArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_R4;
    for(int i=0;i<len;i++) {
      V_R4(&v) = iarr[i];
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_R4) {
    void *pData;
    SafeArrayAccessData(psa, &pData);
    memcpy(pData, iarr, len*sizeof(float));
    SafeArrayUnaccessData(psa);
  } else {
    ThrowComFail(env, "safearray cannot be assigned from float", -1);
  }
  env->ReleaseFloatArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromBooleanArray
 * Signature: ([Z)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromBooleanArray
  (JNIEnv *env, jobject _this, jbooleanArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  // get the boolean array - don't make a copy
  jboolean *iarr = env->GetBooleanArrayElements(a, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_BOOL;
    for(int i=0;i<len;i++) {
      V_BOOL(&v) = iarr[i] ? VARIANT_TRUE : VARIANT_FALSE;
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_BOOL) {
    // jboolean is 1 byte and VARIANT_BOOL is 2
    VARIANT_BOOL v;
    for(int i=0;i<len;i++) {
      v = iarr[i] ? VARIANT_TRUE : VARIANT_FALSE;
      long x = i;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else {
    ThrowComFail(env, "safearray cannot be assigned from boolean", -1);
  }
  env->ReleaseBooleanArrayElements(a, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    fromVariantArray
 * Signature: ([LVariant;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_fromVariantArray
  (JNIEnv *env, jobject _this, jobjectArray a)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(a);
  if (len > numElements(psa)) 
  {
    // max size of memcpy
    len = numElements(psa);
  }
  if (vt == VT_VARIANT) {
    for(int i=0;i<len;i++) {
      jobject var = env->GetObjectArrayElement(a, i);
      VARIANT *v  = extractVariant(env, var);
      long x = i;
      if (v) SafeArrayPutElement(psa,&x,v);
    }
  } else {
    ThrowComFail(env, "safearray cannot be assigned from variant", -1);
  }
}

/*
 * Class:     SafeArray
 * Method:    toCharArray
 * Signature: ()[C
 */
JNIEXPORT jcharArray JNICALL Java_com_jacob_com_SafeArray_toCharArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_UI2 || vt == VT_I2) {
    jcharArray iarr = env->NewCharArray(num);
    void *pData;
    SafeArrayAccessData(sa, &pData);
    env->SetCharArrayRegion(iarr, 0, num, (jchar *)pData);
    SafeArrayUnaccessData(sa);
    return iarr;
  } else if (vt == VT_VARIANT) {
    jcharArray iarr = env->NewCharArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_UI2))) {
        return NULL;
      }
      jchar val = V_UI2(&v);
      env->SetCharArrayRegion(iarr, i, 1, &val);
    }
    return iarr;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toIntArray
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_com_jacob_com_SafeArray_toIntArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_I4) {
    jintArray iarr = env->NewIntArray(num);
    void *pData;
    SafeArrayAccessData(sa, &pData);
    env->SetIntArrayRegion(iarr, 0, num, (jint *)pData);
    SafeArrayUnaccessData(sa);
    return iarr;
  } else if (vt == VT_VARIANT) {
    jintArray iarr = env->NewIntArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_I4))) {
        return NULL;
      }
      jint val = V_I4(&v);
      env->SetIntArrayRegion(iarr, i, 1, &val);
    }
    return iarr;
  }
  return NULL;
}


/*
 * Class:     SafeArray
 * Method:    toShortArray
 * Signature: ()[S
 */
JNIEXPORT jshortArray JNICALL Java_com_jacob_com_SafeArray_toShortArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_I2) {
    jshortArray iarr = env->NewShortArray(num);
    void *pData;
    SafeArrayAccessData(sa, &pData);
    env->SetShortArrayRegion(iarr, 0, num, (jshort *)pData);
    SafeArrayUnaccessData(sa);
    return iarr;
  } else if (vt == VT_VARIANT) {
    jshortArray iarr = env->NewShortArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_I2))) {
        return NULL;
      }
      jshort val = V_I2(&v);
      env->SetShortArrayRegion(iarr, i, 1, &val);
    }
    return iarr;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toDoubleArray
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_jacob_com_SafeArray_toDoubleArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_R8) {
    jdoubleArray iarr = env->NewDoubleArray(num);
    void *pData;
    SafeArrayAccessData(sa, &pData);
    env->SetDoubleArrayRegion(iarr, 0, num, (jdouble *)pData);
    SafeArrayUnaccessData(sa);
    return iarr;
  } else if (vt == VT_VARIANT) {
    jdoubleArray iarr = env->NewDoubleArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_R8))) {
        return NULL;
      }
      jdouble val = V_R8(&v);
      env->SetDoubleArrayRegion(iarr, i, 1, &val);
    }
    return iarr;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toStringArray
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_jacob_com_SafeArray_toStringArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_VARIANT) 
  {
    jclass sClass = env->FindClass("java/lang/String");
    jobjectArray iarr = env->NewObjectArray(num, sClass, NULL);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_BSTR))) {
        return NULL;
      }
      BSTR bs = V_BSTR(&v);
      jstring js = env->NewString(bs, SysStringLen(bs));
      env->SetObjectArrayElement(iarr, i, js);
    }
    return iarr;
  } else if (vt == VT_BSTR) {
    jclass sClass = env->FindClass("java/lang/String");
    jobjectArray iarr = env->NewObjectArray(num, sClass, NULL);
    for(int i=lb;i<=ub;i++) {
      BSTR bs = NULL;
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &bs);
      jstring js = env->NewString(bs, SysStringLen(bs));
      env->SetObjectArrayElement(iarr, i, js);
    }
    return iarr;
  }
  ThrowComFail(env, "safearray cannot be converted to string[]", 0);
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toByteArray
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_jacob_com_SafeArray_toByteArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_I1 || vt == VT_UI1) {
    jbyteArray iarr = env->NewByteArray(num);
    jbyte *pData;
    SafeArrayAccessData(sa, (void **)&pData);
    env->SetByteArrayRegion(iarr, 0, num, pData);
    SafeArrayUnaccessData(sa);
    return iarr;
  } else if (vt == VT_VARIANT) {
    jbyteArray iarr = env->NewByteArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb,j=0;i<=ub;i++,j++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_UI1))) {
        return NULL;
      }
      jbyte val = V_UI1(&v);
      env->SetByteArrayRegion(iarr, j, 1, &val);
    }
    return iarr;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toFloatArray
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_com_jacob_com_SafeArray_toFloatArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_R4) {
    jfloatArray iarr = env->NewFloatArray(num);
    void *pData;
    SafeArrayAccessData(sa, &pData);
    env->SetFloatArrayRegion(iarr, 0, num, (jfloat *)pData);
    SafeArrayUnaccessData(sa);
    return iarr;
  } else if (vt == VT_VARIANT) {
    jfloatArray iarr = env->NewFloatArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_R4))) {
        return NULL;
      }
      jfloat val = V_R4(&v);
      env->SetFloatArrayRegion(iarr, i, 1, &val);
    }
    return iarr;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toBooleanArray
 * Signature: ()[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_com_jacob_com_SafeArray_toBooleanArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_BOOL) {
    // need to loop because jboolean=1 byte and VARIANT_BOOL=2 bytes
    jbooleanArray iarr = env->NewBooleanArray(num);
    VARIANT_BOOL v;
    for(int i=lb,j=0;i<=ub;i++,j++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      jboolean val = v == VARIANT_TRUE ? JNI_TRUE : JNI_FALSE;
      env->SetBooleanArrayRegion(iarr, j, 1, &val);
    }
    return iarr;
  } else if (vt == VT_VARIANT) {
    jbooleanArray iarr = env->NewBooleanArray(num);
    VARIANT v;
    VariantInit(&v);
    for(int i=lb;i<=ub;i++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_BOOL))) {
        return NULL;
      }
      jboolean val = V_BOOL(&v) == VARIANT_TRUE ? JNI_TRUE : JNI_FALSE;
      env->SetBooleanArrayRegion(iarr, i, 1, &val);
    }
    return iarr;
  }
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    toVariantArray
 * Signature: ()[LVariant;
 */
JNIEXPORT jobjectArray JNICALL Java_com_jacob_com_SafeArray_toVariantArray
  (JNIEnv *env, jobject _this)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  jclass vClass = env->FindClass("com/jacob/com/Variant");
  // create an array of Variant's
  jobjectArray varr = env->NewObjectArray(num, vClass, 0);
  // fill them in
  jmethodID variantCons =
         env->GetMethodID(vClass, "<init>", "()V");
  for(int i=lb;i<=ub;i++) {
    long ix = i;
    // construct a variant to return
    jobject newVariant = env->NewObject(vClass, variantCons);
    // get the VARIANT from the newVariant
    VARIANT *v = extractVariant(env, newVariant);
    SafeArrayGetElement(sa, &ix, (void*) v);
    // put in object array
    env->SetObjectArrayElement(varr, i, newVariant);
  }
  return varr;
}

// this is an ugly way to avoid copy/pasting the same code

#define GET1DCODE(varType, varAccess, jtyp) \
  SAFEARRAY *sa = extractSA(env, _this); \
  if (!sa) { \
    ThrowComFail(env, "safearray object corrupted", -1); \
    return NULL; \
  } \
  if (SafeArrayGetDim(sa) != 1) { \
    ThrowComFail(env, "safearray is not 1D", -1); \
    return NULL; \
  } \
  VARTYPE vt; \
  SafeArrayGetVartype(sa, &vt); \
  if (vt == VT_VARIANT) { \
    VARIANT v; \
    VariantInit(&v); \
    SafeArrayGetElement(sa, &idx, (void*) &v); \
    if (FAILED(VariantChangeType(&v, &v, 0, varType))) { \
      ThrowComFail(env, "VariantChangeType failed", -1); \
      return NULL; \
    } \
    return (jtyp)varAccess(&v); \
  } else if (vt == varType) { \
    jtyp jc; \
    SafeArrayGetElement(sa, &idx, (void*) &jc); \
    return jc; \
  } else { \
    return NULL; \
  }

#define GET2DCODE(varType, varAccess, jtyp) \
  SAFEARRAY *sa = extractSA(env, _this); \
  if (!sa) { \
    ThrowComFail(env, "safearray object corrupted", -1); \
    return NULL; \
  } \
  if (SafeArrayGetDim(sa) != 2) { \
    ThrowComFail(env, "safearray is not 2D", -1); \
    return NULL; \
  } \
  long idx[2] = {i, j}; \
  VARTYPE vt; \
  SafeArrayGetVartype(sa, &vt); \
  if (vt == VT_VARIANT) { \
    VARIANT v; \
    VariantInit(&v); \
    SafeArrayGetElement(sa, idx, (void*) &v); \
    if (FAILED(VariantChangeType(&v, &v, 0, varType))) { \
      ThrowComFail(env, "VariantChangeType failed", -1); \
      return NULL; \
    } \
    return (jtyp)varAccess(&v); \
  } else if (vt == varType) { \
    jtyp jc; \
    SafeArrayGetElement(sa, idx, (void*) &jc); \
    return jc; \
  } else { \
    return NULL; \
  }


#define SET1DCODE(varType, varAccess) \
  SAFEARRAY *sa = extractSA(env, _this); \
  if (!sa) { \
    ThrowComFail(env, "safearray object corrupted", -1); \
    return; \
  } \
  if (SafeArrayGetDim(sa) != 1) { \
    ThrowComFail(env, "safearray is not 1D", -1); \
    return; \
  } \
  VARTYPE vt; \
  SafeArrayGetVartype(sa, &vt); \
  if (vt == VT_VARIANT) { \
    VARIANT v; \
    VariantInit(&v); \
    V_VT(&v) = varType; \
    varAccess(&v) = c; \
    SafeArrayPutElement(sa,&idx,&v); \
  } else if (vt == varType) { \
    SafeArrayPutElement(sa,&idx,&c); \
  } else { \
    ThrowComFail(env, "safearray type mismatch", -1); \
  } \

#define SET2DCODE(varType, varAccess) \
  SAFEARRAY *sa = extractSA(env, _this); \
  if (!sa) { \
    ThrowComFail(env, "safearray object corrupted", -1); \
    return; \
  } \
  if (SafeArrayGetDim(sa) != 2) { \
    ThrowComFail(env, "safearray is not 2D", -1); \
    return; \
  } \
  VARTYPE vt; \
  SafeArrayGetVartype(sa, &vt); \
  long idx[2] = {i,j}; \
  if (vt == VT_VARIANT) { \
    VARIANT v; \
    VariantInit(&v); \
    V_VT(&v) = varType; \
    varAccess(&v) = c; \
    SafeArrayPutElement(sa,idx,&v); \
  } else if (vt == varType) { \
    SafeArrayPutElement(sa,idx,&c); \
  } else { \
    ThrowComFail(env, "safearray type mismatch", -1); \
  }

#define GETARRAYCODE(varType, varType2, varAccess, jtyp, jsetArr) \
  SAFEARRAY *sa = extractSA(env, _this); \
  if (!sa) { \
    ThrowComFail(env, "safearray object corrupted", -1); \
    return; \
  } \
  VARTYPE vt; \
  SafeArrayGetVartype(sa, &vt); \
  if (vt == varType || vt == varType2) { \
    jtyp *pData; \
    SafeArrayAccessData(sa, (void **)&pData); \
    env->jsetArr(ja, ja_start, nelem, &pData[idx]); \
    SafeArrayUnaccessData(sa); \
    return; \
  } else if (vt == VT_VARIANT) { \
    VARIANT v; \
    VariantInit(&v); \
    for(int i=idx, j=ja_start;i<idx+nelem;i++,j++) { \
      long ix = i; \
      SafeArrayGetElement(sa, &ix, (void*) &v); \
      if (FAILED(VariantChangeType(&v, &v, 0, varType))) { \
        return; \
      } \
      jtyp val = varAccess(&v); \
      env->jsetArr(ja, j, 1, &val); \
    } \
  }

#define SETARRAYCODE(varType, varType2, varAccess, jtyp, jgetArr, jrelArr) \
  SAFEARRAY *psa = extractSA(env, _this); \
  if (!psa) { \
    ThrowComFail(env, "safearray object corrupted", -1); \
    return; \
  } \
  VARTYPE vt; \
  SafeArrayGetVartype(psa, &vt); \
  jtyp *iarr = env->jgetArr(ja, 0); \
  if (vt == VT_VARIANT) { \
    VARIANT v; \
    VariantInit(&v); \
    V_VT(&v) = varType; \
    for(int i=ja_start,j=idx;i<ja_start+nelem;i++,j++) { \
      varAccess(&v) = iarr[i]; \
      long x = j; \
      SafeArrayPutElement(psa,&x,&v); \
    } \
  } else if (vt == varType || vt == varType2) { \
    jtyp *pData; \
    SafeArrayAccessData(psa, (void **)&pData); \
    memcpy(&pData[idx], &iarr[ja_start], nelem*sizeof(jtyp)); \
    SafeArrayUnaccessData(psa); \
  } else { \
    ThrowComFail(env, "safearray type mismatch", -1); \
  } \
  env->jrelArr(ja, iarr, 0);

/*
 * Class:     SafeArray
 * Method:    getChar
 * Signature: (I)C
 */
JNIEXPORT jchar JNICALL Java_com_jacob_com_SafeArray_getChar__I
  (JNIEnv *env, jobject _this, jint idx)
{
  GET1DCODE(VT_UI2, V_UI2, jchar)
}

/*
 * Class:     SafeArray
 * Method:    getChar
 * Signature: (II)C
 */
JNIEXPORT jchar JNICALL Java_com_jacob_com_SafeArray_getChar__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  GET2DCODE(VT_UI2, V_UI2, jchar)
}


/*
 * Class:     SafeArray
 * Method:    setChar
 * Signature: (IC)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setChar__IC
  (JNIEnv *env, jobject _this, jint idx, jchar c)
{
  SET1DCODE(VT_UI2, V_UI2);
}

/*
 * Class:     SafeArray
 * Method:    setChar
 * Signature: (IIC)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setChar__IIC
  (JNIEnv *env, jobject _this, jint i, jint j, jchar c)
{
  SET2DCODE(VT_UI2, V_UI2);
}

/*
 * Class:     SafeArray
 * Method:    getChars
 * Signature: (II[CI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getChars
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jcharArray ja, jint ja_start)
{
  GETARRAYCODE(VT_UI2, VT_I2, V_UI2, jchar, SetCharArrayRegion);
}

/*
 * Class:     SafeArray
 * Method:    setChars
 * Signature: (II[CI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setChars
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jcharArray ja, jint ja_start)
{
  SETARRAYCODE(VT_UI2, VT_I2, V_UI2, jchar, 
       GetCharArrayElements, ReleaseCharArrayElements);
}

/*
 * Class:     SafeArray
 * Method:    getInt
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getInt__I
  (JNIEnv *env, jobject _this, jint idx)
{
  GET1DCODE(VT_I4, V_I4, jint)
}

/*
 * Class:     SafeArray
 * Method:    getInt
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_jacob_com_SafeArray_getInt__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  GET2DCODE(VT_I4, V_I4, jint)
}

/*
 * Class:     SafeArray
 * Method:    setInt
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setInt__II
  (JNIEnv *env, jobject _this, jint idx, jint c)
{
  SET1DCODE(VT_I4, V_I4);
}

/*
 * Class:     SafeArray
 * Method:    setInt
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setInt__III
  (JNIEnv *env, jobject _this, jint i, jint j, jint c)
{
  SET2DCODE(VT_I4, V_I4);
}

/*
 * Class:     SafeArray
 * Method:    getInts
 * Signature: (II[II)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getInts
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jintArray ja, jint ja_start)
{
  GETARRAYCODE(VT_I4, VT_I4, V_I4, jint, SetIntArrayRegion);
}

/*
 * Class:     SafeArray
 * Method:    setInts
 * Signature: (II[II)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setInts
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jintArray ja, jint ja_start)
{
  SETARRAYCODE(VT_I4, VT_I4, V_I4, jint, 
       GetIntArrayElements, ReleaseIntArrayElements);
}

/*
 * Class:     SafeArray
 * Method:    getShort
 * Signature: (I)S
 */
JNIEXPORT jshort JNICALL Java_com_jacob_com_SafeArray_getShort__I
  (JNIEnv *env, jobject _this, jint idx)
{
  GET1DCODE(VT_I2, V_I2, jshort)
}

/*
 * Class:     SafeArray
 * Method:    getShort
 * Signature: (II)S
 */
JNIEXPORT jshort JNICALL Java_com_jacob_com_SafeArray_getShort__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  GET2DCODE(VT_I2, V_I2, jshort)
}

/*
 * Class:     SafeArray
 * Method:    setShort
 * Signature: (IS)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setShort__IS
  (JNIEnv *env, jobject _this, jint idx, jshort c)
{
  SET1DCODE(VT_I2, V_I2);
}

/*
 * Class:     SafeArray
 * Method:    setShort
 * Signature: (IIS)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setShort__IIS
  (JNIEnv *env, jobject _this, jint i, jint j, jshort c)
{
  SET2DCODE(VT_I2, V_I2);
}

/*
 * Class:     SafeArray
 * Method:    getShorts
 * Signature: (II[SI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getShorts
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jshortArray ja, jint ja_start)
{
  GETARRAYCODE(VT_I2, VT_I2, V_I2, jshort, SetShortArrayRegion);
}

/*
 * Class:     SafeArray
 * Method:    setShorts
 * Signature: (II[SI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setShorts
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jshortArray ja, jint ja_start)
{
  SETARRAYCODE(VT_I2, VT_I2, V_I2, jshort, 
       GetShortArrayElements, ReleaseShortArrayElements);
}

/*
 * Class:     SafeArray
 * Method:    getDouble
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_com_jacob_com_SafeArray_getDouble__I
  (JNIEnv *env, jobject _this, jint idx)
{
  GET1DCODE(VT_R8, V_R8, jdouble)
}

/*
 * Class:     SafeArray
 * Method:    getDouble
 * Signature: (II)D
 */
JNIEXPORT jdouble JNICALL Java_com_jacob_com_SafeArray_getDouble__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  GET2DCODE(VT_R8, V_R8, jdouble)
}

/*
 * Class:     SafeArray
 * Method:    setDouble
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setDouble__ID
  (JNIEnv *env, jobject _this, jint idx, jdouble c)
{
  SET1DCODE(VT_R8, V_R8);
}

/*
 * Class:     SafeArray
 * Method:    setDouble
 * Signature: (IID)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setDouble__IID
  (JNIEnv *env, jobject _this, jint i, jint j, jdouble c)
{
  SET2DCODE(VT_R8, V_R8);
}

/*
 * Class:     SafeArray
 * Method:    getDoubles
 * Signature: (II[DI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getDoubles
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jdoubleArray ja, jint ja_start)
{
  GETARRAYCODE(VT_R8, VT_R8, V_R8, jdouble, SetDoubleArrayRegion);
}

/*
 * Class:     SafeArray
 * Method:    setDoubles
 * Signature: (II[DI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setDoubles
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jdoubleArray ja, jint ja_start)
{
  SETARRAYCODE(VT_R8, VT_R8, V_R8, jdouble, 
       GetDoubleArrayElements, ReleaseDoubleArrayElements);
}

/*
 * Class:     SafeArray
 * Method:    getString
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_jacob_com_SafeArray_getString__I
  (JNIEnv *env, jobject _this, jint idx)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1); \
    return NULL;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    SafeArrayGetElement(psa, &idx, &v);
    if (FAILED(VariantChangeType(&v, &v, 0, VT_BSTR))) {
      return NULL;
    }
    BSTR bs = V_BSTR(&v);
    jstring js = env->NewString(bs, SysStringLen(bs));
    return js;
  } else if (vt == VT_BSTR) {
    BSTR bs = NULL;
    SafeArrayGetElement(psa, &idx, &bs);
    jstring js = env->NewString(bs, SysStringLen(bs));
    return js;
  }
  ThrowComFail(env, "safearray cannot get string", 0);
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    getString
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_jacob_com_SafeArray_getString__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1); \
    return NULL;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    long idx[2] = {i, j};
    SafeArrayGetElement(psa, idx, &v);
    if (FAILED(VariantChangeType(&v, &v, 0, VT_BSTR))) {
      return NULL;
    }
    BSTR bs = V_BSTR(&v);
    jstring js = env->NewString(bs, SysStringLen(bs));
    return js;
  } else if (vt == VT_BSTR) {
    long idx[2] = {i, j};
    BSTR bs = NULL;
    SafeArrayGetElement(psa, idx, &bs);
    jstring js = env->NewString(bs, SysStringLen(bs));
    return js;
  }
  ThrowComFail(env, "safearray cannot get string", 0);
  return NULL;
}

/*
 * Class:     SafeArray
 * Method:    setString
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setString__ILjava_lang_String_2
  (JNIEnv *env, jobject _this, jint idx, jstring s)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1); \
    return;
  } 
  if (SafeArrayGetDim(sa) != 1) { 
    ThrowComFail(env, "safearray not 1D", -1); \
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    const char *str = env->GetStringUTFChars(s, NULL);
    CComBSTR bs(str);
    V_VT(&v) = VT_BSTR;
    V_BSTR(&v) = bs.Copy();
    SafeArrayPutElement(sa,&idx,&v); \
    env->ReleaseStringUTFChars(s, str);
    VariantClear(&v);
  } else if (vt == VT_BSTR) {
    const char *str = env->GetStringUTFChars(s, NULL);
    CComBSTR bs(str);
    SafeArrayPutElement(sa,&idx,bs.Detach());
    env->ReleaseStringUTFChars(s, str);
  } else {
    ThrowComFail(env, "safearray cannot set string", 0);
  }
}

/*
 * Class:     SafeArray
 * Method:    setString
 * Signature: (IILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setString__IILjava_lang_String_2
  (JNIEnv *env, jobject _this, jint i, jint j, jstring s)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  } 
  if (SafeArrayGetDim(sa) != 2) { 
    ThrowComFail(env, "safearray is not 2D", -1); 
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    const char *str = env->GetStringUTFChars(s, NULL);
    CComBSTR bs(str);
    V_VT(&v) = VT_BSTR;
    V_BSTR(&v) = bs.Copy();
    long idx[2] = {i,j};
    SafeArrayPutElement(sa,idx,&v);
    env->ReleaseStringUTFChars(s, str);
    VariantClear(&v);
  } else if (vt == VT_BSTR) {
    long idx[2] = {i,j};
    const char *str = env->GetStringUTFChars(s, NULL);
    CComBSTR bs(str);
    SafeArrayPutElement(sa,idx,bs.Detach());
    env->ReleaseStringUTFChars(s, str);
  } else {
    ThrowComFail(env, "safearray cannot set string", 0);
  }
}

/*
 * Class:     SafeArray
 * Method:    getStrings
 * Signature: (II[Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getStrings
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jobjectArray ja, jint ja_start)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb); 
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1; 
  VARTYPE vt; 
  SafeArrayGetVartype(sa, &vt); 
  if (vt == VT_VARIANT) 
  {
    VARIANT v;
    for(int i=idx, j=ja_start;i<idx+nelem;i++,j++) 
    {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_BSTR))) {
        return;
      }
      BSTR bs = V_BSTR(&v);
      jstring js = env->NewString(bs, SysStringLen(bs));
      env->SetObjectArrayElement(ja, j, js);
      VariantClear(&v);
    } 
  }
  else if (vt == VT_BSTR) 
  {
    BSTR bs = NULL;
    for(int i=idx, j=ja_start;i<idx+nelem;i++,j++) 
    {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &bs);
      jstring js = env->NewString(bs, SysStringLen(bs));
      env->SetObjectArrayElement(ja, j, js);
    } 
  } else {
    ThrowComFail(env, "safearray cannot get strings", 0);
  }
}

/*
 * Class:     SafeArray
 * Method:    setStrings
 * Signature: (II[Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setStrings
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jobjectArray ja, jint ja_start)
{
  SAFEARRAY *psa = extractSA(env, _this); 
  if (!psa) { 
    ThrowComFail(env, "safearray object corrupted", -1);
    return; 
  } 
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt); 
  int len = env->GetArrayLength(ja);
  if (len > numElements(psa)) 
  { 
    len = numElements(psa); 
  } 
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    for(int i=ja_start,j=idx;i<ja_start+nelem;i++,j++) 
    {
      jstring s = (jstring)env->GetObjectArrayElement(ja, i);
      const char *str = env->GetStringUTFChars(s, NULL);
      CComBSTR bs(str);
      V_VT(&v) = VT_BSTR;
      V_BSTR(&v) = bs.Copy();
      long x = j; 
      SafeArrayPutElement(psa,&x,&v); 
      VariantClear(&v);
      env->ReleaseStringUTFChars(s, str);
    } 
  } else if (vt == VT_BSTR) {
    for(int i=ja_start,j=idx;i<ja_start+nelem;i++,j++) 
    {
      jstring s = (jstring)env->GetObjectArrayElement(ja, i);
      const char *str = env->GetStringUTFChars(s, NULL);
      CComBSTR bs(str);
      long x = j; 
      SafeArrayPutElement(psa,&x,bs.Detach()); 
      env->ReleaseStringUTFChars(s, str);
    } 
  } else {
    ThrowComFail(env, "safearray cannot set strings", 0);
  } 
}

/*
 * Class:     SafeArray
 * Method:    getByte
 * Signature: (I)B
 */
JNIEXPORT jbyte JNICALL Java_com_jacob_com_SafeArray_getByte__I
  (JNIEnv *env, jobject _this, jint idx)
{
  GET1DCODE(VT_UI1, V_UI1, jbyte)
}

/*
 * Class:     SafeArray
 * Method:    getByte
 * Signature: (II)B
 */
JNIEXPORT jbyte JNICALL Java_com_jacob_com_SafeArray_getByte__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  GET2DCODE(VT_UI1, V_UI1, jbyte)
}

/*
 * Class:     SafeArray
 * Method:    setByte
 * Signature: (IB)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setByte__IB
  (JNIEnv *env, jobject _this, jint idx, jbyte c)
{
  SET1DCODE(VT_UI1, V_UI1);
}

/*
 * Class:     SafeArray
 * Method:    setByte
 * Signature: (IIB)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setByte__IIB
  (JNIEnv *env, jobject _this, jint i, jint j, jbyte c)
{
  SET2DCODE(VT_UI1, V_UI1);
}

/*
 * Class:     SafeArray
 * Method:    getBytes
 * Signature: (II[BI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getBytes
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jbyteArray ja, jint ja_start)
{
  GETARRAYCODE(VT_UI1, VT_I1, V_UI1, jbyte, SetByteArrayRegion);
}

/*
 * Class:     SafeArray
 * Method:    setBytes
 * Signature: (II[BI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setBytes
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jbyteArray ja, jint ja_start)
{
  SETARRAYCODE(VT_UI1, VT_I1, V_UI1, jbyte, 
       GetByteArrayElements, ReleaseByteArrayElements);
}

/*
 * Class:     SafeArray
 * Method:    getFloat
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_com_jacob_com_SafeArray_getFloat__I
  (JNIEnv *env, jobject _this, jint idx)
{
  GET1DCODE(VT_R4, V_R4, jfloat)
}

/*
 * Class:     SafeArray
 * Method:    getFloat
 * Signature: (II)F
 */
JNIEXPORT jfloat JNICALL Java_com_jacob_com_SafeArray_getFloat__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  GET2DCODE(VT_R4, V_R4, jfloat)
}

/*
 * Class:     SafeArray
 * Method:    setFloat
 * Signature: (IF)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setFloat__IF
  (JNIEnv *env, jobject _this, jint idx, jfloat c)
{
  SET1DCODE(VT_R4, V_R4);
}

/*
 * Class:     SafeArray
 * Method:    setFloat
 * Signature: (IIF)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setFloat__IIF
  (JNIEnv *env, jobject _this, jint i, jint j, jfloat c)
{
  SET2DCODE(VT_R4, V_R4);
}

/*
 * Class:     SafeArray
 * Method:    getFloats
 * Signature: (II[FI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getFloats
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jfloatArray ja, jint ja_start)
{
  GETARRAYCODE(VT_R4, VT_R4, V_R4, jfloat, SetFloatArrayRegion);
}

/*
 * Class:     SafeArray
 * Method:    setFloats
 * Signature: (II[FI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setFloats
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jfloatArray ja, jint ja_start)
{
  SETARRAYCODE(VT_R4, VT_R4, V_R4, jfloat, 
       GetFloatArrayElements, ReleaseFloatArrayElements);
}

/*
 * Class:     SafeArray
 * Method:    getBoolean
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_jacob_com_SafeArray_getBoolean__I
  (JNIEnv *env, jobject _this, jint idx)
{
  // code is inline because of size mismatch
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) { 
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL; 
  } 
  if (SafeArrayGetDim(sa) != 1) {
    ThrowComFail(env, "safearray not 1D", -1); \
    return NULL;
  } 
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    SafeArrayGetElement(sa, &idx, (void*) &v);
    if (FAILED(VariantChangeType(&v, &v, 0, VT_BOOL))) {
      ThrowComFail(env, "safearray change type failed", -1); \
      return NULL;
    }
    jboolean jb = V_BOOL(&v) == VARIANT_TRUE ? JNI_TRUE: JNI_FALSE;
    return jb;
  } else if (vt == VT_BOOL) {
    VARIANT_BOOL vb;
    SafeArrayGetElement(sa, &idx, (void*) &vb);
    jboolean jb = vb == VARIANT_TRUE ? JNI_TRUE: JNI_FALSE;
    return jb;
  } else {
    return NULL;
  }
}

/*
 * Class:     SafeArray
 * Method:    getBoolean
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_jacob_com_SafeArray_getBoolean__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  // code is inline because of size mismatch
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) { 
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL; 
  } 
  if (SafeArrayGetDim(sa) != 2) {
    ThrowComFail(env, "safearray is not 2D", -1);
    return NULL;
  } 
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  long idx[2] = {i,j};
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    SafeArrayGetElement(sa, idx, (void*) &v);
    if (FAILED(VariantChangeType(&v, &v, 0, VT_BOOL))) {
      ThrowComFail(env, "safearray change type failed", -1);
      return NULL;
    }
    jboolean jb = V_BOOL(&v) == VARIANT_TRUE ? JNI_TRUE: JNI_FALSE;
    return jb;
  } else if (vt == VT_BOOL) {
    VARIANT_BOOL vb;
    SafeArrayGetElement(sa, idx, (void*) &vb);
    jboolean jb = vb == VARIANT_TRUE ? JNI_TRUE: JNI_FALSE;
    return jb;
  } else {
    return NULL;
  }
}

/*
 * Class:     SafeArray
 * Method:    setBoolean
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setBoolean__IZ
  (JNIEnv *env, jobject _this, jint idx, jboolean c)
{
  // code is inline because of size mismatch
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  if (SafeArrayGetDim(sa) != 1) {
    ThrowComFail(env, "safearray is not 1D", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_BOOL;
    V_BOOL(&v) = c == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
    SafeArrayPutElement(sa,&idx,&v);
  } else if (vt == VT_BOOL) {
    VARIANT_BOOL vb = c == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
    SafeArrayPutElement(sa,&idx,&vb);
  } else {
    ThrowComFail(env, "safearray type mismatch", -1);
  }
}

/*
 * Class:     SafeArray
 * Method:    setBoolean
 * Signature: (IIZ)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setBoolean__IIZ
  (JNIEnv *env, jobject _this, jint i, jint j, jboolean c)
{
  // code is inline because of size mismatch
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  if (SafeArrayGetDim(sa) != 2) {
    ThrowComFail(env, "safearray is not 2D", -1);
    return;
  } 
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  long idx[2] = {i,j};
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_BOOL;
    V_BOOL(&v) = c == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
    SafeArrayPutElement(sa,idx,&v);
  } else if (vt == VT_BOOL) {
    VARIANT_BOOL vb = c == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
    SafeArrayPutElement(sa,idx,&vb);
  } else {
    ThrowComFail(env, "safearray type mismatch", -1);
  }
}

/*
 * Class:     SafeArray
 * Method:    getBooleans
 * Signature: (II[ZI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getBooleans
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jbooleanArray ja, jint ja_start)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb);
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1;
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  if (vt == VT_BOOL) {
    VARIANT_BOOL v;
    for(int i=idx, j=ja_start;i<idx+nelem;i++,j++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      jboolean val = v == VARIANT_TRUE ? JNI_TRUE : JNI_FALSE;
      env->SetBooleanArrayRegion(ja, j, 1, &val);
    }
  } else if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    for(int i=idx, j=ja_start;i<idx+nelem;i++,j++) {
      long ix = i;
      SafeArrayGetElement(sa, &ix, (void*) &v);
      if (FAILED(VariantChangeType(&v, &v, 0, VT_BOOL))) {
        return;
      }
      jboolean val = V_BOOL(&v) == VARIANT_TRUE ? JNI_TRUE : JNI_FALSE;
      env->SetBooleanArrayRegion(ja, j, 1, &val);
    }
  }
}

/*
 * Class:     SafeArray
 * Method:    setBooleans
 * Signature: (II[ZI)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setBooleans
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jbooleanArray ja, jint ja_start)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  int len = env->GetArrayLength(ja);
  if (len > numElements(psa))
  {
    len = numElements(psa);
  }
  jboolean *iarr = env->GetBooleanArrayElements(ja, 0);
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    V_VT(&v) = VT_BOOL;
    for(int i=ja_start,j=idx;i<ja_start+nelem;i++,j++) {
      V_BOOL(&v) = iarr[i] == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
      long x = j;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else if (vt == VT_BOOL) {
    VARIANT_BOOL v;
    for(int i=ja_start,j=idx;i<ja_start+nelem;i++,j++) {
      v = iarr[i] == JNI_TRUE ? VARIANT_TRUE : VARIANT_FALSE;
      long x = j;
      SafeArrayPutElement(psa,&x,&v);
    }
  } else {
    ThrowComFail(env, "safearray type mismatch", -1);
  } 
  env->ReleaseBooleanArrayElements(ja, iarr, 0);
}

/*
 * Class:     SafeArray
 * Method:    getVariant
 * Signature: (I)LVariant;
 */
JNIEXPORT jobject JNICALL Java_com_jacob_com_SafeArray_getVariant__I
  (JNIEnv *env, jobject _this, jint idx)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  // prepare a new return value
  jclass variantClass = env->FindClass("com/jacob/com/Variant");
  jmethodID variantCons = 
      env->GetMethodID(variantClass, "<init>", "()V");
  // construct a variant to return
  jobject newVariant = env->NewObject(variantClass, variantCons);
  // get the VARIANT from the newVariant
  VARIANT *v = extractVariant(env, newVariant);
  if (vt == VT_VARIANT) {
    SafeArrayGetElement(psa, &idx, v);
  } else if (vt == VT_DISPATCH || vt == VT_UNKNOWN) {
    IDispatch *disp;
    SafeArrayGetElement(psa, &idx, (void *)&disp);
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_DISPATCH;
    V_DISPATCH(v) = disp;
    // I am handing the pointer to COM
    disp->AddRef();
  } else {
    ThrowComFail(env, "safearray type is not variant/dispatch", -1);
  }
  return newVariant;
}

/*
 * Class:     SafeArray
 * Method:    getVariant
 * Signature: (II)LVariant;
 */
JNIEXPORT jobject JNICALL Java_com_jacob_com_SafeArray_getVariant__II
  (JNIEnv *env, jobject _this, jint i, jint j)
{
  SAFEARRAY *psa = extractSA(env, _this);
  if (!psa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return NULL;
  }
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt);
  // prepare a new return value
  jclass variantClass = env->FindClass("com/jacob/com/Variant");
  jmethodID variantCons = 
      env->GetMethodID(variantClass, "<init>", "()V");
  // construct a variant to return
  jobject newVariant = env->NewObject(variantClass, variantCons);
  // get the VARIANT from the newVariant
  VARIANT *v = extractVariant(env, newVariant);
  long idx[2] = {i,j};
  if (vt == VT_VARIANT) {
    SafeArrayGetElement(psa, idx, v);
  } else if (vt == VT_DISPATCH || vt == VT_UNKNOWN) {
    IDispatch *disp;
    SafeArrayGetElement(psa, idx, (void *)&disp);
    VariantClear(v); // whatever was there before
    V_VT(v) = VT_DISPATCH;
    V_DISPATCH(v) = disp;
    // I am handing the pointer to COM
    disp->AddRef();
  } else {
    ThrowComFail(env, "safearray type is not variant/dispatch", -1);
  }
  return newVariant;
}

/*
 * Class:     SafeArray
 * Method:    setVariant
 * Signature: (ILVariant;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setVariant__ILcom_jacob_com_Variant_2
  (JNIEnv *env, jobject _this, jint idx, jobject s)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  } 
  if (SafeArrayGetDim(sa) != 1) { 
    ThrowComFail(env, "safearray is not 1D", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  VARIANT *v = extractVariant(env, s);
  if (vt == VT_VARIANT) {
    SafeArrayPutElement(sa,&idx,v);
  } else if (vt == VT_DISPATCH || vt == VT_UNKNOWN) {
    if (V_VT(v) != VT_DISPATCH) {
      ThrowComFail(env, "variant is not dispatch", -1);
      return;
    }
    IDispatch *disp = V_DISPATCH(v);
    disp->AddRef();
    SafeArrayPutElement(sa,&idx,disp);
  } else {
    ThrowComFail(env, "safearray type is not variant/dispatch", -1);
  }
}

/*
 * Class:     SafeArray
 * Method:    setVariant
 * Signature: (IILVariant;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setVariant__IILcom_jacob_com_Variant_2
  (JNIEnv *env, jobject _this, jint i, jint j, jobject s)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  } 
  if (SafeArrayGetDim(sa) != 2) { 
    ThrowComFail(env, "safearray is not 2D", -1);
    return;
  }
  VARTYPE vt;
  SafeArrayGetVartype(sa, &vt);
  long idx[2] = {i, j};
  if (vt == VT_VARIANT) {
    VARIANT *v = extractVariant(env, s);
    SafeArrayPutElement(sa,idx,v);
  } else if (vt == VT_DISPATCH || vt == VT_UNKNOWN) {
    VARIANT *v = extractVariant(env, s);
    if (V_VT(v) != VT_DISPATCH) {
      ThrowComFail(env, "variant is not dispatch", -1);
      return;
    }
    IDispatch *disp = V_DISPATCH(v);
    SafeArrayPutElement(sa,idx,disp);
  } else {
    ThrowComFail(env, "safearray type is not variant/dispatch", -1);
  }
}

/*
 * Class:     SafeArray
 * Method:    getVariants
 * Signature: (II[LVariant;I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_getVariants
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jobjectArray ja, jint ja_start)
{
  SAFEARRAY *sa = extractSA(env, _this);
  if (!sa) {
    ThrowComFail(env, "safearray object corrupted", -1);
    return;
  }
  long lb, ub;
  SafeArrayGetLBound(sa, 1, &lb); 
  SafeArrayGetUBound(sa, 1, &ub);
  int num = ub - lb + 1; 
  VARTYPE vt; 
  SafeArrayGetVartype(sa, &vt); 
  if (vt == VT_VARIANT) 
  {
    jclass variantClass = env->FindClass("com/jacob/com/Variant");
    jmethodID variantCons = 
        env->GetMethodID(variantClass, "<init>", "()V");
    for(int i=idx, j=ja_start;i<idx+nelem;i++,j++) 
    {
      long ix = i;
      // construct a variant to return
      jobject newVariant = env->NewObject(variantClass, variantCons);
      // get the VARIANT from the newVariant
      VARIANT *v = extractVariant(env, newVariant);
      SafeArrayGetElement(sa, &ix, (void*) v);
      env->SetObjectArrayElement(ja, j, newVariant);
    } 
  } else {
    ThrowComFail(env, "safearray type is not variant", -1);
  }
}

/*
 * Class:     SafeArray
 * Method:    setVariants
 * Signature: (II[LVariant;I)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_SafeArray_setVariants
  (JNIEnv *env, jobject _this, jint idx, jint nelem, jobjectArray ja, jint ja_start)
{
  SAFEARRAY *psa = extractSA(env, _this); 
  if (!psa) { 
    ThrowComFail(env, "safearray object corrupted", -1);
    return; 
  } 
  VARTYPE vt;
  SafeArrayGetVartype(psa, &vt); 
  int len = env->GetArrayLength(ja);
  if (len > numElements(psa)) 
  { 
    len = numElements(psa); 
  } 
  if (vt == VT_VARIANT) {
    VARIANT v;
    VariantInit(&v);
    for(int i=ja_start,j=idx;i<ja_start+nelem;i++,j++) 
    {
      jobject var = env->GetObjectArrayElement(ja, i);
      VARIANT *v  = extractVariant(env, var);
      long x = j;
      SafeArrayPutElement(psa,&x,v);
    } 
  } else {
    ThrowComFail(env, "safearray type is not variant", -1);
  } 
}

}

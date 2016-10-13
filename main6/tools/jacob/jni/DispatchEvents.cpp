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
#include "DispatchEvents.h"
#include "EventProxy.h"
// Win32 support for Ole Automation
#include <wchar.h>
#include <string.h>
#include <objbase.h>
#include <atlbase.h>
#include <oleauto.h>
#include <olectl.h>

#include "util.h"

extern "C"
{

#define PROXY_FLD "m_pConnPtProxy"

// defined below
BOOL GetEventIID(IUnknown*, IID*, CComBSTR **, DISPID **, int *,LPOLESTR);
BOOL getClassInfoFromProgId(LPOLESTR bsProgId,LPTYPEINFO *pClassInfo);

// extract a EventProxy* from a jobject
EventProxy *extractProxy(JNIEnv *env, jobject arg)
{
  jclass argClass = env->GetObjectClass(arg);
  jfieldID ajf = env->GetFieldID( argClass, PROXY_FLD, "I");
  jint anum = env->GetIntField(arg, ajf);
  EventProxy *v = (EventProxy *)anum;
  return v;
}

void putProxy(JNIEnv *env, jobject arg, EventProxy *ep)
{
  jclass argClass = env->GetObjectClass(arg);
  jfieldID ajf = env->GetFieldID( argClass, PROXY_FLD, "I");
  jint anum = env->GetIntField(arg, ajf);
  env->SetIntField(arg, ajf, (jint)ep);
}

/*
 * Class:     DispatchEvents
 * Method:    init2
 * Signature: (LDispatch;Ljava/lang/Object;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_DispatchEvents_init2
  (JNIEnv *env, 
  jobject _this, jobject src, 
  jobject sink, jobject protoVariant, 
  jstring _progid)
{
	USES_CONVERSION;
  // find progid if any
  LPOLESTR bsProgId = NULL;
  if (_progid!=NULL) {
    const char *progid = env->GetStringUTFChars(_progid, NULL);
		bsProgId = A2W(progid);
  }

  // get the IDispatch for the source object
  IDispatch *pDisp = extractDispatch(env, src);
  CComQIPtr<IUnknown, &IID_IUnknown> pUnk(pDisp);
  // see if it implements connection points
  CComQIPtr<IConnectionPointContainer, &IID_IConnectionPointContainer> pCPC(pUnk);
  if (!pCPC)
  {
    // no events, throw something
    ThrowComFail(env, "Can't find IConnectionPointContainer", -1);
    return;
  }
  // hook up to the default source iid
  CComPtr<IConnectionPoint> pCP;
  IID eventIID;
  CComBSTR *mNames;
  DISPID *mIDs;
  int n_EventMethods;
  if (!GetEventIID(pUnk, &eventIID, &mNames, &mIDs, &n_EventMethods,bsProgId)) {
    ThrowComFail(env, "Can't find event iid", -1);
    return;
  }
  HRESULT hr = pCPC->FindConnectionPoint(eventIID, &pCP);
  // VC++ 6.0 compiler realiized we weren't using this variable
  //DWORD dwEventCookie;
  if (SUCCEEDED(hr))
  {
    EventProxy *ep = new EventProxy(env, sink, protoVariant, pCP, eventIID, mNames, mIDs, n_EventMethods);
    // need to store ep on _this, in case it gets collected
    putProxy(env, _this, ep);
  } else {
    ThrowComFail(env, "Can't FindConnectionPoint", hr);
  }
}

/*
 * Class:     DispatchEvents
 * Method:    init
 * Signature: (LDispatch;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_DispatchEvents_init
  (JNIEnv *env, jobject _this, jobject src, jobject sink, jobject protoVariant)
{
	Java_com_jacob_com_DispatchEvents_init2(env,_this,src,sink, protoVariant, NULL);
}

/*
 * Class:     DispatchEvents
 * Method:    release
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jacob_com_DispatchEvents_release
  (JNIEnv *env, jobject _this)
{
  EventProxy *ep = extractProxy(env, _this);
  if (ep) {
    ep->Release();
    putProxy(env, _this, NULL);
  }
}

/*
 * I need a reverse map from the event interface's dispids to
 * function names so that we can reflect them to java
 */
void
LoadNameCache(LPTYPEINFO pTypeInfo, LPTYPEATTR pta,
   CComBSTR **mNames, DISPID **mIDs, int *nmeth)
{
  CComBSTR *names = NULL;
  DISPID   *ids = NULL;
  int m_nCount;

  m_nCount = pta->cFuncs;
  *nmeth = m_nCount;
  names = m_nCount == 0 ? NULL : new CComBSTR[m_nCount];
  ids = m_nCount == 0 ? NULL : new DISPID[m_nCount];
  for (int i=0; i<m_nCount; i++)
  {
    FUNCDESC* pfd;
    if (SUCCEEDED(pTypeInfo->GetFuncDesc(i, &pfd)))
    {
      CComBSTR bstrName;
      if (SUCCEEDED(pTypeInfo->GetDocumentation(pfd->memid, &bstrName, NULL, NULL, NULL)))
      {
        names[i].Attach(bstrName.Detach());
        ids[i] = pfd->memid;
        /*
        USES_CONVERSION;
        printf("map:%d -> %s\n", ids[i], W2A((OLECHAR *)names[i]));
        */
      }
      pTypeInfo->ReleaseFuncDesc(pfd);
    }
  }
  *mNames = names;
  *mIDs = ids;
}

#define IMPLTYPE_MASK \
  (IMPLTYPEFLAG_FDEFAULT | IMPLTYPEFLAG_FSOURCE | IMPLTYPEFLAG_FRESTRICTED)

#define IMPLTYPE_DEFAULTSOURCE \
  (IMPLTYPEFLAG_FDEFAULT | IMPLTYPEFLAG_FSOURCE)


BOOL GetEventIID(IUnknown *m_pObject, IID* piid,
     CComBSTR **mNames, DISPID **mIDs, int *nmeth,LPOLESTR bsProgId)
{
  *piid = GUID_NULL;
  ATLASSERT(m_pObject != NULL);
  // I Always use IProvideClassInfo rather than IProvideClassInfo2
  // since I also need to create a mapping from dispid to name
  LPPROVIDECLASSINFO pPCI = NULL;
  LPTYPEINFO pClassInfo = NULL;
  if (SUCCEEDED(m_pObject->QueryInterface(IID_IProvideClassInfo, (LPVOID*)&pPCI)))
  {
    //printf("got IProvideClassInfo\n");
    ATLASSERT(pPCI != NULL);
		HRESULT hr = pPCI->GetClassInfo(&pClassInfo);
    pPCI->Release();
    if (!SUCCEEDED(hr)) return false;
  }
  else if (getClassInfoFromProgId(bsProgId,&pClassInfo)) {
	}
	else  {
    printf("couldn't get IProvideClassInfo\n");
		return false;
  }


      //printf("got ClassInfo\n");
      ATLASSERT(pClassInfo != NULL);
      LPTYPEATTR pClassAttr;
      if (SUCCEEDED(pClassInfo->GetTypeAttr(&pClassAttr)))
      {
        //printf("got TypeAttr\n");
        ATLASSERT(pClassAttr != NULL);
        ATLASSERT(pClassAttr->typekind == TKIND_COCLASS);

        // Search for typeinfo of the default events interface.
        int nFlags;
        HREFTYPE hRefType;

        for (unsigned int i = 0; i < pClassAttr->cImplTypes; i++)
        {
          if (SUCCEEDED(pClassInfo->GetImplTypeFlags(i, &nFlags)) &&
            ((nFlags & IMPLTYPE_MASK) == IMPLTYPE_DEFAULTSOURCE))
          {
            // Found it.  Now look at its attributes to get IID.
            LPTYPEINFO pEventInfo = NULL;
            if (SUCCEEDED(pClassInfo->GetRefTypeOfImplType(i,
                &hRefType)) &&
              SUCCEEDED(pClassInfo->GetRefTypeInfo(hRefType,
                &pEventInfo)))
            {
              ATLASSERT(pEventInfo != NULL);
              LPTYPEATTR pEventAttr;
              if (SUCCEEDED(pEventInfo->GetTypeAttr(&pEventAttr)))
              {
                ATLASSERT(pEventAttr != NULL);

                // create a mapping from dispid to string
                LoadNameCache(pEventInfo, pEventAttr,
                              mNames, mIDs, nmeth);

                *piid = pEventAttr->guid;
                pEventInfo->ReleaseTypeAttr(pEventAttr);
              }
              pEventInfo->Release();
            }
            break;
          }
        }
        pClassInfo->ReleaseTypeAttr(pClassAttr);
      }
      pClassInfo->Release();

  return (!IsEqualIID(*piid, GUID_NULL));
}

BOOL getClassInfoFromProgId(LPOLESTR bsProgId,LPTYPEINFO *pClassInfo)
{
  USES_CONVERSION;
  CLSID clsid;
  GUID libid;
  if (FAILED(CLSIDFromProgID(bsProgId, &clsid))) return false;
  if (FAILED(StringFromCLSID(clsid,&bsProgId))) return false;
  HKEY keySoftware, keyClasses, keyCLSID, keyXXXX, keyTypeLib;
  DWORD dwType, dwCountData=50;
  BYTE abData[50];
  LONG lVal;
  lVal = RegOpenKeyEx(HKEY_LOCAL_MACHINE,_T("SOFTWARE"),0,KEY_READ,&keySoftware);
  if (lVal==ERROR_SUCCESS) {
		lVal = RegOpenKeyEx(keySoftware,_T("Classes"),0,KEY_READ,&keyClasses);
		if (lVal==ERROR_SUCCESS) {
			lVal = RegOpenKeyEx(keyClasses,_T("CLSID"),0,KEY_READ,&keyCLSID);
			if (lVal==ERROR_SUCCESS) {
				_TCHAR *tsProgId = W2T(bsProgId);
				lVal = RegOpenKeyEx(keyCLSID,tsProgId,0,KEY_READ,&keyXXXX);
				if (lVal==ERROR_SUCCESS) {
					lVal = RegOpenKeyEx(keyXXXX,_T("TypeLib"),0,KEY_READ,&keyTypeLib);
					if (lVal==ERROR_SUCCESS) {
						lVal = RegQueryValueEx(keyTypeLib,NULL,NULL,&dwType,abData,&dwCountData);
						RegCloseKey(keyTypeLib);
				  }
					RegCloseKey(keyXXXX);
			  }
				RegCloseKey(keyCLSID);
			}
			RegCloseKey(keyClasses);
		}
		RegCloseKey(keySoftware);
  }
  if (lVal!=ERROR_SUCCESS) return false;
  BSTR bsLibId = A2BSTR((char*)abData);
  if (FAILED(CLSIDFromString(bsLibId,&libid))) return false;
  //Try loading from registry information.
  ITypeLib* pITypeLib;
  if (FAILED(LoadRegTypeLib(libid,1,0, LANG_NEUTRAL, &pITypeLib))) return false;
  //Find ITypeInfo for coclass.
  pITypeLib->GetTypeInfoOfGuid(clsid, pClassInfo);
  pITypeLib->Release();
  return true;
}

}



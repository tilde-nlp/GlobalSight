/**
 *  Copyright 2009 Welocalize, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.globalsight.everest.webapp.pagehandler.administration.cotijob;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.coti.COTIJobAllStatusSearcher;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * Handler class for COTI jobs page
 *
 */
public class CotiJobsMainHandler extends CotiJobsManagement
{
    private static final Logger logger = Logger
            .getLogger(CotiJobsMainHandler.class);
    private static final String BASE_BEAN = "allStatus";

    @Override
    public void myInvokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        // permission check
        HttpSession session = p_request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.COTI_JOB))
        {
            logger.error("User doesn't have the permission to create COTI jobs via this page.");
            p_response.sendRedirect("/globalsight/ControlServlet?");
            return;
        }
        // get the operator
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        
        setJobSearchFilters(sessionMgr, p_request, true);
        
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = p_request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }
        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, p_request,
                BASE_BEAN);
        COTIJobAllStatusSearcher searcher = new COTIJobAllStatusSearcher();
        searcher.setJobVos(p_request, true);

        ResourceBundle bundle = PageHandler.getBundle(session);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        p_request.setAttribute(JOB_ID, JOB_ID);
        p_request.setAttribute(JOB_LIST_START_PARAM,
                p_request.getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(
                PAGING_SCRIPTLET,
                getPagingText(p_request,
                        ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(),
                        null));

        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_thePageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }
}

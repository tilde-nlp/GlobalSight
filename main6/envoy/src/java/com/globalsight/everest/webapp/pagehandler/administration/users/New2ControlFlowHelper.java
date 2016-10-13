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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.calendar.CalendarManagerException;
import com.globalsight.util.modules.Modules;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Dispatches the user to the correct JSP.
 */
class New2ControlFlowHelper
    implements ControlFlowHelper, WebAppConstants 
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            New2ControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public New2ControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then 
     * returns the name of the link to follow
     * 
     * @return 
     * @exception EnvoyServletException
     */
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        HttpSession p_session = m_request.getSession(false);
        String destinationPage = null;

        // action should only be null for paging purposes
        if (m_request.getParameter(USER_ACTION) == null) 
        {
            destinationPage = "self";
        }
        else if (m_request.getParameter(USER_ACTION).equals(CANCEL)) 
        {
            destinationPage = "cancel";
        }
        else if (m_request.getParameter(USER_ACTION).equals(USER_ACTION_SET_RATE)) 
        {
            destinationPage = "setRate";
        }
        else if (m_request.getParameter(USER_ACTION).equals(USER_ACTION_SET_SOURCE)) 
        {
            destinationPage = "setSource";
        }
        else if (m_request.getParameter(USER_ACTION).equals(WebAppConstants.USER_ACTION_ADD_ANOTHER_LOCALES)) 
        {
            destinationPage = "addAnother";
        }
        else if (m_request.getParameter(USER_ACTION).equals(WebAppConstants.USER_ACTION_CREATE_USER)) 
        {
            destinationPage = "done";
        }
        else if (m_request.getParameter(USER_ACTION).equals("next"))
        {
            destinationPage = "next";
        }
        else if (m_request.getParameter(USER_ACTION).equals("previous")) 
        {
            if (Modules.isCalendaringInstalled())
            {
                destinationPage = "prev";
            }
            else
            {   
                destinationPage = "prevNoPM";
            }
        }

        return destinationPage;
    }

}

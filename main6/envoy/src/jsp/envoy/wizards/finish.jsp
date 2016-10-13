<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,         
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.foundation.User,
            java.util.ResourceBundle"
    session="true" %>

<%    
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("guide_finish_title");
    
    SessionManager sessionMgrGuide = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    User userGuide = (User)sessionMgrGuide.getAttribute(WebAppConstants.USER);
%>


<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
  var needWarning = false;
  var objectName = "";
  var guideNode = "finish";
  var helpFile = "<%=bundle.getString("help_main")%>";

</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
<TR>
<TD WIDTH=500>
<B><SCRIPT LANGUAGE="JAVASCRIPT">
       printf("<%= bundle.getString("guide_congratulations") %>",
              "<%= userGuide.getFirstName() %>");
   </SCRIPT></B>

<P>
<SCRIPT LANGUAGE="JAVASCRIPT">
    printf("<%= bundle.getString("guide_finished") %>", guideName());
</SCRIPT>

<P> 
<SCRIPT LANGUAGE="JAVASCRIPT">
    printf("<%= bundle.getString("guide_goto_myjob") %>",
       "<%=myJobsUrlGuide%>");
</SCRIPT>

<P>
<%= bundle.getString("guide_click_help") %>

<P>
<%= bundle.getString("guide_thanks") %>

</TD>
</TR>
</TABLE>
                         

  </DIV>
  </BODY>
</HTML>

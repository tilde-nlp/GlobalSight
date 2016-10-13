<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            java.util.*"
    session="true"
%>
<jsp:useBean id="content" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="targetMenu" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="preview" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previewXML" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previewPage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
String contentUrl = content.getPageURL();
int iViewMode = state.getLayout().getTargetViewMode();
String viewMode = "";
switch (iViewMode)
{
  case EditorConstants.VIEWMODE_PREVIEW: viewMode = "preview"; break;
  case EditorConstants.VIEWMODE_TEXT:    viewMode = "text"; break;
  case EditorConstants.VIEWMODE_DETAIL:  viewMode = "list"; break;
}
%>
<HTML>
<HEAD>
<SCRIPT>

var mode = "<%=viewMode%>";
var isVisible = false; //is progress bar visible

function showProgressBar()
{
   try
   {
	   var div = content.document.getElementById('tgt_prograssbar');
	   isVisible = div.style.visibility == "visible";
	   if (!isVisible)
       {
		   content.showProgressBar();
       }
   }
   catch(e)
   {
   }
}
function reloadContent(modeId)
{
  try
  {
    content.document.location =
      "<%=contentUrl%>" + "&trgViewMode=" + modeId;
  }catch(e)
  {
     document.location= "/globalsight/ControlServlet?linkName=pane2&pageName=ED3&trgViewMode=" + modeId;
  }
}

function showList()
{
    mode = "list";
    reloadContent(<%=EditorConstants.VIEWMODE_DETAIL%>+"&reuseData=target");
}

function showText()
{
    mode = "text";
    reloadContent(<%=EditorConstants.VIEWMODE_TEXT%>);
}

function showPreview()
{
    mode = "preview";
    reloadContent(<%=EditorConstants.VIEWMODE_PREVIEW%>);
}

function HighlightSegment(p_tuId, p_tuvId, p_subId)
{
    content.HighlightSegment(p_tuId, p_tuvId, p_subId);
}

function UnhighlightSegment(p_tuId, p_tuvId, p_subId)
{
    content.UnhighlightSegment(p_tuId, p_tuvId, p_subId);
}

function RefreshTargetPane()
{
    content.Refresh();
}

function showPDFPreview(pageName)
{
  try
  {
      if(!isVisible)
      {
         content.document.location =
           "<%=preview.getPageURL()%>" + "&action=previewTar&file=" + encodeURIComponent(pageName);
      }
  }catch(e)
  {
  }
}

function showXMLPreview(pageName)
{
  try
  {
    if(!isVisible)
    {
       content.document.location = "<%=previewXML.getPageURL()%>" + "&action=previewTar&file=" + pageName;
    }
  }catch(e)
  {
  }
}

function showPreviewPage(pageName)
{
  try
  {
    if(!isVisible)
    {
       content.document.location = "<%=previewPage.getPageURL()%>" + "&action=previewTar&file=" + pageName;
    }
  }catch(e)
  {
  }
}

function showPreviewPage2(pageName, type, pageId)
{
  try
  {
    if(!isVisible)
    {
       content.document.location = "<%=previewPage.getPageURL()%>" 
       + "&action=previewTar&file=" + encodeURIComponent(pageName) 
       + "&type=" + type
       + "&pageId=" + pageId
    }
  }catch(e)
  {
  }
}
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<FRAMESET ROWS="*,25" BORDER="0">
 <FRAME SRC="<%=contentUrl%>" NAME="content" SCROLLING="auto"
  NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
 <FRAME SRC="<%=targetMenu.getPageURL()%>" NAME="targetMenu" SCROLLING="no"
  NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
</HTML>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         session="true"
%>
<html>
<head>
<title><c:out value="${lb_filter_import}"/></title>
<STYLE type="text/css">
#idProgressContainer { border: solid 1px #0C1476; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; height:15px}
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px #0C1476; 
                 position: absolute; top: 42; left: 20; width: 0; height:15px}
</STYLE>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script type="text/javascript">
var guideNode = "filterConfiguration";
var helpFile = "<c:out value='${help_filter_configuration_import_screen}'/>";
function confirmJump()
{
	return true;
}

$(document).ready(function(){
	$("#okBtn").click(function(){
		document.location.href="/globalsight/ControlServlet?activityName=filterConfiguration";
	});
	$("#backBtn").click(function(){
		document.location.href="/globalsight/ControlServlet?linkName=imports&pageName=FC1&action=importFilter";
	});
	$("#refreshBtn").click(function(){
		$("#idProgressBar").stop(true);
		refreshBar();
	});
});

function doOnLoad() {
	loadGuides();
	startUpload();
	refreshBar();
}

function startUpload() {
	$.post("/globalsight/ControlServlet?pageName=FILTERIMPORT&linkName=startUpload&action=doImport");
}

function refreshBar() {
	if ($.browser.msie) {
		$("#idProgressBar").height(17);
	}
	$.get("/globalsight/ControlServlet?pageName=FILTERIMPORT&linkName=startUpload&action=refreshProgress", 
		{"no":Math.random()},
		function(data){
			var tmp = data.split("&");
			var percentage = tmp[0];
			var errorMsg = tmp[1];
			
			$("#msgBox").html(errorMsg);
			var wii = parseInt(percentage) / 100 * 400;
			$("#idProgressBar").animate({width : wii}, function(){
				$("#idProgress").html(percentage + "%");
				
				if (percentage == 100) {
					$("#okBtn").show();
					$("#refreshBtn").hide();
					$("#idPleaseWait").hide();
				} else {
					setTimeout('refreshBar()',1000);
				}
			});
		});
}
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body CLASS="standardText" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<SPAN CLASS="mainHeading" id="idHeading"><c:out value='${lb_processing_import_file}'/></SPAN><BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"><c:out value='${lb_please_wait}'/></SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
<tr><td height="50px">&nbsp;</td></tr>
</TABLE>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
<tr>
	<td id="msgBox" height="25px" style="padding-left:20px"></td>
</tr>
</TABLE>
<br><br>
<input type="button" id="okBtn" value="<c:out value='${lb_ok}'/>" style="display:none">
<input type="button" id="refreshBtn" value="<c:out value='${lb_refresh}'/>">
<input type="button" id="backBtn" value="<c:out value='${lb_back}'/>" >
</div>
</body>
</html>
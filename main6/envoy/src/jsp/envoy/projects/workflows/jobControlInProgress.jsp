<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.projecthandler.ProjectInfo,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.company.Company,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.foundation.User,
            com.globalsight.util.GlobalSightLocale,
            java.text.MessageFormat,
            java.util.Vector,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="allStatus" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="archived" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="complete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exported" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="progress" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="changeWfMgr" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ready" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="estimatedCompletionDate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="estimatedTranslateCompletionDate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="addJobToGroup" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-store");
	response.setDateHeader("Expires", 0);

    String DEFAULT_PARAM = "&jobListStart=0";
    Object param = request.getAttribute(JobManagementHandler.JOB_LIST_START_PARAM);
    String startIndex = param == null ? DEFAULT_PARAM : "&jobListStart="+param;
    String thisSearch = (String) request.getAttribute("searchType");
    if (thisSearch == null)
        thisSearch = (String) session.getAttribute("searchType");

    ResourceBundle bundle = PageHandler.getBundle(session);
    String archivedURL = archived.getPageURL()+ DEFAULT_PARAM;
    String pendingURL = pending.getPageURL()+ DEFAULT_PARAM;
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM + "&searchType=" + thisSearch;
    String completeURL = complete.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String allStatusURL = allStatus.getPageURL()+ DEFAULT_PARAM;
    String addJobToGroupURL = addJobToGroup.getPageURL()+ DEFAULT_PARAM;

    // WF3 is used for In Progress tab (in EnvoyConfig.xml)
    String changeWfMgrURL = changeWfMgr.getPageURL() + DEFAULT_PARAM +
           "&" + JobManagementHandler.MY_JOBS_TAB + "=WF3";
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM;
    String modifyURL = modify.getPageURL();
    String detailsURL = jobDetails.getPageURL();
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    String searchURL = search.getPageURL() + "&action=search";
    String downloadURL = download.getPageURL();
    String title = bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_inprogress");
    String lbPending= bundle.getString("lb_pending");
    String lbReady= bundle.getString("lb_ready");
    String lbInProgress= bundle.getString("lb_inprogress");
    String lbLocalized= bundle.getString("lb_localized");
    String lbExported= bundle.getString("lb_exported");
    String lbArchived= bundle.getString("lb_archived");
    Company company = (Company)request.getAttribute("company");
    boolean enableQAChecks = company.getEnableQAChecks();
	boolean showButton = true;
	if (company.getId() == 1)
	{
		showButton = false;
	}

	String refreshUrl = progressURL;
	boolean b_addDelete = false;
	boolean b_searchEnabled = false;
	try
	{
		SystemConfiguration sc = SystemConfiguration.getInstance();
		b_addDelete = sc
				.getBooleanParameter(SystemConfigParamNames.ADD_DELETE_ENABLED);
		b_searchEnabled = sc
				.getBooleanParameter(SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED);

	}
	catch (Exception ge)
	{
		// assume false
	}

	String helperText = bundle.getString("helper_text_job_inprogress");
	SessionManager sessMr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);
	String badresults = (String) sessMr
			.getMyjobsAttribute("badresults");
	if (badresults == null)
		badresults = "";
	sessMr.setMyjobsAttribute("badresults", "");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<%@ include file="/envoy/projects/workflows/myJobContextMenu.jspIncl" %>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_in_progress_tab")%>";
var jobActionParam = "";
var downloadCheck;
var startExportDate;
var exportEnd = false;
var exportDownloadRandom;
var exportFrom = "jobList";
var exportPercent = 0;
var w_updateLeverage = null;

function startExport()
{
	var jobId = getSelectJobId();
	
	var checkUrl = "${self.pageURL}&checkIsUploadingForExport=true&jobId=" + jobId + "&t=" + new Date().getTime();
    var isContinue = true;
    $.ajaxSetup({async: false}); 
    $.get(checkUrl,function(data){
        if (data == "exporting")
        {
            alert("<%=bundle.getString("msg_job_exporting")%>");
            isContinue =  false;
        }   
    });
    $.ajaxSetup({ async: true}); 

    if(!isContinue)
        return;
	
    var random = Math.random();
    exportDownloadRandom = Math.random();
    $.getJSON("/globalsight/TaskListServlet", {
        action:"checkUploadingStatus",
        state:8,
        jobId:jobId,
        exportFrom:exportFrom,
        random:random
    }, function(data) {
    	if(data.isUploading)
    	{
    		alert("The activities of the job are uploading. Please wait.");
    	}
    	else if (data.isExporting)
        {
    		alert("The job is uploading. Please wait.");
        }
    	else
    	{
    		$.getJSON("/globalsight/TaskListServlet", {
                action:"export",
                state:8,
                jobId:jobId,
                exportFrom:exportFrom,
                random:random
            }, function(data) {
            	startExportDate = data.startExportDate;
            	exportEnd = false;
            	exportPercent = 0;
            	if(downloadCheck != null)
            	{
            		clearInterval(downloadCheck);
            		downloadCheck = null;
            	}
            	showExportDownloadProgressDiv();
            });
    	}
    });
}

function doExportDownload()
{
	var jobId = getSelectJobId();
    var random = Math.random();
    var exportDownloadMessage = "";
	$.getJSON("/globalsight/TaskListServlet", {
        action:"download",
        state:8,
        jobId:jobId,
        startExportDate:startExportDate,
        exportDownloadRandom:exportDownloadRandom,
        exportFrom:exportFrom,
        random:random
    }, function(data) {
    	if(!exportEnd)
	    {
	    	if(data.selectFiles != "")
	    	{
	    		exportEnd = true;
	    		clearInterval(downloadCheck);
	    		downloadCheck = null;
	    		var selectedFiles = "";
	    		$("#selectedFileList").val(selectedFiles);
	    		$.each(data.selectFiles, function(i, item) {
	    			item = encodeURIComponent(item.replace(/%C2%A0/g, "%20"));
	    			selectedFiles += ("," + item);
	    		});
	    		selectedFiles = selectedFiles.substring(1,selectedFiles.length);
	    		$("#selectedFileList").val(selectedFiles);
	    		downloadFilesForm.action = "/globalsight/ControlServlet?linkName=downloadApplet&pageName=CUST_FILE_Download&action=download&taskId="+null+"&state=8&isChecked="+false;
	    		downloadFilesForm.submit();
	    	}
	    	if(data.percent == 100 && data.selectFiles != "")
    		{
    			exportDownloadMessage = "Finish export. Start download."
    			showExportDownloadProgress("", data.percent, exportDownloadMessage);
    			exportPercent = 0;
    		}
    		if(exportPercent < data.percent && data.percent < 100 )
    		{
    			exportPercent = data.percent;
    			showExportDownloadProgress("", data.percent, exportDownloadMessage);
    		}
		}
    });
}

function showExportDownloadProgressDiv()
{
	if(downloadCheck == null)
	{
	    idExportDownloadMessagesDownload.innerHTML = "";
	    document.getElementById("idExportDownloadProgressDownload").innerHTML = "0%"
	    document.getElementById("idExportDownloadProgressBarDownload").style.width = 0;
	    document.getElementById("idExportDownloadProgressDivDownload").style.display = "";
	    showExportDownloadProgress("", 0 , "Start Export...");
	    downloadCheck = window.setInterval("doExportDownload()", 2000);
	}
}


function getSelectJobId()
{
	var jobId = "";
	var dtpIndexes = dtpSelectedIndex();
	var transIndexes = transSelectedIndex();

	   // If more than one radio button is displayed, loop
	   // through the array to find the one checked
	   // Note that valuesArray[1], the jobState is not used in this jsp page.
	   if (transIndexes.length > 0)
	   {
		   if (JobForm.transCheckbox.length)
		   {
		      for (var i = 0; i < JobForm.transCheckbox.length; i++)
		      {
		         if (JobForm.transCheckbox[i].checked == true)
		         {
		            if( jobId != "" )
		            {
		               jobId += " "; // must add a [white space] delimiter
		            }
		            valuesArray = getRadioValuesConGroupId(JobForm.transCheckbox[i].value);
		            jobId += valuesArray[0];
		         }
		      }
		   }
		   // If only one radio button is displayed, there is no radio button array, so
		   // just check if the single radio button is checked
		   else
		   {
		      if (JobForm.transCheckbox.checked == true)
		      {
		         valuesArray = getRadioValuesConGroupId(JobForm.transCheckbox.value);
		         jobId += valuesArray[0];
		      }
		   }
	   }
	   if (dtpIndexes.length > 0)
	   {
		   if (JobForm.dtpCheckbox.length)
		   {
		      for (var i = 0; i < JobForm.dtpCheckbox.length; i++)
		      {
		         if (JobForm.dtpCheckbox[i].checked == true)
		         {
		            if( jobId != "" )
		            {
		               jobId += " "; // must add a [white space] delimiter
		            }
		            valuesArray = getRadioValuesConGroupId(JobForm.dtpCheckbox[i].value);
		            jobId += valuesArray[0];
		         }
		      }
		   }
		   // If only one radio button is displayed, there is no radio button array, so
		   // just check if the single radio button is checked
		   else
		   {
		      if (JobForm.dtpCheckbox.checked == true)
		      {
		         valuesArray = getRadioValuesConGroupId(JobForm.dtpCheckbox.value);
		         jobId += valuesArray[0];
		      }
		   }
	   }
	   
	   if (JobForm.jobIdHidden && JobForm.jobIdHidden.length)
	   {
	      for (i = 0; i < JobForm.jobIdHidden.length; i++)
	      {
	         if (JobForm.jobIdHidden[i].checked == true)
	         {
	            if( jobId != "" )
	            {
	               jobId += " "; // must add a [white space] delimiter
	            }
	            valuesArray = getRadioValuesConGroupId(JobForm.jobIdHidden[i].value);
	            jobId += valuesArray[0];
	         }
	       }
	    }
	   return jobId;
}

function loadPage()
{

   // Only show the download button if something is available to download
   if (JobForm.transCheckbox || JobForm.dtpCheckbox)
   {
       document.all.ButtonLayer.style.visibility = "visible";
       document.all.CheckAllLayer.style.visibility = "visible";
   }
   // Load the Guide
   loadGuides();
   
   ContextMenu.intializeContextMenu();
}

function dtpSelectedIndex()
{
   var dtpSelectedIndex = new Array();
   
      var dtpCheckboxes = JobForm.dtpCheckbox;
		if (dtpCheckboxes != null) {
			if (dtpCheckboxes.length) {
				for (var i = 0; i < dtpCheckboxes.length; i++) {
					var checkbox = dtpCheckboxes[i];
					if (checkbox.checked) {
						dtpSelectedIndex.push(i);
					}
				}
			} else {
				if (dtpCheckboxes.checked) {
					dtpSelectedIndex.push(0);
				}
			}
		}
	return dtpSelectedIndex;
}

function transSelectedIndex() 
{
	var transSelectedIndex = new Array();
		
	var transCheckboxes = JobForm.transCheckbox;
	if (transCheckboxes != null) {
		if (transCheckboxes.length) {
			for (var i = 0; i < transCheckboxes.length; i++) {
				var checkbox = transCheckboxes[i];
				if (checkbox.checked) {
					transSelectedIndex.push(i);
				}
			}
		} else {
			if (transCheckboxes.checked) {
				transSelectedIndex.push(0);
			}
		}
	}
	return transSelectedIndex;
}

function updateButtonState(transSelectedIndex, dtpSelectedIndex)
{
   if (transSelectedIndex.length == 0 && dtpSelectedIndex.length == 1)
   {
      if (document.JobForm.ChangeWFMgr)
          document.JobForm.ChangeWFMgr.disabled = false;
      if (document.JobForm.Export)
      {
          document.JobForm.Export.disabled = false;
          document.JobForm.Export.value = "<%=bundle.getString("lb_move_to_dtp")%>...";
      }
      if (document.JobForm.ExportDownload)
          document.JobForm.ExportDownload.disabled = false;
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = false;
<% } %>
   }
   else if (transSelectedIndex.length == 1 && dtpSelectedIndex.length == 0)
   {
      if (document.JobForm.ChangeWFMgr)
          document.JobForm.ChangeWFMgr.disabled = false;
      if (document.JobForm.Export)
      {
          document.JobForm.Export.disabled = false;
          document.JobForm.Export.value = "<%=bundle.getString("lb_export")%>...";
      }
      if (document.JobForm.ExportDownload)
          document.JobForm.ExportDownload.disabled = false;
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = false;
<% } %>
   }
   else
   {
      if (document.JobForm.ChangeWFMgr)
          document.JobForm.ChangeWFMgr.disabled = true;
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = true;
      if (document.JobForm.ExportDownload)
          document.JobForm.ExportDownload.disabled = true;
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = true;
<% } %>
   }
}

function setButtonState()
{
   updateButtonState(transSelectedIndex(), dtpSelectedIndex());
}

function ShowStatusMessage(p_msg)
{
    if (document.layers)
    {
        document.menu.document.statusMessage.innerHTML = p_msg;
    }
    else
    {
       statusMessage.innerHTML = p_msg;
    }
}

function submitForm(buttonClicked, curJobId)
{
   var dtpIndexes = dtpSelectedIndex();
   var transIndexes = transSelectedIndex();

   if (dtpIndexes.length == 0 && transIndexes.length == 0 && typeof curJobId == "undefined")
   {
      alert ("<%= bundle.getString("jsmsg_please_select_a_row") %>");
      return false;
   }

   if(buttonClicked == "ExportForUpdate")
   {
      if (!confirm("<%=bundle.getString("jsmsg_warning")%>\n" +
          "<%=bundle.getString("jsmsg_export_source_warning")%>"))
      {
         return false;
      }
   }
   var valuesArray;
   var jobId = "";

   // If more than one radio button is displayed, loop
   // through the array to find the one checked
   // Note that valuesArray[1], the jobState is not used in this jsp page.
   if (transIndexes.length > 0)
   {
	   if (JobForm.transCheckbox.length)
	   {
	      for (var i = 0; i < JobForm.transCheckbox.length; i++)
	      {
	         if (JobForm.transCheckbox[i].checked == true)
	         {
	            if( jobId != "" )
	            {
	               jobId += " "; // must add a [white space] delimiter
	            }
	            valuesArray = getRadioValuesConGroupId(JobForm.transCheckbox[i].value);
	            jobId += valuesArray[0];
	         }
	      }
	   }
	   // If only one radio button is displayed, there is no radio button array, so
	   // just check if the single radio button is checked
	   else
	   {
	      if (JobForm.transCheckbox.checked == true)
	      {
	         valuesArray = getRadioValuesConGroupId(JobForm.transCheckbox.value);
	         jobId += valuesArray[0];
	      }
	   }
   }
   if (dtpIndexes.length > 0)
   {
	   if (JobForm.dtpCheckbox.length)
	   {
	      for (var i = 0; i < JobForm.dtpCheckbox.length; i++)
	      {
	         if (JobForm.dtpCheckbox[i].checked == true)
	         {
	            if( jobId != "" )
	            {
	               jobId += " "; // must add a [white space] delimiter
	            }
	            valuesArray = getRadioValuesConGroupId(JobForm.dtpCheckbox[i].value);
	            jobId += valuesArray[0];
	         }
	      }
	   }
	   // If only one radio button is displayed, there is no radio button array, so
	   // just check if the single radio button is checked
	   else
	   {
	      if (JobForm.dtpCheckbox.checked == true)
	      {
	         valuesArray = getRadioValuesConGroupId(JobForm.dtpCheckbox.value);
	         jobId += valuesArray[0];
	      }
	   }
   }
   
   if (JobForm.jobIdHidden && JobForm.jobIdHidden.length)
   {
      for (i = 0; i < JobForm.jobIdHidden.length; i++)
      {
         if (JobForm.jobIdHidden[i].checked == true)
         {
            if( jobId != "" )
            {
               jobId += " "; // must add a [white space] delimiter
            }
            valuesArray = getRadioValuesConGroupId(JobForm.jobIdHidden[i].value);
            jobId += valuesArray[0];
         }
       }
    }

   if (buttonClicked == "Export")
   {
	 	var checkUrl = "${self.pageURL}&checkIsUploadingForExport=true&jobId=" + jobId + "&t=" + new Date().getTime();
		var isContinue = true;
		$.ajaxSetup({async: false}); 
		$.get(checkUrl,function(data){
			if(data == "uploading")
			{
				alert("The job is uploading. Please wait.");
				isContinue =  false;
			}
			else if (data == "exporting")
			{
				alert("<%=bundle.getString("msg_job_exporting")%>");
                isContinue =  false;
			}	
		});
		$.ajaxSetup({ async: true}); 
	
		if(!isContinue)
			return false;
		  
		ShowStatusMessage("<%=bundle.getString("jsmsg_preparing_for_export")%>");
	    JobForm.action = "<%=request.getAttribute(JobManagementHandler.EXPORT_URL_PARAM)%>";
	    jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
	    JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=<%=thisSearch%>";
	    JobForm.submit();
	    return;
   }
   else if(buttonClicked == "ExportForUpdate")
   {
	   	ShowStatusMessage("<%=bundle.getString("jsmsg_preparing_for_export")%>");
	    JobForm.action = "<%=request.getAttribute(JobManagementHandler.EXPORT_URL_PARAM)%>";
	    jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
	    JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=<%=thisSearch%>";
	    JobForm.action += "&" + "<%=JobManagementHandler.EXPORT_FOR_UPDATE_PARAM%>" + "=true";
	    JobForm.submit();
	    return;
   }
   else if (buttonClicked == "Discard")
   {
      if ( !confirm("<%=bundle.getString("jsmsg_warning")%>\n\n" +
                    "<%=bundle.getString("jsmsg_discard_job")%>"))
      {
         return false;
      };

      ShowStatusMessage("<%=bundle.getString("jsmsg_discarding_selected_jobs")%>")
      JobForm.action = "<%=refreshUrl%>";
      jobActionParam = "<%=JobManagementHandler.DISCARD_JOB_PARAM%>";
      JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=<%=thisSearch%>";
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "changeWFMgr")
   {
      JobForm.action = "<%=changeWfMgrURL%>";
      jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
      JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=<%=thisSearch%>";
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "search")
   {
      JobForm.action = "<%=searchURL%>";
      jobActionParam = "search";
      JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=<%=thisSearch%>";
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "Download")
   {
      $("#downloadJobIds").val(jobId);
      JobForm.action = "<%=downloadURL%>&firstEntry=true"
                        + "&<%=DownloadFileHandler.DOWNLOAD_FROM_JOB%>=true";
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "estimatedTranslateCompletionDate")
   {
	   if (jobId == "" || jobId == " ")
	   {
		   jobId = curJobId;
	   }
	   $("#checkedJobIds").val(jobId);
	   JobForm.action = "${estimatedTranslateCompletionDate.pageURL}&jobId=" + curJobId + "&from=jobInProgress";
	   JobForm.submit();
	   return;
   }
   else if (buttonClicked == "estimatedCompletionDate")
   {
	   if (jobId == "" || jobId == " ")
	   {
		   jobId = curJobId;
	   }
	   $("#checkedJobIds").val(jobId);
	   JobForm.action = "${estimatedCompletionDate.pageURL}&jobId=" + curJobId + "&from=jobInProgress";
	   JobForm.submit();
	   return;
   }
   else if(buttonClicked == "downloadQAReport")
   {
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=refreshUrl%>&action=checkDownloadQAReport",
		   data: "jobIds="+jobId,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.download == "fail")
	          {
	   			alert("<%=bundle.getString("lb_download_qa_reports_message")%>");
	          }
	          else if(returnData.download == "success")
	          {
		       	  JobForm.action = "<%=refreshUrl%>"+"&action=downloadQAReport";
		    	  JobForm.submit();
	          }
		   },
	   	   error:function(error)
	       {
          		alert(error.message);
           }
		});
   }
}

//for GBS-2599
function handleSelectAll() {
	if (JobForm && JobForm.selectAll) {
		if (JobForm.selectAll.checked) {
			checkAllWithName('JobForm', 'transCheckbox');
			setButtonState();
	    }
	    else {
			clearAll('JobForm'); 
			setButtonState();
	    }
	}
}

function searchJob(fromRequest)
{
	var baseUrl = "";
	var state = $("#sto").val();
	if(state =="PENDING")
		baseUrl = "<%=pendingURL%>";
	else if(state =="READY_TO_BE_DISPATCHED")
		baseUrl = "<%=readyURL%>";
	else if(state =="DISPATCHED")
		baseUrl = "<%=progressURL%>"  + "&fromRequest=true";
	else if(state =="LOCALIZED")
		baseUrl = "<%=completeURL%>";
	else if(state =="EXPORTED")
		baseUrl = "<%=exportedURL%>";
	else if(state =="ARCHIVED")
		baseUrl = "<%=archivedURL%>";
	else if(state =="ALL_STATUS")
		baseUrl = "<%=allStatusURL%>";

	if(fromRequest && state != "DISPATCHED")
	{
		window.location = baseUrl+"&fromRequest=true"+"&sto="+$("#sto").val()
		+"&csf="+$("#creationStartFilter").val()+"&cso="+$("#creationStartOptionsFilter").val()
		+"&cef="+$("#creationEndFilter").val()+"&ceo="+$("#creationEndOptionsFilter").val()
		+"&esf="+$("#completionStartFilter").val()+"&eso="+$("#completionStartOptionsFilter").val()
		+"&eef="+$("#completionEndFilter").val()+"&eeo="+$("#completionEndOptionsFilter").val()
		+"&edss="+$("#exportDateStartFilter").val()+"&edso="+$("#exportDateStartOptionsFilter").val()
		+"&edee="+$("#exportDateEndFilter").val()+"&edes="+$("#exportDateEndOptionsFilter").val()
		+"&advancedSearch="+advancedSearch;
	}
	else
	{
		window.location = baseUrl
			+ "&sto="+$("#sto").val()+"&nf="+$("#jobNameFilter").val()
			+"&idf="+$("#jobIdFilter").val()+"&idg="+$("#jobGroupIdFilter").val()+"&io="+$("#jobIdOption").val()+"&po="+$("#jobProjectFilter").val()
			+"&sl="+$("#sourceLocaleFilter").val()+"&npp="+$("#numPerPage").val()+"&pro="+$("#priorityFilter").val()
			+"&csf="+$("#creationStartFilter").val()+"&cso="+$("#creationStartOptionsFilter").val()
			+"&cef="+$("#creationEndFilter").val()+"&ceo="+$("#creationEndOptionsFilter").val()
			+"&esf="+$("#completionStartFilter").val()+"&eso="+$("#completionStartOptionsFilter").val()
			+"&eef="+$("#completionEndFilter").val()+"&eeo="+$("#completionEndOptionsFilter").val()
			+"&edss="+$("#exportDateStartFilter").val()+"&edso="+$("#exportDateStartOptionsFilter").val()
			+"&edee="+$("#exportDateEndFilter").val()+"&edes="+$("#exportDateEndOptionsFilter").val()
			+"&advancedSearch="+advancedSearch;
	}
}

var jobGroupId = "";
var jobId = "";
function addJobToGroup()
{
   var transIndexes = transSelectedIndex();
   
   if (transIndexes.length == 0)
   {
      alert ("<%= bundle.getString("jsmsg_please_select_a_row") %>");
      return false;
   }
   //get select jobId and groupId
   getJobIdsAndGroupIds(transIndexes);
   
   var jobGroupIdArr = jobGroupId.split(",");
   var jobIdArr = jobId.split(",");
   jobGroupId="";
   jobId="";
   var newJobIds = "";
   var jobIdsInGroup = "";
   for(var i=0;i<jobGroupIdArr.length;i++)
   {
	   if(jobGroupIdArr[i] != null && jobGroupIdArr[i] != "")
	   {
		   if(jobIdsInGroup != ""){
			   jobIdsInGroup += ",";
		   }
		   jobIdsInGroup += jobIdArr[i];
	   }
	   else
	   {
		   if(newJobIds != ""){
			   newJobIds += ",";
		   }
			newJobIds += jobIdArr[i];
	   }
   }

   if(jobIdsInGroup != "")
   {
	  var message = "The jobs ("+jobIdsInGroup+") have been in job group, please reselect!";
	  alert(message);
	  return;
   }

   var url = "<%=addJobToGroupURL%>&action=addJobToGroup&pageState=inprogress&jobIds="+newJobIds;
   window.open(url, "UpdateLeverage", "height=400,width=400,top=300,left=400,resizable=no,scrollbars=no");
}

function removeJobFromGroup()
{
	var transIndexes = transSelectedIndex();
	   
    if (transIndexes.length == 0)
    {
       alert ("<%= bundle.getString("jsmsg_please_select_a_row") %>");
       return false;
    }
    //get select jobId and groupId
	getJobIdsAndGroupIds(transIndexes);
	   
   var jobGroupIdArr = jobGroupId.split(",");
   var jobIdArr = jobId.split(",");
   jobGroupId="";
   jobId="";
   var newJobIds = "";
   for(var i=0;i<jobIdArr.length;i++)
   {
	   if(jobGroupIdArr[i] != null && jobGroupIdArr[i] != "")
	   {
		   if(newJobIds != "")
		   {
			   newJobIds += ",";
		   }
			newJobIds += jobIdArr[i];
	   }
   }
   
   var url = "<%=selfURL%>&action=removeJobFromGroup&jobIds="+newJobIds;
   window.location.href = url;
}

function getJobIdsAndGroupIds(transIndexes)
{
   var valuesArray;
   if (transIndexes.length > 0)
   {
	   if (JobForm.transCheckbox.length)
	   {
	      for (var i = 0; i < JobForm.transCheckbox.length; i++)
	      {
	         if (JobForm.transCheckbox[i].checked == true)
	         {
	            if( jobGroupId != "" || jobId != "")
	            {
	            	jobGroupId += ","; // must add a [white space] delimiter
	            	jobId += ",";
	            }
	            valuesArray = getRadioValuesConGroupId(JobForm.transCheckbox[i].value);
	            
	            jobId += valuesArray[0];
	            jobGroupId += valuesArray[2];
	         }
	      }
	   }
	   else
	   {
	      if (JobForm.transCheckbox.checked == true)
	      {
	         valuesArray = getRadioValuesConGroupId(JobForm.transCheckbox.value);
	         jobId += valuesArray[0];
	         jobGroupId += valuesArray[2];
	      }
	   }
   }
}
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/projects/workflows/jobSort.jspIncl" %>
<STYLE>
<%--
This stylesheet should be in the HEAD element but the skin bean
is defined in header.jspIncl which must be included in the body.
--%>
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
.headerCell {
    padding-right: 10px;
    padding-top: 2px;
    padding-bottom: 2px;
}
</STYLE>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR VALIGN="TOP">
        <TD COLSPAN=2>
            <SPAN CLASS="mainHeading">
            <%=title%>
            </SPAN>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText>    
        <TD>
        <br>
        <%=helperText%>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText> 
        <TD>
        <span style="color:red"><%=badresults%></span>
        </TD>
    </TR>
</TABLE>
<%@ include file="miniSearch.jspIncl" %>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
<TR><TD>
    <DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
    <%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>
    </DIV>
</TD></TR>

<TR><TD>
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
<FORM METHOD="post" NAME="downloadFilesForm" style="display:none">
<INPUT NAME="fileAction" VALUE="download" TYPE="HIDDEN">
<INPUT ID="selectedFileList" NAME="selectedFileList" VALUE="" TYPE="HIDDEN">
</FORM>
<FORM NAME="JobForm" METHOD="POST">
<input type="hidden" id="downloadJobIds" name="<%=DownloadFileHandler.PARAM_JOB_ID%>" value=""/>
<input type="hidden" id="checkedJobIds" name="checkedJobIds" value=""/>
    <TR>
        <TD COLSPAN=3>

<!-- Data Table  -->
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" id="list" CLASS="list" width="100%">
<COL> <!-- Radio button -->
<COL> <!-- Priority -->
<COL> <!-- Job ID -->
<COL WIDTH=130> <!-- Job Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Word Count -->
<COL> <!-- Date Created -->
<COL> <!-- Est Completion Created -->
<thead>
<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell" WIDTH="1%"><input type="checkbox" onclick="handleSelectAll()" id="selectAll" name="selectAll"/></TD>
    <TD CLASS="headerCell" WIDTH="1%"><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>" onclick="return addFilters(this)"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
    <amb:permission name="<%=Permission.JOBS_GROUP%>" >
    <TD CLASS="headerCell" WIDTH="1%"><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_GROUP_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_group_id")%></A><%=jobGroupIdSortArrow%></TD>
    </amb:permission>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>" onclick="return addFilters(this)"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.EST_TRANSLATE_COMPLETION_DATE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_estimated_translate_completion_date")%></A><%=jobEstTranslateCompletionDateSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=progressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.EST_COMPLETION_DATE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_estimated_workflow_completion_date")%></A><%=jobEstCompletionDateSortArrow%></TD>
</TR>
<TR CLASS="tableHeadingFilter" VALIGN="BOTTOM">
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">
    	<select id="priorityFilter" class="filterSelect">
	        <option value='-1'></option>
	        <option value='1'>1</option>
	        <option value='2'>2</option>
	        <option value='3'>3</option>
	        <option value='4'>4</option>
	        <option value='5'>5</option>
        </select>
    </TD>
    <amb:permission name="<%=Permission.JOBS_GROUP%>" >
    <TD CLASS="headerCell"  style="" nowrap>
    	<input class="standardText" style="width:80px" type="text" id="jobGroupIdFilter" name="jobGroupIdFilter" value="<%=jobGroupIdFilter%>"/>
    </TD>
    </amb:permission>
    <TD CLASS="headerCell" style="width:150px" nowrap>
    	<select id="jobIdOption">
	        <option value='<%=SearchCriteriaParameters.EQUALS%>'>=</option>
	        <option value='<%=SearchCriteriaParameters.GREATER_THAN%>'>&gt;</option>
	        <option value='<%=SearchCriteriaParameters.LESS_THAN%>'>&lt;</option>
        </select>
    	<input class="standardText" style="width:80px" type="text" id="jobIdFilter" name="jobIdFilter" value="<%=jobIdFilter %>"/>
    </TD>
    <TD CLASS="headerCell"><input class="standardText" type="text" id="jobNameFilter" name="jobNameFilter" value="<%=jobNameFilter %>"/></TD>
    <TD CLASS="headerCell">
    <select name="<%=JobSearchConstants.PROJECT_OPTIONS%>" id="jobProjectFilter" class="filterSelect">
        <option value="-1">Choose...</option>
	    <%
			if (projects != null)
			{
			    for (int i=0; i < projects.size(); i++)
			    {
			        ProjectInfo p = (ProjectInfo)projects.get(i);
			        String projectName = p.getName();
			        long projectId = p.getProjectId();
			        String option = "<option value='" + projectId + "'>" +projectName + "</option>";
			        out.println(option);
			    }
			}
		%>
	</select>
    </TD>
    <TD CLASS="headerCell">
    <select name="<%=JobSearchConstants.SRC_LOCALE%>" class="standardText filterSelect" id="sourceLocaleFilter">
		<option value="-1">Choose...</option>
	    <%
		if (srcLocales != null)
		{
		    for (int i = 0; i < srcLocales.size();  i++)
		    {
		        GlobalSightLocale locale = (GlobalSightLocale)srcLocales.get(i);
		        String disp = locale.getDisplayName(uiLocale);
		        long lpId = locale.getId();
		        String option = "<option value='" + lpId + "'>" + disp + "</option>";
		        out.println(option);
		    }
		}
		%>
	</select>
    </TD>
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">&nbsp;</TD>
</TR>
</thead>
<tbody>
<c:forEach items="${jobVos}" var="jobVo" varStatus="i">
    <TR VALIGN=TOP STYLE="padding-top: 5px; padding-bottom: 5px;" BGCOLOR="#FFFFFF" CLASS=standardText>
    <TD><INPUT onclick="setButtonState()" TYPE=checkbox NAME=transCheckbox VALUE="jobId=${jobVo.id}&jobState=${jobVo.statues}&jobGroupId=${jobVo.groupId}"></TD>
	<TD CLASS=standardText >${jobVo.priority}</TD>
	<amb:permission name="<%=Permission.JOBS_GROUP%>" >
	<TD CLASS=standardText style="text-align: center;">${jobVo.groupId}</TD>
	</amb:permission>
	<TD CLASS=standardText style="text-align: center;">${jobVo.id}</TD>
	<TD CLASS=standardText style="word-break:break-all" >	
	    <SCRIPT language="javascript">
	    if (navigator.userAgent.indexOf('Firefox') >= 0){
		    document.write("<DIV>");
		    }</SCRIPT>
		    <c:choose>
		    <c:when  test="${jobVo.hasDetail}">
		<B><A  CLASS="${fn:replace(jobVo.textType,"Text","HREF")}"  HREF="/globalsight/ControlServlet?linkName=jobDetails&pageName=ALLS&jobId=${jobVo.id}&fromJobs=true" oncontextmenu="contextForTab('${jobVo.id}',event)">${jobVo.name}</A></B>
		    </c:when >
		    <c:otherwise>${jobVo.name}</c:otherwise>
		    </c:choose>
		<SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("</DIV>")}</SCRIPT></TD>	 
	<TD CLASS=${jobVo.textType} >${jobVo.project}</TD>
	<TD CLASS=${jobVo.textType} >${jobVo.sourceLocale}</TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >${jobVo.wordcount}</TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >${jobVo.createDate}</TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >
		<amb:permission name="<%=Permission.JOBS_ESTIMATEDTRANSLATECOMPDATE%>">
			 <a class="standardHREF" style="word-wrap:break-word;word-break:break-all;" href="#" onclick="submitForm('estimatedTranslateCompletionDate', '${jobVo.id}')">
		</amb:permission>
	    ${jobVo.estimatedTranslateCompletionDate}
		<amb:permission name="<%=Permission.JOBS_ESTIMATEDTRANSLATECOMPDATE%>">
			 </a>
		</amb:permission>
    </TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >
	    <amb:permission name="<%=Permission.JOBS_ESTIMATEDCOMPDATE%>">
			 <a class="standardHREF" style="word-wrap:break-word;word-break:break-all;" href="#" onclick="submitForm('estimatedCompletionDate', '${jobVo.id}')">
		</amb:permission>
	    ${jobVo.plannedCompletionDate}
		<amb:permission name="<%=Permission.JOBS_ESTIMATEDCOMPDATE%>">
			 </a>
		</amb:permission>
	</TD>
    </TR>
</c:forEach>
</tbody>
<tr><td><div id='restofjobs' style="display:none">
<c:forEach items="${otherJobIds}" var="otherJobId" varStatus="i">
<input type='checkbox' name=jobIdHidden value="jobId=${otherJobId}&jobState=notused">
</c:forEach>
</div></td></tr>
</TABLE>
<!-- End Data Table  -->
        </TD>
     </TR>
     <TR>
        <TD CLASS="standardText">
            <DIV ID="CheckAllLayer"  ALIGN="RIGHT" style="padding-top: 5px">
            	Display #:
		        <select id="numPerPage" class="filterSelect">
		           <option value="10">10</option>
		           <option value="20">20</option>
		           <option value="50">50</option>
                   <option value="100">100</option>
		           <option value="200">200</option>
		        </select>
		        &nbsp;
                <%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>
            </DIV>
         </TD>
     </TR>
</TABLE>
</TD></TR>
<TR><TD>
<DIV ID="ButtonLayer" ALIGN="LEFT" STYLE="visibility: hidden">
<br>
<% if (b_searchEnabled) { %>
    <amb:permission name="<%=Permission.JOBS_SEARCH_REPLACE%>" >
        <INPUT TYPE="BUTTON" NAME=search VALUE="<%=bundle.getString("lb_search_replace")%>..." onClick="submitForm('search');">
    </amb:permission>
<% } %>
    <amb:permission name="<%=Permission.JOBS_CHANGE_WFM%>" >
        <INPUT TYPE="BUTTON" NAME=ChangeWFMgr VALUE="<%=bundle.getString("lb_change_workflow_manager")%>..." onClick="submitForm('changeWFMgr');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DISCARD%>" >
            <INPUT TYPE="BUTTON" NAME=Discard VALUE="<%=bundle.getString("lb_discard")%>" onClick="submitForm('Discard');">
    </amb:permission>
<% if (b_addDelete) { %>
    <amb:permission name="<%=Permission.JOBS_EXPORT_SOURCE%>" >
        <INPUT TYPE="BUTTON" NAME=ExportForUpdate VALUE="<%=bundle.getString("lb_export_source")%>..." onClick="submitForm('ExportForUpdate');">
    </amb:permission>
<% } %>
    <amb:permission name="<%=Permission.JOBS_EXPORT%>" >
        <INPUT TYPE="BUTTON" NAME=Export VALUE="<%=bundle.getString("lb_export")%>..." onClick="submitForm('Export');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DOWNLOAD%>" >
        <INPUT TYPE="BUTTON" NAME=Download VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_EXPORT_DOWNLOAD%>" >
        <INPUT TYPE="BUTTON" NAME=ExportDownload VALUE="<%=bundle.getString("lb_export_download")%>..." onClick="startExport()">
    </amb:permission>
     <amb:permission name="<%=Permission.JOBS_ADDJOBTOGROUP%>" >
  		<INPUT TYPE="BUTTON" NAME=search VALUE="<%=bundle.getString("lb_add_job_to_group")%>..." onClick="addJobToGroup();">
	</amb:permission>
	<amb:permission name="<%=Permission.JOBS_REMOVEJOBFROMGROUP%>" >
  		<INPUT TYPE="BUTTON" NAME=search VALUE="<%=bundle.getString("lb_remove_job_from_group")%>" onClick="removeJobFromGroup();">
	</amb:permission>
	<%if(enableQAChecks && showButton){ %>
  		<INPUT TYPE="BUTTON" NAME=downloadQAReport VALUE="<%=bundle.getString("lb_download_qa_reports")%>" onClick="submitForm('downloadQAReport')">
    <% } %>
</DIV>
<P id="statusMessage" CLASS="standardText" >&nbsp;</P>
<span id="exportdownload_progress_content">
    <div id="idExportDownloadProgressDivDownload"
         style='border-style: solid; border-width: 1pt; border-color: #0c1476; background-color: white; display:none; left: 300px; height: 370; width: 500px; position: absolute; top: 150px; z-index: 21'>
        <%@ include file="/envoy/tasks/exportDownloadProgressIncl.jsp" %>
    </div>
</span>
</DIV>
</TD></TR>
</FORM>
</TABLE>
</BODY>
</HTML>

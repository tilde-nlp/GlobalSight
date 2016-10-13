<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
         com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
         com.globalsight.everest.foundation.SearchCriteriaParameters,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.projecthandler.Project,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.everest.costing.Currency"
          session="true"
%>
<%  String EMEA = CompanyWrapper.getCurrentCompanyName();
    ResourceBundle bundle = PageHandler.getBundle(session);

    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null) {
        uiLocale = Locale.US;
    }

    String creationStart = JobSearchConstants.CREATION_START;
    String creationEnd = JobSearchConstants.CREATION_END;
    
    List<ReportJobInfo> jobList = (ArrayList<ReportJobInfo>)
        request.getAttribute(ReportConstants.REPORTJOBINFO_LIST);
    List<Project> projectList = (ArrayList<Project>)
        request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
        request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
%>
<html>
<!-- This JSP is: /envoy/administration/reports/vendorPOXlsReportWebForm.jsp-->
<head>
<title><%= EMEA%> <%=bundle.getString("vendor_po_report_web_form")%></title>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<SCRIPT LANGUAGE="JAVASCRIPT">
$(document).ready(function(){
	$("#csf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#cef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#cef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#csf").datepicker( "option", "maxDate", selectedDate );
		}
	});
});

function submitForm()
{
    searchForm.submit();
}
</script>
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%= EMEA%> <%=bundle.getString("vendor_po_report_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="/globalsight/envoy/administration/reports/vendorPOXlsReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">

    <tr>
        <td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="jobId" MULTIPLE size=6 style="width:300px">
            <option value="*" SELECTED><B>&lt;<%=bundle.getString("all")%>&gt;</B></OPTION>
<%
            for (ReportJobInfo j : jobList)
            {
%>          <option title="<%=j.getJobName()%>" VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_project")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="projectId" multiple="true" size=4>
            <OPTION value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for (Project p : projectList)
            {
%>          <option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_status")%><span class="asterisk"></span>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="status" MULTIPLE size=4>
            <option value="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option value='<%=Job.READY_TO_BE_DISPATCHED%>'><%= bundle.getString("lb_ready") %></option>
            <option value='<%=Job.DISPATCHED%>'><%= bundle.getString("lb_inprogress") %></option>
            <option value='<%=Job.LOCALIZED%>'><%= bundle.getString("lb_localized") %></option>
            <option value='<%=Job.EXPORTED%>'><%= bundle.getString("lb_exported") %></option>
            <option value='<%=Job.EXPORT_FAIL%>'><%= bundle.getString("lb_exported_failed") %></option>
            <option value='<%=Job.ARCHIVED%>'><%= bundle.getString("lb_archived") %></option>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_language")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLang" multiple="true" size=4>
            <OPTION value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for (GlobalSightLocale gsl : targetLocales)
            {
%>          <option VALUE="<%=gsl.getId()%>"><%=gsl.getDisplayName(uiLocale)%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText" colspan=2><%=bundle.getString("lb_creation_date_range")%>:</td>
    </tr>
    <tr>
        <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
            <%=bundle.getString("lb_starts")%>:
            <input type="text" id="csf" name="<%=creationStart%>">
            <%=bundle.getString("lb_ends")%>:
            <input type="text" id ="cef" name="<%=creationEnd%>">
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_currency")%>:</td>
        <td>
        <SELECT NAME="currency">
<%
        Collection<?> currencies = ServerProxy.getCostingEngine().getCurrencies();
        Currency pivotCurrency = ServerProxy.getCostingEngine().getPivotCurrency();

        ArrayList<String> labeledCurrencies = new ArrayList<String>();
        ArrayList<String> valueCurrencies = new ArrayList<String>();
        Iterator iter = currencies.iterator();
        while ( iter.hasNext() ) 
        {
            Currency c = (Currency) iter.next();
            if (!labeledCurrencies.contains(c.getDisplayName())) 
            {
                labeledCurrencies.add(c.getDisplayName(uiLocale));
            	valueCurrencies.add(c.getDisplayName());
            }
        }
		
        for (int j = 0; j < labeledCurrencies.size(); j++)
        {
            String currencyLabel = labeledCurrencies.get(j);
            String currencyText = valueCurrencies.get(j);
%>
            <OPTION VALUE="<%=currencyText%>"><%=currencyLabel%> <%  }  %>
        </SELECT>
        </td>
    </tr>

    <amb:permission name="<%=Permission.REPORTS_DELL_ONLINE_JOBS_RECALC%>" >
    <TR>
        <TD><%=bundle.getString("re_calculate_costs")%>?<br></TD>
        <TD>
        <SELECT NAME="recalc">
            <OPTION name="false" VALUE="false" SELECTED><%=bundle.getString("lb_false")%></OPTION>
            <OPTION name="true" VALUE="true"><%=bundle.getString("lb_true")%></OPTION>
        </SELECT>
        </TD>
    </TR>
    </amb:permission>

    <tr>
        <td><input type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
    </tr>
</table>
</form>
<BODY>
</HTML>

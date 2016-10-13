package com.globalsight.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.RemoteException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.globalsight.www.webservices.Ambassador4Falcon;
import com.globalsight.www.webservices.WebServiceException;

public class ClientTester
{
    private static String HOST_NAME = "localhost";
    private static String HOST_PORT = "8080";
    private static String userName = "york";
    private static String password = "password";
    private static boolean enableHttps = false;

    public static Ambassador4Falcon getAmbassador() throws Exception
    {
        Ambassador4Falcon ambassador = WebServiceClientHelper
                .getClientAmbassador(HOST_NAME, HOST_PORT, userName, password,
                        enableHttps);
        return ambassador;
    }

    public static void main(String[] args)
    {
        try
        {
            Ambassador4Falcon ambassador = getAmbassador();
            String fullAccessToken = ambassador.login(userName, password);
            System.out.println("fullAccessToken : " + fullAccessToken);

//            // GBS-3167 (8.5.1)
//            testGetJobIDsWithStatusChanged(ambassador, fullAccessToken);
//
//            // GBS-3067 (8.5.2)
//            testGetDetailedWordcounts(ambassador, fullAccessToken);
//
//            // GBS-3308 #1 (8.5.2)
//            testGetWorkflowTemplateNames(ambassador, fullAccessToken);
//            // GBS-3308 #2 (8.5.2)
//            testGetWorkflowTemplateInfo(ambassador, fullAccessToken);
//            // GBS-3308 #3 (8.5.2)
//           testModifyWorkflowTemplateAssignees(ambassador, fullAccessToken);
//
            // GBS-3132 #1 (8.5.2)
//            testCreateUser(ambassador, fullAccessToken);
//            // GBS-3132 #2 (8.5.2)
//            testModifyUser(ambassador, fullAccessToken);
//            // GBS-3132 #3 (8.5.2)
//            testTaskReassign(ambassador, fullAccessToken);

            // GBS-3421(8.5.3) & GBS-3696(8.5.8) & GBS-3536
//            File file = testGetWorkOfflineFiles(ambassador, fullAccessToken);
//            String identifyKey = testUploadWorkOfflineFiles(ambassador,
//                    fullAccessToken);
//            testImportWorkOfflineFiles(ambassador, fullAccessToken, identifyKey);

            testGetInContextReviewLink(ambassador, fullAccessToken);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // GBS-3167 (8.5.1)
    private static void testGetJobIDsWithStatusChanged(
            Ambassador4Falcon ambassador, String p_accessToken)
            throws Exception
    {
        int intervalInMinute = 1;

        String result = ambassador.getJobIDsWithStatusChanged(p_accessToken,
                intervalInMinute,"york");
        System.out.println(result);
    }

    // GBS-3067 (8.5.2)
    private static void testGetDetailedWordcounts(Ambassador4Falcon ambassador,
            String p_accessToken) throws Exception
    {
        String[] jobIds = new String[]{ "148", "149" };
        Boolean includeMTDate = false;

        String result = ambassador.getDetailedWordcounts(p_accessToken, jobIds,
                includeMTDate);
        System.out.println(result);
    }

    // GBS-3308 #1 (8.5.2)
    private static void testGetWorkflowTemplateNames(
            Ambassador4Falcon ambassador, String p_accessToken)
            throws Exception
    {
        String result = ambassador.getWorkflowTemplateNames(p_accessToken);
        System.out.println(result);
    }

    // GBS-3308 #2 (8.5.2)
    private static void testGetWorkflowTemplateInfo(
            Ambassador4Falcon ambassador, String p_accessToken)
            throws Exception
    {
        String workflowTemplateName = "01_en_US_zh_CN";
        String companyName = "york";

        String result = ambassador.getWorkflowTemplateInfo(p_accessToken,
                workflowTemplateName, companyName);
        System.out.println(result);
    }

    // GBS-3308 #3 (8.5.2)
    private static void testModifyWorkflowTemplateAssignees(
            Ambassador4Falcon ambassador, String p_accessToken)
            throws Exception
    {
        // These data should be from the returning of previous API #2.
        String workflowTemplateName = "01_en_US_zh_CN";
        String companyName = "york";

        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("Participants", "yorkanyone");
        object.put("activityName", "Translation1_2");
        object.put("sequence", "1");
        array.put(object);

        JSONObject object2 = new JSONObject();
        object2.put("Participants", "All qualified users");
        object2.put("activityName", "review_linguistc1_2");
        object2.put("sequence", "2");
        array.put(object2);

        String activityAssigneesInJson = array.toString();
        System.out.println("activityAssigneesInJson ::" + activityAssigneesInJson);

        String result = ambassador.modifyWorkflowTemplateAssignees(
                p_accessToken, workflowTemplateName, companyName,
                activityAssigneesInJson);
        System.out.println(result);
    }

    // GBS-3132 #1 (8.5.2)
    private static void testCreateUser(Ambassador4Falcon ambassador,
            String p_accessToken) throws Exception
    {
        String userId = "qaNewUser2";
        String password = "password";
        String firstName = "Walter2";
        String lastName = "Xu";
        String email = "walter.xu@test.com";
        String[] permissionGroups = new String[]
        { "Administrator", "ProjectManager" };
        String status = null;
        StringBuilder roleXml = new StringBuilder();
        roleXml.append("<?xml version=\"1.0\"?>");
        roleXml.append("<roles>");
        roleXml.append("<role>");
        roleXml.append("<sourceLocale>en_US</sourceLocale>");
        roleXml.append("<targetLocale>de_DE</targetLocale>");
        roleXml.append("<activities>");
        roleXml.append("<activity><name>Translation1</name></activity>");
        roleXml.append("<activity><name>review_linguistc1</name></activity>");
        roleXml.append("</activities>");
        roleXml.append("</role>");
        roleXml.append("</roles>");
        boolean isInAllProjects = false;
        String[] projectIds = new String[]
        { "1003" };

        System.out.println("Create new user [" + userId + "] ...");
        int result = ambassador.createUser(p_accessToken, userId, password,
                firstName, lastName, email, permissionGroups,
                roleXml.toString(), isInAllProjects, projectIds);
        System.out.println(result);
    }

    // GBS-3132 #2 (8.5.2)
    private static void testModifyUser(Ambassador4Falcon ambassador,
            String p_accessToken) throws Exception
    {
        String userId = "qaNewUser";
        String password = "password";
        String firstName = "Walter";
        String lastName = "Xu_Modify";
        String email = "walter.xu@test.com";
        String[] permissionGroups = new String[]
        { "Administrator", "ProjectManager" };
        String status = null;
        StringBuilder roleXml = new StringBuilder();
        roleXml.append("<?xml version=\"1.0\"?>");
        roleXml.append("<roles>");
        roleXml.append("<role>");
        roleXml.append("<sourceLocale>en_US</sourceLocale>");
        roleXml.append("<targetLocale>de_DE</targetLocale>");
        roleXml.append("<activities>");
        roleXml.append("<activity><name>Translation1</name></activity>");
        roleXml.append("</activities>");
        roleXml.append("</role>");
        roleXml.append("</roles>");
        boolean isInAllProjects = true;
        String[] projectIds = new String[]
        { "1003" };

        System.out.println("Modify user [" + userId + "] ...");
        int result = ambassador.modifyUser(p_accessToken, userId, password,
                firstName, lastName, email, permissionGroups,
                roleXml.toString(), isInAllProjects, projectIds);
        System.out.println(result);
    }

    // GBS-3132 #3 (8.5.2)
    private static void testTaskReassign(Ambassador4Falcon ambassador,
            String p_accessToken) throws Exception
    {
        String taskId = "530";
        String[] user = new String[]{"test","test2"};
        
        System.out.println("Task reassign ...");
        String result = ambassador.taskReassign(p_accessToken, taskId, user);
        System.out.println(result);
    }

    private static void saveFile(InputStream is, File file) throws IOException,
            FileNotFoundException
    {
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream outstream = new FileOutputStream(file);
        int c;
        while ((c = is.read()) != -1)
        {
            outstream.write(c);
        }
        outstream.close();
        is.close();
        if (file.length() == 0)
        {
            file.delete();
        }
    }

    private static String testUploadWorkOfflineFiles(
            Ambassador4Falcon ambassador, String p_accessToken) throws Exception
    {
        long taskId = 4383;
        int workOfflineFileType = 2;

        File uploadFile = new File("D:\\_tmp\\as_850129247.rtf");
        String fileName = uploadFile.getName();

        byte[] bytes = null;
        bytes = new byte[(int) uploadFile.length()];
        FileInputStream fin = new FileInputStream(uploadFile);
        fin.read(bytes, 0, (int) uploadFile.length());

        String result = ambassador.uploadWorkOfflineFiles(p_accessToken,
                taskId, workOfflineFileType, fileName, bytes);
        System.out.println(result);

        String identifyKey = null;
        try
        {
            JSONObject object = new JSONObject(result);
            identifyKey = (String) object.get("identifyKey");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return identifyKey;
    }

    private static void testImportWorkOfflineFiles(
            Ambassador4Falcon ambassador, String p_accessToken, String p_identifyKey)
            throws Exception
    {
        long taskId = 4383;
        int workOfflineFileType = 2;

        String result = ambassador.importWorkOfflineFiles(p_accessToken,
                taskId, p_identifyKey, workOfflineFileType);
        System.out.println(result);
    }

    private static void testGetInContextReviewLink(
            Ambassador4Falcon ambassador, String p_accessToken)
            throws WebServiceException, RemoteException
    {
        String taskId = "7535";
        String result = ambassador
                .getInContextReviewLink(p_accessToken, taskId);
        System.out.println(result);
    }
}

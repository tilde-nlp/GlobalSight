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
package com.globalsight.connector.mindtouch;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.connector.mindtouch.form.CreateMindTouchJobForm;
import com.globalsight.connector.mindtouch.util.MindTouchHelper;
import com.globalsight.connector.mindtouch.vo.MindTouchPage;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.SaveCommentThread;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

public class CreateMindTouchJobThread implements Runnable
{
    static private final Logger logger = Logger
            .getLogger(CreateMindTouchJobThread.class);

    private User user;
    private String currentCompanyId;
    private MindTouchConnector conn;
    private File attachment;
    private CreateMindTouchJobForm mtForm;
    private String[] targetLocales;
    private String attachmentName;
    private String uuid;
    
    public CreateMindTouchJobThread(User user, String currentCompanyId,
            MindTouchConnector conn, CreateMindTouchJobForm mtForm,
            String[] targetLocales, File attachment, String attachmentName,
            String uuid)
    {
        super();

        this.user = user;
        this.currentCompanyId = currentCompanyId;
        this.conn = conn;
        this.mtForm = mtForm;
        this.targetLocales = targetLocales;
        this.attachment = attachment;
        this.attachmentName = attachmentName;
        this.uuid = uuid;
    }

    private void createJob() throws Exception
    {
        try
        {
            String jobName = mtForm.getJobName();
            String comment = mtForm.getComment();
            String priority = mtForm.getPriority();
            
            String randomStr = String.valueOf((new Random()).nextInt(999999999));
            while (randomStr.length() < 9)
            {
                randomStr = "0" + randomStr;
            }
            jobName = jobName + "_" + randomStr;

            // The fileMapFileProfile should be like 1234contents-1001, 1234tags-1001.
            // "1234" means MindTouch "pageID", "1001" means file profile ID.
            List<String> files = new ArrayList<String>();
            List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
            String fileMapFileProfile = mtForm.getFileMapFileProfile();
            String[] ffs = fileMapFileProfile.split(",");
            for (String ff : ffs)
            {
                String[] f = ff.split("-");
                files.add(f[0]);
                fileProfileList.add(HibernateUtil.get(FileProfileImpl.class,
                        Long.parseLong(f[1])));
            }

            long l10Id = fileProfileList.get(0).getL10nProfileId();
            BasicL10nProfile l10Profile = HibernateUtil.get(
                    BasicL10nProfile.class, l10Id);

            String locs = this.initTargetLocale(targetLocales);

            Job job = JobCreationMonitor.initializeJob(jobName, uuid,
                    user.getUserId(), l10Id, priority, Job.IN_QUEUE);

            String sourceLocaleName = l10Profile.getSourceLocale()
                    .getLocaleCode();
            // init files and file profiles infomation
            List<String> descList = new ArrayList<String>();
            retrieveRealFilesFromMindTouch(descList, job, files, sourceLocaleName);
            Map<String, long[]> filesToFpId = excuteScriptOfFileProfile(
                    descList, fileProfileList, job);

            Set<String> fileNames = filesToFpId.keySet();
            Integer pageCount = new Integer(fileNames.size());
            List<JobAttribute> jobAttribtues = getJobAttributes(
                    mtForm.getAttributeString(), l10Profile);
            // cache job attributes if there are any
            if (jobAttribtues != null && jobAttribtues.size() != 0)
            {
                RuntimeCache.addJobAtttibutesCache(uuid, jobAttribtues);
            }

            int count = 0;
            for (Iterator<String> i = fileNames.iterator(); i.hasNext();)
            {
                String fileName = i.next();
                long[] tmp = filesToFpId.get(fileName);
                String fileProfileId = String.valueOf(tmp[0]);
                int exitValue = (int) tmp[1];

                String key = jobName + fileName + ++count;
                CxeProxy.setTargetLocales(key, locs);
                CxeProxy.importFromFileSystem(fileName,
                        String.valueOf(job.getId()), jobName, fileProfileId,
                        pageCount, count, 1, 1, Boolean.TRUE, Boolean.FALSE,
                        CxeProxy.IMPORT_TYPE_L10N, exitValue, priority);
            }

            // save job attributes if there are any
            if (jobAttribtues != null)
            {
                saveAttributes(jobAttribtues, currentCompanyId, job);
            }

            // save job comment
            if (!StringUtils.isEmpty(comment)
                    || !StringUtils.isEmpty(attachmentName))
            {
                String dir = convertFilePath(AmbFileStoragePathUtils
                        .getFileStorageDirPath(currentCompanyId))
                        + File.separator
                        + "GlobalSight"
                        + File.separator
                        + "CommentReference"
                        + File.separator + "tmp" + File.separator + uuid;
                File src = new File(dir + File.separator + attachmentName);
                if (attachment != null)
                {
                    FileUtil.copyFile(attachment, src);
                    attachment.delete();
                }

                SaveCommentThread sct = new SaveCommentThread(jobName, comment,
                        attachmentName, user.getUserId(), dir);
                sct.start();
            }
        }
        catch (FileNotFoundException ex)
        {
            logger.error("Cannot find the tmp uploaded files.", ex);
        }
        catch (Exception e)
        {
            logger.error("Create job failed.", e);
        }
    }

    /**
     * Replace "\" and "/" to file separator
     * 
     * @param path
     * @return
     */
    private String convertFilePath(String path)
    {
        if (path != null)
        {
            return path.replace("\\", File.separator).replace("/",
                    File.separator);
        }
        else
        {
            return "";
        }
    }

    /**
     * Save job attributes of the job
     * 
     * @param attributeString
     * @param job
     * @param currentCompanyId
     * @param l10Profile
     * @throws ParseException
     */
    private void saveAttributes(List<JobAttribute> jobAttributeList,
            String currentCompanyId, Job job)
    {
        AddJobAttributeThread thread = new AddJobAttributeThread(
                ((JobImpl) job).getUuid(), currentCompanyId);
        thread.setJobAttributes(jobAttributeList);
        thread.createJobAttributes();
    }

    private List<JobAttribute> getJobAttributes(String attributeString,
            BasicL10nProfile l10Profile)
    {
        List<JobAttribute> jobAttributeList = new ArrayList<JobAttribute>();

        if (l10Profile.getProject().getAttributeSet() == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(attributeString))
        {
            String[] attributes = attributeString.split(";.;");
            for (String ele : attributes)
            {
                try
                {
                    String attributeId = ele.substring(ele.indexOf(",.,") + 3,
                            ele.lastIndexOf(",.,"));
                    String attributeValue = ele.substring(ele
                            .lastIndexOf(",.,") + 3);

                    Attribute attribute = HibernateUtil.get(Attribute.class,
                            Long.parseLong(attributeId));
                    JobAttribute jobAttribute = new JobAttribute();
                    jobAttribute.setAttribute(attribute.getCloneAttribute());
                    if (attribute != null
                            && StringUtils.isNotEmpty(attributeValue))
                    {
                        Condition condition = attribute.getCondition();
                        if (condition instanceof TextCondition)
                        {
                            jobAttribute.setStringValue(attributeValue);
                        }
                        else if (condition instanceof IntCondition)
                        {
                            jobAttribute.setIntegerValue(Integer
                                    .parseInt(attributeValue));
                        }
                        else if (condition instanceof FloatCondition)
                        {
                            jobAttribute.setFloatValue(Float
                                    .parseFloat(attributeValue));
                        }
                        else if (condition instanceof DateCondition)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    DateCondition.FORMAT);
                            jobAttribute
                                    .setDateValue(sdf.parse(attributeValue));
                        }
                        else if (condition instanceof ListCondition)
                        {
                            String[] options = attributeValue.split("#@#");
                            List<String> optionValues = Arrays.asList(options);
                            jobAttribute.setValue(optionValues, false);
                        }
                    }
                    jobAttributeList.add(jobAttribute);
                }
                catch (Exception e)
                {
                    logger.error("Failed to get job attributes", e);
                }
            }
        }
        else
        {
            List<Attribute> attsList = l10Profile.getProject()
                    .getAttributeSet().getAttributeAsList();
            for (Attribute att : attsList)
            {
                JobAttribute jobAttribute = new JobAttribute();
                jobAttribute.setAttribute(att.getCloneAttribute());
                jobAttributeList.add(jobAttribute);
            }
        }

        return jobAttributeList;
    }

    /**
     * If the fileprofile has a script, excute the script and create job
     * 
     * @param descList
     * @param fpIdList
     * @param fileProfileList
     * @return
     */
    private Map<String, long[]> excuteScriptOfFileProfile(
            List<String> descList, List<FileProfile> fileProfileList, Job p_job)
    {
        Map<String, long[]> filesToFpId = new HashMap<String, long[]>();

        for (int i = 0; i < descList.size(); i++)
        {
            String fileName = descList.get(i);
            FileProfile fp = fileProfileList.get(i);

            String scriptOnImport = fp.getScriptOnImport();
            long exitValue = 0;
            if (StringUtils.isNotEmpty(scriptOnImport))
            {
                String oldScriptedDir = fileName.substring(0,
                        fileName.lastIndexOf("."));
                String oldScriptedFolderPath = AmbFileStoragePathUtils
                        .getCxeDocDirPath(fp.getCompanyId())
                        + File.separator
                        + oldScriptedDir;
                File oldScriptedFolder = new File(oldScriptedFolderPath);

                String scriptedFolderNamePrefix = FileSystemUtil
                        .getScriptedFolderNamePrefixByJob(p_job.getId());
                String name = fileName.substring(
                        fileName.lastIndexOf(File.separator) + 1,
                        fileName.lastIndexOf("."));
                String extension = fileName
                        .substring(fileName.lastIndexOf(".") + 1);

                String scriptedDir = fileName.substring(0,
                        fileName.lastIndexOf(File.separator))
                        + File.separator
                        + scriptedFolderNamePrefix
                        + "_"
                        + name + "_" + extension;
                String scriptedFolderPath = AmbFileStoragePathUtils
                        .getCxeDocDirPath(fp.getCompanyId())
                        + File.separator
                        + scriptedDir;
                File scriptedFolder = new File(scriptedFolderPath);
                if (!scriptedFolder.exists())
                {
                    File file = new File(fileName);
                    String filePath = AmbFileStoragePathUtils
                            .getCxeDocDirPath(fp.getCompanyId())
                            + File.separator + file.getParent();
                    // Call the script on import to convert the file
                    try
                    {
                        String cmd = "cmd.exe /c " + scriptOnImport + " \""
                                + filePath + "\" \"" + scriptedFolderNamePrefix
                                + "\"";
                        // If the script is Lexmark tool, another parameter
                        // -encoding is passed.
                        if ("lexmarktool.bat".equalsIgnoreCase(new File(
                                scriptOnImport).getName()))
                        {
                            cmd += " \"-encoding " + fp.getCodeSet() + "\"";
                        }
                        ProcessRunner pr = new ProcessRunner(cmd);
                        Thread t = new Thread(pr);
                        t.start();
                        try
                        {
                            t.join();
                        }
                        catch (InterruptedException ie)
                        {
                        }
                        logger.info("Script on Import " + scriptOnImport
                                + " is called to handle " + filePath);
                    }
                    catch (Exception e)
                    {
                        exitValue = 1;
                        logger.error("The script on import was not executed successfully.");
                    }
                }

                // Iterator the files converted by the script and import
                // each one of them.
                if (scriptedFolder.exists() || oldScriptedFolder.exists())
                {
                    String scriptedFiles[];
                    if (scriptedFolder.exists())
                    {
                        scriptedFiles = scriptedFolder.list();
                    }
                    else
                    {
                        scriptedFiles = oldScriptedFolder.list();
                    }
                    if (scriptedFiles != null && scriptedFiles.length > 0)
                    {
                        for (int j = 0; j < scriptedFiles.length; j++)
                        {
                            String scriptedFileName = scriptedFiles[j];
                            String oldName = fileName.substring(fileName
                                    .lastIndexOf(File.separator) + 1);
                            if (!oldName.equals(scriptedFileName))
                            {
                                continue;
                            }
                            long fileProfileId = fp.getId();
                            String key_fileName;
                            if (scriptedFolder.exists())
                            {
                                key_fileName = scriptedDir + File.separator
                                        + scriptedFileName;
                            }
                            else
                            {
                                key_fileName = oldScriptedDir + File.separator
                                        + scriptedFileName;
                            }
                            filesToFpId.put(key_fileName, new long[] {
                                    fileProfileId, exitValue });
                        }
                    }
                    else
                    // there are no scripted files in the folder
                    {
                        filesToFpId.put(fileName, new long[] { fp.getId(),
                                exitValue });
                    }
                }
                else
                // the corresponding folder was not created by the script.
                {
                    filesToFpId.put(fileName, new long[] { fp.getId(),
                            exitValue });
                }
            }
            else
            {
                filesToFpId.put(fileName, new long[] { fp.getId(), exitValue });
            }
        }

        return filesToFpId;
    }

    /**
     * According to the array input, return a string of locales. Locales are
     * seperated with commas.
     * 
     * @param targetLocales
     * @return
     * @throws LocaleManagerException
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws GeneralException
     */
    private String initTargetLocale(String[] targetLocales)
            throws LocaleManagerException, NumberFormatException,
            RemoteException, GeneralException
    {
        StringBuffer targetLocaleString = new StringBuffer();
        for (int i = 0; i < targetLocales.length; i++)
        {
            GlobalSightLocale gsl = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(targetLocales[i]));
            String loaleName = gsl.toString();
            if (targetLocaleString.length() != 0)
            {
                targetLocaleString.append(",");
            }
            targetLocaleString.append(loaleName);
        }
        return targetLocaleString.toString();
    }
    
    private void retrieveRealFilesFromMindTouch(List<String> descList, Job job,
            List<String> files, String sourceLocale)
            throws FileNotFoundException, Exception
    {
        String pageId = null;
        String pageInfoXml = null;
        File srcFile = null;
        File objFile = null;
        String flag = null;
        String contentsOrTags = null;
        // <pageId:MindTouchPage>
        HashMap<String, MindTouchPage> pageInfoMap = new HashMap<String, MindTouchPage>();
        MindTouchHelper helper = new MindTouchHelper(conn);
        for (String f : files)
        {
            if (f.endsWith("contents"))
            {
                pageId = f.substring(0, f.indexOf("contents"));
                flag = "contents";
            }
            else if (f.endsWith("tags"))
            {
                pageId = f.substring(0, f.indexOf("tags"));
                flag = "tags";
            }

            MindTouchPage mtp = pageInfoMap.get(pageId);
            if (mtp == null)
            {
                pageInfoXml = helper.getPageInfo(Long.parseLong(pageId));
                if (pageInfoXml != null)
                {
                    mtp = helper.parsePageInfoXml(pageInfoXml);
                }
            }
            if (mtp != null)
            {
                String externalPageId = getFilePath(sourceLocale, job.getId(),
                        mtp, flag);
                srcFile = new File(
                        AmbFileStoragePathUtils.getCxeDocDir(currentCompanyId)
                                + File.separator + externalPageId);
                if ("contents".equals(flag))
                {
                    contentsOrTags = helper.getPageContents(pageId);
                }
                else if ("tags".equals(flag))
                {
                    contentsOrTags = helper.getPageTags(Long.parseLong(pageId));
                }
                contentsOrTags = EditUtil.decodeXmlEntities(contentsOrTags);
                FileUtil.writeFile(srcFile, contentsOrTags);
                descList.add(externalPageId);

                // save one ".obj" file with same pathname which is used to send
                // translated files back to MindTouch server.
                String objFilePathName = externalPageId + ".obj";
                objFile = new File(
                        AmbFileStoragePathUtils.getCxeDocDir(currentCompanyId)
                                + File.separator + objFilePathName);
                JSONObject json = new JSONObject();
                json.put("mindTouchConnectorId", String.valueOf(conn.getId()));
                json.put("pageId", pageId);
                json.put("path", mtp.getPath());
                json.put("title", mtp.getTitle());
                FileUtil.writeFile(objFile, json.toString());
            }
        }
        pageInfoMap = null;
    }

    private String getFilePath(String sourceLocale, long jobId,
            MindTouchPage mtp, String flag)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(sourceLocale).append(File.separator).append(jobId);
        if (mtp.getPath() != null && mtp.getPath().trim().length() > 0)
        {
            filePath.append(File.separator).append(mtp.getPath());
        }
        // The path includes the title generally, but not always. A title like
        // "Get Involved" will be "Get_Involved" in path.
        String title = mtp.getTitle().replace(" ", "_");
        if (!filePath.toString().endsWith(title))
        {
            filePath.append(File.separator).append(mtp.getTitle());
        }
        if ("contents".equals(flag))
        {
            filePath.append("(contents).xml");
        }
        else if ("tags".equals(flag))
        {
            filePath.append("(tags).xml");
        }

        String str = filePath.toString().replace("\\", "/");

        return handleSpecialChars(str);
    }

    // The "pathName" uses "/" as separator.
    private String handleSpecialChars(String pathName)
    {
        StringBuffer sb = new StringBuffer();
        String[] arr = pathName.split("/");
        if (arr != null && arr.length > 0)
        {
            for (String str : arr)
            {
                str = str.replace("\\", "");
                str = str.replace("/", "");
                str = str.replace(":", "");
                str = str.replace("*", "");
                str = str.replace("?", "");
                str = str.replace("\"", "");
                str = str.replace("<", "");
                str = str.replace(">", "");
                str = str.replace("|", "");

                sb.append(str).append(File.separator);
            }
        }
        if (sb.toString().endsWith(File.separator))
        {
            return sb.substring(0, sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public void run()
    {
        try
        {
            createJob();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
}

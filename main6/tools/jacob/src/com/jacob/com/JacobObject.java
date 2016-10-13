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
package com.jacob.com;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * The superclass of all Jacob objects.  It is used to
 * create a standard API framework and to facillitate memory management
 * for Java and COM memory elements.
 * <p>
 * All COM object created by JACOB extend this class so that we can
 * automatically release them when the thread is detached from COM - if we leave
 * it to the finalizer it will call the release from another thread, which may
 * result in a segmentation violation.
 */
public class JacobObject
{
    private static String buildVersion = "";
    private static String buildDate = "";

    /**
     *  Standard constructor that adds this JacobObject
     * to the memory management pool.
     */
    public JacobObject()
    {
        ROT.addObject(this);
    }

    /**
     * Loads version information from version.properties that was
     * built as part of this release.
     */
    private static void loadVersionProperties()
    {
        Properties versionProps = new Properties();
        // can't use system class loader cause won't work in jws
        InputStream stream =
            JacobObject.class.getClassLoader().getResourceAsStream("version.properties");
        try
        {
            versionProps.load(stream);
            stream.close();
            buildVersion = (String)versionProps.get("version");
            buildDate = (String)versionProps.get("build.date");
        }
        catch (IOException ioe)
        {
            System.out.println("blah, couldn't load props");
        }
    }

    /**
     * loads version.properties and returns the value of versin in it
     * @return String value of version in version.properties or "" if none
     */
    public static String getBuildDate()
    {
        if (buildDate.equals(""))
        {
            loadVersionProperties();
        }
        return buildDate;
    }

    /**
     * loads version.properties and returns the value of versin in it
     * @return String value of version in version.properties or "" if none
     */
    public static String getBuildVersion()
    {
        if (buildVersion.equals(""))
        {
            loadVersionProperties();
        }
        return buildVersion;
    }

    /**
     *  Finalizers call this method.
     *  This method should release any COM data structures in a way
     *  that it can be called multiple times.
     *  This can happen if someone manually calls this and then
     *  a finalizer calls it.
     */
    public void safeRelease()
    {
        // currently does nothing - subclasses may do something
        if (isDebugEnabled())
        {
            debug(this.getClass().getName() + ":" + this.hashCode() + " release");
        }
    }

    /**
     * When things go wrong, it is useful to be able to debug the ROT.
     */
    private static final boolean DEBUG =
        "true".equalsIgnoreCase(System.getProperty("com.jacob.debug"));

    protected static boolean isDebugEnabled()
    {
        return DEBUG;
    }

    /**
     * Very basic debugging fucntion.
     * @param istrMessage
     */
    protected static void debug(String istrMessage)
    {
        if (isDebugEnabled())
        {
            System.out.println(istrMessage);
        }
    }
}

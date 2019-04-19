/*
 * Copyright 2008-2010 The MxUpdate Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.mxupdate.eclipse.test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.settings.DefaultMavenSettingsBuilder;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;

/**
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class AbstractTest
{
    /**
     * Name of the property used for the URL to the MX instance.
     */
    private static final String PROP_URL = "org.mxupdate.mx.url";

    /**
     * Name of the property used for the user to the MX instance.
     */
    private static final String PROP_USER = "org.mxupdate.mx.user";

    /**
     * Name of the property used for the password to the MX instance.
     */
    private static final String PROP_PASSWORD = "org.mxupdate.mx.password";

    /**
     * Name of the property used to get the MX Jar library.
     */
    private static final String PROP_MXLIB = "org.mxupdate.mx.jar.ematrix";

    /**
     * Settings for maven.
     */
    private Settings settings;

    /**
     * Returns depending on the <code>_key</code> related value within the
     * properties.
     *
     * @param _key      searched key within property settings
     * @return value of the property
     * @throws Exception if setting could not be fetched
     * @see #settings
     */
    protected String getSetting(final String _key)
        throws Exception
    {
        // load settings if required
        if (this.settings == null)  {
            final DefaultMavenSettingsBuilder builder = new DefaultMavenSettingsBuilder();
            final File settingsFile = new File(new File(System.getProperty("user.home")), ".m2/settings.xml");
            this.settings = builder.buildSettings(settingsFile);
        }

        // get value
        String tmp = null;
        for (final String active : this.settings.getActiveProfiles())  {
            tmp = ((Profile) this.settings.getProfilesAsMap().get(active)).getProperties().getProperty(_key);
            if (tmp != null)  {
                break;
            }
        }
        final String value;
        if (tmp != null)  {
            value = tmp;
        } else  {
            value = "";
        }

        // replace macros
        final Pattern pattern = Pattern.compile("\\$\\{[^\\}]*\\}");
        final Matcher matcher = pattern.matcher(value);
        final StringBuilder ret = new StringBuilder();
        int prevIdx = 0;
        while (matcher.find())  {

            ret.append(value.substring(prevIdx, matcher.start()));

            final String match = matcher.group();
            if ("${basedir}".equals(match))  {
                ret.append(this.getProjectPath().toString());
            } else  {
                ret.append(this.getSetting(match.substring(2, match.length() - 1)));
            }
            prevIdx = matcher.end();
        }
        ret.append(value.substring(prevIdx, value.length()));

        return ret.toString();
    }

    /**
     * Returns the {@link #PROP_URL URL} from the settings.
     *
     * @return URL of the MX application
     * @throws Exception if URL could not be fetched from the settings
     * @see #PROP_URL
     */
    protected String getURL()
        throws Exception
    {
        return this.getSetting(AbstractTest.PROP_URL);
    }

    /**
     * Returns the {@link #PROP_USER user} from the settings.
     *
     * @return user for the MX application
     * @throws Exception if user could not be fetched from the settings
     * @see #PROP_USER
     */
    protected String getUser()
        throws Exception
    {
        return this.getSetting(AbstractTest.PROP_USER);
    }

    /**
     * Returns the {@link #PROP_PASSWORD password} from the settings.
     *
     * @return password for the MX application
     * @throws Exception if password could not be fetched from the settings
     * @see #PROP_PASSWORD
     */
    protected String getPassword()
        throws Exception
    {
        return this.getSetting(AbstractTest.PROP_PASSWORD);
    }

    /**
     * Returns the {@link #PROP_MXLIB MX Jar class path} from the settings.
     *
     * @return MX Jar class path
     * @throws Exception if MX Jar class path could not be fetched from the
     *                   settings
     * @see #PROP_MXLIB
     */
    protected String getMXLibJarPath()
        throws Exception
    {
        return this.getSetting(AbstractTest.PROP_MXLIB);
    }

    /**
     * Returns the project path.
     *
     * @return path for the project path
     */
    protected File getProjectPath()
    {
        return new File(System.getProperty( "user.dir" )).getAbsoluteFile();
    }

    /**
     * Returns the target path within the project folder.
     *
     * @return target path
     */
    protected File getTargetPath()
    {
        return new File(new File(this.getProjectPath(), "target"), "test").getAbsoluteFile();
    }
}

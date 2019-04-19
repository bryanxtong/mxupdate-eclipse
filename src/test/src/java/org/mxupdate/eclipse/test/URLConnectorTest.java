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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.mxupdate.eclipse.mxadapter.connectors.URLConnector;
import org.mxupdate.eclipse.util.CommunicationUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the {@link URLConnector}.
 *
 * @author The MxUpdate Team
 * @version $Id$
 * @see URLConnector
 */
public class URLConnectorTest
    extends AbstractTest
{
    /**
     * Negative test if the called method is wrong.
     *
     * @throws Exception if test failed
     */
    @Test(timeOut = 10000,
          description = "negative test if the called method is wrong")
    public void negativeMethodWrong()
        throws Exception
    {
        final URLConnector connector = new URLConnector(
                new File(this.getTargetPath(), "URLConnectorTest.negativeMethodWrong"),
                new BundleTest(),
                "java",
                this.getMXLibJarPath(),
                this.getURL(),
                this.getUser(),
                this.getPassword(),
                true);
        final String bck = connector.execute(
                CommunicationUtil.encode(null),
                CommunicationUtil.encode("GetVersion111"),
                CommunicationUtil.encode(null));
        connector.disconnect();

        final Map<?,?> map = CommunicationUtil.decode(bck);

        Assert.assertNotNull(map);
        Assert.assertNull(map.get("value"), "values must be null");
        Assert.assertNull(map.get("error"), "error log must be null");
        Assert.assertNotNull(map.get("exception"), "exception must not be null");
        Assert.assertTrue(
                map.get("exception").toString().contains("unknown plug-in method 'GetVersion111'"),
                "returned exception contains not the unknown plug-in exception " + map.get("exception").toString());
    }

    /**
     * Negative test if the URL is wrong.
     *
     * @throws Exception if test failed
     */
    @Test(timeOut = 10000,
          description = "negative test if the URL is wrong")
    public void negativeURLWrong()
        throws Exception
    {
        Exception exception = null;
        try  {
            final URLConnector connector = new URLConnector(
                    new File(this.getTargetPath(), "URLConnectorTest.negativeURLWrong"),
                    new BundleTest(),
                    "java",
                    this.getMXLibJarPath(),
                    this.getURL() + "1",
                    this.getUser(),
                    this.getPassword(),
                    true);
            connector.disconnect();
        } catch (final Exception e)  {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertTrue(
                exception.getMessage().contains("java.io.FileNotFoundException: " + this.getURL() + "1/servlet/MatrixXMLServlet"),
                "exception contains file not found exception: " + exception.toString());
    }

    /**
     * Negative test if the name of the user is wrong.
     *
     * @throws Exception if test failed
     */
    @Test(timeOut = 10000,
          description = "negative test if the name of the user is wrong")
    public void negativeUserNameWrong()
        throws Exception
    {
        final String userName = this.getUser() + this.getUser();
        Exception exception = null;
        try  {
            final URLConnector connector = new URLConnector(
                    new File(this.getTargetPath(), "URLConnectorTest.negativeUserNameWrong"),
                    new BundleTest(),
                    "java",
                    this.getMXLibJarPath(),
                    this.getURL(),
                    userName,
                    this.getPassword(),
                    true);
            connector.disconnect();
        } catch (final Exception e)  {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertTrue(
                exception.getMessage().contains("person '" + userName + "' does not exist"),
                "exception contains person does not exists " + exception.toString());
    }

    /**
     * Negative test if the password of the user is wrong.
     *
     * @throws Exception if test failed
     */
    @Test(timeOut = 10000,
          description = "negative test if the password of the user is wrong")
    public void negativeUserPasswordWrong()
        throws Exception
    {
        Exception exception = null;
        try  {
            final URLConnector connector = new URLConnector(
                    new File(this.getTargetPath(), "URLConnectorTest.negativeUserPasswordWrong"),
                    new BundleTest(),
                    "java",
                    this.getMXLibJarPath(),
                    this.getURL(),
                    this.getUser(),
                    this.getUser() + this.getPassword() + "sdfsf",
                    true);
            connector.disconnect();
        } catch (final Exception e)  {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertTrue(
                exception.getMessage().contains("Invalid password"),
                "exception contains no invalid password " + exception.toString());
    }

    /**
     * Negative test if MX Jar library is not correct defined.
     *
     * @throws Exception if test failed
     */
    @Test(timeOut = 10000,
          description = "negative test if MX Jar library is not correct defined")
    public void negativeWrongMXLibJarPath()
        throws Exception
    {
        Exception exception = null;
        try  {
            final URLConnector connector = new URLConnector(
                    new File(this.getTargetPath(), "URLConnectorTest.negativeWrongMXLibJarPath"),
                    new BundleTest(),
                    "java",
                    this.getMXLibJarPath() + "1",
                    this.getURL(),
                    this.getUser(),
                    this.getPassword(),
                    true);
            connector.disconnect();
        } catch (final Exception e)  {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertTrue(
                exception.getMessage().contains("java.lang.ClassNotFoundException"),
                "exception contains no class not found exception: " + exception.toString());
    }

    /**
     * Test that the connection works as expected by calling the 'GetVersion'
     * method.
     *
     * @throws Exception if test failed
     */
    @Test(timeOut = 10000,
          description = "test that the connection works as expected by calling the 'GetVersion' method")
    public void positiveConnect()
        throws Exception
    {
        final URLConnector connector = new URLConnector(
                new File(this.getTargetPath(), "URLConnectorTest.positiveConnect"),
                new BundleTest(),
                "java",
                this.getMXLibJarPath(),
                this.getURL() + "",
                this.getUser(),
                this.getPassword(),
                true);
        final String bck = connector.execute(
                CommunicationUtil.encode(null),
                CommunicationUtil.encode("GetVersion"),
                CommunicationUtil.encode(null));

        final Map<?,?> map = CommunicationUtil.decode(bck);
        Assert.assertEquals(map.get("error"), null);
        Assert.assertEquals(map.get("exception"), null);
        Assert.assertEquals(map.get("log"), "");
        Assert.assertTrue(map.get("values").toString().matches("[0-9]*-[0-9]*-[0-9]*"));

        connector.disconnect();
    }

    /**
     * Implements all required methods used from {@link URLConnector} for test
     * purposes.
     */
    private class BundleTest
        implements Bundle
    {
        /**
         * Dummy method to implement the interface.
         *
         * @param _arg0     not used
         * @param _arg1     not used
         * @param _arg2     not used
         * @return always <code>null</code>
         */
        @Override()
        public Enumeration<?> findEntries(final String _arg0,
                                          final String _arg1,
                                          final boolean _arg2)
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public BundleContext getBundleContext()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>0</code>
         */
        @Override()
        public long getBundleId()
        {
            return 0;
        }

        /**
         * Returns an URL for given <code>_entry</code> within
         * {@link AbstractTest#getProjectPath() project path}.
         *
         * @param _entry    entry for which the URL is searched
         * @return URL for <code>_entry</code>
         * @see AbstractTest#getProjectPath()
         */
        @Override()
        public URL getEntry(final String _entry)
        {
            try {
                return new URL("file://" + new File(URLConnectorTest.this.getProjectPath(), _entry).getAbsolutePath().toString());
            } catch (final MalformedURLException e) {
                throw new Error(e);
            }
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg  not used
         * @return empty enumeration on an hash table, because the method is
         *         used to check if the bin directory exists
         */
        @Override()
        public Enumeration<?> getEntryPaths(final String _arg)
        {
            return (new Hashtable<Object,Object>()).elements();
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public Dictionary<?,?> getHeaders()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         * @return always <code>null</code>
         */
        @Override()
        public Dictionary<?,?> getHeaders(final String _arg)
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>0</code>
         */
        @Override()
        public long getLastModified()
        {
            return 0;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public String getLocation()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public ServiceReference[] getRegisteredServices()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         * @return always <code>null</code>
         */
        @Override()
        public URL getResource(final String _arg)
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         * @return always <code>null</code>
         */
        @Override()
        public Enumeration<?> getResources(final String _arg)
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public ServiceReference[] getServicesInUse()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         * @return always <code>null</code>
         */
        @Override()
        public Map<?,?> getSignerCertificates(final int _arg)
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>0</code>
         */
        @Override()
        public int getState()
        {
            return 0;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public String getSymbolicName()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @return always <code>null</code>
         */
        @Override()
        public Version getVersion()
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         * @return always <i>false</i>
         */
        @Override()
        public boolean hasPermission(final Object _arg)
        {
            return false;
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         * @return always <code>null</code>
         */
        @Override()
        public Class<?> loadClass(final String _arg)
        {
            return null;
        }

        /**
         * Dummy method to implement the interface.
         */
        @Override()
        public void start()
        {
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         */
        @Override()
        public void start(final int _arg)
        {
        }

        /**
         * Dummy method to implement the interface.
         */
        @Override()
        public void stop()
        {
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         */
        @Override()
        public void stop(final int _arg)
        {
        }

        /**
         * Dummy method to implement the interface.
         */
        @Override
        public void uninstall()
        {
        }

        /**
         * Dummy method to implement the interface.
         */
        @Override()
        public void update()
        {
        }

        /**
         * Dummy method to implement the interface.
         *
         * @param _arg      not used
         */
        @Override()
        public void update(final InputStream _arg)
        {
        }
    }
}

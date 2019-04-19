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

package org.mxupdate.eclipse.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.mxupdate.eclipse.Messages;
import org.mxupdate.eclipse.adapter.IDeploymentAdapter;
import org.mxupdate.eclipse.console.Console;
import org.mxupdate.eclipse.mxadapter.MXAdapter;
import org.mxupdate.eclipse.mxadapter.connectors.IConnector;
import org.mxupdate.eclipse.mxadapter.connectors.SSHConnector;
import org.mxupdate.eclipse.mxadapter.connectors.URLConnector;

/**
 * Enumeration to differ the modes of projects.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public enum ProjectMode
{
    /**
     * The mode is now defined and unknown.
     */
    UNKNOWN {
        /** Prefix used for property keys. */
        private final String prefix = "Unknown."; //$NON-NLS-1$

        /**
         * {@inheritDoc}
         */
        @Override()
        public String getTitle()
        {
            return Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("Title"));
        }

        /**
         * {@inheritDoc}
         * Throws always an error because the mode is unknown and no adapter is
         * defined.
         */
        @Override()
        public IDeploymentAdapter initAdapter(final IProject _project,
                                          final ProjectProperties _properties,
                                          final Console _console)
            throws IOException
        {
            throw new Error("not implemented");
        }

        /**
         * {@inheritDoc}
         * Throws always an error because the mode is unknown and no connector
         * is defined.
         */
        @Override()
        public IConnector initConnector(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
            throws Exception
        {
            throw new Error("not implemented");
        }

        /**
         * Appends nothing because unknown mode contains no settings.
         * {@inheritDoc}
         */
        @Override()
        public void createContent(final Composite _composite,
                                  final ProjectProperties _properties)
        {
        }

        /**
         * {@inheritDoc}
         * Because not values could be defined always <code>null</code> is
         * returned.
         *
         * @return always <code>null</code>
         */
        @Override()
        public String isValid(final ProjectProperties _properties)
        {
            return null;
        }
    },

    /**
     * Classic, MXUpdate Eclipse Plug-In connects via URL or via shared
     * library.
     */
    MXUPDATE_VIA_URL
    {
        /** Prefix used for property keys. */
        private final String prefix = "MxUpdateViaURL."; //$NON-NLS-1$

        /** Name of the property key for the Java path. */
        private final String propJavaPath = this.prefix + "JavaPath"; //$NON-NLS-1$

        /** Default value for the Java path. */
        private final String valJavaPath = "java"; //$NON-NLS-1$

        /** Name of the property key for the path of the MX Jar library. */
        private final String propMxJarLibraryPath = this.prefix + "MxJarLibraryPath"; //$NON-NLS-1$

        /** Name of the property key for the URL to the MX server. */
        private final String propUrl = this.prefix + "URL"; //$NON-NLS-1$

        /** Name of the property key for the MX user name. */
        private final String propUser = this.prefix + "UserName"; //$NON-NLS-1$

        /** Name of the property key for the password. */
        private final String propPassword = this.prefix+ "Password"; //$NON-NLS-1$

        /** Name of the property key if the password could be saved. */
        private final String propSavePassword = this.prefix + "SavePassword"; //$NON-NLS-1$

        /** Default value for the flag if the password could be saved. */
        private final boolean valSavePassword = false;

        /**
         * Name of the property key if the update is done with the file
         * content.
         */
        private final String propUpdateByFileContent = this.prefix + "UpdateByFileContent"; //$NON-NLS-1$

        /**
         * {@inheritDoc}
         */
        @Override()
        public String getTitle()
        {
            return Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("Title"));
        }

        /**
         * {@inheritDoc}
         */
        @Override()
        public void createContent(final Composite _parent,
                                  final ProjectProperties _properties)
        {
            // Java instance
            final Group javaGroup = FieldUtil.createGroup(_parent, this.prefix + "JavaGroup"); //$NON-NLS-1$
            FieldUtil.addFileField(javaGroup, _properties, this.propJavaPath, this.valJavaPath);
            FieldUtil.addFileField(javaGroup, _properties, this.propMxJarLibraryPath, "", "*.jar", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            // MX connection settings
            final Group mxGroup = FieldUtil.createGroup(_parent, this.prefix + "MxGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propUrl, ""); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propUser, ""); //$NON-NLS-1$
            FieldUtil.addPasswordField(mxGroup, _properties, this.propPassword, this.propSavePassword);

            FieldUtil.addBooleanField(_parent, _properties, this.propUpdateByFileContent, true);
        }

        /**
         * {@inheritDoc}
         * @see MXAdapter
         */
        @Override()
        public IDeploymentAdapter initAdapter(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
        {
            return new MXAdapter(_project, _properties, _console);
        }

        /**
         * {@inheritDoc}
         * The {@link URLConnector} is initialized. Depending on the
         * {@link #propSavePassword} the MX user name / password is asked from
         * the user.
         *
         * @see SSHConnector
         */
        @Override()
        public IConnector initConnector(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
            throws Exception
        {
            final String mxURL = _properties.getString(this.propUrl, "");

            // MX authentication
            final String mxUser;
            final String mxPasswd;
            if (_properties.getBoolean(this.propSavePassword, this.valSavePassword))  {
                mxUser = _properties.getString(this.propUser, null);
                mxPasswd = _properties.getPassword(this.propPassword);
            } else  {
                final AuthenticationDialog loginPage = new AuthenticationDialog(
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MxGroup")),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propUser)),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propPassword)),
                        _properties.getString(this.propUser, null));
                loginPage.open();
                if (!loginPage.isOkPressed())  {
                    throw new Exception(
                            Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterNotMXAuthenticated")));
                }
                mxUser = loginPage.getUserName();
                mxPasswd = loginPage.getPassword();
            }

            _console.logInfo(
                    Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterConnectTo"), //$NON-NLS-1$
                                       mxURL));

            return new URLConnector(
                    _project,
                    _properties.getString(this.propJavaPath, this.valJavaPath),
                    _properties.getString(this.propMxJarLibraryPath, ""), //$NON-NLS-1$
                    mxURL, mxUser, mxPasswd,
                    _properties.getBoolean(this.propUpdateByFileContent, true));
        }

        /**
         * {@inheritDoc}
         *
         * @return <code>null</code> if property keys {@link #propJavaPath},
         *         {@link #propMxJarLibraryPath} and {@link #propUser} are not
         *         empty
         */
        @Override()
        public String isValid(final ProjectProperties _properties)
        {
            final String ret;

            if (_properties.getString(this.propJavaPath, this.valJavaPath).isEmpty())  {
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingJavaPath")); //$NON-NLS-1$
            } else if (_properties.getString(this.propMxJarLibraryPath, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingMxJarLibraryPath")); //$NON-NLS-1$
            } else  if (_properties.getString(this.propUser, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingUserName")); //$NON-NLS-1$
            } else  {
                ret = null;
            }
            return ret;
        }
    },

    /**
     * Classic with external definition, means that the settings for the MX
     * connection are defined in a property file.
     */
    MXUPDATE_VIA_URL_WITH_PROPERTY_FILE
    {
        /** Prefix used for property keys. */
        private final String prefix = "MxUpdateViaURLWithPropFile."; //$NON-NLS-1$

        /** Name of the property key for the property file path. */
        private final String propPropPath = this.prefix + "PropFilePath"; //$NON-NLS-1$

        /** Name of the property key for the Java path. */
        private final String propJavaPath = this.prefix + "KeyJavaPath"; //$NON-NLS-1$

        /** Default value for the Java path. */
        private final String valJavaPath = "java"; //$NON-NLS-1$

        /** Name of the property key for the path of the MX Jar library. */
        private final String propMxJarLibraryPath = this.prefix + "KeyMxJarLibraryPath"; //$NON-NLS-1$

        /** Name of the property key for the URL to the MX server. */
        private final String propUrl = this.prefix + "KeyURL"; //$NON-NLS-1$

        /** Name of the property key for the MX user name. */
        private final String propName = this.prefix + "KeyUserName"; //$NON-NLS-1$

        /** Name of the property key for the password. */
        private final String propPassword = this.prefix+ "KeyPassword"; //$NON-NLS-1$

        /**
         * Name of the property key if the update is done with the file
         * content.
         */
        private final String propUpdateByFileContent = this.prefix + "KeyUpdateByFileContent"; //$NON-NLS-1$

        /**
         * {@inheritDoc}
         */
        @Override()
        public String getTitle()
        {
            return Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("Title"));
        }

        /**
         * {@inheritDoc}
         */
        @Override()
        public void createContent(final Composite _parent,
                                  final ProjectProperties _properties)
        {
            FieldUtil.addFileField(_parent, _properties, this.propPropPath, ""); //$NON-NLS-1$

            // Java instance
            final Group javaGroup = FieldUtil.createGroup(_parent, this.prefix + "JavaGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(javaGroup, _properties, this.propJavaPath, ""); //$NON-NLS-1$
            FieldUtil.addStringField(javaGroup, _properties, this.propMxJarLibraryPath, ""); //$NON-NLS-1$

            // MX settings
            final Group mxGroup = FieldUtil.createGroup(_parent, this.prefix + "MXGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propUrl, ""); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propName, ""); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propPassword, ""); //$NON-NLS-1$

            // Other settings
            final Group other = FieldUtil.createGroup(_parent, this.prefix + "OtherGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(other, _properties, this.propUpdateByFileContent, ""); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         * @see MXAdapter
         */
        @Override()
        public IDeploymentAdapter initAdapter(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
        {
            return new MXAdapter(_project, _properties, _console);
        }

        /**
         * {@inheritDoc}
         * The connection settings are read from external file and the
         * connection is initialized.
         */
        @Override()
        public IConnector initConnector(final IProject _project,
                                        final ProjectProperties _properties,
                                        final Console _console)
            throws Exception
        {
            final String propFile       = _properties.getString(this.propPropPath, ""); //$NON-NLS-1$
            final String propKeyJava    = _properties.getString(this.propJavaPath, ""); //$NON-NLS-1$
            final String propKeyMXJar   = _properties.getString(this.propMxJarLibraryPath, ""); //$NON-NLS-1$
            final String propKeyUrl     = _properties.getString(this.propUrl, ""); //$NON-NLS-1$
            final String propKeyName    = _properties.getString(this.propName, ""); //$NON-NLS-1$
            final String propKeyPass    = _properties.getString(this.propPassword, ""); //$NON-NLS-1$
            final String propKeyFileCnt = _properties.getString(this.propUpdateByFileContent, ""); //$NON-NLS-1$

            // read file
            _console.logInfo(Messages.getString(
                    new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterReadExternalFile"), propFile)); //$NON-NLS-1$
            final Properties extProps = new Properties();
            try {
                extProps.load(new FileInputStream(propFile));
            } catch (final FileNotFoundException e) {
                final String message = Messages.getString(
                        new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterFileOpenFailed"), propFile); //$NON-NLS-1$
                _console.logError(message, e); //$NON-NLS-1$
                throw new Exception(message, e);
            } catch (final IOException e) {
                final String message = Messages.getString(
                        new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterFileOpenFailed"), propFile); //$NON-NLS-1$
                _console.logError(message, e); //$NON-NLS-1$
                throw new Exception(message, e);
            }

            // Java path
            final String javaPath;
            if (propKeyJava.isEmpty())  {
                javaPath = this.valJavaPath;
            } else  {
                javaPath = extProps.getProperty(propKeyJava);
            }

            // MX Jar Library path
            final String mxJarLibraryPath = extProps.getProperty(propKeyMXJar);

            // URL
            final String mxURL;
            if (propKeyUrl.isEmpty())  {
                mxURL = "";
            } else  {
                mxURL = extProps.getProperty(propKeyUrl);
            }

            // user and password
            final String mxUser;
            final String mxPasswd;
            if (propKeyName.isEmpty() || propKeyPass.isEmpty())  {
                final AuthenticationDialog loginPage = new AuthenticationDialog(
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MXGroupLogin")),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propName)),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propPassword)),
                        extProps.getProperty(propKeyName, null));
                loginPage.open();
                if (!loginPage.isOkPressed())  {
                    throw new Exception(
                            Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterNotMXAuthenticated")));
                }
                mxUser = loginPage.getUserName();
                mxPasswd = loginPage.getPassword();
            } else  {
                mxUser = extProps.getProperty(propKeyName);
                mxPasswd = extProps.getProperty(propKeyPass);
            }

            // update by file content flag
            final boolean flagByFileContent;
            if (propKeyFileCnt.isEmpty())  {
                flagByFileContent = true;
            } else  {
                flagByFileContent = Boolean.valueOf(extProps.getProperty(propKeyFileCnt));
            }

            _console.logInfo(Messages.getString(
                    new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterConnectTo"), //$NON-NLS-1$
                    mxURL));

            return new URLConnector(_project, javaPath, mxJarLibraryPath, mxURL, mxUser, mxPasswd, flagByFileContent);
        }

        /**
         * {@inheritDoc}
         *
         * @return <code>null</code> if property keys {@link #propPropPath} and
         *         {@link #propMxJarLibraryPath} are not empty
         */
        @Override()
        public String isValid(final ProjectProperties _properties)
        {
            final String ret;

            if (_properties.getString(this.propPropPath, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingPropFilePath")); //$NON-NLS-1$
            } else if (_properties.getString(this.propMxJarLibraryPath, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(
                        new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingKeyMxJarLibraryPath")); //$NON-NLS-1$
            } else  {
                ret = null;
            }
            return ret;
        }
    },

    /**
     * MxUpdate Plug-In uses MQL on a SSH server.
     */
    MXUPDATE_SSH_MQL
    {
        /** Prefix used for property keys. */
        private final String prefix = "MxUpdateViaSSHMQL."; //$NON-NLS-1$

        /** Name of the property key for the name of the SSH server. */
        private final String propSSHServer = this.prefix + "SSHServer"; //$NON-NLS-1$

        /** Name of the property key for the port of the SSH server. */
        private final String propSSHPort = this.prefix + "SSHPort"; //$NON-NLS-1$

        /** Default value for the SSH port. */
        private final int valSSHPort = 22;

        /** Name of the property key for the name of the SSH user. */
        private final String propSSHUser = this.prefix + "SSHUser"; //$NON-NLS-1$

        /** Name of the property key for the password of the SSH user. */
        private final String propSSHPassword = this.prefix + "SSHPassword"; //$NON-NLS-1$

        /** Name of the property key for the flag if the password is saved. */
        private final String propSSHSavePassword = this.prefix + "SSHSavePassword"; //$NON-NLS-1$

        /** Default value for flag if the SSH password could be saved. */
        private final boolean valSSHSavePassword = false;

        /**
         * Name of the property key for the path of the MQL command on the SSH
         * server.
         */
        private final String propMXMQLPath = this.prefix + "MXMQLPath"; //$NON-NLS-1$

        /** Default value for the MQL path. */
        private final String valMXMQLPath = "mql"; //$NON-NLS-1$

        /** Name of the property key for the MX user name. */
        private final String propMXUserName = this.prefix + "MXUserName"; //$NON-NLS-1$

        /** Name of the property key for the MX password. */
        private final String propMXPassword = this.prefix+ "MXPassword"; //$NON-NLS-1$

        /** Name of the property key if the password could be saved. */
        private final String propMXSavePassword = this.prefix + "MXSavePassword"; //$NON-NLS-1$

        /** Default value for flag if the MX password could be saved. */
        private final boolean valMXSavePassword = false;

        /**
         * Name of the property for the flag if the MX communication is
         * logged.
         */
        private final String propMXLog = this.prefix + "MXLog";

        /** Default value for the MX communication logging. */
        private final boolean valMXLog = false;

        /**
         * Name of the property key if the update is done with the file
         * content.
         */
        private final String prefMXUpdateByFileContent = this.prefix + "MXUpdateByFileContent"; //$NON-NLS-1$

        /** Default value if the update is done with the file content. */
        private final boolean valMXUpdateByFileContent = true;

        /**
         * {@inheritDoc}
         */
        @Override()
        public String getTitle()
        {
            return Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("Title"));
        }

        /**
         * {@inheritDoc}
         */
        @Override()
        public void createContent(final Composite _parent,
                                  final ProjectProperties _properties)
        {
            // SSH connection settings
            final Group sshGroup = FieldUtil.createGroup(_parent,  this.prefix + "SSHGroup"); //$NON-NLS-1$
            FieldUtil.addSSHField(sshGroup, _properties, this.propSSHServer, this.propSSHPort, this.valSSHPort);
            FieldUtil.addStringField(sshGroup, _properties, this.propSSHUser, ""); //$NON-NLS-1$
            FieldUtil.addPasswordField(sshGroup, _properties, this.propSSHPassword, this.propSSHSavePassword);

            // MX connection settings
            final Group mxGroup = FieldUtil.createGroup(_parent,  this.prefix + "MXGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propMXMQLPath, this.valMXMQLPath);
            FieldUtil.addStringField(mxGroup, _properties, this.propMXUserName, ""); //$NON-NLS-1$
            FieldUtil.addPasswordField(mxGroup, _properties, this.propMXPassword, this.propMXSavePassword);

            FieldUtil.addBooleanField(_parent, _properties, this.propMXLog, this.valMXLog);
            FieldUtil.addBooleanField(_parent, _properties, this.prefMXUpdateByFileContent, this.valMXUpdateByFileContent);
        }

        /**
         * {@inheritDoc}
         *
         * @return <code>null</code> if property keys {@link #propSSHServer},
         *         {@link #propSSHUser}, {@link #propMXMQLPath} and
         *         {@link #propMXUserName} are not empty and
         *         {@link #propSSHPort} is between 0 and 65535;
         *         otherwise message with error text
         */
        @Override()
        public String isValid(final ProjectProperties _properties)
        {
            final String ret;
            if (_properties.getString(this.propSSHServer, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingSSHServer")); //$NON-NLS-1$
            } else if (_properties.isWrong(this.propSSHPort))  {
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("WrongSSHPort")); //$NON-NLS-1$
            } else if (_properties.getInteger(this.propSSHPort, this.valSSHPort) < 0)  {
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("SSHPortToLow"), //$NON-NLS-1$
                                         _properties.getInteger(this.propSSHPort, this.valSSHPort));
            } else if (_properties.getInteger(this.propSSHPort, this.valSSHPort) > 65535)  {
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("SSHPortToBig"), //$NON-NLS-1$
                                         _properties.getInteger(this.propSSHPort, this.valSSHPort));
            } else if (_properties.getString(this.propSSHUser, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingSSHUser")); //$NON-NLS-1$
            } else if (_properties.getString(this.propMXMQLPath, this.valMXMQLPath).isEmpty())  {
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingMXMQLPath")); //$NON-NLS-1$
            } else if (_properties.getString(this.propMXUserName, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingMXUserName")); //$NON-NLS-1$
            } else  {
                ret = null;
            }
            return ret;
        }

        /**
         * {@inheritDoc}
         * @see MXAdapter
         */
        @Override()
        public IDeploymentAdapter initAdapter(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
        {
            return new MXAdapter(_project, _properties, _console);
        }

        /**
         * {@inheritDoc}
         * The {@link SSHConnector} is initialized. Depending on the
         * {@link #propSSHSavePassword} and {@link #propMXSavePassword} the SSH
         * user name / password or the MX user name / password is asked.
         *
         * @see SSHConnector
         */
        @Override()
        public IConnector initConnector(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
            throws Exception
        {
            // SSH authentication
            final String sshServer = _properties.getString(this.propSSHServer, null);
            final int sshPort = _properties.getInteger(this.propSSHPort, this.valSSHPort);
            final String sshUser;
            final String sshPasswd;
            if (_properties.getBoolean(this.propSSHSavePassword, this.valSSHSavePassword))  {
                sshUser = _properties.getString(this.propSSHUser, null);
                sshPasswd = _properties.getPassword(this.propSSHPassword);
            } else  {
                final AuthenticationDialog loginPage = new AuthenticationDialog(
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("SSHGroup")),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propSSHUser)),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propSSHPassword)),
                        _properties.getString(this.propSSHUser, null));
                loginPage.open();
                if (!loginPage.isOkPressed())  {
                    throw new Exception(
                            Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterNotSSHAuthenticated")));
                }
                sshUser = loginPage.getUserName();
                sshPasswd = loginPage.getPassword();
            }

            // MX authentication
            final String mxUser;
            final String mxPasswd;
            if (_properties.getBoolean(this.propMXSavePassword, this.valMXSavePassword))  {
                mxUser = _properties.getString(this.propMXUserName, null);
                mxPasswd = _properties.getPassword(this.propMXPassword);
            } else  {
                final AuthenticationDialog loginPage = new AuthenticationDialog(
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MXGroup")),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propMXUserName)),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propMXPassword)),
                        _properties.getString(this.propMXUserName, null));
                loginPage.open();
                if (!loginPage.isOkPressed())  {
                    throw new Exception(
                            Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterNotMXAuthenticated")));
                }
                mxUser = loginPage.getUserName();
                mxPasswd = loginPage.getPassword();
            }

            _console.logInfo(
                    Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterConnectTo"), //$NON-NLS-1$
                                                sshServer, sshPort));

            return new SSHConnector(
                    sshServer, sshPort, sshUser, sshPasswd,
                    _properties.getString(this.propMXMQLPath, this.valMXMQLPath),
                    mxUser, mxPasswd,
                    _properties.getBoolean(this.propMXLog, this.valMXLog),
                    _properties.getBoolean(this.prefMXUpdateByFileContent, this.valMXUpdateByFileContent));
        }
    },

    /**
     * MxUpdate Plug-In uses MQL on a SSH server and the properties are stored
     * in an external property file.
     */
    MXUPDATE_SSH_MQL_WITH_PROPERTY_FILE
    {
        /** Prefix used for property keys. */
        private final String prefix = "MxUpdateViaSSHMQLWithPropFile."; //$NON-NLS-1$

        /** Name of the property key for the property file path. */
        private final String propPropPath = this.prefix + "PropFilePath"; //$NON-NLS-1$

        /** Name of the property key for the name of the SSH server. */
        private final String propSSHServer = this.prefix + "KeySSHServer"; //$NON-NLS-1$

        /** Name of the property key for the port of the SSH server. */
        private final String propSSHPort = this.prefix + "KeySSHPort"; //$NON-NLS-1$

        /** Default value for the SSH port. */
        private final int valSSHPort = 22;

        /** Name of the property key for the name of the SSH user. */
        private final String propSSHUser = this.prefix + "KeySSHUser"; //$NON-NLS-1$

        /** Name of the property key for the password of the SSH user. */
        private final String propSSHPassword = this.prefix + "KeySSHPassword"; //$NON-NLS-1$

        /**
         * Name of the property key for the path of the MQL command on the SSH
         * server.
         */
        private final String propMXMQLPath = this.prefix + "KeyMXMQLPath"; //$NON-NLS-1$

        /** Default value for the MQL path. */
        private final String valMXMQLPath = "mql"; //$NON-NLS-1$

        /** Name of the property key for the MX user name. */
        private final String propMXUserName = this.prefix + "KeyMXUserName"; //$NON-NLS-1$

        /** Name of the property key for the MX password. */
        private final String propMXPassword = this.prefix+ "KeyMXPassword"; //$NON-NLS-1$

        /**
         * Name of the property for the flag if the MX communication is
         * logged.
         */
        private final String propMXLog = this.prefix + "KeyMXLog";

        /** Default value for the MX communication logging. */
        private final boolean valMXLog = false;

        /**
         * Name of the property key if the update is done with the file
         * content.
         */
        private final String prefMXUpdateByFileContent = this.prefix + "KeyUpdateByFileContent"; //$NON-NLS-1$

        /** Default value if the update is done with the file content. */
        private final boolean valMXUpdateByFileContent = true;

        /**
         * {@inheritDoc}
         */
        @Override()
        public String getTitle()
        {
            return Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("Title"));
        }

        /**
         * {@inheritDoc}
         */
        @Override()
        public void createContent(final Composite _parent,
                                  final ProjectProperties _properties)
        {
            FieldUtil.addFileField(_parent, _properties, this.propPropPath, ""); //$NON-NLS-1$

            // SSH connection settings
            final Group sshGroup = FieldUtil.createGroup(_parent,  this.prefix + "SSHGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(sshGroup, _properties, this.propSSHServer, ""); //$NON-NLS-1$
            FieldUtil.addStringField(sshGroup, _properties, this.propSSHPort, ""); //$NON-NLS-1$
            FieldUtil.addStringField(sshGroup, _properties, this.propSSHUser, ""); //$NON-NLS-1$
            FieldUtil.addStringField(sshGroup, _properties, this.propSSHPassword, ""); //$NON-NLS-1$

            // MX connection settings
            final Group mxGroup = FieldUtil.createGroup(_parent,  this.prefix + "MXGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propMXMQLPath, ""); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propMXUserName, ""); //$NON-NLS-1$
            FieldUtil.addStringField(mxGroup, _properties, this.propMXPassword, "");

            // Other settings
            final Group other = FieldUtil.createGroup(_parent, this.prefix + "OtherGroup"); //$NON-NLS-1$
            FieldUtil.addStringField(other, _properties, this.propMXLog, ""); //$NON-NLS-1$
            FieldUtil.addStringField(other, _properties, this.prefMXUpdateByFileContent, ""); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         * @see MXAdapter
         */
        @Override()
        public IDeploymentAdapter initAdapter(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
        {
            return new MXAdapter(_project, _properties, _console);
        }

        @Override()
        public IConnector initConnector(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
        throws Exception
        {
            final String propFile           = _properties.getString(this.propPropPath, ""); //$NON-NLS-1$
            final String propKeySSHServer   = _properties.getString(this.propSSHServer, ""); //$NON-NLS-1$
            final String propKeySSHPort     = _properties.getString(this.propSSHPort, ""); //$NON-NLS-1$
            final String propKeySSHUser     = _properties.getString(this.propSSHUser, ""); //$NON-NLS-1$
            final String propKeySSHPassword = _properties.getString(this.propSSHPassword, ""); //$NON-NLS-1$
            final String propKeyMXMqlPath   = _properties.getString(this.propMXMQLPath, ""); //$NON-NLS-1$
            final String propKeyMXUser      = _properties.getString(this.propMXUserName, ""); //$NON-NLS-1$
            final String propKeyMXPassword  = _properties.getString(this.propMXPassword, ""); //$NON-NLS-1$
            final String propKeyLog         = _properties.getString(this.propMXLog, ""); //$NON-NLS-1$
            final String propKeyFileCnt     = _properties.getString(this.prefMXUpdateByFileContent, ""); //$NON-NLS-1$

            // read file
            _console.logInfo(Messages.getString(
                    new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterReadExternalFile"), propFile)); //$NON-NLS-1$
            final Properties extProps = new Properties();
            try {
                extProps.load(new FileInputStream(propFile));
            } catch (final FileNotFoundException e) {
                final String message = Messages.getString(
                        new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterFileOpenFailed"), propFile); //$NON-NLS-1$
                _console.logError(message, e); //$NON-NLS-1$
                throw new Exception(message, e);
            } catch (final IOException e) {
                final String message = Messages.getString(
                        new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterFileOpenFailed"), propFile); //$NON-NLS-1$
                _console.logError(message, e); //$NON-NLS-1$
                throw new Exception(message, e);
            }

            // SSH server
            final String sshServer = extProps.getProperty(propKeySSHServer);

            // SSH port
            final int sshPort = propKeySSHPort.isEmpty() ? this.valSSHPort : Integer.parseInt(extProps.getProperty(propKeySSHPort));

            // SSH user and SSH password
            final String sshUser;
            final String sshPasswd;
            if (propKeySSHUser.isEmpty() || propKeySSHPassword.isEmpty())  {
                final AuthenticationDialog loginPage = new AuthenticationDialog(
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("SSHGroupLogin")), //$NON-NLS-1$
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propSSHUser)),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propSSHPassword)),
                        extProps.getProperty(propKeySSHUser, ""));
                loginPage.open();
                if (!loginPage.isOkPressed())  {
                    throw new Exception(
                            Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX)
                                                    .append(this.prefix).append("InitAdapterNotSSHAuthenticated"))); //$NON-NLS-1$
                }
                sshUser = loginPage.getUserName();
                sshPasswd = loginPage.getPassword();
            } else  {
                sshUser = extProps.getProperty(propKeySSHUser);
                sshPasswd = extProps.getProperty(propKeySSHPassword);
            }

            // MX MQL path
            final String mxMqlPath;
            if (propKeyMXMqlPath.isEmpty())  {
                mxMqlPath = this.valMXMQLPath;
            } else  {
                mxMqlPath = extProps.getProperty(propKeyMXMqlPath);
            }

            // MX user and MX password
            final String mxUser;
            final String mxPasswd;
            if (propKeyMXPassword.isEmpty() || propKeyMXPassword.isEmpty())  {
                final AuthenticationDialog loginPage = new AuthenticationDialog(
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MXGroupLogin")), //$NON-NLS-1$
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propMXUserName)),
                        Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.propMXPassword)),
                        extProps.getProperty(propKeyMXUser, null));
                loginPage.open();
                if (!loginPage.isOkPressed())  {
                    throw new Exception(
                            Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("InitAdapterNotMXAuthenticated")));
                }
                mxUser = loginPage.getUserName();
                mxPasswd = loginPage.getPassword();
            } else  {
                mxUser = extProps.getProperty(propKeyMXPassword);
                mxPasswd = extProps.getProperty(propKeyMXPassword);
            }

            // update by file content flag
            final boolean flagLog = propKeyLog.isEmpty() ? this.valMXLog : Boolean.valueOf(extProps.getProperty(propKeyLog));

            // update by file content flag
            final boolean flagByFileContent = propKeyFileCnt.isEmpty() ? this.valMXUpdateByFileContent : Boolean.valueOf(extProps.getProperty(propKeyFileCnt));

            return new SSHConnector(
                    sshServer, sshPort, sshUser, sshPasswd,
                    mxMqlPath, mxUser, mxPasswd,
                    flagLog, flagByFileContent);
        }

        /**
         * {@inheritDoc}
         *
         * @return <code>null</code> if property keys
         *         {@link #propPropPath} or {@link #propSSHServer} are not
         *         empty
         */
        @Override()
        public String isValid(final ProjectProperties _properties)
        {
            final String ret;

            if (_properties.getString(this.propPropPath, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingPropFilePath")); //$NON-NLS-1$
            } else if (_properties.getString(this.propSSHServer, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingKeySSHServer")); //$NON-NLS-1$
            } else  {
                ret = null;
            }
            return ret;
        }
    },

    /**
     * MxUpdapte connection where all values are stored in external property
     * file.
     */
    MXUPDATE_WITH_PROPERTY_FILE
    {
        /** Prefix used for property keys. */
        private final String prefix = "MxUpdateWithPropFile."; //$NON-NLS-1$

        /** Name of the property key for the property file path. */
        private final String propPropPath = this.prefix + "PropFilePath"; //$NON-NLS-1$

        /**
         * {@inheritDoc}
         */
        @Override()
        public String getTitle()
        {
            return Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("Title"));
        }

        @Override()
        public void createContent(final Composite _parent,
                                  final ProjectProperties _properties)
        {
            FieldUtil.addFileField(_parent, _properties, this.propPropPath, ""); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         * @see MXAdapter
         */
        @Override()
        public IDeploymentAdapter initAdapter(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
        {
            return new MXAdapter(_project, _properties, _console);
        }

        @Override()
        public IConnector initConnector(final IProject _project,
                                              final ProjectProperties _properties,
                                              final Console _console)
            throws Exception
        {
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @return <code>null</code> if property key
         *         {@link #propPropPath} is not empty
         */
        @Override()
        public String isValid(final ProjectProperties _properties)
        {
            final String ret;

            if (_properties.getString(this.propPropPath, "").isEmpty())  { //$NON-NLS-1$
                ret = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(this.prefix).append("MissingPropFilePath")); //$NON-NLS-1$
            } else  {
                ret = null;
            }
            return ret;
        }
    };

    /**
     * Returns the title of the project mode.
     *
     * @return title of the project mode for the user interface
     */
    public abstract String getTitle();

    /**
     * Appends all specific fields needed to configure defined mode.
     *
     * @param _composite        parent composite where the content must be
     *                          appended
     * @param _properties       project specific properties
     */
    public abstract void createContent(final Composite _composite,
                                       final ProjectProperties _properties);

    /**
     * Initialize the adapter depending on the eclipse <code>_project</code>
     * for given <code>_properties</code>.
     *
     * @param _project      eclipse project
     * @param _properties   related properties of the eclipse
     *                      <code>_project</code>
     * @param _console      logging console
     * @return initialized adapter
     * @throws Exception if adapter could not be initialized
     */
    public abstract IDeploymentAdapter initAdapter(final IProject _project,
                                                   final ProjectProperties _properties,
                                                   final Console _console)
        throws Exception;

    /**
     * Initialized the connector and establish the connection to the database.
     *
     * @param _project      eclipse project
     * @param _properties   related properties of the eclipse
     *                      <code>_project</code>
     * @param _console      logging console
     * @return initialized connector
     * @throws Exception if connector could not be initialized
     */
    public abstract IConnector initConnector(final IProject _project,
                                             final ProjectProperties _properties,
                                             final Console _console)
        throws Exception;

    /**
     * Checks if the property values for current mode are valid.
     *
     * @param _properties   related property values
     * @return <i>true</i> if property values are valid; otherwise <i>false</i>
     */
    public abstract String isValid(final ProjectProperties _properties);

    /**
     * Initialize the adapter depending on the eclipse <code>_project</code>.
     *
     * @param _project      eclipse project for which the adapter is searched
     * @param _console      logging console
     * @return initialized adapter
     * @throws Exception if adapter could not be initialized
     */
    public static IDeploymentAdapter initAdapter(final IProject _project,
                                                 final Console _console)
        throws Exception
    {
        // load properties
        final ProjectProperties properties = new ProjectProperties(_project);
        // and connect
        return (properties.getMode() == ProjectMode.UNKNOWN)
               ? null
               : properties.getMode().initAdapter(_project, properties, _console);
    }

    /**
     * Initialized the connector and establish the connection to the database.
     *
     * @param _project      eclipse project
     * @param _console      logging console
     * @return initialized connector
     * @throws Exception if connector could not be initialized
     */
    public IConnector initConnector(final IProject _project,
                                    final Console _console)
        throws Exception
    {
            // load properties
            final ProjectProperties properties = new ProjectProperties(_project);
            // and connect
            return properties.getMode().initConnector(_project, properties, _console);
    }
}

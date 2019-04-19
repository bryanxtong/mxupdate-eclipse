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

package org.mxupdate.eclipse.mxadapter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.mxupdate.eclipse.Activator;
import org.mxupdate.eclipse.Messages;
import org.mxupdate.eclipse.adapter.IDeploymentAdapter;
import org.mxupdate.eclipse.adapter.IExportItem;
import org.mxupdate.eclipse.adapter.ISearchItem;
import org.mxupdate.eclipse.adapter.ITypeDefNode;
import org.mxupdate.eclipse.adapter.ITypeDefRoot;
import org.mxupdate.eclipse.console.Console;
import org.mxupdate.eclipse.mxadapter.connectors.IConnector;
import org.mxupdate.eclipse.properties.ProjectProperties;
import org.mxupdate.eclipse.util.CommunicationUtil;

/**
 * Adapter to the MX database.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class MXAdapter
    implements IDeploymentAdapter
{
    /**
     * Name and place of the manifest file.
     *
     * @see #getPlugInVersion()
     */
    private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF"; //$NON-NLS-1$

    /**
     * Label of the bundle version within the <code>META-INF/MANIFEST.MF</code>
     * file.
     *
     * @see #getPlugInVersion()
     */
    private static final String TEXT_BUNDLE_VERSION = "Bundle-Version:"; //$NON-NLS-1$

    /**
     * Length of the label of the bundle version within the
     * <code>META-INF/MANIFEST.MF</code> file.
     *
     * @see #getPlugInVersion()
     */
    private static final int LENGTH_BUNDLE_VERSION = MXAdapter.TEXT_BUNDLE_VERSION.length();

    /**
     * Name of the key in the return map for the log message.
     */
    private static final String RETURN_KEY_LOG = "log";

    /**
     * Name of the key in the return map for the error message.
     */
    private static final String RETURN_KEY_ERROR = "error";

    /**
     * Name of the key in the return map for the exception.
     */
    private static final String RETURN_KEY_EXCEPTION = "exception";

    /**
     * Name of the key in the return map for the values.
     */
    private static final String RETURN_KEY_VALUES = "values";

    /**
     * Regular expression for the package line. The package name must be
     * extracted to get the real name of the JPO used within MX.
     *
     * @see #extractMxName(IFile)
     */
    private static final Pattern PATTERN_PACKAGE = Pattern.compile("(?<=package)[ \\t]+[A-Za-z0-9\\._]*[ \\t]*;");

    /**
     * End of a file name which represents a JPO.
     *
     * @see #export(IFile)
     * @see #extractMxName(IFile)
     */
    private static final String END_JPO_FILE = "_mxJPO.java";

    /** Properties for the project. */
    private final ProjectProperties properties;

    /**
     * MxUpdate plug-in console.
     *
     * @see #showConsole()
     */
    private final Console console;

    /**
     * Map used to hold the images for MxUpdate files. The first key is the
     * file extension, the second key the file prefix.
     *
     * @see #initImageDescriptors()
     * @see #getImageDescriptor(IFile)
     */
    private final Map<String,Map<String,ImageDescriptor>> imageMap
            = new HashMap<String,Map<String,ImageDescriptor>>();

    /**
     * Mapping between type definition and related image descriptors.
     *
     * @see #initImageDescriptors()
     * @see #getImageDescriptor(String)
     */
    private final Map<String,ImageDescriptor> typeDef2Image = new HashMap<String,ImageDescriptor>();

    /**
     * Related Eclipse project for which this MX adapter is initiated.
     */
    private final IProject project;

    /**
     * Connector to the database.
     */
    private IConnector connector;

    /**
     * Initializes the MX adapter.
     *
     * @param _project          related Eclipse project
     * @param _properties       project properties
     * @param _console          console used for logging purposes
     */
    public MXAdapter(final IProject _project,
                     final ProjectProperties _properties,
                     final Console _console)
    {
        this.properties = _properties;
        this.project = _project;
        this.console = _console;
        this.initImageDescriptors();
    }

    /**
     * Initializes the image descriptors read from the plug in properties.
     *
     * @see #imageMap
     * @see #typeDef2Image
     */
    protected void initImageDescriptors()
    {
        final Properties imageConfig = new Properties();
        if (this.properties.getImageConfig() != null)  {
            final InputStream is = new ByteArrayInputStream(this.properties.getImageConfig().getBytes());
            try {
                imageConfig.load(is);
            } catch (final IOException e) {
                this.console.logError("MXAdapter.ExceptionInitImageDescriptorsLoadPropertiesFailed", e);
            }
        }

        // extract all admin type names
        final Set<String> admins = new HashSet<String>();
        for (final Object keyObj : imageConfig.keySet())  {
            final String key = keyObj.toString().replaceAll("\\..*", ""); //$NON-NLS-1$ //$NON-NLS-2$
            admins.add(key);
        }

        // remove already stored images
        this.typeDef2Image.clear();
        this.imageMap.clear();

        // prepare image cache
        for (final String admin : admins)  {
            final String prefix = imageConfig.getProperty(admin + ".FilePrefix"); //$NON-NLS-1$
            final String suffix = imageConfig.getProperty(admin + ".FileSuffix"); //$NON-NLS-1$
            final String iconStr = imageConfig.getProperty(admin + ".Icon"); //$NON-NLS-1$

            final byte[] bin = Base64.decodeBase64(iconStr.getBytes());
            final InputStream in = new ByteArrayInputStream(bin);

            final ImageDescriptor imageDesriptor = ImageDescriptor.createFromImageData(new ImageData(in));

            // mapping between file prefix / extension and image
            Map<String,ImageDescriptor> mapPrefix = this.imageMap.get(suffix);
            if (mapPrefix == null)  {
                mapPrefix = new HashMap<String,ImageDescriptor>();
                this.imageMap.put(suffix, mapPrefix);
            }
            mapPrefix.put(prefix, imageDesriptor);

            // mapping between type definition and image
            this.typeDef2Image.put(admin, imageDesriptor);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return <i>true</i> if the {@link #connector} is not null (means adapter
     *         is already connected); otherwise <i>false</i>
     * @see #connector
     */
    public boolean isConnected()
    {
        return (this.connector != null);
    }

    /**
     * Connects to the MX database.
     *
     * @throws Exception if connect failed
     * @see #connected
     * @see #mxContext
     * @see #checkVersions()
     */
    public void connect()
        throws Exception
    {
        if (this.connector != null)  {
            this.console.logInfo(Messages.getString("MXAdapter.AlreadyConnected")); //$NON-NLS-1$
        } else  {
            this.connector = this.properties.getMode().initConnector(this.project, this.console);

            // check versions
            this.checkVersions();

            try {
                // read properties
                final String newProps = (String) this.executeEncoded(null, "GetProperty", null).get(MXAdapter.RETURN_KEY_VALUES);
                final String curProps = this.properties.getImageConfig();

                // update if required
                if (!newProps.equals(curProps))  {
                    this.properties.storeImageConfig(newProps);
                    this.console.logInfo(Messages.getString("MXAdapter.PluginPropertiesChanged")); //$NON-NLS-1$

                    // reload image descriptors
                    this.initImageDescriptors();

                    // and refresh project
                    this.project.touch(new NullProgressMonitor());
                }

            } catch (final Exception e) {
                this.console.logError(Messages.getString("MXAdapter.ConnectFailed"), e); //$NON-NLS-1$
            }

        }
    }

    /**
     * Checks that the version of the MxUpdate Eclipse Plug-In and the version
     * of the MxUpdate Update Deployment Tool have the same major and minor
     * number within their versions. If not equal the plug-in automatically
     * disconnects from MX.
     *
     * @see #disconnect()
     * @see #getPlugInVersion()
     * @see #connect()
     */
    protected void checkVersions()
    {
        String pluginVersion = null;
        try {
            pluginVersion = this.getPlugInVersion();
        } catch (final IOException e) {
            this.console.logError(Messages.getString("MXAdapter.ExceptionGetPlugInVersion"), e); //$NON-NLS-1$
        }

        String updateVersionOrg = null;
        try {
            updateVersionOrg = (String) this.executeEncoded(null, "GetVersion", null).get(MXAdapter.RETURN_KEY_VALUES);
        } catch (final Exception e) {
            this.console.logError(Messages.getString("MXAdapter.ExceptionGetUpdateVersion"), e); //$NON-NLS-1$
        }
        final String updateVersion = (updateVersionOrg != null) ? updateVersionOrg.replace('-', '.') : null;

        final String[] pluginVersions = (pluginVersion != null) ? pluginVersion.split("\\.") : null; //$NON-NLS-1$
        final String[] updateVersions = (updateVersion != null) ? updateVersion.split("\\.") : null; //$NON-NLS-1$
        if ((pluginVersion == null) || (pluginVersions.length < 2)
                || (updateVersion == null) || (updateVersions.length < 2)
                || !pluginVersions[0].equals(updateVersions[0]) || !pluginVersions[1].equals(updateVersions[1]))  {
            this.console.logError(Messages.getString("MXAdapter.CheckVersionsNoConnectAllowed", //$NON-NLS-1$
                                                     pluginVersion,
                                                     updateVersion));
        }
    }

    /**
     * Returns for this plug-in the version stored within manifest file.
     *
     * @return found plug-in version within manifest file
     * @throws IOException if the manifest file could not found or not opened
     * @see #MANIFEST_FILE
     */
    protected String getPlugInVersion()
        throws IOException
    {
        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(MXAdapter.MANIFEST_FILE);
        final byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();

        final String manifest = new String(buffer);
        String version = null;
        for (final String line : manifest.split("\n"))  {
            if (line.startsWith(MXAdapter.TEXT_BUNDLE_VERSION))  {
                version = line.substring(MXAdapter.LENGTH_BUNDLE_VERSION).trim();
                break;
            }
        }

        return version;
    }

    /**
     * Disconnect from the MX database.
     *
     * @return <i>true</i> if already disconnected or disconnect from MX
     *         database was successfully; otherwise <i>false</i> is returned
     * @throws Exception if disconnect failed
     * @see #connected
     * @see #mxContext
     */
    public boolean disconnect()
        throws Exception
    {
        boolean disconnect = false;
        if (this.connector == null)  {
            this.console.logInfo(Messages.getString("MXAdapter.AlreadyDisconnected")); //$NON-NLS-1$
            disconnect = true;
        } else  {
            this.connector.disconnect();
            this.connector = null;
            this.console.logInfo(Messages.getString("MXAdapter.Disconnected")); //$NON-NLS-1$
        }
        return disconnect;
    }

    /**
     * Updates given MX update files in the MX database. If
     * {@link #PREF_UPDATE_FILE_CONTENT} is set, also the file content is
     * transfered within the update (e.g. if an update on another server is
     * done).
     *
     * @param _files    MxUpdate file which must be updated
     * @param _compile  if <i>true</i> all JPOs are compiled; if <i>false</i>
     *                  no JPOs are compiled, only an update is done
     * @throws Exception if update failed (or included connect)
     * @see #execMql(CharSequence)
     */
    public void update(final List<IFile> _files,
                       final boolean _compile)
        throws Exception
    {
        if (this.connector == null)  {
            this.connect();
        }

        // update by file content
        if (this.connector.isUpdateByFileContent())  {
            final Map<String,String> files = new HashMap<String,String>();
            for (final IFile file: _files)  {
                try  {
                    final InputStream in = new FileInputStream(file.getLocation().toFile());
                    final byte[] bytes = new byte[in.available()];
                    in.read(bytes);
                    in.close();
                    files.put(file.getLocation().toString(),
                              new String(bytes, file.getCharset()));
                } catch (final UnsupportedEncodingException e)  {
                    this.console.logError(Messages.getString("MXAdapter.ExceptionConvertFileContent", //$NON-NLS-1$
                                                             file.getLocation().toString()),
                                          e);
                } catch (final CoreException e) {
                    this.console.logError(Messages.getString("MXAdapter.ExceptionFileCharSet", //$NON-NLS-1$
                                                             file.getLocation().toString()),
                                          e);
                } catch (final IOException e) {
                    this.console.logError(Messages.getString("MXAdapter.ExceptionReadFileContentFailed", //$NON-NLS-1$
                                                             file.getLocation().toString()),
                                          e);
                }
            }
            try {
                final Map<?,?> bck = this.executeEncoded(new String[]{"Compile", String.valueOf(_compile)},
                                                         "Update",
                                                         new Object[]{"FileContents", files});
                this.console.appendLog((String) bck.get(MXAdapter.RETURN_KEY_LOG));
                final Exception ex = (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION);
                if (ex != null)  {
                    this.console.logError(Messages.getString("MXAdapter.ExceptionUpdateFailed",  //$NON-NLS-1$
                                                             files.keySet().toString()),
                                          ex);
                }
            } catch (final Exception e)  {
                this.console.logError(Messages.getString("MXAdapter.ExceptionUpdateFailed",  //$NON-NLS-1$
                                                         files.keySet().toString()),
                                      e);
            }
        // update by file names
        } else  {
            final Set<String> fileNames = new HashSet<String>();
            for (final IFile file: _files)  {
                fileNames.add(file.getLocation().toString());
            }
            try {
                final Map<?,?> bck = this.executeEncoded(new String[]{"Compile", String.valueOf(_compile)},
                                                         "Update",
                                                         new Object[]{"FileNames", fileNames});
                this.console.appendLog((String) bck.get(MXAdapter.RETURN_KEY_LOG));
                final Exception ex = (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION);
                if (ex != null)  {
                    this.console.logError(Messages.getString("MXAdapter.ExceptionUpdateFailed",  //$NON-NLS-1$
                                                             fileNames.toString()),
                                          ex);
                }
            } catch (final Exception e)  {
                this.console.logError(Messages.getString("MXAdapter.ExceptionUpdateFailed", //$NON-NLS-1$
                                                         fileNames.toString()),
                                      e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * The type definition root is evaluated directly in MX and only converted
     * in the required format.
     */
    public ITypeDefRoot getTypeDefRoot()
        throws Exception
    {
        if (this.connector == null)  {
            this.connect();
        }

        Map<?,?> bck = null;
        try {
            bck = this.executeEncoded(null, "TypeDefTreeList", null);
        } catch (final Exception e) {
            this.console.logError(Messages.getString("MXAdapter.ExceptionRootTypeDefFailed"), e); //$NON-NLS-1$
        }

        final ITypeDefRoot ret;
        if (bck == null)  {
            ret = null;
        } else if (bck.get(MXAdapter.RETURN_KEY_EXCEPTION) != null)  {
            ret = null;
            this.console.logError(Messages.getString("MXAdapter.ExceptionRootTypeDefFailed"), //$NON-NLS-1$
                                  (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION));
        } else  {
            final Map<?,?> treeMap = (Map<?,?>) bck.get(MXAdapter.RETURN_KEY_VALUES);
            ret = new ITypeDefRoot()  {
                private final Collection<ITypeDefNode> typeDefNodes = new ArrayList<ITypeDefNode>();
                public Collection<ITypeDefNode> getSubTypeDef()
                {
                    return this.typeDefNodes;
                }
            };
            this.appendSubNode(ret, (Map<?,?>) treeMap.get("All"), treeMap);
        }
        return ret;
    }

    /**
     * Appends to the <code>_node</code> all required / defined sub nodes.
     *
     * @param _node         node where sub nodes must be appended
     * @param _treeNode     current tree node which must be appended
     * @param _treeMap      map with all tree definitions from MX
     */
    protected void appendSubNode(final ITypeDefRoot _node,
                                 final Map<?,?> _treeNode,
                                 final Map<?,?> _treeMap)
    {
        final Collection<?> typeDefTreeList = (Collection<?>) _treeNode.get("TypeDefTreeList");
        if (typeDefTreeList != null)  {
            for (final Object subTreeNameObj : typeDefTreeList)  {
                if ((subTreeNameObj != null) && !"".equals(subTreeNameObj))  {
                    final Map<?,?> subTreeNode = (Map<?,?>) _treeMap.get(subTreeNameObj);
                    final String subLabel = (String) subTreeNode.get("Label");
                    final Collection<?> subTypeDefs = (Collection<?>) subTreeNode.get("TypeDefList");
                    final String[] typeDefArr;
                    if ((subTypeDefs != null) && !subTypeDefs.isEmpty())  {
                        typeDefArr = new String[subTypeDefs.size()];
                        int idx = 0;
                        for (final Object typeDefObj : subTypeDefs)  {
                            typeDefArr[idx++] = (String) typeDefObj;
                        }
                    } else  {
                        typeDefArr = new String[0];
                    }
                    final ITypeDefNode subNode = new ITypeDefNode() {
                        private final Collection<ITypeDefNode> typeDefNodes = new ArrayList<ITypeDefNode>();
                        public String getLabel()
                        {
                            return subLabel;
                        }
                        public Set<String> getTypeDefs()
                        {
                            return new HashSet<String>(Arrays.asList(typeDefArr));
                        }
                        public Collection<ITypeDefNode> getSubTypeDef()
                        {
                            return this.typeDefNodes;
                        }
                    };
                    this.appendSubNode(subNode, subTreeNode, _treeMap);
                    _node.getSubTypeDef().add(subNode);
                }
            }
        }
    }

    /**
     * Searches for configuration items within MX.
     *
     * @param _typeDefList  list with searched type definitions
     * @param _match        string for the names with must match
     * @return found search items
     */
    public List<ISearchItem> search(final Set<String> _typeDefList,
                                    final String _match)
    {
        Map<?,?> bck = null;
        try {
            bck = this.executeEncoded(null,
                                      "Search",
                                      new Object[]{"TypeDefList", _typeDefList,
                                                   "Match", _match});
        } catch (final Exception e) {
            this.console.logError(Messages.getString("MXAdapter.ExceptionExportFailed"), e); //$NON-NLS-1$
        }

        final List<ISearchItem> ret = new ArrayList<ISearchItem>();
        if (bck.get(MXAdapter.RETURN_KEY_EXCEPTION) != null)  {
            this.console.logError(Messages.getString("MXAdapter.ExceptionSearchFailed"), //$NON-NLS-1$
                                  (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION));
        } else  {
            final List<?> values = (List<?>) bck.get(MXAdapter.RETURN_KEY_VALUES);
            for (final Object valueObj : values)  {
                final Map<?,?> value = (Map<?,?>) valueObj;
                ret.add(new ISearchItem() {
                    public String getFileName()
                    {
                        return (String) value.get("FileName");
                    }
                    public String getFilePath()
                    {
                        return (String) value.get("FilePath");
                    }
                    public String getName()
                    {
                        return (String) value.get("Name");
                    }
                    public String getTypeDef()
                    {
                        return (String) value.get("TypeDef");
                    }
                });
            }
        }
        return ret;
    }

    /**
     * Extract the TCL update code for given <code>_file</code> from MX.
     *
     * @param _file     name of the update file for which the TCL update code
     *                  within MX must be extracted
     * @return configuration item update code for given <code>_file</code>
     * @throws Exception if export failed
     */
    public IExportItem export(final IFile _file)
        throws Exception
    {
        final IExportItem ret;

        if (this.connector == null)  {
            this.connect();
        }

        // if the file is a JPO, the package name is included in the MX name
        if (_file.getName().endsWith(MXAdapter.END_JPO_FILE))  {
            // hard coded for the workaround that the package of a JPO must read
            ret = this.export("JPO", this.extractMxName(_file));
        } else  {
            final Map<?,?> bck = this.executeEncoded(null, "Export", new Object[]{"FileName", _file.getName()});

            if (bck.get(MXAdapter.RETURN_KEY_EXCEPTION) != null)  {
                throw (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION);
            }

            final Map<?,?> value = (Map<?,?>) bck.get(MXAdapter.RETURN_KEY_VALUES);
            Activator.getDefault().getConsole().logInfo(Messages.getString("MXAdapter.ExportLog", //$NON-NLS-1$
                                                                           (String) value.get("FileName"))); //$NON-NLS-1$
            Activator.getDefault().getConsole().appendLog((String) bck.get(MXAdapter.RETURN_KEY_LOG));
            ret = new IExportItem() {
                public String getFileName()
                {
                    return (String) value.get("FileName");
                }
                public String getFilePath()
                {
                    return (String) value.get("FilePath");
                }
                public String getName()
                {
                    return (String) value.get("Name");
                }
                public String getTypeDef()
                {
                    return (String) value.get("TypeDef");
                }
                public String getContent()
                {
                    return (String) value.get("Code");
                }
            };
        }

        return ret;
    }

    /**
     * If a file is a JPO (checked by calling the extraxtMxName method from
     * super class), the package is extracted from file and returned together
     * with the extracted MxName from the file.
     *
     * @param _file         file for which the MX name is searched
     * @return MX name or <code>null</code> if the file is not an update file
     *         for current type definition
     * @throws CoreException    if file content incl. character set could not
     *                          be fetched
     * @throws IOException      if file could not be read
     * @see #PATTERN_PACKAGE
     */
    protected String extractMxName(final IFile _file)
        throws CoreException, IOException
    {
        final InputStream in = _file.getContents();
        final byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();

        final String code = new String(bytes, _file.getCharset());

        String mxName = _file.getName().substring(0, _file.getName().length() - MXAdapter.END_JPO_FILE.length());
        for (final String line : code.split("\n"))  {
            final Matcher pckMatch = MXAdapter.PATTERN_PACKAGE.matcher(line);
            if (pckMatch.find())  {
                mxName = pckMatch.group().replace(';', ' ').trim() + "." + mxName;
                break;
            }
        }
        return mxName;
    }


    /**
     * {@inheritDoc}
     */
    public IExportItem export(final String _typeDef,
                              final String _item)
    {
        Map<?,?> bck = null;
        try {
            bck = this.executeEncoded(null,
                                      "Export",
                                      new Object[]{"TypeDef", _typeDef,
                                                   "Name", _item});
        } catch (final Exception e) {
            this.console.logError(Messages.getString("MXAdapter.ExceptionExportFailed"), e); //$NON-NLS-1$
        }

        final IExportItem ret;
        if (bck.get(MXAdapter.RETURN_KEY_EXCEPTION) != null)  {
            ret = null;
            this.console.logError(Messages.getString("MXAdapter.ExceptionExportFailed"), //$NON-NLS-1$
                                  (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION));
        } else  {
            final Map<?,?> value = (Map<?,?>) bck.get(MXAdapter.RETURN_KEY_VALUES);
            Activator.getDefault().getConsole().logInfo(Messages.getString("MXAdapter.ExportLog", //$NON-NLS-1$
                                                                           (String) value.get("FileName")));
            Activator.getDefault().getConsole().appendLog((String) bck.get(MXAdapter.RETURN_KEY_LOG));
            ret = new IExportItem() {
                public String getFileName()
                {
                    return (String) value.get("FileName");
                }
                public String getFilePath()
                {
                    return (String) value.get("FilePath");
                }
                public String getName()
                {
                    return (String) value.get("Name");
                }
                public String getTypeDef()
                {
                    return (String) value.get("TypeDef");
                }
                public String getContent()
                {
                    return (String) value.get("Code");
                }
            };
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @see #imageMap
     */
    public ImageDescriptor getImageDescriptor(final IFile _file)
    {
        final String name = _file.getName();

        ImageDescriptor ret = null;
        for (final Map.Entry<String,Map<String,ImageDescriptor>> suffixEntry : this.imageMap.entrySet())  {
            if (name.endsWith(suffixEntry.getKey()))  {
                for (final Map.Entry<String, ImageDescriptor> entry : suffixEntry.getValue().entrySet())  {
                    if (name.startsWith(entry.getKey()))  {
                        ret = entry.getValue();
                        break;
                    }
                }
                break;
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @see #typeDef2Image
     */
    public ImageDescriptor getImageDescriptor(final String _typeDef)
    {
        return this.typeDef2Image.get(_typeDef);
    }

    /**
     * {@inheritDoc}
     * To do this the plug-in method &quot;<code>Execute</code>&quot; is called
     * on the server.
     *
     * @param _command  MQL command to execute
     * @return result of the MQL execution
     * @throws Exception if MQL execution fails or a connect failed
     * @see #connect()
     */
    public String execute(final CharSequence _command)
        throws Exception
    {
        if (this.connector == null)  {
            this.connect();
        }

        final Map<?,?> bck = this.executeEncoded(null, "Execute", new Object[]{"Command", _command});

        if (bck.get(MXAdapter.RETURN_KEY_EXCEPTION) != null)  {
            throw (Exception) bck.get(MXAdapter.RETURN_KEY_EXCEPTION);
        }

        return (String) bck.get(MXAdapter.RETURN_KEY_VALUES);
    }

    /**
     * Calls given <code>_method</code> in of the MxUpdate eclipse plug-in
     * dispatcher. The MX context {@link #mxContext} is connected to the
     * database if not already done.
     *
     * @param _parameters   parameters
     * @param _method       method of the called <code>_jpo</code>
     * @param _arguments    list of all parameters for the <code>_jpo</code>
     *                      which are automatically encoded encoded
     * @return returned value from the called <code>_jpo</code>
     * @throws Exception    if the parameter could not be encoded, or if the
     *                      called <code>_jpo</code> throws an exception, or
     *                      if the class which is decoded from the returned
     *                      string value could not be found
     * @see #mxContext
     * @see #connect()
     */
    protected Map<?,?> executeEncoded(final String[] _parameters,
                                      final String _method,
                                      final Object[] _arguments)
        throws Exception
    {
        // prepare parameters in a map
        final Map<String,String> parameters;
        if ((_parameters == null) || (_parameters.length == 0))  {
            parameters = null;
        } else  {
            parameters = new HashMap<String,String>();
            for (int idx = 0; idx < _parameters.length; )  {
                parameters.put(_parameters[idx++], _parameters[idx++]);
            }
        }

        // prepare arguments in a map
        final Map<String,Object> arguments;
        if ((_arguments == null) || (_arguments.length == 0))  {
            arguments = null;
        } else  {
            arguments = new HashMap<String,Object>();
            for (int idx = 0; idx < _arguments.length; )  {
                arguments.put((String) _arguments[idx++], _arguments[idx++]);
            }
        }

        final String bck = this.connector.execute(
                CommunicationUtil.encode(parameters),
                CommunicationUtil.encode(_method),
                CommunicationUtil.encode(arguments));

        return CommunicationUtil.<Map<?,?>>decode(bck);
    }
}

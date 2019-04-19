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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Wrapper for project specific properties to get better interface for values
 * depending on different types. Also the event handling needed to update the
 * related {@link ProjectPropertyPage project property page} if a property is
 * updated is done.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class ProjectProperties
{
    /**
     * Name of the property file name.
     *
     * @see #load()
     * @see #store()
     */
    protected static final String PROP_FILE_NAME = ".org.mxupdate.properties";

    /**
     * Prefix used for the translation of the messages.
     */
    public static final String MSG_PREFIX = "ProjectProperty."; //$NON-NLS-1$

    /**
     * Key of the property for the cached image configuration.
     */
    private static final String PROP_IMAGE_CONFIG = "ImageConfiguration"; //$NON-NLS-1$

    /**
     * Key where the project mode is stored.
     */
    private static final String PREF_MODE = "mode"; //$NON-NLS-1$

    /**
     * Used encoding for strings.
     */
    private static final String ENCODING = "UTF8"; //$NON-NLS-1$

    /**
     * Algorithm of the password based encryption.
     *
     * @see #decryptEncrypt(String, boolean)
     */
    private static final String PDE_ALGORITHM = "PBEWithMD5AndDES"; //$NON-NLS-1$

    /**
     * Password used for the {@link #PDE_ALGORITHM password based encryption}.
     *
     * @see #decryptEncrypt(String, boolean)
     */
    private static final char[] PDE_PASSWORD = "grtlu3#3f6sr87*".toCharArray(); //$NON-NLS-1$

    /**
     * The salt used for the {@link #PDE_ALGORITHM password based encryption}.
     *
     * @see #decryptEncrypt(String, boolean)
     */
    private static final byte[] PDE_SALT = {(byte) 0xA0, (byte) 0x1B, (byte) 0x1C, (byte) 0xB5, (byte) 0x86, (byte) 0x3D, (byte) 0xE2, (byte) 0xA3 };

    /**
     * The iteration count used for the
     * {@link #PDE_ALGORITHM password based encryption}.
     *
     * @see #decryptEncrypt(String, boolean)
     */
    private static final int PDE_ITERATION = 19;

    /**
     * All properties.
     */
    private final Properties properties = new Properties();

    /**
     * Defines keys of current wrong defined properties.
     *
     * @see #setWrong(String)
     */
    private final Set<String> wrongs = new HashSet<String>();

    /**
     * References the property page to update the valid state.
     *
     * @see #ProjectProperties(ProjectPropertyPage)
     * @see #checkValuesValid()
     */
    private final ProjectPropertyPage propertyPage;

    /**
     * Project mode for which this properties are defined.
     */
    private ProjectMode mode;

    /**
     * Related project of the properties.
     */
    private final IProject project;

    /**
     * Initializes the properties for no property page.
     *
     * @param _project      project for which the property file is opened
     * @throws CoreException    if property file could not be created
     * @throws IOException      if property file could not be opened and read
     */
    public ProjectProperties(final IProject _project)
        throws CoreException, IOException
    {
        this.project = _project;
        this.propertyPage = null;
        this.load();
    }

    /**
     * Initializes the properties for defined <code>_propertyPage</code>.
     *
     * @param _project          project for which the property file is opened
     * @param _propertyPage     related property page
     */
    public ProjectProperties(final IProject _project,
                             final ProjectPropertyPage _propertyPage)
    {
        this.project = _project;
        this.propertyPage = _propertyPage;
    }

    /**
     * Returns the string of the image configuration.
     *
     * @return string of the image configuration
     */
    public String getImageConfig()
    {
        return this.getString(ProjectProperties.PROP_IMAGE_CONFIG, null);
    }

    /**
     * Stores new image configuration <code>_imageConf</code> in the
     * properties. To be sure that the newest properties exists, the properties
     * will be loaded first, then updated and at least stored.
     *
     * @param _imageConf        new image configuration
     * @throws Exception if storing of the properties failed
     */
    public void storeImageConfig(final String _imageConf)
        throws Exception
    {
        this.properties.clear();
        this.load();
        this.setString(ProjectProperties.PROP_IMAGE_CONFIG, _imageConf);
        this.store();
    }

    /**
     * Returns for given <code>_key</code> related string value in the
     * {@link #properties}. If no value for the <code>_key</code> is defined,
     * the <code>_default</code> is returned.
     *
     * @param _key          key of the property
     * @param _default      default string value of the property
     * @return if key is defined related value; otherwise <code>_default</code>
     * @see #properties
     */
    public String getString(final String _key,
                            final String _default)
    {
        return this.properties.getProperty(_key, _default);
    }

    /**
     * Updates the property with <code>_key</code> in the {@link #properties}.
     * If the string property with the <code>_key</code> was signed as wrong,
     * the "wrong" flag is {@link #wrongs removed}. A
     * {@link #checkValuesValid() valid check} is done for the
     * {@link ProjectPropertyPage project property page} (if the user could
     * press the &quot;done&quot; button)..
     *
     * @param _key      key of the property
     * @param _value    new string value of the property
     * @see #properties
     * @see #wrongs
     * @see #checkValuesValid()
     */
    public void setString(final String _key,
                          final String _value)
    {
        this.properties.setProperty(_key, _value);
        this.wrongs.remove(_key);
        this.checkValuesValid();
    }

    /**
     * Returns the password from the {@link #properties} with given
     * <code>_key</code>. Because the passwords are encrypted in the
     * {@link #properties}, the password is
     * {@link #decryptEncrypt(String, boolean) decrypted} and then returned.
     *
     * @param _key  key of the password
     * @return decrypted password string; if no password is defined in
     *         {@link #properties} an empty string is returned
     * @see #properties
     */
    public String getPassword(final String _key)
    {
        final String ret;
        if (this.properties.containsKey(_key))  {
            ret = this.decryptEncrypt(this.properties.getProperty(_key), true);
        } else  {
            ret = "";
        }
        return ret;
    }

    /**
     * Defines with given <code>_key</code> a new password in the
     * {@link #properties}. The password is stored
     * {@link #decryptEncrypt(String, boolean) encrypted}. If the password with
     * the <code>_key</code> was signed as wrong, the "wrong" flag is
     * {@link #wrongs removed}. A{@link #checkValuesValid() valid check} is
     * done for the {@link ProjectPropertyPage project property page} (if the
     * user could press the &quot;done&quot; button)..
     *
     * @param _key      key of the password
     * @param _value    new password
     * @see #properties
     * @see #wrongs
     * @see #checkValuesValid()
     */
    public void setPassword(final String _key,
                            final String _value)
    {
        this.properties.setProperty(_key, this.decryptEncrypt(_value, false));
        this.wrongs.remove(_key);
        this.checkValuesValid();
    }

    /**
     * Returns for given <code>_key</code> related boolean value in the
     * {@link #properties}. If no value for the <code>_key</code> is defined,
     * the <code>_default</code> is returned.
     *
     * @param _key          key of the property
     * @param _default      default boolean value of the property
     * @return if key is defined related value; otherwise <code>_default</code>
     * @see #properties
     */
    public boolean getBoolean(final String _key,
                              final boolean _default)
    {
        return Boolean.valueOf(this.properties.getProperty(_key, String.valueOf(_default)));
    }

    /**
     * Updates the property with <code>_key</code> in the {@link #properties}.
     * The <code>_value</code> is converted to related string representation.
     * If the boolean property with the <code>_key</code> was signed as wrong,
     * the "wrong" flag is {@link #wrongs removed}. A
     * {@link #checkValuesValid() valid check} is done for the
     * {@link ProjectPropertyPage project property page} (if the user could
     * press the &quot;done&quot; button)..
     *
     * @param _key      key of the property
     * @param _value    new boolean value of the property
     * @see #properties
     * @see #wrongs
     * @see #checkValuesValid()
     */
    public void setBoolean(final String _key,
                           final boolean _value)
    {
        this.properties.setProperty(_key, String.valueOf(_value));
        this.wrongs.remove(_key);
        this.checkValuesValid();
    }

    /**
     * Returns for given <code>_key</code> related integer value in the
     * {@link #properties}. If no value for the <code>_key</code> is defined,
     * the <code>_default</code> is returned.
     *
     * @param _key          key of the property
     * @param _default      default integer value of the property
     * @return if key is defined related value; otherwise <code>_default</code>
     * @see #properties
     */
    public int getInteger(final String _key,
                          final int _default)
    {
        return Integer.valueOf(this.properties.getProperty(_key, String.valueOf(_default)));
    }

    /**
     * Updates the property with <code>_key</code> in the {@link #properties}.
     * The <code>_value</code> is converted to a string representation.
     * If the integer property with the <code>_key</code> was signed as wrong,
     * the "wrong" flag is {@link #wrongs removed}. A
     * {@link #checkValuesValid() valid check} is done for the
     * {@link ProjectPropertyPage project property page} (if the user could
     * press the &quot;done&quot; button)..
     *
     * @param _key      key of the property
     * @param _value    new integer value of the property
     * @see #properties
     * @see #wrongs
     * @see #checkValuesValid()
     */
    public void setInteger(final String _key,
                           final int _value)
    {
        this.properties.setProperty(_key, String.valueOf(_value));
        this.wrongs.remove(_key);
        this.checkValuesValid();
    }

    /**
     * Returns current mode defined with key {@link #PREF_MODE} in the
     * {@link #properties}. If the value is loaded once the value will be
     * cached.
     *
     * @return defined mode in the properties
     * @see #PREF_MODE
     * @see #mode
     */
    public ProjectMode getMode()
    {
        if (this.mode == null)  {
            this.mode = ProjectMode.valueOf(this.properties.getProperty(ProjectProperties.PREF_MODE, ProjectMode.UNKNOWN.name()));
        }
        return this.mode;
    }

    /**
     * Defines the new mode of the project. The mode is stored in the
     * {@link #properties} with key {@link #PREF_MODE}.
     *
     * @param _mode     new mode of the project
     * @see #PREF_MODE
     * @see #mode
     */
    public void setMode(final ProjectMode _mode)
    {
        this.mode = _mode;
        this.properties.setProperty(ProjectProperties.PREF_MODE, _mode.name());
        this.checkValuesValid();
    }

    /**
     * Removes given property <code>_key</code> from {@link #properties}.
     *
     * @param _key  key of the property to remove
     */
    public void remove(final String _key)
    {
        this.properties.remove(_key);
        this.checkValuesValid();
    }

    /**
     * Property with <code>_key</code> is defined as wrong. The related value
     * is removed from {@link #properties}.
     *
     * @param _key      key of the property which is wrong
     * @see #wrongs
     */
    public void setWrong(final String _key)
    {
        this.wrongs.add(_key);
        this.remove(_key);
    }

    /**
     * Checks if the property with <code>_key</code> is wrong.
     *
     * @param _key      property key to check
     * @return <i>true</i> if property with <code>_key</code> is wrong;
     *         otherwise <i>false</i>
     */
    public boolean isWrong(final String _key)
    {
        return this.wrongs.contains(_key);
    }

    /**
     * Checks if the defined properties for current mode are correct.
     */
    public void checkValuesValid()
    {
        if (this.propertyPage != null)  {
            final String error = this.getMode().isValid(this);
            this.propertyPage.setValid(error == null);
            this.propertyPage.setErrorMessage(error);
        }
    }

    /**
     * Loads from given <code>_file</code> the properties (if
     * <code>_file</code> exists).
     *
     * @throws CoreException    if file could not be created
     * @throws IOException      if file could not be opened and read
     * @see #PROP_FILE_NAME
     */
    public void load()
        throws CoreException, IOException
    {
        final IFile file = this.project.getFile(ProjectProperties.PROP_FILE_NAME);
        this.properties.clear();
        file.refreshLocal(IResource.DEPTH_ZERO, null);
        if (file.exists())  {
            this.properties.load(file.getContents(true));
        }
    }

    /**
     * Stores current values of the {@link #properties} in given
     * <code>_file</code>. If the <code>_file</code> does not exists, the file
     * is created.
     *
     * @throws CoreException    if file could not be created or updated
     * @throws IOException      if properties could not be written
     * @see #PROP_FILE_NAME
     */
    public void store()
        throws CoreException, IOException
    {
        final IFile file = this.project.getFile(ProjectProperties.PROP_FILE_NAME);
        file.refreshLocal(IResource.DEPTH_ZERO, null);
        if (!file.exists())  {
            file.create(new ByteArrayInputStream(new byte[]{}), true, null);
        }
        final StringWriter writer = new StringWriter();
        this.properties.store(writer, null);
        final String content = writer.getBuffer().toString();
        writer.close();

        file.setContents(new ByteArrayInputStream(content.getBytes(ProjectProperties.ENCODING)), true, false, null);
    }


    /**
     * Returns encrypted/decrypted by salt password. Uses SHA-1 Message Digest
     * Algorithm as defined in NIST's FIPS 180-1. The output of this algorithm
     * is a 160-bit digest.
     *
     * @param _password     password to encrypt / decrypt
     * @param _decrypt      <i>true</i> to decrypt or <i>false</i> to encrypt
     * @return decrypted / encrypted by salt password
     * @see #PDE_ALGORITHM
     * @see #PDE_PASSWORD
     * @see #PDE_SALT
     * @see #PDE_ITERATION
     */
    private String decryptEncrypt(final String _password,
                                  final boolean _decrypt)
    {
        String ret = null;
        if (_password != null) {
            try {
                // create PBE parameter set
                final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(ProjectProperties.PDE_SALT, ProjectProperties.PDE_ITERATION);
                final PBEKeySpec pbeKeySpec = new PBEKeySpec(ProjectProperties.PDE_PASSWORD, ProjectProperties.PDE_SALT, ProjectProperties.PDE_ITERATION);
                final SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ProjectProperties.PDE_ALGORITHM);
                final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

                final Cipher cipher = Cipher.getInstance(pbeKey.getAlgorithm());

                if (_decrypt) {
                    cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
                    // decode base64 to get bytes
                    final byte[] dec = Base64.decodeBase64(_password.getBytes(ProjectProperties.ENCODING));
                    // decrypt
                    final byte[] ciphertext = cipher.doFinal(dec);

                    ret = new String(ciphertext, ProjectProperties.ENCODING);

                } else {
                    cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
                    final byte[] pwdText = _password.getBytes(ProjectProperties.ENCODING);
                    // encrypt the cleartext
                    final byte[] ciphertext = cipher.doFinal(pwdText);

                    ret = new String(Base64.encodeBase64(ciphertext), ProjectProperties.ENCODING);
                }
            } catch (final Exception e) {
                throw new Error(e);
            }
        }
        return ret;
    }
}

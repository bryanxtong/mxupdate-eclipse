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

package org.mxupdate.eclipse;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class to handle message translation for the MxUpdate eclipse
 * plug-in.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public final class Messages
{
    /**
     * Name of the property bundle holding all messages.
     */
    private static final String BUNDLE_NAME = "src/main/resources/plugin"; //$NON-NLS-1$

    /**
     * Resource bundle to the MxUpdate eclipse plug-in properties.
     */
    private static final ResourceBundle RESOURCE_BUNDLE
            = ResourceBundle.getBundle(Messages.BUNDLE_NAME);

    /**
     * Constructor is defined to avoid creating an instance of this message
     * utility class.
     */
    private Messages()
    {
    }

    /**
     * Translated given key by using the resource bundle
     * {@link #RESOURCE_BUNDLE}. The translated key is then formated (with the
     * message formatter {@link MessageFormat}). If the key could not be
     * translated, the key itself surrounded with <code>!!!</code> is returned.
     *
     * @param _key          key to translate
     * @param _arguments    arguments to used to fill into the translated
     *                      string
     * @return translated string for key; or if key could not be translated,
     *         original key surrounded with &quot;!!!&quot;.
     * @see #RESOURCE_BUNDLE
     */
    public static String getString(final CharSequence _key,
                                   final Object... _arguments)
    {
        String ret;
        try {
            ret = Messages.RESOURCE_BUNDLE.getString(_key.toString());
        } catch (final MissingResourceException e) {
            ret = "!!!" + _key + "!!!";
        }
        return MessageFormat.format(ret, _arguments);
    }
}

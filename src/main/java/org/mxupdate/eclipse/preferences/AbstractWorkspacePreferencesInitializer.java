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

package org.mxupdate.eclipse.preferences;

import java.util.Collection;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.mxupdate.eclipse.Activator;

/**
 *
 * @author The MxUpdate Team
 *
 * @param <T>
 * @author The MxUpdate Team
 * @version $Id$
 */
public abstract class AbstractWorkspacePreferencesInitializer<T extends AbstractWorkspacePreference<?>>
        extends AbstractPreferenceInitializer
{
    /**
     *
     * @param _preferences  collection of prefernces to initialize
     */
    public void initializeDefaultPreferences(final Collection<T> _preferences)
    {
        final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        for (final AbstractWorkspacePreference<?> pref : _preferences)  {
            if (pref.getDefaultValue() != null)  {
                if (pref.getDefaultValue() instanceof RGB)  {
                    PreferenceConverter.setDefault(prefs, pref.name(), (RGB) pref.getDefaultValue());
                } else if (pref.getDefaultValue() instanceof FontData)  {
                    PreferenceConverter.setDefault(prefs, pref.name(), (FontData) pref.getDefaultValue());
                } else if (pref.getDefaultValue() instanceof Integer)  {
                    prefs.setDefault(pref.name(), (Integer) pref.getDefaultValue());
                }
            }
        }
    }
}

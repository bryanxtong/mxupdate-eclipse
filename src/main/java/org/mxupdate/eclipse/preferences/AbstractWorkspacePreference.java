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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.mxupdate.eclipse.Activator;

/**
 *
 * @param <T>   preference class derived from this abstract workspace
 *              preference class
 * @author The MxUpdate Team
 * @version $Id$
 */
public abstract class AbstractWorkspacePreference<T extends AbstractWorkspacePreference<?>>
{
    private static class EnumInfo<T extends AbstractWorkspacePreference<?>>
    {
        int count = 0;

        final Vector<T> values = new Vector<T>();
    }

    /**
     *
     */
    private static final Map<Class<? extends AbstractWorkspacePreference<?>>, AbstractWorkspacePreference.EnumInfo<?>> INFOMAP
            = new HashMap<Class<? extends AbstractWorkspacePreference<?>>, AbstractWorkspacePreference.EnumInfo<?>>();

    /**
     * Name of the preference.
     */
    private final String name;

    /**
     * Class of the default value.
     */
    private final Class<?> defaultClazz;

    /**
     * Instance object of the default value.
     */
    private final Object defaultValue;

    /**
     *
     * @param _name             name of the preference
     * @param _defaultClazz     default class of the default value
     * @param _defaultValue     instance object of the default value
     */
    @SuppressWarnings("unchecked")
    public AbstractWorkspacePreference(final String _name,
                                       final Class<?> _defaultClazz,
                                       final Object _defaultValue,
                                       final Class<T> _clazz)
    {
        this.name = _name;
        this.defaultClazz = _defaultClazz;
        this.defaultValue = _defaultValue;

        EnumInfo<T> elem;
        synchronized (AbstractWorkspacePreference.INFOMAP)  {
            elem = (EnumInfo<T>) AbstractWorkspacePreference.INFOMAP.get(_clazz);
            if (elem == null)  {
                elem = new EnumInfo<T>();
                AbstractWorkspacePreference.INFOMAP.put(_clazz, elem);
            }
        }

        elem.count++;
        elem.values.add((T) this);
    }


    @SuppressWarnings("unchecked")
    protected static <U extends AbstractWorkspacePreference<?>> Collection<U> values(final Class<U> _clazz)
    {
        final EnumInfo<U> elem;
        synchronized (AbstractWorkspacePreference.INFOMAP)  {
            elem = (EnumInfo<U>) AbstractWorkspacePreference.INFOMAP.get(_clazz);
        }
        return (elem != null) ? elem.values : null;
    }


    public String name()
    {
        return this.name;
    }

    public Class<?> getDefaultClazz()
    {
        return this.defaultClazz;
    }

    public Object getDefaultValue()
    {
        return this.defaultValue;
    }

    public RGB getRGB()
    {
        final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        return PreferenceConverter.getColor(prefs, this.name());
    }

    public FontData getFontData()
    {
        final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        final FontData ret = PreferenceConverter.getFontData(prefs, this.name());
        return (ret == null)
               ? Display.getCurrent().getSystemFont().getFontData()[0]
               : ret;
    }

    public int getInt()
    {
        return Activator.getDefault().getPreferenceStore().getInt(this.name());
    }

    /**
     * Returns the name of the workspace preference.
     *
     * @return name of the preference
     * @see #name
     */
    @Override
    public String toString()
    {
        return this.name;
    }

    /**
     * Adds given listener to the workspace preferences.
     *
     * @param _listener     property change listener to add
     */
    public static void addListener(final IPropertyChangeListener _listener)
    {
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(_listener);
    }

    /**
     * Removes given listener from the workspace preferences.
     *
     * @param _listener     property change listener to remove
     */
    public static void removeListener(final IPropertyChangeListener _listener)
    {
        Activator.getDefault().getPreferenceStore().removePropertyChangeListener(_listener);
    }
}

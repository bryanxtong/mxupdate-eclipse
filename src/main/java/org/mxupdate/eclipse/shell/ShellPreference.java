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

package org.mxupdate.eclipse.shell;

import java.util.Collection;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.mxupdate.eclipse.preferences.AbstractWorkspacePreference;

/**
 *
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public final class ShellPreference
        extends AbstractWorkspacePreference<ShellPreference>
{
    /**
     * Shell preference to define the foreground color of the input text field.
     * Default color is black.
     */
    public static final ShellPreference INPUT_COLOR;

    /**
     * Shell preference to define the background color of the input text field.
     * Default color is white.
     */
    public static final ShellPreference INPUT_BACKGROUND;

    /**
     * Shell preference to define the font of the input text field. The default
     * font is current used system font.
     */
    public static final ShellPreference INPUT_FONT;

    /**
     * Shell preference to define the color of the shown user input within
     * output text field. Default color is gray.
     */
    public static final ShellPreference OUTPUT_COLOR_INPUT;

    /**
     * Shell preference to define the color of the shell output (returned from
     * database) within output text field. Default color is blue.
     */
    public static final ShellPreference OUTPUT_COLOR_OUTPUT;

    /**
     * Shell preference to define the color for errors within output text
     * field. Default color is red.
     */
    public static final ShellPreference OUTPUT_COLOR_ERROR;

    /**
     * Shell preference to define the font of the output window. The default
     * font is current used system font.
     */
    public static final ShellPreference OUTPUT_FONT;
    /**
     * Shell preference to define the background color of the output window.
     * The default color is white.
     */
    public static final ShellPreference OUTPUT_BACKGROUND;

    /**
     * Shell preference to define the maximum amount of history entries. The
     * default value is &quot;100&quot;.
     */
    public static final ShellPreference MAX_HISTORY;

    /**
     * The values of the preferences are defined at this position because the
     * correct order of the initialize is required. The order of the values is
     * used to create the preference page.
     */
    static
    {
        INPUT_COLOR = new ShellPreference("SHELL_INPUT_COLOR", RGB.class, new RGB(0, 0, 0));
        INPUT_BACKGROUND = new ShellPreference("SHELL_INPUT_BACKGROUND", RGB.class, new RGB(255, 255, 255));
        INPUT_FONT = new ShellPreference("SHELL_INPUT_FONT", FontData.class, null);
        OUTPUT_COLOR_INPUT = new ShellPreference("SHELL_OUTPUT_COLOR_INPUT", RGB.class, new RGB(80, 80, 80));
        OUTPUT_COLOR_OUTPUT = new ShellPreference("SHELL_OUTPUT_COLOR_OUTPUT", RGB.class, new RGB(0, 0, 255));
        OUTPUT_COLOR_ERROR = new ShellPreference("SHELL_OUTPUT_COLOR_ERROR", RGB.class, new RGB(255, 0, 0));
        OUTPUT_FONT = new ShellPreference("SHELL_OUTPUT_FONT", FontData.class, null);
        OUTPUT_BACKGROUND = new ShellPreference("SHELL_OUTPUT_BACKGROUND", RGB.class, new RGB(255, 255, 255));
        MAX_HISTORY = new ShellPreference("SHELL_MAX_HISTORY", Integer.class, new Integer(100));
    }

    /**
     *
     * @param _name             name of the shell preference
     * @param _defaultClazz     related class of the default value
     * @param _defaultValue     value of the default value
     */
    private ShellPreference(final String _name,
                            final Class<?> _defaultClazz,
                            final Object _defaultValue)
    {
        super(_name, _defaultClazz, _defaultValue, ShellPreference.class);
    }

    /**
     *
     * @return collection of all shell preferences
     */
    public static Collection<ShellPreference> values()
    {
        return AbstractWorkspacePreference.values(ShellPreference.class);
    }
}

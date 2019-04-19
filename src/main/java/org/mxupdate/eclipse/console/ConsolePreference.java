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

package org.mxupdate.eclipse.console;

import java.util.Collection;

import org.eclipse.swt.graphics.RGB;
import org.mxupdate.eclipse.Messages;
import org.mxupdate.eclipse.preferences.AbstractWorkspacePreference;

/**
 * Class with all console specific preferences to define the colors depending
 * on the logging level.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public final class ConsolePreference
        extends AbstractWorkspacePreference<ConsolePreference>
{
    /**
     * Debug log level with blue color.
     */
    public static final ConsolePreference DEBUG;

    /**
     * Error log level with dark red color.
     */
    public static final ConsolePreference ERROR;

    /**
     * Info log level with black color.
     */
    public static final ConsolePreference INFO;

    /**
     * Trace log level with gray color.
     */
    public static final ConsolePreference TRACE;

    /**
     * Warning log level with dark magenta color.
     */
    public static final ConsolePreference WARN;

    static
    {
        ERROR = new ConsolePreference("CONSOLE_COLOR_ERROR", RGB.class, new RGB(128, 0, 0));
        WARN = new ConsolePreference("CONSOLE_COLOR_WARN", RGB.class, new RGB(255, 0, 255));
        INFO = new ConsolePreference("CONSOLE_COLOR_INFO", RGB.class, new RGB(0, 0, 0));
        DEBUG = new ConsolePreference("CONSOLE_COLOR_DEBUG", RGB.class, new RGB(0, 0, 255));
        TRACE = new ConsolePreference("CONSOLE_COLOR_TRACE", RGB.class, new RGB(192, 192, 192));
    }


    /**
     *
     * @param _name             name of the shell preference
     * @param _defaultClazz     related class of the default value
     * @param _defaultValue     value of the default value
     */
    private ConsolePreference(final String _name,
                              final Class<?> _defaultClazz,
                              final Object _defaultValue)
    {
        super(_name, _defaultClazz, _defaultValue, ConsolePreference.class);
    }

    /**
     * Returns the string used in the console as prefix for each line to show
     * the logging level. The console text is translated by using the message
     * key
     * &quot;ConsolePreference.CONSOLE_TEXT_&lt;LogLevel&gt;.ConsoleText&quot;.
     *
     * @return console text string
     */
    public String getConsoleText()
    {
        return Messages.getString(new StringBuilder()
                .append(this.getClass().getSimpleName())
                .append('.').append(this.name())
                .append('.').append("ConsoleText")
                .toString());
    }

    /**
     *
     * @return collection of all console preferences
     */
    public static Collection<ConsolePreference> values()
    {
        return AbstractWorkspacePreference.values(ConsolePreference.class);
    }
}

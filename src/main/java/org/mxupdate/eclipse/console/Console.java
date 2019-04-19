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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.mxupdate.eclipse.Messages;

/**
 * MxUpdate specific eclipse plug-in console.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class Console
        extends MessageConsole
        implements IPropertyChangeListener
{
    private final static String LOG_ERROR_TEXT = "[ERROR] ";

    private final static int LOG_ERROR_LENGTH = LOG_ERROR_TEXT.length();

    private final static String LOG_WARNING_TEXT = "[WARNING] ";

    private final static int LOG_WARNING_LENGTH = LOG_WARNING_TEXT.length();

    private final static String LOG_INFO_TEXT = "[INFO] ";

    private final static int LOG_INFO_LENGTH = LOG_INFO_TEXT.length();

    private final static String LOG_DEBUG_TEXT = "[DEBUG] ";

    private final static int LOG_DEBUG_LENGTH = LOG_DEBUG_TEXT.length();

    private final static String LOG_TRACE_TEXT = "[TRACE] ";

    private final static int LOG_TRACE_LENGTH = LOG_TRACE_TEXT.length();

    /**
     * Map of all console streams depending on the log level (and the different
     * colors for each console).
     *
     * @see LogLevel
     */
    private final Map<ConsolePreference,MessageConsoleStream> streams
            = new HashMap<ConsolePreference,MessageConsoleStream>();

    /**
     * Creates new MxUpdate console with all {@link #streams} depending on the
     * log levels {@link LogLevel}.
     */
    public Console()
    {
        super(Messages.getString("plugin.console.label"), null); //$NON-NLS-1$
        for (final ConsolePreference pref : ConsolePreference.values())  {
            final MessageConsoleStream stream = this.newMessageStream();
            stream.setActivateOnWrite(true);
            stream.setColor(new Color(null, pref.getRGB()));
            this.streams.put(pref, stream);
        }
        ConsolePreference.addListener(this);
    }

    public void appendLog(final String _log)
    {
        if (_log != null)  {
            for (final String line : _log.split("\n"))  {
                if (line.startsWith(Console.LOG_DEBUG_TEXT))  {
                    this.println(ConsolePreference.DEBUG, line.substring(Console.LOG_DEBUG_LENGTH), null);
                } else if (line.startsWith(Console.LOG_ERROR_TEXT))  {
                    this.println(ConsolePreference.ERROR, line.substring(Console.LOG_ERROR_LENGTH), null);
                } else if (line.startsWith(Console.LOG_INFO_TEXT))  {
                    this.println(ConsolePreference.INFO, line.substring(Console.LOG_INFO_LENGTH), null);
                } else if (line.startsWith(Console.LOG_TRACE_TEXT))  {
                    this.println(ConsolePreference.TRACE, line.substring(Console.LOG_TRACE_LENGTH), null);
                } else if (line.startsWith(Console.LOG_WARNING_TEXT))  {
                    this.println(ConsolePreference.WARN, line.substring(Console.LOG_WARNING_LENGTH), null);
                }
            }
        }
    }

    /**
     * Logging with level info.
     *
     * @param _text     text for the info logging
     * @see #println(ConsolePreference, String, Throwable)
     */
    public void logInfo(final String _text)
    {
        this.logInfo(_text, null);
    }

    /**
     * Logging with level info.
     *
     * @param _text     text for the info logging
     * @param _ex       exception instance
     * @see #println(ConsolePreference, String, Throwable)
     */
    public void logInfo(final String _text,
                        final Throwable _ex)
    {
        this.println(ConsolePreference.INFO, _text, _ex);
    }

    /**
     * Logging with level trace.
     *
     * @param _text     text for the trace logging
     * @see #println(ConsolePreference, String, Throwable)
     */
    public void logTrace(final String _text)
    {
        this.println(ConsolePreference.TRACE, _text, null);
    }

    /**
     * Logging with level error.
     *
     * @param _text     text for the error logging
     * @see #println(ConsolePreference, String, Throwable)
     */
    public void logError(final String _text)
    {
        this.logError(_text, null);
    }

    /**
     *
     * @param _text     text for the error logging
     * @param _ex       exception instance
     * @see #println(ConsolePreference, String, Throwable)
     */
    public void logError(final String _text,
                         final Throwable _ex)
    {
        this.println(ConsolePreference.ERROR, _text, _ex);
    }

    /**
     * Prints a text and stack trace of related exception. The MxUpdate console
     * is always shown before the text is printed.
     *
     * @param _logLevel   log level (used to add to the output)
     * @param _text       text to print
     * @param _e          exception with stack trace (or null if no exception is
     *                    defined)
     */
    private void println(final ConsolePreference _logLevel,
                         final String _text,
                         final Throwable _e)
    {
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
        final MessageConsoleStream stream = this.streams.get(_logLevel);
        final StringBuilder text = new StringBuilder().append(_text);

        if (_e != null)  {
            final StringWriter sw = new StringWriter();
            _e.printStackTrace(new PrintWriter(sw));
            text.append('\n')
                .append(sw.toString());
        }
        for (final String line : text.toString().split("\n"))  {
            stream.print(_logLevel.getConsoleText());
            stream.print(" ");
            stream.println(line);
        }
        try {
            stream.flush();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(System.out);
        }
    }

    /**
     * If the console is removed, also the event listening on the preferences
     * must be removed.
     */
    @Override()
    protected void dispose()
    {
        ConsolePreference.removeListener(this);
        super.dispose();
    }

    /**
     * If the console preferences are changed with the console preference page
     * {@link ConsolePreferencesPage} all {@link #streams} must be updated to
     * new defined colors.
     *
     * @param _event    property change event (not used)
     * @see #streams
     */
    public void propertyChange(final PropertyChangeEvent _event)
    {
        for (final Map.Entry<ConsolePreference, MessageConsoleStream> entry : this.streams.entrySet())  {
            entry.getValue().setColor(new Color(null, entry.getKey().getRGB()));
        }
    }
}

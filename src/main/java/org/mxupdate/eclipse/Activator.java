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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.mxupdate.eclipse.adapter.IDeploymentAdapter;
import org.mxupdate.eclipse.console.Console;
import org.mxupdate.eclipse.properties.ProjectMode;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the MxUpdate plug-in life cycle.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class Activator
    extends AbstractUIPlugin
{
    /**
     * ID of the MxUpdate plug-in.
     */
    public static final String PLUGIN_ID = "MxUpdateEclipsePlugIn"; //$NON-NLS-1$

    /**
     * Shared instance of the MxUpdate plug-in.
     */
    private static Activator plugIn;

    /**
     * MxUpdate plug-in console.
     *
     * @see #showConsole()
     */
    private Console console = null;

    /**
     * Adapter implementing the interface to MX.
     */
    private final Map<String,IDeploymentAdapter> adapters = new HashMap<String,IDeploymentAdapter>();


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
    @Override
    public void start(final BundleContext _context)
            throws Exception
    {
try  {
        super.start(_context);
        Activator.plugIn = this;

        this.console = new Console();
        this.console.activate();
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{this.console});

} catch (final Throwable e)  {
    e.printStackTrace(System.out);
    this.console.logError("ERROR", e); //$NON-NLS-1$
}
    }

    /**
     * Disconnects all {@link #adapters} and removes the {@link #console}.
     *
     * @param _context      bundle context
     * @throws Exception if an internal exception was thrown
     */
    @Override()
    public void stop(final BundleContext _context)
        throws Exception
    {
        // disconnect all adapters
        final List<Exception> exceptions = new ArrayList<Exception>();
        for (final IDeploymentAdapter adapter : this.adapters.values())  {
            try  {
                adapter.disconnect();
            } catch (final Exception e) {
                exceptions.add(e);
            }
        }

        // remove console
        ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{this.console});
        this.console = null;
        Activator.plugIn = null;

        // stop stuff from super class
        super.stop(_context);

        // throw exception if exists
        if (!exceptions.isEmpty())  {
            throw new Exception(exceptions.toString());
        }
    }

    /**
     * Returns the default shared instance of the MxUpdate eclipse plug-in.
     *
     * @return shared instance
     * @see #plugIn
     */
    public static Activator getDefault()
    {
        return Activator.plugIn;
    }

    /**
     * Returns the console for the plug-in. The method is the getter method for
     * instance variable {@link #console}.
     *
     * @return console of the plug-in
     * @see #console
     */
    public Console getConsole()
    {
        return this.console;
    }

    /**
     * Shows the MxUpdate plug-in console.
     *
     * @see #console    MxUpdate plug-in console
     */
    public void showConsole()
    {
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this.console);
    }

    /**
     * Evaluates the list of names of possible projects which the user could
     * select.
     *
     * @return alphabetically sorted list of projects
     */
    public Set<String> getProjectNames()
    {
        final Set<String> ret = new TreeSet<String>();
        for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())  {
            if (project.isOpen())  {
                ret.add(project.getName());
            }
        }
        return ret;
    }

    /**
     * Returns the deployment adapter depending on the project.
     *
     * @param _project      project for which the deployment adapter is
     *                      searched
     * @return deployment adapter
     * @throws Exception if adapter could not be initialized
     */
    public IDeploymentAdapter getAdapter(final IProject _project)
        throws Exception
    {
        final String projectKey = _project.getName();
        if (!this.adapters.containsKey(projectKey))  {
            final IDeploymentAdapter adapter = ProjectMode.initAdapter(_project, this.console);
            if (adapter != null)  {
                this.adapters.put(projectKey, adapter);
            }
        }
        return this.adapters.get(projectKey);
    }
}

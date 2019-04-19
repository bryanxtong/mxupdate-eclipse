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

package org.mxupdate.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mxupdate.eclipse.Activator;
import org.mxupdate.eclipse.Messages;

/**
 * Eclipse Handler called from the connect command used to connect to the MX.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class ConnectHandler
    extends AbstractHandler
{
    /**
     * Calls the connect to data base method.
     *
     * @param _event  execution event
     * @return always <code>null</code>
     * @see Activator#connect()
     */
    public Object execute(final ExecutionEvent _event)
    {
        final TreeSelection treeSel = (TreeSelection) HandlerUtil.getCurrentSelection(_event);
        if (treeSel.size() != 1)  {
            ErrorDialog.openError(
                    (Shell) null,
                    Messages.getString("ConnectHandler.NotOrMoreThanOneProjectSelected.Title"), //$NON-NLS-1$
                    Messages.getString("ConnectHandler.NotOrMoreThanOneProjectSelected.Message"), //$NON-NLS-1$
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, null, null));
        } else  {
            final IAdaptable adaptable = (IAdaptable) treeSel.getFirstElement();
            final IProject project = (IProject) adaptable.getAdapter(IProject.class);
            try {
                Activator.getDefault().getAdapter(project).connect();
            } catch (final Exception ex) {
                final String msg = Messages.getString("ConnectHandler.ConnectFailed.Message", project.getName()); //$NON-NLS-1$
                Activator.getDefault().getConsole().logError(msg, ex); //$NON-NLS-1$
                ErrorDialog.openError(
                        (Shell) null,
                        Messages.getString("ConnectHandler.ConnectFailed.Title"), //$NON-NLS-1$
                        msg,
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, ex.getMessage(), ex));
            }
        }
        return null;
    }
}

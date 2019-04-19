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

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.mxupdate.eclipse.Activator;
import org.mxupdate.eclipse.Messages;

/**
 * Eclipse Handler called from the update and compile command used to update
 * and compile selected update files.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class UpdateCompileHandler
        extends AbstractFileHandler
{
    /**
     * Executes the update with compile depending step by step for each
     * project. If within a project an update failed, an error message is
     * shown.
     *
     * @param _files    set of files for which this handler is called
     * @see Activator#update(String)
     */
    @Override()
    protected void execute(final Map<IProject,List<IFile>> _files)
    {
        for (final Map.Entry<IProject,List<IFile>> fileEntry : _files.entrySet())  {
            try  {
                Activator.getDefault().getAdapter(fileEntry.getKey()).update(fileEntry.getValue(), true);
            } catch (final Throwable ex) {
                final String msg = Messages.getString("UpdateCompileHandler.ExecuteException.Message", fileEntry.getKey().getName()); //$NON-NLS-1$
                Activator.getDefault().getConsole().logError(msg, ex);
                ErrorDialog.openError(
                        (Shell) null,
                        Messages.getString("UpdateCompileHandler.ExecuteException.Title"), //$NON-NLS-1$
                        msg,
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, ex.getMessage(), ex));
            }
        }
    }
}

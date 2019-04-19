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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The abstract class handles selection of files (or selected file of editor)
 * and calls method {@link #execute(List)}.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public abstract class AbstractFileHandler
        extends AbstractHandler
{
    /**
     * Depending where the handler is called, the list of files is prepared.
     * For pop-up commands, the selected files are used. If the current
     * selection is within an editor, current file of opened editor is used.
     *
     * @param _event    execution event
     * @return always <code>null</code>
     */
    public Object execute(final ExecutionEvent _event)
    {
        final ISelection selection = HandlerUtil.getCurrentSelection(_event);

        final Map<IProject,List<IFile>> files = new HashMap<IProject,List<IFile>>();

        // selection from the navigator? (popup)
        if (selection instanceof TreeSelection)  {
            final TreeSelection treeSel = (TreeSelection) selection;
            for (final Object obj : treeSel.toList())  {
                final IFile file = (IFile) obj;
                if (!files.containsKey(file.getProject()))  {
                    files.put(file.getProject(), new ArrayList<IFile>());
                }
                files.get(file.getProject()).add(file);
            }
        // started within editor or as toolbar command
        } else  {
            final IEditorPart activeEditor = HandlerUtil.getActiveEditor(_event);
            if (activeEditor != null)  {
                final IEditorInput input = activeEditor.getEditorInput();
                if (input instanceof IFileEditorInput)  {
                    final IFile file = ((IFileEditorInput) input).getFile();
                    final ArrayList<IFile> tmpFiles = new ArrayList<IFile>();
                    tmpFiles.add(file);
                    files.put(file.getProject(), tmpFiles);
                }
            }
        }

        this.execute(files);

        return null;
    }

    /**
     * A list of files is selected, a command is called and for this command
     * the handler must be executed.
     *
     * @param _files    set of files depending on the related project for which
     *                  this handler is called
     */
    protected abstract void execute(final Map<IProject,List<IFile>> _files);
}

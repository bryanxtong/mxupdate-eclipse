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

package org.mxupdate.eclipse.mxadapter;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Common preference page for the MxUpdate eclipse plug-in.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class MXPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{
    /**
     * Initialize the layout of the preference (defined as grid).
     */
    public MXPreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);
    }

    /**
     *
     * @see FieldEditorPreferencePage#createFieldEditors()
     */
    @Override()
    protected void createFieldEditors()
    {
    }

    /**
     * @param _workbench    workbench which must be initialized
     * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(final IWorkbench _workbench)
    {
    }
}

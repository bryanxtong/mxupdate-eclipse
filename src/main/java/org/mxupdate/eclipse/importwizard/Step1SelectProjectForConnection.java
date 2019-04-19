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

package org.mxupdate.eclipse.importwizard;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.mxupdate.eclipse.Activator;
import org.mxupdate.eclipse.Messages;

/**
 * Page to select the export path within the target project for the
 * {@link ImportWizard import wizard}.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class Step1SelectProjectForConnection
    extends WizardPage
{
    /**
     * Combo box for the project field.
     */
    private Combo projectField;

    /**
     * First pre-select project.
     *
     * @see #Step1SelectTargetProjectAndPath(IStructuredSelection)
     * @see #createControl(Composite)
     */
    private final IProject preSelProject;

    /**
     * Initializes step 1 page of the import wizard.
     *
     * @param _selection        selection of the project / path
     */
    public Step1SelectProjectForConnection(final IStructuredSelection _selection)
    {
        super("Step1");
        this.setTitle(Messages.getString("ImportWizard.Wizard.Step1.Title")); //$NON-NLS-1$
        this.setDescription(Messages.getString("ImportWizard.Wizard.Step1.Description")); //$NON-NLS-1$

        // calculate pre-selected project
        final Object selObj = _selection.getFirstElement();
        if (selObj instanceof IProject)  {
            this.preSelProject = (IProject) selObj;
        } else if (selObj instanceof IFolder)  {
            this.preSelProject = ((IFolder) selObj).getProject();
        } else if (selObj instanceof IFile)  {
            this.preSelProject = ((IFile) selObj).getProject();
        } else  {
            this.preSelProject = null;
        }
    }

    /**
     * A combo box with all current open projects are shown which the user
     * could select as connection information for the import.
     *
     * @param _parent   parent composite
     */
    @Override()
    public void createControl(final Composite _parent)
    {
        this.initializeDialogUnits(_parent);

        final Composite containerGroup = new Composite(_parent, 0);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        containerGroup.setLayout(layout);
        containerGroup.setLayoutData(new GridData(768));
        containerGroup.setFont(_parent.getFont());

        // combo box for projects
        new Label(containerGroup, SWT.READ_ONLY).setText("Project");
        this.projectField = new Combo(containerGroup, SWT.READ_ONLY);
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        this.projectField.setLayoutData(gridData);
        final Set<String> projects = Activator.getDefault().getProjectNames();
        this.projectField.setItems(projects.toArray(new String[projects.size()]));
        if ((this.preSelProject != null) && this.preSelProject.isOpen())  {
            this.projectField.select(new ArrayList<String>(projects).indexOf(this.preSelProject.getName()));
        }
        this.projectField.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(final SelectionEvent _event)
            {
            }
            public void widgetSelected(final SelectionEvent _event)
            {
                Step1SelectProjectForConnection.this.validate();
            }
        });

        this.setControl(containerGroup);

        this.validate();
    }

    /**
     * Returns selected project from the user.
     *
     * @return selected project from the user
     */
    public IProject getSelectedProject()
    {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(this.projectField.getText());
    }

    /**
     * Validates if a project is selected in {@link #projectField}. If not an
     * error message is shown. Otherwise the page is completed.
     *
     * @see #projectField
     */
    protected void validate()
    {
        if (this.projectField.getSelectionIndex() < 0)  {
            this.setErrorMessage(Messages.getString("ImportWizard.Wizard.Step1.ProjectError")); //$NON-NLS-1$
            this.setPageComplete(false);
        } else  {
            this.setErrorMessage(null);
            this.setPageComplete(true);
        }
    }

    /**
     * Checks that one project of the {@link #projectField} is selected.
     *
     * @return <i>true</i> if at one project is selected
     * @see #projectField
     */
    @Override()
    public boolean canFlipToNextPage()
    {
        return this.projectField.getSelectionIndex() >= 0;
    }

    /**
     * If this page is the current page of the wizard, the page (meaning the
     * wizard) is not complete (because not the last step of the wizard).
     *
     * @return always <i>false</i> if this page is the current page of the
     *         wizard; otherwise <i>false</i>
     */
    @Override()
    public boolean isPageComplete()
    {
        return !this.isCurrentPage();
    }
}

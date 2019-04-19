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

package org.mxupdate.eclipse.properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.mxupdate.eclipse.Messages;

/**
 * Login page in the case the password of the user is not stored.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class AuthenticationDialog
    extends Dialog
{
    /**
     * Text box of the user name.
     */
    private Text textUserName;

    /**
     * Text box of the password.
     */
    private Text textPassword;

    /**
     * Title for the authentication dialog.
     *
     * @see #configureShell(Shell)
     */
    private final String title;

    /**
     * Label for the user name.
     *
     * @see #createDialogArea(Composite)
     */
    private final String labelUserName;

    /**
     * Label for the password.
     *
     * @see #createDialogArea(Composite)
     */
    private final String labelPassword;

    /**
     * User name defined by the user interface predefined by the constructor.
     *
     * @see #okPressed()
     * @see #getUserName()
     */
    private String userName;

    /**
     * Password of the {@link #userName user} defined by the user interface
     * if button {@link #okPressed() ok is pressed}.
     *
     * @see #okPressed()
     * @see #getPassword()
     */
    private String password = "";

    /**
     * Stores the information if the OK button is pressed.
     *
     * @see #okPressed()
     * @see #isOkPressed()
     */
    private boolean okPressed = false;

    /**
     * Default constructor.
     *
     * @param _title            title used for the authentication dialog
     * @param _labelUserName    label for the user name
     * @param _labelPassword    label for the password
     * @param _userName         user name from the preferences
     */
    public AuthenticationDialog(final String _title,
                                final String _labelUserName,
                                final String _labelPassword,
                                final String _userName)
    {
        super((Shell) null);
        this.title = _title;
        this.labelUserName = _labelUserName;
        this.labelPassword = _labelPassword;
        this.userName = _userName;
    }

    /**
     * Adds the {@link #textUserName user name text box} and the
     * {@link #textPassword password text box} to the <code>_parent</code>
     * composite.
     *
     * @param _parent   parent composite where the text boxes must be added
     * @return created dialog area
     */
    @Override()
    protected Control createDialogArea(final Composite _parent)
    {
        final Composite comp = (Composite) super.createDialogArea(_parent);
        final GridLayout layout = (GridLayout) comp.getLayout();
        layout.numColumns = 2;
        layout.marginLeft = 5;
        layout.marginRight = 5;
        layout.marginTop = 5;
        layout.marginBottom = 5;

        final GridData labelData = new GridData(SWT.FILL, SWT.FILL, true, true);
        labelData.widthHint = 75;
        final GridData fieldData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fieldData.horizontalIndent = 5;
        fieldData.widthHint = 150;

        final Label userLabel = new Label(comp, SWT.LEFT);
        userLabel.setText(this.labelUserName);
        userLabel.setLayoutData(labelData);

        this.textUserName = new Text(comp, SWT.BORDER);
        this.textUserName.setLayoutData(fieldData);
        this.textUserName.setText(this.userName);

        final Label passLabel = new Label(comp, SWT.LEFT);
        passLabel.setText(this.labelPassword);
        passLabel.setLayoutData(labelData);

        this.textPassword = new Text(comp, SWT.BORDER | SWT.PASSWORD);
        this.textPassword.setLayoutData(fieldData);

        return comp;
    }

    /**
     * Method is called if the user presses the connect button. The
     * {@link #userName user name}, {@link #password password} and
     * {@link #okPressed flag if OK pressed} is set.
     *
     * @see #userName
     * @see #password
     * @see #okPressed
     */
    @Override()
    protected void okPressed()
    {
        this.userName = this.textUserName.getText();
        this.password = this.textPassword.getText();
        this.okPressed = true;
        super.okPressed();
    }

    /**
     * Returns the user name from the input of the login page.
     *
     * @return user name
     * @see #userName
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * Returns the password from the input of the login page.
     *
     * @return password
     * @see #password
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Returns <i>true</i> if the user has pressed the OK button.
     *
     * @return <i>true</i> if the user has pressed the OK button
     * @see #okPressed
     */
    public boolean isOkPressed()
    {
        return this.okPressed;
    }

    /**
     * Defines the title of the MX login page. The title includes the name of
     * the {@link #title}.
     *
     * @param _shell    new shell
     * @see #title
     */
    @Override()
    protected void configureShell(final Shell _shell)
    {
        super.configureShell(_shell);
        _shell.setText(this.title);
    }

    /**
     * Appends the connect and cancel button for the login page dialog.
     *
     * @param _parent   parent composite where the buttons must be appended
     */
    @Override()
    protected void createButtonsForButtonBar(final Composite _parent)
    {
        // create OK and Cancel buttons by default
        this.createButton(_parent,
                          IDialogConstants.OK_ID,
                          Messages.getString("AuthenticationDialog.Connect"), //$NON-NLS-1$
                          true);
        this.createButton(_parent,
                          IDialogConstants.CANCEL_ID,
                          IDialogConstants.CANCEL_LABEL,
                          false);
    }
}

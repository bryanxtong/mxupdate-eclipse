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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.mxupdate.eclipse.Messages;

/**
 * Field utility class for project property page.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public final class FieldUtil
{
    /**
     * Defines the width of the labels in the first column.
     */
    private static final int LABEL_WIDTH = 130;

    /**
     * Private constructor so that the field utility class could not be
     * initialized.
     */
    private FieldUtil()
    {
    }

    /**
     * Creates a new group composite with four columns in grid layout.
     *
     * @param _parent       parent composite where the group is added
     * @param _labelKey     key for the label of the group
     * @return new created group
     */
    public static Group createGroup(final Composite _parent,
                                    final String _labelKey)
    {
        final Group group = new Group(_parent, SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_labelKey)));

        final GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        group.setLayout(layout);

        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 4;
        group.setLayoutData(gridData);

        return group;
    }

    /**
     * Creates a new composite with four columns in grid layout. Depending on
     * <code>_visible</code> the composite is visible (or not).
     *
     * @param _parent       parent composite where the composite is added
     * @param _visible      <i>true</i> if the composite is visible;
     *                      <i>false</i> if the composite is not visible
     * @return new created composite
     */
    public static Composite createComposite(final Composite _parent,
                                            final boolean _visible)
    {
        final Composite composite = new Composite(_parent, SWT.NONE);

        final GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        composite.setLayout(layout);

        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        gridData.exclude = !_visible;
        composite.setLayoutData(gridData);

        composite.setVisible(_visible);

        return composite;
    }

    /**
     * Appends a new boolean field. The boolean field has a span of two so that
     * the field is inserted within one line.
     *
     * @param _parent       parent composite where the field is added
     * @param _properties   properties where to store updated value
     * @param _propertyKey  property key
     * @param _default      default value of the boolean field
     */
    public static void addBooleanField(final Composite _parent,
                                       final ProjectProperties _properties,
                                       final String _propertyKey,
                                       final boolean _default)
    {
        final Button checkBox = new Button(_parent, SWT.LEFT | SWT.CHECK);
        checkBox.setSelection(_properties.getBoolean(_propertyKey, _default));
        checkBox.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertyKey)));
        final GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        checkBox.setLayoutData(gridData);
        checkBox.addSelectionListener(new SelectionAdapter() {
            @Override()
            public void widgetSelected(final SelectionEvent _event)
            {
                _properties.setBoolean(_propertyKey, checkBox.getSelection());
            }
        });
    }

    /**
     * Appends a new string field.
     *
     * @param _parent       parent composite where the field is added
     * @param _properties   properties where to store updated value
     * @param _propertyKey  property key
     * @param _default      default value of the string field
     */
    public static void addStringField(final Composite _parent,
                                      final ProjectProperties _properties,
                                      final String _propertyKey,
                                      final String _default)
    {
        final Label labelField = new Label(_parent, SWT.LEFT);
        final GridData labelGridData = new GridData();
        labelGridData.widthHint = FieldUtil.LABEL_WIDTH;
        labelField.setLayoutData(labelGridData);
        labelField.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertyKey)));

        final Text textField = new Text(_parent, SWT.LEFT | SWT.BORDER);
        final GridData textGridData = new GridData();
        textGridData.horizontalSpan = 3;
        textGridData.horizontalAlignment = SWT.FILL;
        textGridData.grabExcessHorizontalSpace = true;
        textGridData.widthHint = 150;
        textField.setLayoutData(textGridData);
        textField.setText(_properties.getString(_propertyKey, _default));
        textField.addKeyListener(new KeyAdapter()  {
            @Override()
            public void keyReleased(final KeyEvent _event)
            {
                _properties.setString(_propertyKey, textField.getText());
            }
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override()
            public void focusLost(final FocusEvent _event)
            {
                _properties.setString(_propertyKey, textField.getText());
            }
        });
    }

    /**
     * Appends a file field with a button to choose a file from the file
     * system.
     *
     * @param _parent       parent composite where the field is added
     * @param _properties   properties where to store updated value
     * @param _propertyKey  property key
     * @param _default      default value of the file field
     * @param _extensions   allowed extensions of the file (could be
     *                      <code>null</code>)
     */
    public static void addFileField(final Composite _parent,
                                    final ProjectProperties _properties,
                                    final String _propertyKey,
                                    final String _default,
                                    final String... _extensions)
    {
        final String label = Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertyKey));

        // label
        final Label labelField = new Label(_parent, SWT.LEFT);
        final GridData labelGridData = new GridData();
        labelGridData.widthHint = FieldUtil.LABEL_WIDTH;
        labelField.setLayoutData(labelGridData);
        labelField.setText(label);

        // text field
        final Text textField = new Text(_parent, SWT.LEFT | SWT.BORDER);
        final GridData textGridData = new GridData();
        textGridData.horizontalSpan = 2;
        textGridData.horizontalAlignment = SWT.FILL;
        textGridData.grabExcessHorizontalSpace = true;
        textGridData.widthHint = 150;
        textField.setLayoutData(textGridData);
        textField.setText(_properties.getString(_propertyKey, _default));
        textField.addKeyListener(new KeyAdapter()  {
            @Override()
            public void keyReleased(final KeyEvent _event)
            {
                _properties.setString(_propertyKey, textField.getText());
            }
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override()
            public void focusLost(final FocusEvent _event)
            {
                _properties.setString(_propertyKey, textField.getText());
            }
        });

        // button to browse for files
        final Button button = new Button(_parent, SWT.PUSH);
        button.setText(JFaceResources.getString("openBrowse")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionListener()  {
            public void widgetDefaultSelected(final SelectionEvent _event)
            {
            }
            public void widgetSelected(final SelectionEvent _event)
            {
                final FileDialog dialog = new FileDialog(button.getShell(), SWT.OPEN);
                dialog.setText(label);
                if (!textField.getText().isEmpty())  {
                    dialog.setFileName(textField.getText());
                }
                if (_extensions != null)  {
                    dialog.setFilterExtensions(_extensions);
                }
                final String file = dialog.open();
                if ((file != null) && !file.trim().isEmpty()) {
                    textField.setText(file);
                    _properties.setString(_propertyKey, file);
                }
            }
        });
    }

    /**
     * Appends a password field to the <code>_parent</code> composite. The
     * password field includes a flag if the password is saved in the
     * properties.
     *
     * @param _parent               parent composite where the field is added
     * @param _properties           properties where to store updated value
     * @param _propertyTextPassword property key where the password is stored
     * @param _propertySavePassword property key for the flag if the password
     *                              is saved in the properties
     */
    public static void addPasswordField(final Composite _parent,
                                        final ProjectProperties _properties,
                                        final String _propertyTextPassword,
                                        final String _propertySavePassword)
    {
        final Label label = new Label(_parent, SWT.LEFT);
        final GridData gridLabel = new GridData();
        gridLabel.widthHint = FieldUtil.LABEL_WIDTH;
        label.setLayoutData(gridLabel);
        label.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertyTextPassword)));

        final Text textField = new Text(_parent, SWT.LEFT | SWT.BORDER);
        final GridData textGridData = new GridData();
        textGridData.horizontalSpan = 3;
        textGridData.horizontalAlignment = SWT.FILL;
        textGridData.grabExcessHorizontalSpace = true;
        textGridData.widthHint = 150;
        textField.setEchoChar('\u2022');
        textField.setLayoutData(textGridData);
        textField.setText(_properties.getPassword(_propertyTextPassword));
        textField.addKeyListener(new KeyAdapter()  {
            @Override()
            public void keyReleased(final KeyEvent _event)
            {
                _properties.setPassword(_propertyTextPassword, textField.getText());
            }
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override()
            public void focusLost(final FocusEvent _event)
            {
                _properties.setPassword(_propertyTextPassword, textField.getText());
            }
        });

        // dummy label (so that save password is below input for password)
        new Label(_parent, SWT.LEFT);

        // save password flag
        final Button checkBox = new Button(_parent, SWT.LEFT | SWT.CHECK);
        final boolean preSelected = Boolean.valueOf(_properties.getBoolean(_propertySavePassword, false));
        textField.setEnabled(preSelected);
        checkBox.setSelection(preSelected);
        checkBox.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertySavePassword)));
        checkBox.addSelectionListener(new SelectionAdapter() {
            @Override()
            public void widgetSelected(final SelectionEvent _event)
            {
                final boolean selected = checkBox.getSelection();
                _properties.setBoolean(_propertySavePassword, selected);
                textField.setEnabled(selected);
                if (!selected)  {
                    textField.setText("");
                    _properties.remove(_propertyTextPassword);
                }
            }
        });
    }

    /**
     * Appends a SSH field to the the <code>_parent</code> composite. The SSH
     * field is divided into two fields. The first field defines the name of
     * the SSH server, the second field the port of the SSH server.
     *
     * @param _parent               parent composite where the field is added
     * @param _properties           properties where to store updated value
     * @param _propertyServer       property key for the SSH server name
     * @param _propertyPort         property key for the port of the SSH server
     * @param _defaultPort          default port of the SSH server
     */
    public static void addSSHField(final Composite _parent,
                                   final ProjectProperties _properties,
                                   final String _propertyServer,
                                   final String _propertyPort,
                                   final int _defaultPort)
    {
        final Label labelServerField = new Label(_parent, SWT.LEFT);
        final GridData labelServerGridData = new GridData();
        labelServerGridData.widthHint = FieldUtil.LABEL_WIDTH;
        labelServerField.setLayoutData(labelServerGridData);
        labelServerField.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertyServer)));

        final Text serverField = new Text(_parent, SWT.LEFT | SWT.BORDER);
        final GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textGridData.widthHint = 150;
        textGridData.horizontalSpan = 1;
        serverField.setLayoutData(textGridData);
        serverField.setText(_properties.getString(_propertyServer, ""));
        serverField.addKeyListener(new KeyAdapter()  {
            @Override()
            public void keyReleased(final KeyEvent _event)
            {
                _properties.setString(_propertyServer, serverField.getText());
            }
        });
        serverField.addFocusListener(new FocusAdapter() {
            @Override()
            public void focusLost(final FocusEvent _event)
            {
                _properties.setString(_propertyServer, serverField.getText());
            }
        });

        final Label labelPortField = new Label(_parent, SWT.LEFT);
        labelPortField.setText(Messages.getString(new StringBuilder(ProjectProperties.MSG_PREFIX).append(_propertyPort)));

        final Text portField = new Text(_parent, SWT.LEFT | SWT.BORDER);
        final GridData portGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        portGridData.widthHint = 20;
        portGridData.horizontalSpan = 1;
        portField.setLayoutData(portGridData);
        portField.setText(String.valueOf(_properties.getInteger(_propertyPort, _defaultPort)));
        portField.addKeyListener(new KeyAdapter()  {
            @Override()
            public void keyReleased(final KeyEvent _event)
            {
                try  {
                    _properties.setInteger(_propertyPort, Integer.parseInt(portField.getText()));
                } catch (final NumberFormatException ex)  {
                    _properties.setWrong(_propertyPort);
                }
            }
        });
        portField.addFocusListener(new FocusAdapter() {
            @Override()
            public void focusLost(final FocusEvent _event)
            {
                try  {
                    _properties.setInteger(_propertyPort, Integer.parseInt(portField.getText()));
                } catch (final NumberFormatException ex)  {
                    _properties.setWrong(_propertyPort);
                }
            }
        });
    }
}

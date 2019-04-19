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

package org.mxupdate.eclipse.shell;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.mxupdate.eclipse.Activator;
import org.mxupdate.eclipse.Messages;

/**
 *
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class ShellView
    extends ViewPart
    implements IPropertyChangeListener
{
    /**
     * Used color for the output text (returning back from the data base).
     *
     * @see #execute()
     * @see #updateStyle()
     */
    private Color colorOuput;

    /**
     * Used colors for the error text (returned back from the data base).
     *
     * @see #execute()
     * @see #updateStyle()
     */
    private Color colorError;

    /**
     * Text window for the output.
     */
    private StyledText textOutput = null;

    /**
     * Text window for user input.
     */
    private Text textInput = null;

    /**
     * Current number of executed &quot;commands&quot;.
     */
    private int count = 0;

    /**
     * Current position in the history.
     */
    private int historyPos = 1;

    /**
     * Maximum history entries.
     */
    private int historyMax;

    /**
     * History of all executed shell commands. The maximum amount of history
     * entries depends on {@link #historyMax}.
     *
     * @see #prevHistoryEntry()
     * @see #nextHistoryEntry()
     */
    private final List<String> history = new ArrayList<String>();

    /**
     * Current selected project name.
     */
    private String curProjectName;

    /**
     * Creates the shell view with an output text field {@link #textOutput} and
     * an input text field {@link #textInput}.
     *
     * @param _parent   parent composite element where the new GUI elements
     *                  must be added
     * @see #textInput
     * @see #textOutput
     */
    @Override()
    public void createPartControl(final Composite _parent)
    {
        _parent.setLayout(new GridLayout(1, false));

        this.textOutput = new StyledText(_parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        this.textOutput.setEditable(false);
        this.textOutput.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        this.textOutput.setEnabled(false);

        this.textInput = new Text(_parent, SWT.SINGLE | SWT.BORDER);
        this.textInput.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
        this.textInput.setEnabled(false);

        // key listener for the input text field
        // (execute, previous history entry, next history entry)
        this.textInput.addKeyListener(new KeyAdapter()  {
            @Override
            public void keyReleased(final KeyEvent _event)  {
                switch (_event.keyCode)  {
                    case SWT.CR:
                        ShellView.this.execute();
                        break;
                    case SWT.ARROW_UP:
                        ShellView.this.prevHistoryEntry();
                        break;
                    case SWT.ARROW_DOWN:
                        ShellView.this.nextHistoryEntry();
                        break;
                    default:
                }
            }
        });

        // clear button
        this.getViewSite().getActionBars().getMenuManager().add(new Action(Messages.getString("ShellView.ClearOutputButton")) { //$NON-NLS-1$
            @Override()
            public void run()
            {
                ShellView.this.textOutput.setText(""); //$NON-NLS-1$
            }
        });


        this.getViewSite().getActionBars().getToolBarManager().add(
                new Action(
                        Messages.getString("ShellView.ButtonPrevious"),
                        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK))
                {
                    @Override()
                    public void run()
                    {
                        ShellView.this.prevHistoryEntry();
                    }
                });
        this.getViewSite().getActionBars().getToolBarManager().add(
                new Action(
                        Messages.getString("ShellView.ButtonNext"),
                        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD))
                {
                    @Override()
                    public void run()
                    {
                        ShellView.this.nextHistoryEntry();
                    }
                });


        final Action action = new Action("Select Project", Action.AS_DROP_DOWN_MENU){};

        action.setMenuCreator(new IMenuCreator(){
            @Override()
            public void dispose()
            {
            }
            @Override()
            public Menu getMenu(final Control _parent)
            {
                final MenuManager mg = new MenuManager();
                for (final String project : Activator.getDefault().getProjectNames())  {
                    mg.add(new Action(project){
                        @Override()
                        public void run()
                        {
                            action.setText(Messages.getString("ShellView.ProjectSelect", project)); //$NON-NLS-1$
                            ShellView.this.curProjectName = project;
                            ShellView.this.textInput.setEnabled(true);
                            ShellView.this.textOutput.setEnabled(true);
                            ShellView.this.getViewSite().getActionBars().getToolBarManager().update(true);
                        }
                    });
                }
                return mg.createContextMenu(_parent);
            }
            @Override()
            public Menu getMenu(final Menu _menu)
            {
                return null;
            }});

        this.getViewSite().getActionBars().getToolBarManager().add(action);


        // set colors
        this.updateStyle();

        this.textInput.setFocus();

        ShellPreference.addListener(this);
    }

    /**
     * The style of the text input, text output and max history entries are
     * updated. This includes
     * <ul>
     * <li>foreground color of the output text field (color used to show the
     *     user input)</li>
     * <li>font and background color of the output text field</li>
     * <li>color for the returned values in the output text field</li>
     * <li>color and font of the input text field</li>
     * <li>maximum stored history entries</li>
     * </ul>
     */
    public void updateStyle()
    {
        this.textOutput.setForeground(new Color(null, ShellPreference.OUTPUT_COLOR_INPUT.getRGB()));
        this.textOutput.setBackground(new Color(null, ShellPreference.OUTPUT_BACKGROUND.getRGB()));
        this.textOutput.setFont(new Font(null, ShellPreference.OUTPUT_FONT.getFontData()));
        this.colorOuput = new Color(null, ShellPreference.OUTPUT_COLOR_OUTPUT.getRGB());
        this.colorError = new Color(null, ShellPreference.OUTPUT_COLOR_ERROR.getRGB());

        this.textInput.setForeground(new Color(null, ShellPreference.INPUT_COLOR.getRGB()));
        this.textInput.setBackground(new Color(null, ShellPreference.INPUT_BACKGROUND.getRGB()));
        this.textInput.setFont(new Font(null, ShellPreference.INPUT_FONT.getFontData()));

        this.historyMax = ShellPreference.MAX_HISTORY.getInt();
    }

    /**
     * Updates after a property change the shell styles.
     *
     * @param _event    property change event (not used)
     * @see #updateStyle()
     */
    public void propertyChange(final PropertyChangeEvent _event)
    {
        this.updateStyle();
    }

    /**
     * If the shell gets focus, the text input field must get the focus.
     *
     * @see #textInput
     */
    @Override()
    public void setFocus()
    {
        this.textInput.setFocus();
    }

    /**
     * Executes current string in the input text field {@link #textInput}. The
     * string is added to the history entries of the last entry of the history
     * is not the same or if the string is not an empty string. This string is
     * added to the output text field together with the current amount of
     * input strings defined with {@link #count}. The returned values from the
     * data base are added to the output text field {@link #textOutput} with
     * color {@link #colorOuput} or, if an error was thrown, with color
     * {@link #colorError}.
     *
     * @see #textInput
     * @see #history
     * @see #textOutput
     * @see #colorOuput
     * @see #colorError
     */
    private void execute()
    {
        final String inputText = this.textInput.getText();

        // store new shell command only if not equal and not already stored
        // (as last entry)
        if (!"".equals(inputText) //$NON-NLS-1$
                && ((this.history.size() == 0) || !this.history.get(this.history.size() - 1).equals(inputText)))  {
            this.history.add(inputText);
        }
        this.historyPos = this.history.size();

        // remove all history entries if more than maximum stored
        while (this.historyPos > this.historyMax)  {
            this.history.remove(0);
            this.historyPos = this.history.size();
        }

        // reset input text field
        this.textInput.setText(""); //$NON-NLS-1$

        // write input command into output text field
        this.count++;
        this.textOutput.append("\n" + this.count + "> " + inputText ); //$NON-NLS-1$ //$NON-NLS-2$
        this.textOutput.append("\n"); //$NON-NLS-1$

        final StyleRange txtStyleRange = new StyleRange();
        txtStyleRange.start = this.textOutput.getCharCount();
        try {
            this.showBusy(true);
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(this.curProjectName);
            this.textOutput.append(Activator.getDefault().getAdapter(project).execute(inputText));
            this.showBusy(false);
            txtStyleRange.foreground = this.colorOuput;
        } catch (final Exception e1)  {
            this.textOutput.append(e1.toString());
            txtStyleRange.foreground = this.colorError;
        }
        final int charCount = this.textOutput.getCharCount();
        txtStyleRange.length = charCount - txtStyleRange.start;
        this.textOutput.setStyleRange(txtStyleRange);
        this.textOutput.append("\n"); //$NON-NLS-1$

        // go to last line
        this.textOutput.setSelection(charCount);
    }

    /**
     * Shows previous entry of the history in the input text field.
     *
     * @see #history
     * @see #historyPos
     * @see #textInput
     */
    private void prevHistoryEntry()
    {
        if (this.historyPos > 0)  {
            this.historyPos--;
            this.textInput.setText(this.history.get(this.historyPos));
            this.textInput.setSelection(this.history.get(this.historyPos).length());
        }
    }

    /**
     * Shows next entry of the history in the input text field.
     *
     * @see #history
     * @see #historyPos
     * @see #textInput
     */
    private void nextHistoryEntry()
    {
        // get next history entry (into input text field)
        if (this.historyPos < this.history.size())  {
            this.historyPos++;
            this.textInput.setText(this.history.get(this.historyPos));
            this.textInput.setSelection(this.history.get(this.historyPos).length());
        // if last index => input text must be cleaned
        } else if (this.historyPos == this.history.size())  {
            this.textInput.setText(""); //$NON-NLS-1$
            this.textInput.setSelection(0);
        }
    }

    /**
     * If the shell is closed this instance must be removed as listener from
     * workspace preference change events.
     */
    @Override()
    public void dispose()
    {
        ShellPreference.removeListener(this);
        super.dispose();
    }
}

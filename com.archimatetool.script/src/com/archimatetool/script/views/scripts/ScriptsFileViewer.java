/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.script.views.scripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.archimatetool.editor.utils.PlatformUtils;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.script.ArchiScriptPlugin;
import com.archimatetool.script.preferences.IPreferenceConstants;
import com.archimatetool.script.views.file.AbstractFileView;
import com.archimatetool.script.views.file.NewFileDialog;
import com.archimatetool.script.views.file.PathEditorInput;



/**
 * File Viewer ViewPart for viewing files in a given system folder.
 */
public class ScriptsFileViewer
extends AbstractFileView {
    
    public static String ID = ArchiScriptPlugin.PLUGIN_ID + ".scriptsView"; //$NON-NLS-1$
    public static String HELP_ID = ArchiScriptPlugin.PLUGIN_ID + ".scriptsViewHelp"; //$NON-NLS-1$
    
    RunScriptAction fActionRun;
    RestoreExampleScriptsAction fActionRestoreExamples;
    IAction fActionShowConsole;
    
    @Override
    public File getRootFolder() {
        return ArchiScriptPlugin.INSTANCE.getUserScriptsFolder();
    }
    
    @Override
    protected void makeActions() {
        super.makeActions();
        
        // Run
        fActionRun = new RunScriptAction();
        fActionRun.setEnabled(false);
        
        // Script
        fActionNewFile.setText(Messages.ScriptsFileViewer_0);
        fActionNewFile.setToolTipText(Messages.ScriptsFileViewer_1);
        
        // Restore Examples
        fActionRestoreExamples = new RestoreExampleScriptsAction(getViewer());
        
        // Show Console
        fActionShowConsole = new ShowConsoleAction();
    }
    
    @Override
    protected void makeLocalToolBarActions() {
        super.makeLocalToolBarActions();

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        manager.appendToGroup(IWorkbenchActionConstants.NEW_GROUP, new Separator());
        manager.appendToGroup(IWorkbenchActionConstants.NEW_GROUP, fActionRestoreExamples);
        
        manager.add(fActionRun);
        manager.add(new Separator());
        manager.add(fActionShowConsole);
    }
    
    @Override
    public void updateActions(ISelection selection) {
        super.updateActions(selection);
        
        File file = (File)((IStructuredSelection)selection).getFirstElement();
        fActionRun.setFile(file);
    }
    
    @Override
    protected void fillContextMenu(IMenuManager manager) {
        super.fillContextMenu(manager);
        boolean isEmpty = getViewer().getSelection().isEmpty();

        if(!isEmpty) {
            manager.appendToGroup(IWorkbenchActionConstants.EDIT_START, fActionRun);
        }
    }
    
    @Override
    protected void makeLocalMenuActions() {
        // We don't want this
    }
    
    @Override
    protected void handleDoubleClickAction() {
        fActionRun.run();
    }
    
    @Override
    protected void handleEditAction() {
        File file = (File)((IStructuredSelection)getViewer().getSelection()).getFirstElement();

        if(file != null && !file.isDirectory() && file.exists()) {
            String editor = ArchiScriptPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.PREFS_EDITOR);
            if(StringUtils.isSet(editor)) {
                try {
                    // Windows / Linux
                    String[] paths = new String[] { editor, file.getAbsolutePath() };
                    
                    // Mac
                    if(PlatformUtils.isMac()) {
                        paths = new String[] { "open", "-a", editor, file.getAbsolutePath() }; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    
                    Runtime.getRuntime().exec(paths);
                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
                IWorkbenchPage page = window.getActivePage();
                PathEditorInput input = new PathEditorInput(file);
                
                try {
                    page.openEditor(input, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
                    // Internal Editor
                    //page.openEditor(input, ScriptTextEditor.ID);
                }
                catch(PartInitException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    @Override
    /**
     * New File event happened
     */
    protected void handleNewFileAction() {
        File parent = (File)((IStructuredSelection)getViewer().getSelection()).getFirstElement();

        if(parent == null) {
            parent = getRootFolder();
        }
        else if(!parent.isDirectory()) {
            parent = parent.getParentFile();
        }
        
        if(parent.exists()) {
            NewFileDialog dialog = new NewFileDialog(getViewSite().getShell(), parent);
            dialog.setDefaultExtension(".archiscript"); //$NON-NLS-1$
            
            if(dialog.open()) {
                File newFile = dialog.getFile();
                if(newFile != null) {
                    // Copy new template file over
                    try {
                        URL urlNewFile = ArchiScriptPlugin.INSTANCE.getBundle().getEntry("templates/new.archiscript"); //$NON-NLS-1$
                        InputStream in = urlNewFile.openStream();
                        Files.copy(in, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        in.close();
                    }
                    catch(IOException ex) {
                        ex.printStackTrace();
                    }
                    
                    // Refresh tree
                    getViewer().expandToLevel(parent, 1);
                    getViewer().refresh();
                    getViewer().setSelection(new StructuredSelection(newFile));
                    
                    // Edit file
                    handleEditAction();
                }
            }
        }
    }
    
    // =================================================================================
    //                       Contextual Help support
    // =================================================================================
    
    @Override
    public int getContextChangeMask() {
        return NONE;
    }

    @Override
    public IContext getContext(Object target) {
        return HelpSystem.getContext(HELP_ID);
    }

    @Override
    public String getSearchExpression(Object target) {
        return Messages.ScriptsFileViewer_2;
    }
}

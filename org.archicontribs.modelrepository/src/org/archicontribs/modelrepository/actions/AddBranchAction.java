/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.actions;

import java.io.IOException;

import org.archicontribs.modelrepository.IModelRepositoryImages;
import org.archicontribs.modelrepository.dialogs.AddBranchDialog;
import org.archicontribs.modelrepository.grafico.ArchiRepository;
import org.archicontribs.modelrepository.grafico.BranchStatus;
import org.archicontribs.modelrepository.grafico.GraficoUtils;
import org.archicontribs.modelrepository.grafico.IRepositoryListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;

import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.model.IArchimateModel;

/**
 * Add a Branch
 */
public class AddBranchAction extends AbstractModelAction {
	
    public AddBranchAction(IWorkbenchWindow window, IArchimateModel model) {
        super(window);
        setImageDescriptor(IModelRepositoryImages.ImageFactory.getImageDescriptor(IModelRepositoryImages.ICON_BRANCH));
        setText(Messages.AddBranchAction_0);
        setToolTipText(Messages.AddBranchAction_0);
        
        if(model != null) {
            setRepository(new ArchiRepository(GraficoUtils.getLocalRepositoryFolderForModel(model)));
        }
    }

    @Override
    public void run() {
        AddBranchDialog dialog = new AddBranchDialog(fWindow.getShell());
        int retVal = dialog.open();
        
        String branchName = dialog.getBranchName();
        
        if(retVal == IDialogConstants.CANCEL_ID || !StringUtils.isSet(branchName)) {
            return;
        }
    	
        try(Git git = Git.open(getRepository().getLocalRepositoryFolder())) {
            // If the branch exists
            if(BranchStatus.localBranchExists(getRepository(), branchName)) {
                MessageDialog.openError(fWindow.getShell(),
                        Messages.AddBranchAction_1,
                        NLS.bind(Messages.AddBranchAction_2, branchName));
            }
            else {
                // Create the branch
                git.branchCreate().setName(branchName).call();

                // Checkout if option set
                if(retVal == AddBranchDialog.ADD_BRANCH_CHECKOUT) {
                    git.checkout().setName(Constants.R_HEADS + branchName).call();
                }
                
                // Notify listeners
                notifyChangeListeners(IRepositoryListener.BRANCHES_CHANGED);
            }
        }
        catch(IOException | GitAPIException ex) {
            displayErrorDialog(Messages.AddBranchAction_1, ex);
        }
    }
    
    @Override
    protected boolean shouldBeEnabled() {
        return true;
    }
}
package org.eclipse.wst.jsdt.web.ui.internal.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.html.ui.internal.Logger;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.wst.jsdt.web.ui.SetupProjectsWizzard;
import org.eclipse.wst.sse.core.internal.parser.ForeignRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public class HTMLStructuredTextViewer extends StructuredTextViewer {

	private HTMLTextEditor fTextEditor;

	public HTMLStructuredTextViewer(HTMLTextEditor htmlTextEditor,
			Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean showAnnotationsOverview,
			int styles) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview,
				styles);
		this.fTextEditor = htmlTextEditor;
	}

	public void doOperation(int operation) {
		switch (operation) {
		case CONTENTASSIST_PROPOSALS:
			// Handle javascript content assist when there is no support
			// (instead of printing the stack trace)
			IProject project = null;
			boolean isJavaScriptRegion = false;
			try {
				// Resolve the partition type
				IStructuredDocument sDoc = (IStructuredDocument) getDocument();
				// get the "real" offset - adjusted according to the
				// projection
				int selectionOffset = getSelectedRange().x;
				IStructuredDocumentRegion sdRegion = sDoc
						.getRegionAtCharacterOffset(selectionOffset);
				if (sdRegion == null) {
					super.doOperation(operation);
					return;
				}
				ITextRegion textRegion = sdRegion
						.getRegionAtCharacterOffset(selectionOffset);
				if (textRegion instanceof ForeignRegion) {
					isJavaScriptRegion = (textRegion.getType() == DOMRegionContext.BLOCK_TEXT);
				}

				// Check if the containing project has JS nature or not
				// Check if the containing project has JS nature or not
				if (fTextEditor != null) {
					IEditorInput input = fTextEditor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						IFileEditorInput fileInput = (IFileEditorInput) input;
						project = fileInput.getFile().getProject();
						if (project != null
								&& project.isAccessible()
								&& project.getNature(JavaScriptCore.NATURE_ID) == null) {

							// open dialog if required
							if (isJavaScriptRegion) {
								Shell activeWorkbenchShell = getControl()
										.getShell();
								// Pop a question dialog - if the user selects
								// 'Yes' JS
								// Support is added, otherwise no change
								int addJavaScriptSupport = OptionalMessageDialog
										.open("PROMPT_ADD_JAVASCRIPT_SUPPORT",
												activeWorkbenchShell,
												"JavaScript Content Assist Error",
												null,
												"In order for JavaScript Content Assist to be available, JavaScript support needs to be enabled for the project.\nWould you like to enable it now?",
												MessageDialog.QUESTION,
												new String[] {
														IDialogConstants.YES_LABEL,
														IDialogConstants.NO_LABEL },
												0); //$NON-NLS-1$ 

								// MessageDialog
								// .openQuestion(activeWorkbenchShell,
								// "JavaScript Content Assist Error",
								// "In order for JavaScript Content Assist to be available, JavaScript support needs to be enabled for the project.\nWould you like to enable it now?");
								// run the JSDT action for adding the JS nature
								if (addJavaScriptSupport == 0) {
									SetupProjectsWizzard wiz = new SetupProjectsWizzard();
									wiz.setActivePart(null, fTextEditor);
									wiz.selectionChanged(null,
											new StructuredSelection(project));
									wiz.run(null);
								}
								return;
							}
						}
					}
				}
			} catch (CoreException e) {
				Logger.logException(e);
			}
		}

		super.doOperation(operation);
	}

}
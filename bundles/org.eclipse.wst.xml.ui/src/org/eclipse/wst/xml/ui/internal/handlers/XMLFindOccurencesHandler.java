/*******************************************************************************
 * Copyright (c) 2008 Standards for Technology in Automotive Retail and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Carver - initial API and implementation - bug 212330 -
 *                    Based off CleanupActionXMLDelegate
 *******************************************************************************/

package org.eclipse.wst.xml.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.ui.internal.SSEUIMessages;
import org.eclipse.wst.sse.ui.internal.search.FindOccurrencesProcessor;
import org.eclipse.wst.sse.ui.internal.util.PlatformStatusLineUtil;
import org.eclipse.wst.xml.ui.internal.search.XMLFindOccurrencesProcessor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;

public class XMLFindOccurencesHandler extends AbstractHandler implements IHandler {
	
	private IEditorPart fEditor;
	private List fProcessors;
	
	public void dispose() {
		// nulling out just in case
		fEditor = null;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		fEditor = HandlerUtil.getActiveEditor(event);
		boolean okay = false;
		
		if (fEditor instanceof XMLMultiPageEditorPart) {
			final ITextEditor textEditor = (ITextEditor) fEditor.getAdapter(ITextEditor.class);
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			if (document != null) {
				ITextSelection textSelection = getTextSelection(textEditor);
				FindOccurrencesProcessor findOccurrenceProcessor = getProcessorForCurrentSelection(document, textSelection);
				if (findOccurrenceProcessor != null) {
					if (textEditor.getEditorInput() instanceof IFileEditorInput) {
						IFile file = ((IFileEditorInput) textEditor.getEditorInput()).getFile();
						okay = findOccurrenceProcessor.findOccurrences(document, textSelection, file);
					}
				}
			}
		}
		if (okay) {
			// clear status message
			PlatformStatusLineUtil.clearStatusLine();
		}
		else {
			String errorMessage = SSEUIMessages.FindOccurrencesActionProvider_0; //$NON-NLS-1$
			PlatformStatusLineUtil.displayErrorMessage(errorMessage);
			PlatformStatusLineUtil.addOneTimeClearListener();
		}
		
		return null;
	}
	
	/**
	 * Get the appropriate find occurrences processor
	 * 
	 * @param document -
	 *            assumes not null
	 * @param textSelection
	 * @return FindOccurrencesProcessor
	 */
	private FindOccurrencesProcessor getProcessorForCurrentSelection(IDocument document, ITextSelection textSelection) {
		// check if we have an action that's enabled on the current partition
		ITypedRegion tr = getPartition(document, textSelection);
		String partition = tr != null ? tr.getType() : ""; //$NON-NLS-1$

		Iterator it = getProcessors().iterator();
		FindOccurrencesProcessor action = null;
		while (it.hasNext()) {
			action = (FindOccurrencesProcessor) it.next();
			// we just choose the first action that can handle the partition
			if (action.enabledForParitition(partition))
				return action;
		}
		return null;
	}

	private ITypedRegion getPartition(IDocument document, ITextSelection textSelection) {
		ITypedRegion region = null;
		if (textSelection != null) {
			try {
				region = document.getPartition(textSelection.getOffset());
			}
			catch (BadLocationException e) {
				region = null;
			}
		}
		return region;
	}

	private ITextSelection getTextSelection(ITextEditor textEditor) {
		ITextSelection textSelection = null;
		ISelection selection = textEditor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection && !selection.isEmpty()) {
			textSelection = (ITextSelection) selection;
		}
		return textSelection;
	}
	
	protected List getProcessors() {
		if (fProcessors == null) {
			fProcessors = new ArrayList();
			XMLFindOccurrencesProcessor htmlProcessor = new XMLFindOccurrencesProcessor();
			fProcessors.add(htmlProcessor);
		}
		return fProcessors;
	}
}

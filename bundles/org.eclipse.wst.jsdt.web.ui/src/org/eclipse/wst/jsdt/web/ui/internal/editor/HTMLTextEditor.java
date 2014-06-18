package org.eclipse.wst.jsdt.web.ui.internal.editor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

public class HTMLTextEditor extends StructuredTextEditor {

	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler verticalRuler, int styles) {
		fAnnotationAccess = createAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		StructuredTextViewer sourceViewer = createStructedTextViewer(this,
				parent, verticalRuler, styles);
		initSourceViewer(sourceViewer);
		return sourceViewer;
	}

	protected StructuredTextViewer createStructedTextViewer(
			HTMLTextEditor htmlTextEditor, Composite parent,
			IVerticalRuler verticalRuler, int styles) {
		return new HTMLStructuredTextViewer(htmlTextEditor, parent,
				verticalRuler, getOverviewRuler(), isOverviewRulerVisible(),
				styles);
	}
}
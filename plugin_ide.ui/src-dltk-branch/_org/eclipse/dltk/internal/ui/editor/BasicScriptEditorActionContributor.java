/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package _org.eclipse.dltk.internal.ui.editor;

import melnorme.lang.ide.ui.editor.LangEditorActionContributor;

import org.eclipse.dltk.internal.ui.editor.DLTKEditorMessages;
import org.eclipse.dltk.ui.actions.IScriptEditorActionDefinitionIds;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import _org.eclipse.dltk.internal.ui.actions.FoldingActionGroup;

/**
 * Common base class for action contributors for Script editors.
 */
public abstract class BasicScriptEditorActionContributor extends LangEditorActionContributor {

	private RetargetTextEditorAction fShowOutline;

//	private RetargetTextEditorAction fGotoNextMemberAction;
//	private RetargetTextEditorAction fGotoPreviousMemberAction;

	public BasicScriptEditorActionContributor() {
		super();

		fShowOutline = new RetargetTextEditorAction(
				DLTKEditorMessages.getBundleForConstructedKeys(),
				"ShowOutline.");
		fShowOutline.setActionDefinitionId(IScriptEditorActionDefinitionIds.SHOW_OUTLINE);

//		fGotoNextMemberAction = new RetargetTextEditorAction(b, "GotoNextMember.");
//		fGotoNextMemberAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
//		fGotoPreviousMemberAction = new RetargetTextEditorAction(b, "GotoPreviousMember.");
//		fGotoPreviousMemberAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);

	}
	
	@Override
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);

		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
		}

		IMenuManager gotoMenu = menu.findMenuUsingPath("navigate/goTo");
		if (gotoMenu != null) {
			gotoMenu.add(new Separator("additions2"));
//			gotoMenu.appendToGroup("additions2", fGotoPreviousMemberAction); 
//			gotoMenu.appendToGroup("additions2", fGotoNextMemberAction);
		}
	}

	@Override
	public void setActiveEditor(IEditorPart part) {

		super.setActiveEditor(part);

		ITextEditor textEditor = null;
		if (part instanceof ITextEditor)
			textEditor = (ITextEditor) part;

		fShowOutline.setAction(getAction(textEditor, IScriptEditorActionDefinitionIds.SHOW_OUTLINE));

//		fGotoNextMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.NEXT_MEMBER));
//		fGotoPreviousMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.PREVIOUS_MEMBER));

		if (part instanceof ScriptEditor_Actions) {
			ScriptEditor_Actions editor = (ScriptEditor_Actions) part;
			final FoldingActionGroup foldingActions = editor.getFoldingActionGroup();
			if (foldingActions != null)
				foldingActions.updateActionBars();
		}

		IActionBars actionBars = getActionBars();
		IStatusLineManager manager = actionBars.getStatusLineManager();
		manager.setMessage(null);
		manager.setErrorMessage(null);
		
	}

	@Override
	protected void doDispose() {
		setActiveEditor(null);
	}
	
}
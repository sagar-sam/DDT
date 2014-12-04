/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling.ast;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.context.ISemanticContext;
import melnorme.lang.tooling.engine.IElementSemantics;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.NullElementSemantics;
import melnorme.lang.tooling.engine.scoping.CommonScopeLookup;
import melnorme.lang.tooling.engine.scoping.INonScopedContainer;
import dtool.ast.definitions.DefUnit;


public abstract class CommonSemanticElement implements ISemanticElement {
	
	public CommonSemanticElement() {
	}
	
	@Override
	public abstract ISemanticElement getParent();
	
	/* -----------------  ----------------- */
	
	@Override
	public IElementSemantics getSemantics(ISemanticContext parentContext) {
		return getContextForThisElement(parentContext).getSemanticsEntry(this);
	}
	
	@Override
	public ISemanticContext getContextForThisElement(ISemanticContext parentContext) {
		return parentContext.findSemanticContext(this);
	}
	
	@Override
	public IElementSemantics createSemantics(PickedElement<?> pickedElement) {
		assertTrue(pickedElement.element == this); // Note this precondition!
		return doCreateSemantics(pickedElement);
	}
	
	@SuppressWarnings("unused")
	protected IElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new NullElementSemantics();
	}
	
	@Override
	public void evaluateForScopeLookup(CommonScopeLookup lookup, boolean importsOnly, boolean isSequentialLookup) {
		if(this instanceof INonScopedContainer) {
			INonScopedContainer container = ((INonScopedContainer) this);
			// FIXME: remove need for isSequentialLookup?
			lookup.evaluateScopeElements(container.getMembersIterable(), isSequentialLookup, importsOnly);
		}
		
		if(!importsOnly && this instanceof DefUnit) {
			DefUnit defunit = (DefUnit) this;
			lookup.visitElement(defunit);
		}
	}
	
}
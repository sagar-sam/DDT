/*******************************************************************************
 * Copyright (c) 2011 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.statements;

import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import dtool.ast.expressions.Resolvable;

public class StatementGotoCase extends Statement {
	
	public final Resolvable exp;
	
	public StatementGotoCase(Resolvable exp) {
		this.exp = parentize(exp);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.STATEMENT_GOTO_CASE;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, exp);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new StatementGotoCase(clone(exp));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("goto case ");
		cp.append(exp);
		cp.append(";");
	}
	
}
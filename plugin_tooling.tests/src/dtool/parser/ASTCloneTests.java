/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.parser;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertEquals;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast_actual.ASTNode;

public class ASTCloneTests {

	public static void testCloning(ASTNode node) {
		CommonASTNode clonedNode = node.cloneTree();
		
		// Check that clone is correct
		assertEquals(clonedNode.toStringAsCode(), node.toStringAsCode());
	}
	
}
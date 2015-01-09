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
package dtool.engine.analysis;

import static dtool.engine.analysis.Import_LookupTest.checkIsPackageNamespace;

import org.junit.Test;


public class Import_Selective_Alias_LookupTest extends CommonLookupTest {
	
	@Test
	public void testImportAlias() throws Exception { testImportAlias$(); }
	public void testImportAlias$() throws Exception {
		testLookup(parseModule_WithRef("import foo = pack.foo;", "foo"),  
			checkSingleResult("foo = pack.foo")
		);
		
		testLookup(parseModule_WithRef("int foo; import foo = pack.foo;", "foo"),
			checkNameConflict("foo = pack.foo", "int foo;")
		);
		
		// Test member scope
		testLookup(parseModule_WithRef("import foo = pack.foo;", "foo.PackFoo_member"),  
			checkSingleResult("int PackFoo_member;")
		);
	}
	
	@Test
	public void testImportSelective() throws Exception { testImportSelective$(); }
	public void testImportSelective$() throws Exception {
		 
		testLookup(parseModule_WithRef("import pack.foobar : PackFoobar_member;", "PackFoobar_member"),  
			checkSingleResult("int PackFoobar_member;")
		);
		testLookup(parseModule_WithRef("import pack.foobar : PackFoobar_member;", "PackFoobar_member2"),  
			checkSingleResult(null)
		);
		
		testLookup(parseModule_WithRef("import pack.foobar : PackFoobar_member; void PackFoobar_member;", 
			"PackFoobar_member"),  
			checkNameConflict("int PackFoobar_member;", "void PackFoobar_member;")
		);
		
		
		// Test static import bit of import selective
		testLookup(parseModule_WithRef("import pack.foo : NotFound;", "pack"),  
			checkSingleResult(null)
		);
		
		// Vs. public imports
		testLookup(parseModule_WithRef("import pack.public_import : PackFoo_member;", "PackFoo_member"),  
			checkSingleResult("int PackFoo_member;")
		);
		testLookup(parseModule_WithRef("import pack.public_import : foo_private__xxx;", "foo_private__xxx"),  
			checkSingleResult(null)
		);
		
		testLookup(parseModule_WithRef("import pack.public_import : pack;", "pack"),  
			checkIsPackageNamespace(array("module[pack.foo]"))
		);

		// ---------- selective alias
		test_SelectiveAlias$();
	}
	
	protected void test_SelectiveAlias$() {
		testLookup(parseModule_WithRef("import pack.foo : xxx = PackFoo_member;", "xxx"),  
			checkSingleResult("xxx = PackFoo_member")
		);
		testLookup(parseModule_WithRef("import pack.foo : xxx = PackFoo_member;", "PackFoo_member"),  
			checkSingleResult(null)
		);
		// Test conflict
		testLookup(parseModule_WithRef("import pack.foo : xxx = PackFoo_member; void xxx;", 
			"xxx"),  
			checkNameConflict("xxx = PackFoo_member", "void xxx;")
		);
		
	}
	
}
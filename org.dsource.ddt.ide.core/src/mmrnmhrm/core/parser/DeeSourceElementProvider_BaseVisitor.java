package mmrnmhrm.core.parser;


import dtool.ast.ASTNeoAbstractVisitor;
import dtool.ast.ASTNeoNode;
import dtool.ast.IASTNeoVisitor;
import dtool.ast.declarations.DeclarationConditional;
import dtool.ast.declarations.DeclarationImport;
import dtool.ast.declarations.DeclarationInvariant;
import dtool.ast.declarations.DeclarationUnitTest;
import dtool.ast.definitions.DefUnit;
import dtool.ast.definitions.Symbol;
import dtool.ast.expressions.Resolvable;
import dtool.ast.references.CommonRefNative;
import dtool.ast.references.CommonRefQualified;
import dtool.ast.references.NamedReference;
import dtool.ast.references.RefIdentifier;
import dtool.ast.references.RefTemplateInstance;
import dtool.ast.references.Reference;

public abstract class DeeSourceElementProvider_BaseVisitor extends ASTNeoAbstractVisitor implements IASTNeoVisitor {
	
	@Override
	public final boolean preVisit(ASTNeoNode elem) {
		return true; // Default implementation: do nothing
	}
	
	@Override
	public final void postVisit(ASTNeoNode elem) {
		// Default implementation: do nothing
	}
	
	@Override
	public final boolean visit(ASTNeoNode elem) {
		return true; // Default implementation: do nothing
	}
	
	@Override
	public final boolean visit(Symbol elem) {
		return true;
	}
	
	@Override
	public final boolean visit(DefUnit elem) {
		return true;
	}
	
	/* ---------------------------------- */
	
	@Override
	public final boolean visit(Resolvable elem) {
		return true;
	}
	
	
	@Override
	public final boolean visit(Reference elem) {
		return true;
	}
	
	@Override
	public final boolean visit(CommonRefNative elem) {
		return true;
	}
	
	
	@Override
	public final boolean visit(CommonRefQualified elem) {
		return visit((NamedReference) elem); // Go up the hierarchy
	}
	
	@Override
	public final boolean visit(RefIdentifier elem) {
		return visit((NamedReference) elem); // Go up the hierarchy
	}
	
	@Override
	public final boolean visit(RefTemplateInstance elem) {
		return true;
	}
	
	
	/* ---------------------------------- */
	
	@Override
	public final boolean visit(DeclarationImport elem) {
		return false;
	}
	
	@Override
	public final boolean visit(DeclarationInvariant elem) {
		return true;
	}
	
	@Override
	public final boolean visit(DeclarationUnitTest elem) {
		return true;
	}
	
	@Override
	public final boolean visit(DeclarationConditional elem) {
		return true;
	}
	
}
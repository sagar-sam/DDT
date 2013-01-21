▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ basic samples

extern(C) void foo;
align (1) int foo;

#AST_STRUCTURE_EXPECTED:
DeclarationLinkage(MiscDeclaration)
DeclarationAlign(MiscDeclaration)
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂
public: 
int foo;
void bar;
extern(C):
int foo;
void bar;

#AST_STRUCTURE_EXPECTED:
DeclarationProtection(
	MiscDeclaration MiscDeclaration
	DeclarationLinkage(
		MiscDeclaration MiscDeclaration
	)
)
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂
final {
	int foo;
}
static:
const:
#AST_STRUCTURE_EXPECTED:
DeclarationBasicAttrib( MiscDeclaration )
DeclarationBasicAttrib(
	DeclarationBasicAttrib
)
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂
// Should rule continue or not?
extern(#error:BAD_LINKAGE_ID) int bar;
align(#error:EXP_INTEGER) int bar;

#AST_STRUCTURE_EXPECTED:
DeclarationLinkage(MiscDeclaration)
DeclarationAlign(MiscDeclaration)

#AST_EXPECTED:
extern int bar;
align int bar;
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ EOF case
extern(C) #error(EXPRULE_decl)

#AST_STRUCTURE_EXPECTED:
DeclarationLinkage()
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ Empty Declaration case
extern(C) #error(SE_decl) ;

#AST_STRUCTURE_EXPECTED:
DeclarationLinkage(InvalidSyntaxDeclaration)
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
extern(C) #error(SE_decl) ] int foo;

#AST_STRUCTURE_EXPECTED:
DeclarationLinkage(InvalidSyntaxDeclaration)
MiscDeclaration
Ⓗ▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂

#@LINKAGE_TYPE{C,C++,D,Windows,Pascal,System}
#@EXTERN_ATTRIB《
extern(#@(LINKAGE_TYPE))●
extern(Xpto #error(BAD_LINKAGE_ID) )●
extern(#error(BAD_LINKAGE_ID))●
extern(#error(BAD_LINKAGE_ID)#error(EXP_CLOSE_PARENS)#DECL_BROKEN(flag)●
extern(C++#error(EXP_CLOSE_PARENS)#DECL_BROKEN(flag)
》

#@EXTERN_ATTRIB_EXP{
extern(#@(LINKAGE_TYPE)),
extern,
extern,
extern,
extern(C++)
}

#@ALIGN_ATTRIB《
align●
align(1)●
align(12)●
align(#error(EXP_INTEGER)) ●
align(#error(EXP_INTEGER)#error(EXP_CLOSE_PARENS) #DECL_BROKEN(flag)●
align(16#error(EXP_CLOSE_PARENS) #DECL_BROKEN(flag)
》
#@ALIGN_ATTRIB_EXP{
align,
align(1),
align(12),
align,
align,
align(16)
}

// TODO pragma expression list !
#@PRAGMA_ATTRIB《
pragma(foo)●
pragma #error(EXP_OPEN_PARENS) #DECL_BROKEN(flag) ●
pragma ( #error(EXP_ID) #error(EXP_CLOSE_PARENS) #DECL_BROKEN(flag) ●
pragma ( #error(EXP_ID) ) ●
pragma ( foo2 #error(EXP_CLOSE_PARENS) #DECL_BROKEN(flag) 
》
#@PRAGMA_ATTRIB_EXP《
pragma(foo)●
pragma() ●
pragma() ●
pragma() ●
pragma(foo2) 
》

#@SIMPLE_ATTRIBS{
#@PROT_ATTRIB{private,package,protected,public,export},
deprecated,
static,
extern,
final,
synchronized,
override,
abstract,
const,
scope,
__gshared,
shared,
immutable,
inout
}

TODO:
auto ??
@disable


#@ATTRIBS{#@(EXTERN_ATTRIB),#@(ALIGN_ATTRIB),#@(PRAGMA_ATTRIB)#PRAGMA(flag),#@(SIMPLE_ATTRIBS)}
#@ATTRIBS_EXP{
	#@EXTERN_ATTRIB_EXP(EXTERN_ATTRIB),
	#@ALIGN_ATTRIB_EXP(ALIGN_ATTRIB),
	#@PRAGMA_ATTRIB_EXP(PRAGMA_ATTRIB),
	#@(SIMPLE_ATTRIBS)
}

▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂

#@(ATTRIBS) #@BODY_TYPES【
int foo1;
●
#?DECL_BROKEN{#error:SE_decl} :
int foo2;
void bar;
●
#?DECL_BROKEN{#error:SE_decl} : /* Zero decls */
●
#?DECL_BROKEN{#error:SE_decl} { #?DECL_BROKEN{#error:SE_decl} } // This will change in the future
●
#?DECL_BROKEN{#error:SE_decl} { // This error happening will change in the future
	int fooX;
	void bar;
#?DECL_BROKEN{#error:SE_decl}}
●
/*EMPTY DECLARATION*/ #?DECL_BROKEN{,#?PRAGMA{,#error:SE_decl}} ;
】

#AST_EXPECTED:

#@ATTRIBS_EXP(ATTRIBS) #@(BODY_TYPES)

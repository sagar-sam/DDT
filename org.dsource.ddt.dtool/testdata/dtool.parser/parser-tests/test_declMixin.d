▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
mixin("int foo");
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
mixin("");
▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
// TODO
//mixin("int" + "foo");

▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
mixin #error:EXP_OPEN_PARENS #error:EXP_SEMICOLON
mixin #error:EXP_OPEN_PARENS ;
mixin #error:EXP_OPEN_PARENS #error:EXP_SEMICOLON #error:SE_decl ) ;
#AST_EXPECTED:
mixin();
mixin();
mixin(); );

▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
mixin ( #error:EXPRULE_exp );
mixin ( #error:EXPRULE_exp #error:EXP_CLOSE_PARENS ; import foo;
mixin ( #error:EXPRULE_exp ) #error:EXP_SEMICOLON
mixin ( #error:EXPRULE_exp #error:EXP_CLOSE_PARENS #error:EXP_SEMICOLON
#AST_EXPECTED:
mixin();
mixin(); import foo;
mixin();
mixin();

▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂▂ 
mixin("Foo1 + foo" #error:EXP_CLOSE_PARENS ;
mixin("Foo2 + foo" #error:EXP_CLOSE_PARENS #error:EXP_SEMICOLON
mixin("Foo3 + foo" ) #error:EXP_SEMICOLON
#AST_EXPECTED:
mixin("Foo1 + foo");
mixin("Foo2 + foo");
mixin("Foo3 + foo");

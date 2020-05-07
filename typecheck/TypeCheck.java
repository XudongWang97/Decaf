package decaf.typecheck;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.*;

import decaf.Driver;
import decaf.Location;
import decaf.tree.Tree;
import decaf.tree.Tree.CaseExpr;
import decaf.tree.Tree.DefaultStmt;
import decaf.frontend.Parser;
import decaf.scope.ClassScope;
import decaf.scope.FormalScope;
import decaf.scope.Scope;
import decaf.scope.ScopeStack;
import decaf.scope.Scope.Kind;
import decaf.symbol.Class;
import decaf.symbol.Function;
import decaf.symbol.Symbol;
import decaf.symbol.Variable;
import decaf.type.*;
import decaf.error.*;


public class TypeCheck extends Tree.Visitor {

	private ScopeStack table;

	private Stack<Tree> breaks;

	private Function currentFunction;

	public TypeCheck(ScopeStack table) {
		this.table = table;
		breaks = new Stack<Tree>();
	}

	public static void checkType(Tree.TopLevel tree) {
		new TypeCheck(Driver.getDriver().getTable()).visitTopLevel(tree);
	}

	@Override
	public void visitBinary(Tree.Binary expr) {
		Tree.Expr L = expr.left;
		Tree.Expr R = expr.right;
		
		L.accept(this);
		R.accept(this);

		if (L.type.equal(BaseType.COMPLEX) && R.type.equal(BaseType.INT)) {
			expr.right = new Tree.Unary(Tree.COMPCAST, R, R.loc);
			expr.right.type = BaseType.COMPLEX;
		}
		
		if (L.type.equal(BaseType.INT) && R.type.equal(BaseType.COMPLEX)) {
			expr.left = new Tree.Unary(Tree.COMPCAST, L, L.loc);
			expr.left.type = BaseType.COMPLEX;
		}
		expr.type = checkBinaryOp(expr.left, expr.right, expr.tag, expr.loc);
	}

	@Override
	public void visitUnary(Tree.Unary expr) {
		expr.expr.accept(this);
		//System.out.println(expr.expr.type);
		if(expr.expr.type.equal(BaseType.ERROR))
		{
			expr.type=BaseType.ERROR;
			return;
		}
		
		if(expr.tag == Tree.NEG){
			if (expr.expr.type.equal(BaseType.ERROR)
					|| expr.expr.type.equal(BaseType.INT)) {
				expr.type = expr.expr.type;
			} else {
				issueError(new IncompatUnOpError(expr.getLocation(), "-",
						expr.expr.type.toString()));
				expr.type = BaseType.ERROR;
			}
		}
		else if(expr.tag==Tree.NOT)
		{
			if (!(expr.expr.type.equal(BaseType.BOOL) || expr.expr.type
					.equal(BaseType.ERROR))) {
				issueError(new IncompatUnOpError(expr.getLocation(), "!",
						expr.expr.type.toString()));
			}
			expr.type = BaseType.BOOL;
		}else if(expr.tag==Tree.IM||expr.tag==Tree.RE)
		{
			if(!expr.expr.type.equal(BaseType.COMPLEX))//???
			{
				issueError(new IncompatUnOpError(expr.getLocation(), Parser.opStr(expr.tag),
						expr.expr.type.toString()));
				expr.type=BaseType.ERROR;
			}else
			{
				expr.type=BaseType.INT;
			}
		}
		else if(expr.tag==Tree.COMPCAST)
		{
			if(!expr.expr.type.equal(BaseType.INT))
			{
				issueError(new IncompatUnOpError(expr.getLocation(), Parser.opStr(expr.tag),
						expr.expr.type.toString()));
				expr.type=BaseType.ERROR;
			}else
			{
				expr.type=BaseType.COMPLEX;
			}
		}
	}

	@Override
	public void visitLiteral(Tree.Literal literal) {
		switch (literal.typeTag) {
		case Tree.INT:
			literal.type = BaseType.INT;
			break;
		case Tree.BOOL:
			literal.type = BaseType.BOOL;
			break;
		case Tree.STRING:
			literal.type = BaseType.STRING;
			break;
		case Tree.IMG:
			literal.type = BaseType.COMPLEX;
			break;
		}
	}

	@Override
	public void visitNull(Tree.Null nullExpr) {
		nullExpr.type = BaseType.NULL;
	}

	@Override
	public void visitReadIntExpr(Tree.ReadIntExpr readIntExpr) {
		readIntExpr.type = BaseType.INT;
	}

	@Override
	public void visitReadLineExpr(Tree.ReadLineExpr readStringExpr) {
		readStringExpr.type = BaseType.STRING;
	}

	@Override
	public void visitIndexed(Tree.Indexed indexed) {
		indexed.lvKind = Tree.LValue.Kind.ARRAY_ELEMENT;
		indexed.array.accept(this);
		if (!indexed.array.type.isArrayType()) {
			issueError(new NotArrayError(indexed.array.getLocation()));
			indexed.type = BaseType.ERROR;
		} else {
			indexed.type = ((ArrayType) indexed.array.type)
					.getElementType();
		}
		indexed.index.accept(this);
		if (!indexed.index.type.equal(BaseType.INT)) {
			issueError(new SubNotIntError(indexed.getLocation()));
		}
	}

	private void checkCallExpr(Tree.CallExpr callExpr, Symbol f) {
		Type receiverType = callExpr.receiver == null ? ((ClassScope) table
				.lookForScope(Scope.Kind.CLASS)).getOwner().getType()
				: callExpr.receiver.type;
		if (f == null) {
			issueError(new FieldNotFoundError(callExpr.getLocation(),
					callExpr.method, receiverType.toString()));
			callExpr.type = BaseType.ERROR;
		} else if (!f.isFunction()) {
			issueError(new NotClassMethodError(callExpr.getLocation(),
					callExpr.method, receiverType.toString()));
			callExpr.type = BaseType.ERROR;
		} else {
			Function func = (Function) f;
			callExpr.symbol = func;
			callExpr.type = func.getReturnType();
			if (callExpr.receiver == null && currentFunction.isStatik()
					&& !func.isStatik()) {
				issueError(new RefNonStaticError(callExpr.getLocation(),
						currentFunction.getName(), func.getName()));
			}
			if (!func.isStatik() && callExpr.receiver != null
					&& callExpr.receiver.isClass) {
				issueError(new NotClassFieldError(callExpr.getLocation(),
						callExpr.method, callExpr.receiver.type.toString()));
			}
			if (func.isStatik()) {
				callExpr.receiver = null;
			} else {
				if (callExpr.receiver == null && !currentFunction.isStatik()) {
					callExpr.receiver = new Tree.ThisExpr(callExpr.getLocation());
					callExpr.receiver.accept(this);
				}
			}
			for (Tree.Expr e : callExpr.actuals) {
				e.accept(this);
			}
			List<Type> argList = func.getType().getArgList();
			int argCount = func.isStatik() ? callExpr.actuals.size()
					: callExpr.actuals.size() + 1;
			if (argList.size() != argCount) {
				issueError(new BadArgCountError(callExpr.getLocation(),
						callExpr.method, func.isStatik() ? argList.size()
								: argList.size() - 1, callExpr.actuals.size()));
			} else {
				Iterator<Type> iter1 = argList.iterator();
				if (!func.isStatik()) {
					iter1.next();
				}
				Iterator<Tree.Expr> iter2 = callExpr.actuals.iterator();
				for (int i = 1; iter1.hasNext(); i++) {
					Type t1 = iter1.next();
					Tree.Expr e = iter2.next();
					Type t2 = e.type;
					if (!t2.equal(BaseType.ERROR) && !t2.compatible(t1)) {
						issueError(new BadArgTypeError(e.getLocation(), i, 
								t2.toString(), t1.toString()));
					}
				}
			}
		}
	}

	@Override
	public void visitCallExpr(Tree.CallExpr callExpr) {
		if (callExpr.receiver == null) {
			callExpr.otype = (ClassType) ((ClassScope)table.lookForScope(Kind.CLASS)).getOwner().getType();
			ClassScope cs = (ClassScope) table.lookForScope(Kind.CLASS);
			checkCallExpr(callExpr, cs.lookupVisible(callExpr.method));
			return;
		}
		callExpr.receiver.usedForRef = true;
		callExpr.receiver.call=true;
		callExpr.receiver.accept(this);
		/*if(callExpr.receiver.Super&&callExpr.receiver.nopar) 
		{
			issueError(new NoPar(callExpr.getLocation(),callExpr.receiver.type.toString()));
			callExpr.receiver.type=BaseType.ERROR;
		}*/
		if (callExpr.receiver.type.equal(BaseType.ERROR)) {
			callExpr.type = BaseType.ERROR;
			return;
		}
		if (callExpr.method.equals("length")) {
			if (callExpr.receiver.type.isArrayType()) {
				if (callExpr.actuals.size() > 0) {
					issueError(new BadLengthArgError(callExpr.getLocation(),
							callExpr.actuals.size()));
				}
				callExpr.type = BaseType.INT;
				callExpr.isArrayLength = true;
				return;
			} else if (!callExpr.receiver.type.isClassType()) {
				issueError(new BadLengthError(callExpr.getLocation()));
				callExpr.type = BaseType.ERROR;
				return;
			}
		}

		if (!callExpr.receiver.type.isClassType()) {
			issueError(new NotClassFieldError(callExpr.getLocation(),
					callExpr.method, callExpr.receiver.type.toString()));
			callExpr.type = BaseType.ERROR;
			return;
		}

		ClassScope cs = ((ClassType) callExpr.receiver.type)
				.getClassScope();
		callExpr.otype = (ClassType)callExpr.receiver.type;
		//System.out.println(callExpr.otype);
		if (callExpr.receiver.getClass() == Tree.SuperExpr.class) 
		{
			ClassType now = ((ClassType)callExpr.receiver.type).getParentType();
			//System.out.println(now);
			while (now != null)
			{
				cs = now.getClassScope();
				//callExpr.receiver.val = tr.genLoad(callExpr.receiver.val, 0);
				if (cs.lookupVisible(callExpr.method) != null) break;
				now = now.getParentType();
			}
			callExpr.receiver.type = now;
			//System.out.println(now);

			//System.out.println("fffff");

		}
		checkCallExpr(callExpr, cs.lookupVisible(callExpr.method));
	}

	@Override
	public void visitExec(Tree.Exec exec){
		exec.expr.accept(this);
	}
	
	@Override
	public void visitNewArray(Tree.NewArray newArrayExpr) {
		newArrayExpr.elementType.accept(this);
		if (newArrayExpr.elementType.type.equal(BaseType.ERROR)) {
			newArrayExpr.type = BaseType.ERROR;
		} else if (newArrayExpr.elementType.type.equal(BaseType.VOID)) {
			issueError(new BadArrElementError(newArrayExpr.elementType
					.getLocation()));
			newArrayExpr.type = BaseType.ERROR;
		} else {
			newArrayExpr.type = new ArrayType(
					newArrayExpr.elementType.type);
		}
		newArrayExpr.length.accept(this);
		if (!newArrayExpr.length.type.equal(BaseType.ERROR)
				&& !newArrayExpr.length.type.equal(BaseType.INT)) {
			issueError(new BadNewArrayLength(newArrayExpr.length.getLocation()));
		}
	}

	@Override
	public void visitNewClass(Tree.NewClass newClass) {
		Class c = table.lookupClass(newClass.className);
		newClass.symbol = c;
		if (c == null) {
			issueError(new ClassNotFoundError(newClass.getLocation(),
					newClass.className));
			newClass.type = BaseType.ERROR;
		} else {
			newClass.type = c.getType();
		}
	}

	@Override
	public void visitThisExpr(Tree.ThisExpr thisExpr) {
		if (currentFunction.isStatik()) {
			issueError(new ThisInStaticFuncError(thisExpr.getLocation()));
			thisExpr.type = BaseType.ERROR;
		} else {
			thisExpr.type = ((ClassScope) table.lookForScope(Scope.Kind.CLASS))
					.getOwner().getType();
		}
	}

	@Override
	public void visitTypeTest(Tree.TypeTest instanceofExpr) {
		instanceofExpr.instance.accept(this);
		if (!instanceofExpr.instance.type.isClassType()) {
			issueError(new NotClassError(instanceofExpr.instance.type
					.toString(), instanceofExpr.getLocation()));
		}
		Class c = table.lookupClass(instanceofExpr.className);
		instanceofExpr.symbol = c;
		instanceofExpr.type = BaseType.BOOL;
		if (c == null) {
			issueError(new ClassNotFoundError(instanceofExpr.getLocation(),
					instanceofExpr.className));
		}
	}

	@Override
	public void visitTypeCast(Tree.TypeCast cast) {
		cast.expr.accept(this);
		if (!cast.expr.type.isClassType()) {
			issueError(new NotClassError(cast.expr.type.toString(),
					cast.getLocation()));
		}
		Class c = table.lookupClass(cast.className);
		cast.symbol = c;
		if (c == null) {
			issueError(new ClassNotFoundError(cast.getLocation(),
					cast.className));
			cast.type = BaseType.ERROR;
		} else {
			cast.type = c.getType();
		}
	}

	@Override
	public void visitIdent(Tree.Ident ident) {
		if (ident.owner == null) {
			Symbol v = table.lookupBeforeLocation(ident.name, ident
					.getLocation());
			if (v == null) {
				issueError(new UndeclVarError(ident.getLocation(), ident.name));
				ident.type = BaseType.ERROR;
			} else if (v.isVariable()) {
				Variable var = (Variable) v;
				ident.type = var.getType();
				ident.symbol = var;
				if (var.isLocalVar()) {
					ident.lvKind = Tree.LValue.Kind.LOCAL_VAR;
				} else if (var.isParam()) {
					ident.lvKind = Tree.LValue.Kind.PARAM_VAR;
				} else {
					if (currentFunction.isStatik()) {
						issueError(new RefNonStaticError(ident.getLocation(),
								currentFunction.getName(), ident.name));
					} else {
						ident.owner = new Tree.ThisExpr(ident.getLocation());
						ident.owner.accept(this);
					}
					ident.lvKind = Tree.LValue.Kind.MEMBER_VAR;
				}
			} else {
				ident.type = v.getType();
				if (v.isClass()) {
					if (ident.usedForRef) {
						ident.isClass = true;
					} else {
						issueError(new UndeclVarError(ident.getLocation(),
								ident.name));
						ident.type = BaseType.ERROR;
					}

				}
			}
		} else {
			ident.owner.usedForRef = true;
			ident.owner.accept(this);
			if (!ident.owner.type.equal(BaseType.ERROR)) {
				if (ident.owner.isClass || !ident.owner.type.isClassType()) {
					issueError(new NotClassFieldError(ident.getLocation(),
							ident.name, ident.owner.type.toString()));
					ident.type = BaseType.ERROR;
				} else {
					if(ident.owner.Super)
					{
						issueError(new SuperVar(ident.getLocation()));
						ident.type = BaseType.ERROR;
						return;
					}
					ClassScope cs = ((ClassType) ident.owner.type)
							.getClassScope();
					Symbol v = cs.lookupVisible(ident.name);
					if (v == null) {
						issueError(new FieldNotFoundError(ident.getLocation(),
								ident.name, ident.owner.type.toString()));
						ident.type = BaseType.ERROR;
					} else if (v.isVariable()) {
						ClassType thisType = ((ClassScope) table
								.lookForScope(Scope.Kind.CLASS)).getOwner()
								.getType();
						ident.type = v.getType();
						if (!thisType.compatible(ident.owner.type)) {
							issueError(new FieldNotAccessError(ident
									.getLocation(), ident.name,
									ident.owner.type.toString()));
						} else {
							ident.symbol = (Variable) v;
							ident.lvKind = Tree.LValue.Kind.MEMBER_VAR;
						}
					} else {
						ident.type = v.getType();
					}
				}
			} else {
				ident.type = BaseType.ERROR;
			}
		}
	}

	@Override
	public void visitClassDef(Tree.ClassDef classDef) {
		table.open(classDef.symbol.getAssociatedScope());
		for (Tree f : classDef.fields) {
			f.accept(this);
		}
		table.close();
	}

	@Override
	public void visitMethodDef(Tree.MethodDef func) {
		this.currentFunction = func.symbol;
		table.open(func.symbol.getAssociatedScope());
		func.body.accept(this);
		table.close();
	}

	@Override
	public void visitTopLevel(Tree.TopLevel program) {
		table.open(program.globalScope);
		for (Tree.ClassDef cd : program.classes) {
			cd.accept(this);
		}
		table.close();
	}

	@Override
	public void visitBlock(Tree.Block block) {
		table.open(block.associatedScope);
		for (Tree s : block.block) {
			s.accept(this);
		}
		table.close();
	}

	@Override
	public void visitAssign(Tree.Assign assign) {
		assign.left.accept(this);
		assign.expr.accept(this);
		//System.out.print(assign);
		if(assign.expr.copy)
		{
			if(!assign.left.type.equal(BaseType.ERROR)&&!assign.expr.type.equal(BaseType.ERROR) &&!assign.left.type.equal(assign.expr.type))
				issueError(new BadCopy2(assign.getLocation(),assign.expr.type.toString(),assign.left.type.toString()));
		}else if (!assign.left.type.equal(BaseType.ERROR)
				&& (assign.left.type.isFuncType() || !assign.expr.type
						.compatible(assign.left.type))) {
			issueError(new IncompatBinOpError(assign.getLocation(),
					assign.left.type.toString(), "=", assign.expr.type
							.toString()));
		}
	}

	@Override
	public void visitBreak(Tree.Break breakStmt) {
		if (breaks.empty()) {
			issueError(new BreakOutOfLoopError(breakStmt.getLocation()));
		}
	}

	@Override
	public void visitForLoop(Tree.ForLoop forLoop) {
		if (forLoop.init != null) {
			forLoop.init.accept(this);
		}
		checkTestExpr(forLoop.condition);
		if (forLoop.update != null) {
			forLoop.update.accept(this);
		}
		breaks.add(forLoop);
		if (forLoop.loopBody != null) {
			forLoop.loopBody.accept(this);
		}
		breaks.pop();
	}

	@Override
	public void visitIf(Tree.If ifStmt) {
		checkTestExpr(ifStmt.condition);
		if (ifStmt.trueBranch != null) {
			ifStmt.trueBranch.accept(this);
		}
		if (ifStmt.falseBranch != null) {
			ifStmt.falseBranch.accept(this);
		}
	}

	@Override
	public void visitPrint(Tree.Print printStmt) {
		int i = 0;
		for (Tree.Expr e : printStmt.exprs) {
			e.accept(this);
			i++;
			if (!e.type.equal(BaseType.ERROR) && !e.type.equal(BaseType.BOOL)
					&& !e.type.equal(BaseType.INT)
					&& !e.type.equal(BaseType.STRING)) {
				issueError(new BadPrintArgError(e.getLocation(), Integer
						.toString(i), e.type.toString()));
			}
		}
	}

	@Override
	public void visitReturn(Tree.Return returnStmt) {
		Type returnType = ((FormalScope) table
				.lookForScope(Scope.Kind.FORMAL)).getOwner().getReturnType();
		if (returnStmt.expr != null) {
			returnStmt.expr.accept(this);
		}
		if (returnType.equal(BaseType.VOID)) {
			if (returnStmt.expr != null) {
				issueError(new BadReturnTypeError(returnStmt.getLocation(),
						returnType.toString(), returnStmt.expr.type.toString()));
			}
		} else if (returnStmt.expr == null) {
			issueError(new BadReturnTypeError(returnStmt.getLocation(),
					returnType.toString(), "void"));
		} else if (!returnStmt.expr.type.equal(BaseType.ERROR)
				&& !returnStmt.expr.type.compatible(returnType)) {
			issueError(new BadReturnTypeError(returnStmt.getLocation(),
					returnType.toString(), returnStmt.expr.type.toString()));
		}
	}

	@Override
	public void visitWhileLoop(Tree.WhileLoop whileLoop) {
		checkTestExpr(whileLoop.condition);
		breaks.add(whileLoop);
		if (whileLoop.loopBody != null) {
			whileLoop.loopBody.accept(this);
		}
		breaks.pop();
	}

	// visiting types
	@Override
	public void visitTypeIdent(Tree.TypeIdent type) {
		switch (type.typeTag) {
		case Tree.VOID:
			type.type = BaseType.VOID;
			break;
		case Tree.INT:
			type.type = BaseType.INT;
			break;
		case Tree.BOOL:
			type.type = BaseType.BOOL;
			break;
		case Tree.COMPLEX:
			type.type = BaseType.COMPLEX;
			break;
		default:
			type.type = BaseType.STRING;
		}
	}

	@Override
	public void visitTypeClass(Tree.TypeClass typeClass) {
		Class c = table.lookupClass(typeClass.name);
		if (c == null) {
			issueError(new ClassNotFoundError(typeClass.getLocation(),
					typeClass.name));
			typeClass.type = BaseType.ERROR;
		} else {
			typeClass.type = c.getType();
		}
	}

	@Override
	public void visitTypeArray(Tree.TypeArray typeArray) {
		typeArray.elementType.accept(this);
		if (typeArray.elementType.type.equal(BaseType.ERROR)) {
			typeArray.type = BaseType.ERROR;
		} else if (typeArray.elementType.type.equal(BaseType.VOID)) {
			issueError(new BadArrElementError(typeArray.getLocation()));
			typeArray.type = BaseType.ERROR;
		} else {
			typeArray.type = new ArrayType(typeArray.elementType.type);
		}
	}
	
	@Override
	public void visitPrintCompStmt(Tree.PrintCompStmt printCompStmt)
	{
		int i=0;
		for(Tree.Expr e:printCompStmt.exprs)
		{
			e.accept(this);
			i++;
			if(!e.type.equal(BaseType.ERROR)&&!e.type.equal(BaseType.COMPLEX))
			{
				issueError(new BadPrintArgError(e.getLocation(), Integer.toString(i), e.type.toString()));///
			}
		}
	}
	
	@Override
	public void visitCaseStmt(Tree.CaseStmt caseStmt)
	{
		//System.out.println("ffff");
		caseStmt.constant.accept(this);
		caseStmt.Expr1.accept(this);
		caseStmt.type=caseStmt.Expr1.type;
	}
	
	@Override
	public void visitDefaultStmt(Tree.DefaultStmt defaultStmt) 
	{
		defaultStmt.Expr1.accept(this);
		defaultStmt.type=defaultStmt.Expr1.type;
	}
	
	@Override
	public void visitCaseExpr(Tree.CaseExpr caseExpr)
	{
		boolean flag=true;
		caseExpr.expr.accept(this);
		caseExpr.defaultStmt.accept(this);
		Type deftype=caseExpr.defaultStmt.type;
		flag=flag&&checkCaseInt(caseExpr.expr);
		if(deftype.equal(BaseType.ERROR))flag=false;
		
		Set<Object> hashset=new HashSet<Object>();

		for(Tree s:caseExpr.caseList)
		{
			Tree.CaseStmt t=(Tree.CaseStmt)s;
			t.accept(this);
			boolean f1=checkCaseInt(t);
			boolean f2=checkCaseExpr(t,deftype);
			flag=flag&&f1;
			flag=flag&&f2;
			if (!t.Expr1.type.equal(BaseType.INT))continue;
			Tree.Literal e=(Tree.Literal)t.constant;
			if(hashset.contains(e.value))
			{
				issueError(new NotUniqueCondition(e.getLocation()));
				flag=false;
			}else hashset.add(e.value);

		}
		if(!flag)caseExpr.type=BaseType.ERROR;
		else caseExpr.type=deftype;
	}
	
	@Override
	public void visitDcopyExpr(Tree.DcopyExpr dcopyExpr)
	{
		dcopyExpr.expr.accept(this);
		dcopyExpr.copy=true;
		dcopyExpr.type=dcopyExpr.expr.type;
		if(!dcopyExpr.type.equal(BaseType.ERROR)&&!dcopyExpr.type.isClassType())
		{
			issueError(new BadCopy(dcopyExpr.getLocation(),dcopyExpr.type.toString()));
			dcopyExpr.type=BaseType.ERROR;
		}
	}
	@Override
	public void visitScopyExpr(Tree.ScopyExpr scopyExpr)
	{
		scopyExpr.expr.accept(this);
		scopyExpr.copy=true;
		scopyExpr.type=scopyExpr.expr.type;
		if(!scopyExpr.type.equal(BaseType.ERROR)&&!scopyExpr.type.isClassType())
		{
			issueError(new BadCopy(scopyExpr.getLocation(),scopyExpr.type.toString()));
			scopyExpr.type=BaseType.ERROR;
		}
	}
	
	@Override
	public void visitSuperExpr(Tree.SuperExpr superExpr) {
		if (currentFunction.isStatik()) {
			issueError(new SuperInStaticFuncError(superExpr.getLocation()));
			superExpr.type = BaseType.ERROR;
			return;
		}
		superExpr.type = ((ClassScope) table.lookForScope(Scope.Kind.CLASS))
				.getOwner().getType();
		superExpr.Super=true;
		if(superExpr.call)
		{
			Symbol parClass = ((ClassScope) table.lookForScope(Scope.Kind.CLASS)).getOwner().getParent();
			if(parClass!=null)
			{
				//superExpr.type=parClass.getType();
				superExpr.nopar=false;
			}
			else superExpr.nopar=true;
		}
	}
	
	@Override
	public void visitDoSubStmt(Tree.DoSubStmt doSubStmt)
	{
		doSubStmt.condition.accept(this);
		checkDo(doSubStmt.condition);
		doSubStmt.loopBody.accept(this);
	}
	@Override
	public void visitDoStmt(Tree.DoStmt doStmt)
	{
		breaks.add(doStmt);
		for(Tree d:doStmt.caseList)
		{
			d.accept(this);
		}
		breaks.pop();
	}
	
	

	private void issueError(DecafError error) {
		Driver.getDriver().issueError(error);
	}

	private Type checkBinaryOp(Tree.Expr left, Tree.Expr right, int op, Location location) {
		
		
		if (left.type.equal(BaseType.ERROR) || right.type.equal(BaseType.ERROR)) {
			return BaseType.ERROR;
		}

		boolean compatible = false;
		Type returnType = BaseType.ERROR;
		switch (op) {
		case Tree.PLUS:
		case Tree.MUL:
			compatible=(left.type.equal(BaseType.INT)||left.type.equal(BaseType.COMPLEX))&&left.type.equal(right.type);
			returnType = left.type;
			break;
		case Tree.MINUS:
		case Tree.DIV:
			compatible = left.type.equals(BaseType.INT)
					&& left.type.equal(right.type);
			returnType = left.type;
			break;
		case Tree.GT:
		case Tree.GE:
		case Tree.LT:
		case Tree.LE:
			compatible = left.type.equal(BaseType.INT)
					&& left.type.equal(right.type);
			returnType = BaseType.BOOL;
			break;
		case Tree.MOD:
			compatible = left.type.equal(BaseType.INT)
					&& right.type.equal(BaseType.INT);
			returnType = BaseType.INT;
			break;
		case Tree.EQ:
		case Tree.NE:
			compatible = left.type.compatible(right.type)
					|| right.type.compatible(left.type);
			returnType = BaseType.BOOL;
			break;
		case Tree.AND:
		case Tree.OR:
			compatible = left.type.equal(BaseType.BOOL)
					&& right.type.equal(BaseType.BOOL);
			returnType = BaseType.BOOL;
			break;
		default:
			break;
		}
		if (!compatible) {
			issueError(new IncompatBinOpError(location, left.type.toString(),
					Parser.opStr(op), right.type.toString()));
			returnType = BaseType.ERROR;
		}
		return returnType;
	}

	private boolean checkCaseInt(Tree.Expr expr) {
		if (!expr.type.compatible(BaseType.INT)) {
			issueError(new BadCaseCond(expr.getLocation(), expr.type.toString()));
			return false;
		}
		return true;
	}
	private boolean checkCaseInt(Tree.CaseStmt caseStmt) {
		if (!caseStmt.constant.type.compatible(BaseType.INT)) 
		{
			issueError(new BadCaseCond(caseStmt.getLocation(), caseStmt.constant.type.toString()));
			return false;
		}
		return true;
	}
	private boolean checkCaseExpr(Tree.CaseStmt caseStmt, Type type) {
		if (!caseStmt.Expr1.type.equal(BaseType.ERROR) && 
			!type.equal(BaseType.ERROR) &&
			!caseStmt.Expr1.type.equal(type)) {
			issueError(new BadCaseExpr(caseStmt.getLocation(), caseStmt.Expr1.type.toString(), type.toString()));
			return false;
		}
		return true;
	}
	private void checkDo(Tree.Expr expr) {
		if (!expr.type.equal(BaseType.ERROR) && !expr.type.equal(BaseType.BOOL)) {
			issueError(new BadDo(expr.getLocation(), expr.type.toString()));
		}
	}
	private void checkTestExpr(Tree.Expr expr) {
		expr.accept(this);
		if (!expr.type.equal(BaseType.ERROR) && !expr.type.equal(BaseType.BOOL)) {
			issueError(new BadTestExpr(expr.getLocation()));
		}
	}

}

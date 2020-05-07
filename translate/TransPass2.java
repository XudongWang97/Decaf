package decaf.translate;

import java.util.List;
import java.util.Stack;
import java.util.ArrayList;


import decaf.tree.Tree;
import decaf.backend.OffsetCounter;
import decaf.machdesc.Intrinsic;
import decaf.scope.ClassScope;
import decaf.tac.Label;
import decaf.tac.Temp;
import decaf.type.*;
import decaf.symbol.*;
import java.util.Iterator;



public class TransPass2 extends Tree.Visitor {

	private Translater tr;

	private Temp currentThis;

	private Stack<Label> loopExits;

	public TransPass2(Translater tr) {
		this.tr = tr;
		loopExits = new Stack<Label>();
	}

	@Override
	public void visitClassDef(Tree.ClassDef classDef) {
		for (Tree f : classDef.fields) {
			f.accept(this);
		}
	}

	@Override
	public void visitMethodDef(Tree.MethodDef funcDefn) {
		if (!funcDefn.statik) {
			currentThis = ((Variable) funcDefn.symbol.getAssociatedScope()
					.lookup("this")).getTemp();
		}
		tr.beginFunc(funcDefn.symbol);
		funcDefn.body.accept(this);
		tr.endFunc();
		currentThis = null;
	}

	@Override
	public void visitTopLevel(Tree.TopLevel program) {
		for (Tree.ClassDef cd : program.classes) {
			cd.accept(this);
		}
	}

	@Override
	public void visitVarDef(Tree.VarDef varDef) {
		if (varDef.symbol.isLocalVar()) {
			Temp t = Temp.createTempI4();
			t.sym = varDef.symbol;
			varDef.symbol.setTemp(t);
		}
	}

	@Override
	public void visitBinary(Tree.Binary expr) {
		expr.left.accept(this);
		expr.right.accept(this);
		switch (expr.tag) {
		case Tree.PLUS:
			if(expr.left.type==BaseType.COMPLEX)expr.val=tr.genCompAdd(expr.left.val,expr.right.val);
			else expr.val = tr.genAdd(expr.left.val, expr.right.val);
			break;
		case Tree.MINUS:
			expr.val = tr.genSub(expr.left.val, expr.right.val);
			break;
		case Tree.MUL:
			if(expr.left.type==BaseType.COMPLEX)expr.val=tr.genCompMul(expr.left.val,expr.right.val);
			else expr.val = tr.genMul(expr.left.val, expr.right.val);
			break;
		case Tree.DIV:
			expr.val = tr.genDiv(expr.left.val, expr.right.val);
			break;
		case Tree.MOD:
			expr.val = tr.genMod(expr.left.val, expr.right.val);
			break;
		case Tree.AND:
			expr.val = tr.genLAnd(expr.left.val, expr.right.val);
			break;
		case Tree.OR:
			expr.val = tr.genLOr(expr.left.val, expr.right.val);
			break;
		case Tree.LT:
			expr.val = tr.genLes(expr.left.val, expr.right.val);
			break;
		case Tree.LE:
			expr.val = tr.genLeq(expr.left.val, expr.right.val);
			break;
		case Tree.GT:
			expr.val = tr.genGtr(expr.left.val, expr.right.val);
			break;
		case Tree.GE:
			expr.val = tr.genGeq(expr.left.val, expr.right.val);
			break;
		case Tree.EQ:
		case Tree.NE:
			genEquNeq(expr);
			break;
		}
	}

	private void genEquNeq(Tree.Binary expr) {
		if (expr.left.type.equal(BaseType.STRING)
				|| expr.right.type.equal(BaseType.STRING)) {
			tr.genParm(expr.left.val);
			tr.genParm(expr.right.val);
			expr.val = tr.genDirectCall(Intrinsic.STRING_EQUAL.label,
					BaseType.BOOL);
			if(expr.tag == Tree.NE){
				expr.val = tr.genLNot(expr.val);
			}
		} else {
			if(expr.tag == Tree.EQ)
				expr.val = tr.genEqu(expr.left.val, expr.right.val);
			else
				expr.val = tr.genNeq(expr.left.val, expr.right.val);
		}
	}

	@Override
	public void visitAssign(Tree.Assign assign) {
		assign.left.accept(this);
		assign.expr.accept(this);
		switch (assign.left.lvKind) {
		case ARRAY_ELEMENT:
			Tree.Indexed arrayRef = (Tree.Indexed) assign.left;
			Temp esz = tr.genLoadImm4(OffsetCounter.WORD_SIZE);
			Temp t = tr.genMul(arrayRef.index.val, esz);
			Temp base = tr.genAdd(arrayRef.array.val, t);
			tr.genStore(assign.expr.val, base, 0);
			break;
		case MEMBER_VAR:
			Tree.Ident varRef = (Tree.Ident) assign.left;
			tr.genStore(assign.expr.val, varRef.owner.val, varRef.symbol
					.getOffset());
			break;
		case PARAM_VAR:
		case LOCAL_VAR:
			if (assign.left.type == BaseType.COMPLEX)
				tr.genCompAssign(((Tree.Ident) assign.left).symbol.getTemp(),
					assign.expr.val);
			else tr.genAssign(((Tree.Ident) assign.left).symbol.getTemp(),
					assign.expr.val);
			break;
		}
	}

	@Override
	public void visitLiteral(Tree.Literal literal) {
		switch (literal.typeTag) {
		case Tree.INT:
			literal.val = tr.genLoadImm4(((Integer)literal.value).intValue());
			break;
		case Tree.BOOL:
			literal.val = tr.genLoadImm4((Boolean)(literal.value) ? 1 : 0);
			break;
		case Tree.STRING:
			literal.val = tr.genLoadStrConst((String)literal.value);
			break;
		default:
			literal.val=tr.genLoadImage(((Integer)literal.value).intValue());
		}
	}

	@Override
	public void visitExec(Tree.Exec exec) {
		exec.expr.accept(this);
	}

	@Override
	public void visitUnary(Tree.Unary expr) {
		expr.expr.accept(this);
		switch (expr.tag){
		case Tree.NEG:
			expr.val = tr.genNeg(expr.expr.val);
			break;
		case Tree.RE:
			expr.val=tr.genRe(expr.expr.val);
			break;
		case Tree.IM:
			expr.val=tr.genIm(expr.expr.val);
			break;
		case Tree.COMPCAST:
			expr.val=tr.genCompcast(expr.expr.val);
			break;
		default:
			expr.val = tr.genLNot(expr.expr.val);
		}
	}
	
	@Override
	public void visitCaseExpr(Tree.CaseExpr expr)
	{
		expr.expr.accept(this);
		Temp res=Temp.createTempI4();
		Label exit=Label.createLabel();
		List<Label> condList=new ArrayList<Label>();
		for(Tree s:expr.caseList)
		{
			condList.add(Label.createLabel());
		}
		Label defaultMark=Label.createLabel();
		for(int i=0;i<expr.caseList.size();i++)
		{
			Tree.CaseStmt s=(Tree.CaseStmt)expr.caseList.get(i);
			tr.genMark(condList.get(i));
			Temp t=Temp.createTempI4();
			s.constant.accept(this);
			Tree.Binary expr1=new Tree.Binary(Tree.EQ,expr.expr,s.constant,s.loc);
			genEquNeq(expr1);
			tr.genAssign(t,expr1.val);
			Label next;
			if(i<expr.caseList.size()-1)
				next=condList.get(i+1);
			else 
				next=defaultMark;
			tr.genBeqz(t,next);
			s.Expr1.accept(this);
			tr.genAssign(res,s.Expr1.val);
			tr.genBranch(exit);
		}
		tr.genMark(defaultMark);
		Tree.DefaultStmt s=(Tree.DefaultStmt)expr.defaultStmt;
		s.Expr1.accept(this);
		tr.genAssign(res,s.Expr1.val);
		tr.genMark(exit);
		expr.val=res;
	}

	@Override
	public void visitNull(Tree.Null nullExpr) {
		nullExpr.val = tr.genLoadImm4(0);
	}

	@Override
	public void visitBlock(Tree.Block block) {
		for (Tree s : block.block) {
			s.accept(this);
		}
	}

	@Override
	public void visitThisExpr(Tree.ThisExpr thisExpr) {
		thisExpr.val = currentThis;
	}

	@Override
	public void visitReadIntExpr(Tree.ReadIntExpr readIntExpr) {
		readIntExpr.val = tr.genIntrinsicCall(Intrinsic.READ_INT);
	}

	@Override
	public void visitReadLineExpr(Tree.ReadLineExpr readStringExpr) {
		readStringExpr.val = tr.genIntrinsicCall(Intrinsic.READ_LINE);
	}

	@Override
	public void visitReturn(Tree.Return returnStmt) {
		if (returnStmt.expr != null) {
			returnStmt.expr.accept(this);
			tr.genReturn(returnStmt.expr.val);
		} else {
			tr.genReturn(null);
		}

	}

	@Override
	public void visitPrint(Tree.Print printStmt) {
		for (Tree.Expr r : printStmt.exprs) {
			r.accept(this);
			tr.genParm(r.val);
			if (r.type.equal(BaseType.BOOL)) {
				tr.genIntrinsicCall(Intrinsic.PRINT_BOOL);
			} else if (r.type.equal(BaseType.INT)) {
				tr.genIntrinsicCall(Intrinsic.PRINT_INT);
			} else if (r.type.equal(BaseType.STRING)) {
				tr.genIntrinsicCall(Intrinsic.PRINT_STRING);
			}
		}
	}

	@Override
	public void visitPrintCompStmt(Tree.PrintCompStmt printStmt)
	{
		for (Tree.Expr expr:printStmt.exprs) {
			expr.accept(this);
			tr.genParm(tr.genLoad(expr.val, 0));
			tr.genIntrinsicCall(Intrinsic.PRINT_INT);
			tr.genParm(tr.genLoadStrConst("+"));
			tr.genIntrinsicCall(Intrinsic.PRINT_STRING);
			tr.genParm(tr.genLoad(expr.val, 4));
			tr.genIntrinsicCall(Intrinsic.PRINT_INT);
			tr.genParm(tr.genLoadStrConst("j"));
			tr.genIntrinsicCall(Intrinsic.PRINT_STRING);
		}
	}
	@Override
	public void visitIndexed(Tree.Indexed indexed) {
		indexed.array.accept(this);
		indexed.index.accept(this);
		tr.genCheckArrayIndex(indexed.array.val, indexed.index.val);
		
		Temp esz = tr.genLoadImm4(OffsetCounter.WORD_SIZE);
		Temp t = tr.genMul(indexed.index.val, esz);
		Temp base = tr.genAdd(indexed.array.val, t);
		indexed.val = tr.genLoad(base, 0);
	}

	@Override
	public void visitIdent(Tree.Ident ident) {
		if(ident.lvKind == Tree.LValue.Kind.MEMBER_VAR){
			ident.owner.accept(this);
		}
		
		switch (ident.lvKind) {
		case MEMBER_VAR:
			ident.val = tr.genLoad(ident.owner.val, ident.symbol.getOffset());
			break;
		default:
			ident.val = ident.symbol.getTemp();
			break;
		}
	}
	
	@Override
	public void visitBreak(Tree.Break breakStmt) {
		tr.genBranch(loopExits.peek());
	}

	@Override
	public void visitCallExpr(Tree.CallExpr callExpr) {
		if (callExpr.isArrayLength) {
			callExpr.receiver.accept(this);
			callExpr.val = tr.genLoad(callExpr.receiver.val,
					-OffsetCounter.WORD_SIZE);
		} else {
			if (callExpr.receiver != null) {
				callExpr.receiver.accept(this);
			}
			for (Tree.Expr expr : callExpr.actuals) {
				expr.accept(this);
			}
			if (callExpr.receiver != null) {
				tr.genParm(callExpr.receiver.val);
			}
			for (Tree.Expr expr : callExpr.actuals) {
				tr.genParm(expr.val);
			}
			if (callExpr.receiver == null) {
				callExpr.val = tr.genDirectCall(
						callExpr.symbol.getFuncty().label, callExpr.symbol
								.getReturnType());

			} else {
				Temp vt, func, ovt;
				vt = tr.genLoad(callExpr.receiver.val, 0);
				ovt = tr.genLoad(callExpr.receiver.val, 0);
				ClassType now = callExpr.otype, trueT = (ClassType)callExpr.receiver.type;
				ClassScope cs = now.getClassScope();
				
				trueT = (ClassType) ((ClassScope)trueT.getClassScope().lookupVisible(callExpr.method).
						getScope()).getOwner().getType();
				
				while (!now.equal(trueT))
				{
					vt = tr.genLoad(vt, 0);
					now = now.getParentType();
				}
				cs = now.getClassScope();
				func = tr.genLoad(vt, ((Function)cs.lookupVisible(callExpr.method)).getOffset());
				
				tr.genStore(vt, callExpr.receiver.val, 0);
				
				callExpr.val = tr.genIndirectCall(func, callExpr.symbol
						.getReturnType());
				tr.genStore(ovt, callExpr.receiver.val, 0);
			}
		}

	}

	
	@Override
	public void visitDoStmt(Tree.DoStmt doStmt)
	{
		Label start=Label.createLabel();
		Label exit=Label.createLabel();
		List<Label> condList=new ArrayList<Label>();
		for(Tree d:doStmt.caseList)
			condList.add(Label.createLabel());
		tr.genMark(start);
		for(int i=0;i<condList.size();i++)
		{
			tr.genMark(condList.get(i));
			Tree.DoSubStmt s=(Tree.DoSubStmt)doStmt.caseList.get(i);
			s.condition.accept(this);
			Label next;
			if(i<condList.size()-1)
				next=condList.get(i+1);
			else 
				next=exit;
			tr.genBeqz(s.condition.val,next);
			loopExits.push(exit);
			if(s.loopBody!=null)
				s.loopBody.accept(this);
			loopExits.pop();
			tr.genBranch(start);
		}
		tr.genMark(exit);
	}
	
	@Override
	public void visitForLoop(Tree.ForLoop forLoop) {
		if (forLoop.init != null) {
			forLoop.init.accept(this);
		}
		Label cond = Label.createLabel();
		Label loop = Label.createLabel();
		tr.genBranch(cond);
		tr.genMark(loop);
		if (forLoop.update != null) {
			forLoop.update.accept(this);
		}
		tr.genMark(cond);
		forLoop.condition.accept(this);
		Label exit = Label.createLabel();
		tr.genBeqz(forLoop.condition.val, exit);
		loopExits.push(exit);
		if (forLoop.loopBody != null) {
			forLoop.loopBody.accept(this);
		}
		tr.genBranch(loop);
		loopExits.pop();
		tr.genMark(exit);
	}

	@Override
	public void visitIf(Tree.If ifStmt) {
		ifStmt.condition.accept(this);
		if (ifStmt.falseBranch != null) {
			Label falseLabel = Label.createLabel();
			tr.genBeqz(ifStmt.condition.val, falseLabel);
			ifStmt.trueBranch.accept(this);
			Label exit = Label.createLabel();
			tr.genBranch(exit);
			tr.genMark(falseLabel);
			ifStmt.falseBranch.accept(this);
			tr.genMark(exit);
		} else if (ifStmt.trueBranch != null) {
			Label exit = Label.createLabel();
			tr.genBeqz(ifStmt.condition.val, exit);
			if (ifStmt.trueBranch != null) {
				ifStmt.trueBranch.accept(this);
			}
			tr.genMark(exit);
		}
	}

	@Override
	public void visitNewArray(Tree.NewArray newArray) {
		newArray.length.accept(this);
		newArray.val = tr.genNewArray(newArray.length.val);
	}

	@Override
	public void visitNewClass(Tree.NewClass newClass) {
		newClass.val = tr.genDirectCall(newClass.symbol.getNewFuncLabel(),
				BaseType.INT);
	}

	@Override
	public void visitWhileLoop(Tree.WhileLoop whileLoop) {
		Label loop = Label.createLabel();
		tr.genMark(loop);
		whileLoop.condition.accept(this);
		Label exit = Label.createLabel();
		tr.genBeqz(whileLoop.condition.val, exit);
		loopExits.push(exit);
		if (whileLoop.loopBody != null) {
			whileLoop.loopBody.accept(this);
		}
		tr.genBranch(loop);
		loopExits.pop();
		tr.genMark(exit);
	}

	@Override
	public void visitTypeTest(Tree.TypeTest typeTest) {
		typeTest.instance.accept(this);
		typeTest.val = tr.genInstanceof(typeTest.instance.val,
				typeTest.symbol);
	}

	@Override
	public void visitTypeCast(Tree.TypeCast typeCast) {
		typeCast.expr.accept(this);
		if (!typeCast.expr.type.compatible(typeCast.symbol.getType())) {
			tr.genClassCast(typeCast.expr.val, typeCast.symbol);
		}
		typeCast.val = typeCast.expr.val;
	}
	
	@Override
	public void visitScopyExpr(Tree.ScopyExpr expr)
	{
		expr.expr.accept(this);
		ClassType c=((ClassType)expr.expr.type);
		Iterator<Symbol> s=c.getClassScope().iterator();
		Temp size=tr.genLoadImm4(c.getSymbol().getSize());
		tr.genParm(size);
		Temp res=tr.genIntrinsicCall(Intrinsic.ALLOCATE);
		while(s.hasNext())
		{
			Symbol x=s.next();
			if(x.isVariable())
			{
				Variable y=(Variable)x;
				tr.genStore(tr.genLoad(expr.expr.val,y.getOffset()),res,y.getOffset());
			}
		}
		tr.genStore(tr.genLoad(expr.expr.val,0),res,0);
		expr.val=res;
	}
	
	public Temp genDcopy(Type type,Temp base)
	{
		ClassType c=((ClassType)type);
		Iterator<Symbol> s=c.getClassScope().iterator();
		Temp size=tr.genLoadImm4(c.getSymbol().getSize());
		tr.genParm(size);
		Temp res=tr.genIntrinsicCall(Intrinsic.ALLOCATE);
		while(s.hasNext())
		{
			Symbol x=s.next();
			if(x.isVariable())
			{
				Variable y=(Variable)x;
				if (x.getType().equal(BaseType.COMPLEX))
				{
					size = tr.genLoadImm4(8);
					tr.genParm(size);
					Temp tmp=tr.genIntrinsicCall(Intrinsic.ALLOCATE);
					tr.genStore(tmp,res,y.getOffset());
					Temp idx=tr.genLoad(base,y.getOffset());
					tr.genStore(tr.genLoad(idx,0),tmp,0);
					tr.genStore(tr.genLoad(idx,4),tmp,4);
				}else if (x.getType().isClassType())
				{
					Temp tmp=genDcopy(x.getType(),tr.genLoad(base,y.getOffset()));
					tr.genStore(tmp,res,y.getOffset());
				}else tr.genStore(tr.genLoad(base,y.getOffset()),res,y.getOffset());
			}
		}
		tr.genStore(tr.genLoad(base,0),res,0);
		return res;
	}
	
	@Override
	public void visitDcopyExpr(Tree.DcopyExpr expr)
	{
		expr.expr.accept(this);
		expr.val=genDcopy(expr.expr.type,expr.expr.val);
	}
	
	@Override
	public void visitSuperExpr(Tree.SuperExpr expr) {
		expr.val = currentThis;
	}
}

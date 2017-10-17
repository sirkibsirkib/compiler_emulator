package instructions;

import java.math.BigInteger;

import compiler.Compiler;
import compiler.Method;

import expressions.Cast;
import expressions.Expression;
import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class FunctionCall implements Expression, Instruction, Assembles{
	Method m;
	Expression args;
	public FunctionCall(Method m, Expression args){
		this.m = m;
		this.args = args;
		int argsExpected = Compiler.getAddrSpaceTakenFor(m.getArgs());
		if(args == null){
			if(argsExpected > 0){
				throw new Error();
			}else{
				return;
			}
		}
		int argsGiven = args.getByteEnumeration();
		if(argsExpected != argsGiven){
			this.args = new Cast(args, argsExpected);
			System.out.println("Warning!: " + m.getName() + "() call expects argspace of size " + argsExpected + ". Casting given " + argsGiven + " to correct size.");
		}
		
	}
	@Override
	public String stringify(int depth) {
		String s = m.getName() + "()\n";
		if(args != null){
			s += Compiler.tabs(depth) + args.stringify(depth+1);
		}
		return s;
	}
	@Override
	public void assembleTo(BinProgram ac) {
		args.assembleTo(ac);
		ac.add(ac.getLinkedBinInstruction(OpCode.INVOKE, m, "fun call"));
	}
	
	@Override
	public int getByteEnumeration() {
		return Compiler.getAddrSpaceTakenFor(m.getRets());
	}
	@Override
	public boolean hasCompileTimeValue() {
		return false;
	}
	@Override
	public BigInteger getCompileTimeValue() {
		throw new Error();
	}
}

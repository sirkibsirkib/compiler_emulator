package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

class Program implements HasVariables, Assembles, HasMethods{
	private List<Method> methods;
	private List<Variable> global;
	private Map<Method, Integer> headerAddr;
	private BinProgram code;
	boolean unlinkedAssemblyDone;
	
	public Program(List<Line> lines, boolean verbose) {
		code = new BinProgram();
		methods = new ArrayList<>();
		global = new ArrayList<>();
		headerAddr = new HashMap<>();
		List<Line> compilerCommands = new ArrayList<>();
		List<Line> codeInstructions = new ArrayList<>();
		for(Line l : lines){
			if(Compiler.lineIsCompilationCommand(l) && l.depth() == 0){
				compilerCommands.add(l);
			}else{
				codeInstructions.add(l);
			}
		}
		for(Line l : compilerCommands){
			Compiler.populateVars(l.getS().trim().substring(1), global, 'g');
		}
		int i = 0;
		while(i < codeInstructions.size()){
			Line l = codeInstructions.get(i);
			if(l.depth() != 0 && l.getS().trim().length() == 0){
				i++;
			}else{
				int methodEndIndex = Compiler.influenceEndIndex(codeInstructions, i, false);
				List<Line> body = codeInstructions.subList(i+1, methodEndIndex+1);
				Method m = new Method(l, body, this, this);
				if(m.getName().length() > 0){
					methods.add(m);
				}
				i = methodEndIndex+1;
			}
		}
		int globalSpaceTaken = Compiler.getAddrSpaceTakenFor(global);
		BinInstruction globalSpace = new BinInstruction(OpCode.PUSH_2, Compiler.cast(globalSpaceTaken, 2), "glob. var space");
		code.add(globalSpace);
		BinInstruction jumpToMain = 
				code.getLinkedBinInstruction(OpCode.INVOKE, lookupMethod("main"), "program start");
		code.add(jumpToMain); //start addr.
		code.add(new BinInstruction(OpCode.END, "program end"));
		for(Method m : methods){
			m.compile();
			int nextAddr = code.numBytesTaken();
			m.assembleTo(code);
			headerAddr.put(m, nextAddr);
		}
		if(verbose && code.danglingLinks() > 0){
			System.out.println(code.danglingLinks() + " DANGLING LINKS!");
		}
		if(!verbose){
			return;
		}
		System.out.println(stringify(1));
		System.out.println(code);
	}

	public byte[] compile() {
		return code.compile();
	}

	@Override
	public Variable lookup(String name) {
		for(Variable g : global){
			if(g.name.equals(name)) return g;
		}
		return null;
	}
	
	@Override
	public String stringify(int depth){
		String s = "PRINTING DATASTRCUTURE:\nProgram\n";
		s += "GLOB: " + (global.toString().replace("\n", "")) + '\n';
		for(Method m : methods){
			s += Compiler.tabs(depth) + m.stringify(depth+1) + "\n\n";
		}
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		for(Method m : methods){
			m.assembleTo(ac);
		}
		ac.add(new BinInstruction(OpCode.END, "program"));
	}

	@Override
	public Method lookupMethod(String functionName) {
		for(Method m : methods){
			if(m.getName().equals(functionName)){
				return m;
			}
		}
		return null;
	}

	@Override
	public int addrOfMethodAreaFor(Method m) {
		return 0;
	}

	public void linkTo(BinInstruction x, HasAddress hasAddress) {
		if(x.getOpCode() == OpCode.INVOKE){
			x.overWriteData(Compiler.cast(headerAddr.get(hasAddress), 4));
		}
	}
}


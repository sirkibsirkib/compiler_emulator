package compiler;

import instructions.Instruction;
import instructions.Return;

import java.util.ArrayList;
import java.util.List;

import assembly.Assembles;
import assembly.BinProgram;
import assembly.BinInstruction;
import assembly.OpCode;

public class Method implements HasVariables, Assembles, HasAddress{

	public static final String UP_PAREN = "{[(";
	public static final String DOWN_PAREN = "}])";
	
	MethodHeader mh;
	private List<Variable> args;
	private List<Variable> rets;
	List<Variable> vars;
	List<Instruction> instructions;
	HasVariables higher;
	HasMethods hasMethods;
	private String name;
	List<Line> body;
	
	Method(Line header, List<Line> body, HasVariables higher, HasMethods hasMethods){
		this.higher = higher;
		this.hasMethods = hasMethods;
		args = new ArrayList<>();
		rets = new ArrayList<>();
		vars = new ArrayList<>();
		instructions = new ArrayList<>();
		parseHeader(header);
		this.body = body;
	}
	
	void compile(){
		if(body.size() > 0){
			parseBody(body);
		}
		mh = new MethodHeader(
				Compiler.getAddrSpaceTakenFor(getRets()),
				Compiler.getAddrSpaceTakenFor(getArgs()),
				Compiler.getAddrSpaceTakenFor(vars),
				name);
	}

	private void parseBody(List<Line> block) {
		List<Line> compilerCommands = new ArrayList<>();
		List<Line> codeInstructions = new ArrayList<>();
		for(Line l : block){
			if(Compiler.lineIsCompilationCommand(l)){
				compilerCommands.add(l);
			}else{
				codeInstructions.add(l);
			}
		}
		parseCompilerCommands(compilerCommands);
		instructions = Parser.parseAll(codeInstructions, this);
		
	}
	private void parseCompilerCommands(List<Line> lines) {
		for(Line l : lines){
			String s = l.getS().trim().substring(1);
			Compiler.populateVars(s, vars, 'v'); //populate definitions
		}
	}

	@Override
	public Variable lookup(String name) {
		for(Variable a : getArgs()){
			if(a.name.equals(name)) return a;
		}
		for(Variable v : vars){
			if(v.name.equals(name)) return v;
		}
		for(Variable r : getRets()){
			if(r.name.equals(name)) return r;
		}
		return higher.lookup(name);
	}
	
	private void parseHeader(Line header) {
		List<String> chunks = Compiler.splitOn(header.getS(), "==>");
		for(int i  = 0; i < chunks.size(); i++){
			chunks.set(i, chunks.get(i).trim());
		}
		boolean[] hasSquareBrackets = new boolean[chunks.size()];
		for(int i = 0; i < hasSquareBrackets.length; i++){
			hasSquareBrackets[i] = chunks.get(i).contains("[");
		}
		extractHeaderStuff(chunks, hasSquareBrackets);
	}

	private void extractHeaderStuff(List<String> chunks, boolean[] hasSqrBrk) {
		if(hasSqrBrk.length == 1){ //name
			if(!hasSqrBrk[0]){
				name = chunks.get(0);
				Compiler.populateVars("", getArgs(), 'a');
				Compiler.populateVars("", getRets(), 'r');
				return;
			}
		}else if(hasSqrBrk.length == 2){
			if(!hasSqrBrk[0] && hasSqrBrk[1]){	//name==>[]
				Compiler.populateVars("", getArgs(), 'a');
				Compiler.populateVars(chunks.get(1), getRets(), 'r');
				name = chunks.get(0);
				return;
			} else if(hasSqrBrk[0] && !hasSqrBrk[1]){//[]==>name
				Compiler.populateVars(chunks.get(0), getArgs(), 'a');
				Compiler.populateVars("", getRets(), 'r');
				name = chunks.get(1);
				return;
			}
		}
		if(hasSqrBrk.length == 3 && hasSqrBrk[0] && !hasSqrBrk[1] && hasSqrBrk[2]){//[]==>name==>[]
			Compiler.populateVars(chunks.get(0), getArgs(), 'a');
			Compiler.populateVars(chunks.get(2), getRets(), 'r');
			name = chunks.get(1);
			return;
		}
		throw new Error();
	}
	
	@Override
	public String toString(){
		String s = getArgs() + " ==> " + getName() + " ==> " + getRets() + '\n';
		s += "  VARS: " + vars + '\n';
		s += "  has " + instructions.size() + " insructions.\n";
		for(Instruction i : instructions){
			s += "  -" + i.toString() + '\n';
		}
		s += "argspace of " + Compiler.getAddrSpaceTakenFor(getArgs()) + 
				", varspace of " + Compiler.getAddrSpaceTakenFor(vars) +
				", retspace of " + Compiler.getAddrSpaceTakenFor(getRets());
		return s;
	}

	@Override
	public void assembleTo(BinProgram ac) {
		ac.myAddressStart(this);
		mh.assembleTo(ac);
		for(Instruction i : instructions){
			i.assembleTo(ac);
		}
		if(instructions.size() == 0 || !(instructions.get(instructions.size()-1) instanceof Return)){
			ac.add(new BinInstruction(OpCode.RET, "method"));
		}
	}

	@Override
	public String stringify(int depth) {
		String s = getName() + "()\n";
		s += Compiler.tabs(depth-1) + "ARGS: " + (getArgs().toString().replace("\n", "")) + '\n';
		s += Compiler.tabs(depth-1) + "VARS: " + (vars.toString().replace("\n", "")) + '\n';
		s += Compiler.tabs(depth-1) + "RETS: " + (getRets().toString().replace("\n", "")) + '\n';
		for(int i = 0; i < instructions.size(); i++){
			s += Compiler.tabs(depth) + instructions.get(i).stringify(depth+1);
		}
		return s;
	}

	@Override
	public Method lookupMethod(String functionName) {
		return higher.lookupMethod(functionName);
	}

	public int myMethodArea() {
		return hasMethods.addrOfMethodAreaFor(this);
	}

	public List<Variable> getArgs() {
		return args;
	}

	public String getName() {
		return name;
	}

	public List<Variable> getRets() {
		return rets;
	}
}

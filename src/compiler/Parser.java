package compiler;

import instructions.ALLOCATE;
import instructions.Assignment;
import instructions.Chuck;
import instructions.END;
import instructions.FREE;
import instructions.FunctionCall;
import instructions.HOLD;
import instructions.IN;
import instructions.IfElse;
import instructions.Instruction;
import instructions.OUT;
import instructions.OUTHEX;
import instructions.Return;
import instructions.TOS;
import instructions.While;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import expressions.And;
import expressions.Cast;
import expressions.Constant;
import expressions.Equal;
import expressions.Expression;
import expressions.Fetch;
import expressions.FrameRefPush;
import expressions.LessThan;
import expressions.Not;
import expressions.Operation;
import expressions.Or;
import expressions.Reference;
import expressions.Split;

public abstract class Parser {
	public static final String UP_PAREN = "{[(";
	public static final String DOWN_PAREN = "}])";
	
	public static List<Instruction> parseAll(List<Line> lines, HasVariables hv){
		cleanEmptyLines(lines);
		List<Instruction> instructions = new ArrayList<>();
		if(lines.size() == 0){
			return instructions;
		}
		int focusDepth = lines.get(0).depth();
		IfElse adjacentPrevConditional = null;
		
		int i = 0;
		while(i < lines.size()){
			if(lines.get(i).depth() != focusDepth){
				i++;
			}else{
				int influenceEndAt = Compiler.influenceEndIndex(lines, i, false);
				Instruction next = null;
				try{
					next = influenceEndAt==i ?
							parse(lines.get(i), hv) :
							parseBlock(adjacentPrevConditional, lines.subList(i, influenceEndAt+1), hv);
					
				}catch(ParseError pe){
					System.out.println("At line " + lines.get(i).getI() + " " + pe.getReason());
					System.exit(1);
				}
				if(next == null){
					i++;
				}else{
					i = influenceEndAt+1;
					adjacentPrevConditional = next instanceof IfElse ? (IfElse) next : null;
					if(next instanceof Expression){
						Expression exp = (Expression) next;
						if(exp.getByteEnumeration() > 0){
							instructions.add(new Chuck(exp));
						}else{
							instructions.add(next);
						}
					}else{
						instructions.add(next);
					}
				}
			}
		}
		if(instructions.contains(null)){
			throw new Error();
		}
		return instructions;
	}
	
	private static void cleanEmptyLines(List<Line> lines) {
		for(int i = 0; i < lines.size(); i++){
			String s = lines.get(i).getS();
			s = trimComment(s).trim();
			if(s.length() == 0){
				lines.remove(i);
				i--;
			}
		}
	}

	private static Instruction parseBlock(IfElse adjacentPrevConditional, List<Line> list, HasVariables hv) {
		Instruction e = null;
		e = parseIfElse(adjacentPrevConditional, list, hv);
		if(e != null) return e;
		e = parseWhile(list, hv);
		if(e != null) return e;
		return null;
	}
	
	private static Instruction parseWhile(List<Line> list, HasVariables hv){
		Expression condExp = null;
		String header = list.get(0).getS().trim();
		if(header.startsWith("while ")){
			condExp = parse(header.substring(5), hv);
			List<Line> nested = list.subList(1, list.size());
			List<Instruction> nestedIns = parseAll(nested, hv);
			return new While(condExp, nestedIns);
		}
		return null;
	}
	
	private static Instruction parseIfElse(IfElse previous, List<Line> list, HasVariables hv){
		String header = list.get(0).getS().trim();
		boolean hasPrevious = false;
		if(header.startsWith("else")){
			hasPrevious = true;
			header = header.substring(4);
		}
		Expression condExp = null;
		if(header.trim().startsWith("if ")){
			condExp = parse(header.substring(3), hv);
		}
		if(!hasPrevious && condExp == null){
			return null;
		}
		List<Line> nested = list.subList(1, list.size());
		List<Instruction> nestedIns = parseAll(nested, hv);
		return new IfElse(condExp, nestedIns, previous);
	}
	
	public static Instruction parse(Line l, HasVariables hv){
		String s = l.getS();
		s = trimComment(s).trim();
		if(s.length() == 0){
			return null;
		}
		Instruction e = null;
		e = parseReturn(s);
		if(e != null) return e;
		e = parseAssignment(s, hv);
		if(e != null) return e;
		e = (Instruction) parseRuntimeCommand(s, hv);
		if(e != null) return e;
		e = (Instruction) parseFunctionCall(s, hv);
		if(e != null) return e;
		throw new ParseError("Couldn't parse instruction <" + s + ">");
	}

	public static Expression parse(String s, HasVariables hv){
		s = s.trim();
		s = peelOuterParens(s).trim();
		if(s.length() == 0){
			return null;
		}
		Expression e = null;
		e = parseSplit(s, hv);
		if(e != null) return e;
		e = parseLogic(s, hv);
		if(e != null) return e;
		e = parseFrameRefPush(s, hv);
		if(e != null) return e;
		e = parseFunctionCall(s, hv);
		if(e != null) return e;
		e = parseOperation(s, hv);
		if(e != null) return e;
		e = parseCast(s, hv);
		if(e != null) return e;
		e = parseConstant(s, hv);
		if(e != null) return e;
		e = parseReference(s, hv);
		if(e != null) return e;
		e = parseFetch(s, hv);
		if(e != null) return e;
		e = parseRuntimeCommand(s, hv);
		if(e != null) return e;
		
		throw new ParseError("Couldn't parse expression given by <" + s + ">");
	}

	private static Expression parseFrameRefPush(String s, HasVariables hv) {
		switch(s){
		case "a|": return new FrameRefPush('a');
		case "v|": return new FrameRefPush('v');
		case "r|": return new FrameRefPush('e');
		case "g|": return new FrameRefPush('g');
		default: return null;
		}
	}

	private static Expression parseLogic(String s, HasVariables hv) {
		for(int i = 0; i < s.length()-1; i++){
			if( depthAtIndex(s, i) != 0 || i >= s.length()-1){
				continue;
			}
			char l = s.charAt(i);
			char r = s.charAt(i+1);
			if(l == '!'  && r != '='){
				return new Not(parse(s.substring(i+1), hv));
			}
			if(l == '=' && r == '='){
				return new Equal(parse(s.substring(0, i-1), hv), parse(s.substring(i+2), hv));
			}
			if(l == '!' && r == '='){
				Expression eq = new Equal(parse(s.substring(0, i-1), hv), parse(s.substring(i+2), hv));
				return new Not(eq);
			}
			if(l == '&' && r == '&'){
				Expression eq = new And(parse(s.substring(0, i-1), hv), parse(s.substring(i+2), hv));
				return new Not(eq);
			}
			if(l == '|' && r == '|'){
				return new Or(parse(s.substring(0, i-1), hv), parse(s.substring(i+2), hv));
			}
			if(l == '<' || s.charAt(i) == '>'){
				boolean strictly = true;
				String left = s.substring(0, i-1);
				String right = null;
				if(s.charAt(i+1) == '='){
					right = s.substring(i+2);
					strictly = false;
				}else{
					right = s.substring(i+1);
				}
				if(s.charAt(i) == '>'){ //swap left and right
					String hold = left;
					left = right;
					right = hold;
				}
				return new LessThan(parse(left, hv), parse(right, hv), strictly);
			}
		}
		return null;
	}

	private static Expression parseFunctionCall(String s, HasVariables hv) {
		s = peelOuterParens(s).trim();
		int index = s.indexOf('(');
		if(index == -1){
			return null;
		}
		Method m = hv.lookupMethod(s.substring(0, index));
		if(m != null){
			return new FunctionCall(m, parse(s.substring(index+1, s.length()-1), hv));
		}
		return null;
	}

	private static Expression parseRuntimeCommand(String s, HasVariables hv) {
		if(s.startsWith("ALLOCATE")){
			return new ALLOCATE(parse(s.substring(s.indexOf('(')+1, s.length()-1), hv));
		}
		if(s.startsWith("HOLD")){
			return new HOLD(parse(s.substring(s.indexOf('(')+1, s.length()-1), hv));
		}
		if(s.startsWith("FREE")){
			return new FREE(parse(s.substring(s.indexOf('(')+1, s.length()-1), hv));
		}
		if(s.startsWith("END")){
			return new END(parse(s.substring(s.indexOf('(')+1, s.length()-1), hv));
		}
		if(s.equals("TOS()")){
			return new TOS();
		}
		if(s.startsWith("OUTHEX")){
			return new OUTHEX(parse(s.substring(s.indexOf('(')+1, s.length()-1), hv));
		}
		if(s.startsWith("OUT")){
			return new OUT(parse(s.substring(s.indexOf('(')+1, s.length()-1), hv));
		}
		if(s.equals("IN()")){
			return new IN();
		}
		return null;
	}

	private static Instruction parseAssignment(String s, HasVariables hv) {
		for(int i = 1; i < s.length()-1; i++){
			if(depthAtIndex(s, i) == 0 && s.charAt(i) == '=' && s.charAt(i-1) != '='
					&& s.charAt(i-1) != '!' && s.charAt(i+1) != '='){
				Expression left = parse(s.substring(0, i), hv);
				if(left.getByteEnumeration() != 4){
					throw new ParseError("Assignment address <"+s.substring(0, i)+"> returns "
				+ left.getByteEnumeration() +" bytes instead of 4.");
				}
				Expression right = parse(s.substring(i+1), hv);
				return new Assignment(left, right);
			}
		}
		return null;
	}
	
	private static String peelOuterParens(String s) {
		s = s.trim();
		if(s.length() < 2 || s.charAt(0) != '(' || s.charAt(s.length()-1) != ')'){
			return s;
		}
		int depth = 1;
		for(int i = 1; i < s.length()-1; i++){
			char c = s.charAt(i);
			if(charInString(c, UP_PAREN)){
				depth++;
			}else if(charInString(c, DOWN_PAREN)){
				depth--;
			}
			if(depth == 0){
				return s;
			}
		}
		return peelOuterParens(s.substring(1, s.length()-1));
	}

	private static String trimComment(String s) {
		int indexOf = s.indexOf("//");
		if(indexOf != -1){
			return s.substring(indexOf+1);
		}
		return s;
	}
	
	private static Reference parseReference(String s, HasVariables hv) {
		Variable v = hv.lookup(s);
		if(v != null){
			return new Reference(v);
		}
		return null;
	}

	private static Split parseSplit(String s, HasVariables hv) {
		if(s.indexOf(',') == -1){ //optimization
			return null;
		}
		List<String> split = new ArrayList<>();
		String toSplit = s;
		for(int i = 0; i < toSplit.length(); i++){
			if(toSplit.charAt(i) == ',' && depthAtIndex(toSplit, i) == 0){
				split.add(toSplit.substring(0, i));
				toSplit = toSplit.substring(i+1);
				i = 0;
			}
		}
		if(split.size() == 0){
			return null;
		}else {
			split.add(toSplit);
			Expression[]  exp = new  Expression[split.size()];
			for(int i = 0; i < exp.length; i++){
				exp[i] = parse(split.get(i), hv);
			}
			return new Split(exp);
		}
	}

	private static Instruction parseReturn(String s) {
		if(s.equals("return")){
			return new Return();
		}
		return null;
	}

	private static Cast parseCast(String s, HasVariables hv) {
		if(s.length() >= 3 && s.charAt(s.length()-1) == ']'){
			for(int i = s.length()-2; i >= 0; i--){
				char c = s.charAt(i);				
				if(depthAtIndex(s, i) != 1){
					return null;
				}else if(c == '['){
					Expression exp = parse(s.substring(0, i), hv);
					int byteEnum = parseCompileTimeConstant(s.substring(i+1, s.length()-1), hv).intValue();
					return new Cast(exp, byteEnum);
				}
			}
		}
		return null;
	}

	private static Fetch parseFetch(String s, HasVariables hv) { //{<exp>}:<int>
		if(s.length() >= 3 && s.charAt(0) == '{'){
			for(int i = 1; i < s.length()-1; i++){
				if(s.charAt(i) == '}' && depthAtIndex(s, i) == 1 && s.charAt(i+1) == ':'){
					int byteEnum = parseCompileTimeConstant(s.substring(i+2, s.length()), hv).intValue();
					String fetchExp = s.substring(1, i);
					Expression exp = parse(fetchExp, hv);
					if(exp.getByteEnumeration() != 4){
						throw new ParseError("Fetch expression <"+fetchExp+"> returns " + exp.getByteEnumeration() + " instead of 4 bytes");
					}
					return new Fetch(byteEnum, exp);
				}else if(s.charAt(s.length()-1) == '}'){
					String fetchExp = s.substring(1, s.length()-1);
					Expression exp = parse(fetchExp, hv);
					if(exp != null && exp instanceof Reference){
						int byteEnum = ((Reference)exp).getVariableLen();
						return new Fetch(byteEnum, exp); 
					}
				}
			}
		}
		return null;
	}

	private static Expression parseConstant(String s, HasVariables hv) {
		if(s.length() > 1 && s.charAt(s.length()-1) == '~'){
			Variable v = hv.lookup(s.substring(0, s.length()-1));
			if(v != null){
				return new Constant(Compiler.cast(v.getByteSize(), 4));
			}
		}
		if(s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\''){
			if(s.length() == 3){
				return new Constant((byte)s.charAt(1));
			} else if(s.length() == 4){
				return parseHexChar(s.substring(1, 3));
			}
		}
		if(s.charAt(0) == '"' && s.charAt(s.length()-1) == '"'){
			return parseStringConstant(s.substring(1, s.length()-1));
		}
		try{
			return new Constant(Compiler.cast(Integer.parseInt(s), 4));
		}catch(Exception e){
			return null;
		}
		
	}
	
	private static BigInteger parseCompileTimeConstant(String s, HasVariables hv) {
		Expression constant = parseConstant(s, hv);
		if(constant == null || !constant.hasCompileTimeValue()){
			throw new ParseError("Value is not resolved at compile time <" + s + ">");
		}
		return constant.getCompileTimeValue();
	}

	private static Expression parseStringConstant(String s) {
		int value = 0;
		for(int i = 0; i < s.length(); i++){
			value = (value*256) + s.charAt(i);
		}
		return new Constant(s.getBytes());
	}

	private static Constant parseHexChar(String s) {
		char small = s.charAt(1);
		char large = s.charAt(0);
		int value = 0;
		if('0' <= small && small <= '9'){
			value = small - '0';
		}else if('a' <= small && small <= 'f'){
			value = small - 'a' + 10;
		}else if('A' <= small && small <= 'F'){
			value = small - 'A' + 10;
		}else{
			throw new ParseError("Couldn't parse constant <" + s + ">");
		}
		if('0' <= large && large <= '9'){
			value += 16*(large - '0');
		}else if('a' <= large && large <= 'f'){
			value += 16*(large - 'a' + 10);
		}else if('A' <= large && large <= 'F'){
			value += 16*(large - 'A' + 10);
		}else{
			throw new Error();
		}
		return new Constant((byte)value);
	}

	private static Operation parseOperation(String s, HasVariables hv) {//<exp>+<exp>, same with -,* etc
		String operations = "+-*/%";
		int charIndex = -1;
		int charPriority = 999;
		for(int i = 1; i < s.length()-1; i++){
			char c = s.charAt(i);
			if(charInString(c, operations) && depthAtIndex(s, i) == 0){
				int thisPriority = priorityOfOpChar(c);
				if(thisPriority < charPriority){
					charPriority = thisPriority;
					charIndex = i;
				}
			}
		}
		if(charIndex != -1){
			String left = s.substring(0, charIndex);
			String right = s.substring(charIndex+1, s.length());
			return new Operation(parse(left, hv), parse(right, hv), s.charAt(charIndex));
		}
		return null;
	}
	
	private static int priorityOfOpChar(char c){
		switch(c){
		case '+': return 0;
		case '-': return 0;
		case '*': return 1;
		case '/': return 1;
		case '%': return 1;
		default : throw new Error();
		}
	}
	
	private static int depthAtIndex(String s, int index){// "a[c]" == "0111"
		boolean insideString = false;
		int depth = 0;
		for(int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if(c == '"'){
				insideString = !insideString;
			}
			if(!insideString && charInString(c, UP_PAREN)){
				depth++;
			}
			if(i == index){
				return depth;
			}else if(!insideString && charInString(c, DOWN_PAREN)){
				depth--;
			}
		}
		throw new Error();
	}
	
	private static boolean charInString(char c, String s) {
		for (int i = 0; i < s.length(); i++){
		    if(c == s.charAt(i)) {
		    	return true;
		    }
		}
		return false;
	}
}

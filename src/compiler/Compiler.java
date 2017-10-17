package compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import assembly.BinInstruction;
import assembly.OpCode;

public abstract class Compiler {
	public static byte[] compile(File f, boolean verbose){		
		List<Line> lines = new ArrayList<>();
		Scanner lineScanner = null;
		try {
			lineScanner = new Scanner(f);
		} catch (FileNotFoundException e) {
			throw new Error();
		}
		int i = 1;
		while(lineScanner.hasNext()){
			lines.add(new Line(i, lineScanner.nextLine()));
			i++;
		}
		if(verbose)	System.out.println("PRINTING INPUT CODE");
		if(verbose)printLines(lines);
		
		applyMacros(lines);
		
		if(verbose)System.out.println("PRINTING CODE AFTER MACROS");
		if(verbose)printLines(lines);
		lineScanner.close();
		removeComments(lines);
		Program p = new Program(lines, verbose);
		byte[] compiled = p.compile();
		String hex = hex(compiled);
		if(verbose){
			System.out.println("BINARY:");
			System.out.println(hex);
			System.out.println("(" + compiled.length + ")bytes.");
		}
		return compiled;
	}
	
	private static void applyMacros(List<Line> lines) {
		for(int i = 0; i < lines.size(); i++){
			Line l = lines.get(i);
			String s = l.getS().trim();
			int influenceEnds = influenceEndIndex(lines, i, true);
			if(l.getS().trim().length() >= 2 && s.charAt(0) == '#' && s.contains("-->")){
				Macro m = parseMacro(s.substring(1).trim());
				if(m != null){
					for(int j = i+1; j <= influenceEnds; j++){
						lines.set(j, applyMacroTo(m, lines.get(j)));
					}
					lines.set(i, new Line(i, tabs(l.depth()) + "//mac//" + s.substring(1)));
				}
			}
		}
	}

	private static Line applyMacroTo(Macro m, Line l) {
		return new Line(l.getI(), l.getS().replace(m.from, m.to));
	}

	private static Macro parseMacro(String s) {
		int indexOfArrow = s.indexOf("-->");
		String left = s.substring(0, indexOfArrow);
		String right = s.substring(indexOfArrow+3);
		left = insideQuotes(left);
		right = insideQuotes(right);
		if(left == null || right == null){
			return null;
		}
		return new Macro(left, right);
	}

	private static String insideQuotes(String s) {
		int leftMost = s.indexOf('"');
		int rightMost = s.lastIndexOf('"');
		if(leftMost == -1 || leftMost == rightMost){
			return null;
		}
		return s.substring(leftMost+1, rightMost);
	}

	private static String lineBreak(String s, int lineBreakLim) {
		StringBuilder sb = new StringBuilder();
		int lineLength = 0;
		for(int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if(c == ' ' && lineLength >= lineBreakLim){
				c = '\n';
				lineLength = 0;
			}else{
				lineLength++;
			}
			sb.append(c);
			
		}
		return sb.toString();
	}

	public static String tabs(int number){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < number; i++){
			sb.append('\t');
		}
		return sb.toString();
	}
	
	public static String hex(byte[] x) {
		return DatatypeConverter.printHexBinary(x).replaceAll("..", "$0 ").trim();
	}

	private static void printLines(List<Line> lines) {
		for(Line l : lines){
			System.out.println(l.getI() + ": "  + l.getS());
		}
		System.out.println();
	}

	private static void removeComments(List<Line> lines) {
		for(int i = 0; i < lines.size(); i++){
			lines.set(i, removeComments(lines.get(i)));
		}
	}

	private static Line removeComments(Line line) {
		int index = line.getS().indexOf("//");
		if(index == -1){
			return line;
		}
		return new Line(line.getI(), line.getS().substring(0, index));
	}

	public static boolean isDefinition(Line l) {
		String s = l.getS().trim();
		if(s.length() == 0){
			return false;
		}
		return s.charAt(0) == '#';
	}

	public static int assignmentIndex(String s){
		s = s.trim();
		for(int i = 1; i < s.length()-1; i++){
			if(s.charAt(i-1) != '=' && s.charAt(i) == '=' && s.charAt(i+1) != '='){
				return i;
			}
		}
		return -1;
	}
	
	public static boolean lineIsCompilationCommand(Line l){
		String s = l.getS().trim();
		return s.length() > 0 && s.charAt(0) == '#';
	}
	
	public static byte[] cast(long x, int numBytes){
		byte[] bytes = new byte[numBytes];
		byte fill = (byte) (x < 0 ? 0xFF : 0x00);
		for(int i = 0; i < numBytes; i++){
			int shift = numBytes-i-1;
			if(shift >= 8){
				bytes[i] = fill;
			}else{
				bytes[i] = (byte) (x >> 8*(shift));
			}
		}
		return bytes;
	}

	public static void populateVars(String s, List<Variable> list, char retArgGlobVar){
		s = s.replace("[", " ");
		s = s.replace("]", " ");
		s = s.replace(",", " ");
		Scanner scan = new Scanner(s);
		while(scan.hasNext()){
			String name = scan.next().trim();
			int relativeAddr = getAddrSpaceTakenFor(list);
			int size = 0;
			try{
				size = scan.nextInt();
			}catch(NoSuchElementException e){
				scan.close();
				throw new ParseError("Variable definitions require byte neumerations eg:#i[4]");
			}
			
			list.add(new Variable(name, size, relativeAddr, retArgGlobVar));
		}
		scan.close();
	}


	public static int getAddrSpaceTakenFor(List<Variable> list) {
		int totalSize = 0;
		for(Variable lv : list){
			totalSize += lv.byteSize;
		}
		return totalSize;
	}

	private static void applyMacrosAt(List<Line> lines, int index, Macro m) {
		int influenceEndIndex = influenceEndIndex(lines, index, true);
		for(int i = index; i < influenceEndIndex; i++){
			lines.set(i, m.applyTo(lines.get(i)));
		}
	}
	
//	public static int methodEndIndex(List<Line> lines, int headAt){
//		if(lines.get(headAt).depth() != 0){
//			return -1;
//		}
//		int i = headAt;
//		while(i+1 < lines.size() && lines.get(i+1).depth() >= 1){
//			i++;
//		}
//		return i;
//	}

	public static int influenceEndIndex(List<Line> lines, int i, boolean includeSameLevel){
		int acceptable = lines.get(i).depth();
		if(!includeSameLevel){
			acceptable++;
		}
		while(i < lines.size()-1 && lines.get(i+1).depth() >= acceptable){
			i++;
			if(lines.size() == i){
				return lines.size()-1;
			}
		}
		return i;
	}
	
	public static List<String> splitOn(String s, String delimiter){
		List<String> chunks = new ArrayList<String>(Arrays.asList(s.split(delimiter)));
		for(int i = 0; i < chunks.size(); i++){
			chunks.set(i, chunks.get(i).trim());
		}
		return chunks;
	}

	public static byte[] concat(byte[]... x) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for(int i = 0; i < x.length; i++){
			try {
				outputStream.write(x[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return outputStream.toByteArray( );
	}
	
	public static BinInstruction generatePush(byte[] data){
		OpCode oc = OpCode.pushFor(data.length);
		byte[] args;
		if(oc == OpCode.PUSH_OTH){
			args = concat(cast(data.length, 2), data);
		}else{
			args = data;
		}
		return new BinInstruction(oc, args);
		
	}
	
	public static byte[] concat(byte... x) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for(int i = 0; i < x.length; i++){
			outputStream.write(x[i]);
		}
		return outputStream.toByteArray( );
	}
}

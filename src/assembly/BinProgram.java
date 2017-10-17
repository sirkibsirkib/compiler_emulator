package assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.Compiler;
import compiler.HasAddress;

public class BinProgram {
	private static byte[] placeholder;
	List<BinUnit> operations;
	Map<HasAddress, Integer> addresses;
	List<Job> linkJobsToDo;
	
	public BinProgram(){
		addresses = new HashMap<>();
		operations = new ArrayList<>();
		linkJobsToDo = new ArrayList<>();
		placeholder = new byte[4];
		for(int i = 0; i < 4; i++){
			placeholder[i] = (byte) 0;
		}
	}
	
	public void add(BinUnit ao){
		if(ao == null){
			throw new Error();
		}
		operations.add(ao);
	}
	
	@Override
	public String toString(){
		String s = "ASSEMBLY CODE:\n";
		for(BinUnit ao : operations){
			s += ao.toString() + '\n';
		}
		return s;
	}
	
	public int numberOfOutstandingLinks(){
		return linkJobsToDo.size();
	}
	
	public int numBytesTaken(){
		int total = 0;
		for(BinUnit ao : operations){
			total += ao.numBytesTaken();
		}
		return total;
	}
	
	public void myAddressStart(HasAddress ha){
		addresses.put(ha, numBytesTaken());
		for(int i = 0; i < linkJobsToDo.size(); i++){ //retroactively link placeholder links
			Job j = linkJobsToDo.get(i);
			if(j.linkTo == ha){
				j.toLink.argData = Compiler.cast(addresses.get(ha), 4);
				linkJobsToDo.remove(i);
				i--;
			}
		}
	}
	
	public BinInstruction getLinkedBinInstruction(OpCode oc, HasAddress get, String comment){
		BinInstruction toLink = new BinInstruction(oc, placeholder, comment);
		if(addresses.containsKey(get)){
			toLink.argData = Compiler.cast(addresses.get(get), 4);
			return toLink;
		}else{
			linkJobsToDo.add(new Job(toLink, get));
			return toLink; //returns with placeHolder
		}
	}
	
	private class Job{
		HasAddress linkTo;
		BinInstruction toLink;
		
		Job(BinInstruction toLink, HasAddress linkTo){
			this.toLink = toLink;
			this.linkTo = linkTo;
		}
	}
	
	public int danglingLinks(){
		return linkJobsToDo.size();
	}

	public byte[] compile() {
		byte[] bytes = new byte[numBytesTaken()];
		int i = 0;
		for(BinUnit ao : operations){
			byte[] aoBytes = ao.compile();
			for(byte b : aoBytes){
				bytes[i] = b;
				i++;
			}
		}
		return bytes;
	}
	
	//TODO dont forget that null should not return shit
}

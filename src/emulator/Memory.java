package emulator;

import java.util.HashMap;
import java.util.Map;

import compiler.Compiler;

public class Memory {
	public static final boolean CLEAN_TRASH = true;
	
	private Map<Integer, Byte> mem;
	private int memLimit;
	
	public Memory(int memLimit){
		this.memLimit = memLimit;
		mem = new HashMap<>();
	}
	
	public byte[] read(int addr, int len){ //rightward
		byte[] result = new byte[len];
		for(int i = 0; i < len; i++){
			if(!mem.containsKey(addr+i) && CLEAN_TRASH){
				throw new RunError("READING FROM " + (addr+i) + " (" +Compiler.hex(Compiler.cast(addr+i, 4)) + ") where there is no data");
			}
			try{
				result[i] = mem.get(addr+i);
			}
			catch(NullPointerException e){
				result[i] = (byte) 0;
			}
			
			if(addr+i >= memLimit){
				throw new RunError("MEM LIMIT");
			}
		}
		return result;
	}
	
	public void remove(int addr, int len){ //rightward
		if(!CLEAN_TRASH){
			return;
		}
		for(int i = 0; i < len; i++){
			mem.remove(addr+i);
		}
	}
	
	public int write(int addr, byte[] toWrite){ //rightward
		for(int i = 0; i < toWrite.length; i++){
			mem.put(addr+i, toWrite[i]);
			if(addr+i >= memLimit){
				throw new RunError("ADDR " + addr+i);
			}
		}
		return toWrite.length;
	}

	public int size() {
		return memLimit;
	}

	public boolean addrHasValue(int addr) {
		if(CLEAN_TRASH){
			return mem.containsKey(addr);
		}
		return true;
	}
}

package compiler;

public class Variable {
	int byteSize;
	private int relativeAddr;
	String name;
	char retArgGlobVar;
	
	Variable(String name, int byteSize, int relativeAddr, char retArgGlobVar){
		this.byteSize = byteSize;
		this.name = name;
		this.relativeAddr = relativeAddr;
		this.retArgGlobVar = retArgGlobVar;
	}
	
	public String toString(){
		return String.format("Var '%s' %d| [%d]\n", name, getRelativeAddr(), byteSize);
	}
	
	public char retArgGlobVar(){
		return retArgGlobVar;
	}

	public int getRelativeAddr() {
		return relativeAddr;
	}

	public int getByteSize() {
		return byteSize;
	}
}

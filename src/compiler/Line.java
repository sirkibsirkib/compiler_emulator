package compiler;

public class Line {
	private int i;
	private String s;
	public Line(int i, String s){
		this.s = s;
		this.i = i;
	}
	
	public int depth(){
		int i = 0;
		while(i < s.length()){
			if(s.charAt(i) == '\t'){
				i++;
			}else{
				return i;
			}
		}
		return i;
	}

	public int getI() {
		return i;
	}
	
	public String getS(){
		return s;
	}
	
	public void setS(String s){
		this.s = s;
	}
	
	@Override
	public String toString(){
		return i + ":" + s;
	}
}

package emulator;

@SuppressWarnings("serial")
public class RunError extends Error{
	private String msg;
	
	public RunError(String msg){
		this.msg = msg;
	}
	
	public String readMsg(){
		return msg;
	}
}

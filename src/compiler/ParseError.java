package compiler;

@SuppressWarnings("serial")
public class ParseError extends Error {
	private String reason;
	
	ParseError(String reason){
		this.reason = reason;
	}
	
	public String getReason(){
		return reason;
	}
}

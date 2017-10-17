package compiler;

public class Macro {
	String from, to;
	Macro(String from, String to){
		this.from = from;
		this.to = to;
	}
	public Line applyTo(Line line) {
		return new Line(line.getI(), line.getS().replace(from, to));
	}
}

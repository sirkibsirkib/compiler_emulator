package compiler;


public interface HasVariables {
	Variable lookup(String name);
	Method lookupMethod(String functionName);
}

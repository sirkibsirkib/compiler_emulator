package expressions;

import java.math.BigInteger;

import assembly.Assembles;

public interface Expression extends Assembles{
	int getByteEnumeration();

	boolean hasCompileTimeValue();

	BigInteger getCompileTimeValue();
}

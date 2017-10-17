

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import compiler.Compiler;
import emulator.ProgramInstance;

public class LangMain {
	
	
	public static void main(String[] args){
		boolean verbose = true;
		File src = new File("C:/Users/Christopher/Desktop/inout.txt");
		byte[] binBytes = Compiler.compile(src, verbose);
		try{
			FileOutputStream fos = new FileOutputStream("C:/Users/Christopher/Desktop/bin.code");
			fos.write(binBytes);
			fos.close();
		}catch(IOException e){
			System.out.println("Problem writing bin data to file");
		}
//		System.out.println("\n=============================================================");
//		new ProgramInstance(binBytes, 500, true, 500);
		System.out.println("\n=============================================================");
		new ProgramInstance(binBytes, binBytes.length + 200, false, -1);
	}
}

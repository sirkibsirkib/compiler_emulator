package tools;

import java.io.File;
import java.util.Scanner;

public class ByteFinder {
	public static void main(String[] args){
		int want = 71;
		
		File src = null;
		Scanner scan = null;
		try{
			src = new File("C:/Users/Christopher/Desktop/output.txt");
			scan = new Scanner(src);
		}catch(Exception e){
			
		}
		while(!scan.nextLine().startsWith("ASSEMBLY CODE:")){
			
		}
		String all = "";
		while(scan.hasNext()){
			String line =  scan.nextLine();
			if(line.indexOf("//") >= 0){
				line = line.substring(0, line.indexOf("//"));
			}
			all += line + " ";
		}
		scan.close();
		scan = new Scanner(all);
		String s = "";
		
		int index = 0;
		while(scan.hasNext()){
			String printNext = scan.next();
			if(printNext.startsWith("//"))continue;

			if(printNext.startsWith("BINARY")){
				return;
			}
			
			if(printNext.length() > 2){
				System.out.print('\n');
			}
			if(index == want){
				System.out.print("!![");
			}
			System.out.print(printNext);
			if(index == want){
				System.out.print("]!!");
			}
			System.out.print(' ');
			index++;
		}
	}
}

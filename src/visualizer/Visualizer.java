package visualizer;

import simpleFrame.SimpleFrame;
import simpleFrame.Colour;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import emulator.Memory;
import files.GenFileLoader;
import assembly.OpCode;

public class Visualizer implements MouseWheelListener, KeyListener{
	private SimpleFrame sf;
	private List<Showable> things;
	private int height = 180;
	private int width = 310;
	private int concatMemoryTo;
	private int addrCap;
	private int drawHeight;
	private int opCounter;
	
	private int x, y;
	
	public Visualizer(int concatMemoryTo, int addrCap){
		x = 0;
		y = 0;
		
		opCounter = 0;
		this.addrCap = addrCap;
		this.concatMemoryTo = concatMemoryTo;
//		System.out.println(concatMemoryTo + "!!");
		sf = new SimpleFrame(width, height, 5, "Fucking finally", Colour.BLACK);
		sf.registerKeyListener(this);
		sf.registerMouseWheelListener(this);
		things = new ArrayList<>();
	}
	
	Colour byteCol(byte b){
		int x = (0x444444 + 147 * b*b)%0xFFFFFF;
		return new Colour(x);
	}
	
	
	
	public void drawAllSnaps(){
		//TODO continue here.
		//TODO make the screen scroll left right up down
		//mousewheel vertical. arrow keys horizontal.
//		System.out.println(x + " " + y);
		int relX = x;
		int relY = y;
		drawHeight = 0;
		sf.clear(Colour.BLACK);
		for(Showable sh : things){
			if(relY > -30){
				sh.drawSelfAt(sh.absoluteX()?0:relX, relY, sf);
			}
			relY += sh.getHeight();
			drawHeight += sh.getHeight();
			if(relY > height) break;
		}
		sf.render();
	}

	public void addSnapof(Memory mem, int tos, int bp, int opAddr) {
		things.add(new MemorySnapshot(mem, tos, bp, opAddr));
//		drawAllSnaps();
	}
	
	public void addSnapof(OpCode oc, byte[] args) {
		things.add(new OpCodeSnap(oc, args, opCounter));
		opCounter++;
//		drawAllSnaps();
	}

	public void addReadSnap(int addr, int len) {
		things.add(new MemRead(addr, len, false));
//		drawAllSnaps();
	}

	public void addCrash(int addr, int len) {
		things.add(new MemRead(addr, len, true));
//		drawAllSnaps();
	}

	public void addWriteSnap(int addr, byte[] toWrite) {
		things.add(new MemWrite(addr, toWrite));
//		drawAllSnaps();
	}
	
	class MemRead implements Showable{
		int addr, len;
		private Colour col;
		boolean crash;
		
		MemRead(int addr, int len, boolean crash){
			this.addr = addr;
			this.len = len;
			this.crash = crash;
			col = crash? Colour.RED : Colour.WHITE;
		}

		@Override
		public void drawSelfAt(int x, int y, SimpleFrame sf) {
			if(crash && addr-concatMemoryTo < 0){
				sf.print(addr+"<<", 170, y, col);
				return;
			}else if(crash && addr-concatMemoryTo >= addrCap){
				sf.print(">>"+addr, 170, y, col);
				return;
			}
			for(int i = 0; i < len; i++){
				int dx = (addr+i-concatMemoryTo)*2 + x;
				if( dx >= 0 && addr - concatMemoryTo >= 0){
					sf.dot(dx, y, col);
					sf.dot(dx+1, y, col);
					sf.dot(dx, y+1, col);
					sf.dot(dx+1, y+1, col);
				}
			}
			
		}

		@Override
		public int getHeight() {
			return 3;
		}

		@Override
		public boolean absoluteX() {
			return false;
		}
		
	}
	
	class MemWrite implements Showable{
		int addr;
		byte[] bytes;
		MemWrite(int addr, byte[] bytes){
			this.addr = addr;
			this.bytes = bytes;
		}

		@Override
		public void drawSelfAt(int x, int y, SimpleFrame sf) {
			for(int i = 0; i < bytes.length; i++){
				int dx = (addr+i-concatMemoryTo)*2 + x;
				if( dx >= 0 && addr - concatMemoryTo >= 0){
					sf.rectFilled(dx, y, 2, 2, byteCol(bytes[i]));
				}
			}
			
		}

		@Override
		public int getHeight() {
			return 3;
		}

		@Override
		public boolean absoluteX() {
			return false;
		}
		
	}
	
	class MemorySnapshot implements Showable{
		byte[] bytes;
		byte[] enabled;
		int tos, bp, opAddr;
		
		MemorySnapshot(Memory m, int tos, int bp, int opAddr){
			this.tos = tos-concatMemoryTo;
			this.bp = bp-concatMemoryTo;
			this.opAddr = opAddr-concatMemoryTo;
			bytes = new byte[m.size()-concatMemoryTo];
			enabled = new byte[m.size()-concatMemoryTo];
			
			for(int i = 0; i < enabled.length; i++){
				if(m.addrHasValue(i+concatMemoryTo)){
					enabled[i] = 1;
					bytes[i] = m.read(i+concatMemoryTo, 1)[0];
				}else{
					enabled[i] = 0;
				}
			}
		}

		@Override
		public void drawSelfAt(int x, int y, SimpleFrame sf) {
			for(int i = 0; i < bytes.length; i++){
				if(i == tos){
					sf.dot(2*i+x, y+1, Colour.GREEN);
					sf.dot(2*i+x+1, y, Colour.GREEN);
				}
				if(i == bp){
					sf.dot(2*i+x, y+1, Colour.RED);
					sf.dot(2*i+x-1, y, Colour.RED);
				}
				if(i == opAddr){
					sf.dot(2*i+x+1, y+1, Colour.RED);
				}
				if(enabled[i] == 1){
					sf.rectFilled(2*i+x, y+2, 2, 2, byteCol(bytes[i]));
				}else{
					sf.dot(2*i+x, y+3, Colour.GRAY);
					sf.dot(2*i+x+1, y+3, Colour.GRAY);
				}
				if(i%4 == 0){
					sf.dot(2*i+x, y+4, Colour.GRAY);
					if(i%16 == 0){
						sf.dot(2*i+x, y+5, Colour.BLUE);
					}
					if(i%64 == 0){
						sf.dot(2*i+x, y+6, Colour.RED);
					}
				}
				
			}
		}

		@Override
		public int getHeight() {
			return 9;
		}

		@Override
		public boolean absoluteX() {
			return false;
		}
	}
	
	class OpCodeSnap implements Showable{
		private OpCode oc;
		private byte[] args;
		private int index;
		
		OpCodeSnap(OpCode oc, byte[] args, int index){
			this.oc = oc;
			this.args = args;
			this.index = index;
		}
		
		
		
		@Override
		public void drawSelfAt(int x, int y, SimpleFrame sf) {
			String s = oc.name();
			if(index % 5 == 0){
				s += "  ["+index+"/"+(opCounter-1)+"]";
			}
			sf.print(s, x+20, y+7, Colour.WHITE);
			for(int i = 0; i < args.length; i++){
				sf.rectFilled(6+2*i+x, y+7, 2, 2, byteCol(args[i]));
			}
		}

		@Override
		public int getHeight() {
			return 16;
		}



		@Override
		public boolean absoluteX() {
			return true;
		}
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == 37 && x < 0){
			x += 48;
		}else if(e.getKeyCode() == 39 && x > -addrCap*2+width){
			x -= 48;
		}else if(e.getKeyCode() == KeyEvent.VK_END){
			while(y > -drawHeight + height){
				y -= 50;
				drawAllSnaps();
			}
		}else if(e.getKeyCode() == KeyEvent.VK_HOME){
			y = 0;
		}
		drawAllSnaps();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation() > 0 && y > -drawHeight + height){
			y -= 20;
		}else if(e.getWheelRotation() < 0 && y < 0){
			y += 20;
		}
		drawAllSnaps();
		
	}
}

package visualizer;

import simpleFrame.SimpleFrame;


public interface Showable {
	void drawSelfAt(int x, int y, SimpleFrame sf);
	int getHeight();
	boolean absoluteX();
}

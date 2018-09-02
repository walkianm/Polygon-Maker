import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.*;

public class Whiteboard extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private ArrayList<Line> lines;
	private PolygonGUI gui;
		
	public Whiteboard(ArrayList<Line> lines, PolygonGUI gui) {
		this.lines = lines;
		this.gui = gui;
		setLayout(null);
	}
	
	public void paintComponent(Graphics g) {
		for(Line l : lines) {
			drawLine(l, g);
		}
	}
	
	public void drawLine(Line l, Graphics g) {
		if(l.getSelected()) {
			g.setColor(Color.YELLOW);
		}else if(l.getSet()){
			g.setColor(Color.BLACK);
		}else {
			g.setColor(Color.GRAY);
		}
		g.drawLine(l.getClockwise().getX() + gui.getOffset(), l.getClockwise().getY() + gui.getOffset(), 
				l.getCounterclockwise().getX() + gui.getOffset(), l.getCounterclockwise().getY() + gui.getOffset());
	}
}

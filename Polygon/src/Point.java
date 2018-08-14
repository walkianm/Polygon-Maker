import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;

public class Point extends JLabel{
	
	private static final long serialVersionUID = 1L;
		
	public static ImageIcon selectedIcon;
	public static ImageIcon deselectedIcon;
	
	private double angle;//need to save x and y as well, or done by extension?
	private boolean set;
	private int numLines;
	
	private boolean assigned;
	
	private long lastClicked;
	
	private Point cw;
	private Point ccw;
	
	private Line cwLine;
	private Line ccwLine;
	
	private double x;
	private double y;
	
	private int startX;
	private int startY;
	
	private boolean dragged;
	private boolean selected;
	
	private PolygonGUI gui;
	
	public Point(double x, double y, PolygonGUI gui) {
		
		this.gui = gui;
		
		setSize(25, 25);
		
		selectedIcon = new ImageIcon(getClass().getResource("Images/point-selected.png"));
		deselectedIcon = new ImageIcon(getClass().getResource("Images/point-unselected.png"));
		
		setIcon(deselectedIcon);
		
		this.set = false;
		this.angle = 0.0;
		this.numLines = 0;
		
		this.x = x;
		this.y = y;
		
		selected = false;
		dragged = false;
		assigned = false;
		
		addMouseListener(new PointListener());
		this.addMouseMotionListener(new MovementListener());
	}
	
	public void setClockwise(Point cw) {
		this.cw = cw;
	}
	
	public void setCounterclockWise(Point ccw) {
		this.ccw = ccw;
	}
	
	public void setAssigned(boolean assign) {
		this.assigned = assign;
	}
	
	public boolean getAssigned() {
		return this.assigned;
	}
	
	private Point getThis() {
		return this;
	}
	
	public void removePoint(Point p) {
		if(cw == p) {
			cw = null;
			cwLine = null;
			this.numLines--;
		}
		if(ccw == p) {
			ccw = null;
			ccwLine = null;
			this.numLines--;
		}
	}
	
	class MovementListener implements MouseMotionListener{

		@Override
		public void mouseDragged(MouseEvent e) {
			if(gui.getEditingValue() || gui.getError()) {
				return;
			}
			
			dragged = true;
			
			Point p = getThis();
			
			p.x = startX + e.getX() - gui.getOffset();
			p.y = startY + e.getY() - gui.getOffset();
			
			gui.refresh();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			//Don't need this method.
		}
		
	}
	
	private void editValue(int x, int y) {
		gui.setEditor(new EditValueGUI(this, gui, x, y));
		
		selected = true;
		gui.refresh();
	}
	
	class PointListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			if(gui.getEditingValue() || gui.getError()) {
				return;
			}
			
			//if another point is highlighted
			//create line between two
			//deselct both points.
			if(!gui.noLinesHighlighted()) {
				gui.deselectAllLines();
				gui.hideToggleLock();
				return;
			}
			
			if(!selected && !gui.noPointsSelected()) {
				
				Point other = gui.getSelectedPoint();
				
				if(getThis().getNumLines() < 2 && other.getNumLines() < 2 && cw != other) {
					//Last part of if prevents connecting two points to each other with two lines
					
					Line l = new Line(other, getThis(), gui);
					addPoint(other, l);
					other.addPoint(getThis(), l);
					
					gui.addLine(l);
					
					gui.deselectAllPoints();
				}else {
					if(cw == other) {
						new ErrorMessage("Tried to add two lines connecting the same two points!", (int) x, (int) y, gui);
					}else {
						new ErrorMessage("Tried to add too many lines to the same point!", (int) x, (int) y, gui);
					}
				}
				gui.hideToggleLock();
			}else {
				setIcon(selectedIcon);
				selected = !selected;
				
				long nextClick = System.currentTimeMillis();
				
				if(nextClick - lastClicked < PolygonGUI.DOUBLE_CLICK_WINDOW) {
					editValue((int) x, (int) y);
				}
				lastClicked = nextClick;
				if(selected) {
					gui.showToggleLock();
				}else {
					gui.hideToggleLock();
				}
			}
			
			gui.refresh();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if(gui.getEditingValue() || gui.getError()) {
				return;
			}			
			
			setIcon(selectedIcon);	//Highlight the point
			gui.refresh();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if(gui.getEditingValue() || gui.getError()) {
				return;
			}
			
			if(!selected) {
				setIcon(deselectedIcon);	//Remove highlight from point
			}
			gui.refresh();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(gui.getEditingValue() || gui.getError()) {
				return;
			}
			
			Point p = getThis();
			startX = p.getX();
			startY = p.getY();
			
			gui.refresh();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(gui.getEditingValue() || gui.getError()) {
				return;
			}
			
			if(dragged) {
				Point p = getThis();
				Polygon source = gui.getSourcePolygon(p, false);
				
				if(source != null) {
					if(source.hasASA()) {
						Point cw = p.getClockwise();
						Point ccw = p.getCounterclockwise();
						
						Line cwLine = p.getClockwiseLine();
						Line ccwLine = p.getCounterclockwiseLine();
						
						if(p.set) {
							gui.correctPoint(p, true);
						}else if(cwLine != null && cwLine.getSet()) {
							gui.correctLine(cwLine, true);
						}else if(cw != null && cw.getSet()) {
							gui.correctPoint(cw, true);
						}
						
						if(ccwLine != null && ccwLine.getSet()) {
							gui.correctLine(ccwLine, false);
						}else if(ccw != null && ccw.getSet()){
							gui.correctPoint(ccw, false);
						}
					}else {
						gui.movePolygon(source, x - startX, y - startY, p);
					}
				}
				gui.movePoints();
			}
			gui.refresh();
		}
	}
	
	public boolean getSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		if(selected && this.getIcon() == deselectedIcon) {
			setIcon(selectedIcon);
		}
		if(!selected && this.getIcon() == selectedIcon) {
			setIcon(deselectedIcon);
		}
		this.selected = selected;
	}
	
	public void setAngle(double newAngle, boolean hard) {
		if(hard) {
			set = true;
		}
		if(hard || !set) {
			this.angle = newAngle;
		}
	}
	
	public boolean getSet() {
		return this.set;
	}
	
	public void setSet(boolean set) {
		this.set = set;
	}
	
	public double getAngle() {		
		return this.angle;
	}
	
	public boolean partOfPolygon() {
		if(numLines < 2) {
			return false;
		}
		
		Point next = cw;
		Point previous = this;
		
		while(cw.numLines == 2) {
			if(next.getClockwise() == previous) {
				previous = next;
				next = next.getCounterclockwise();
			}else {
				previous = next;
				next = next.getClockwise();
			}
			
			if(next == this) {
				return true;
			}
		}
		return false;
	}
	
	public int getNumLines() {
		return this.numLines;
	}
	
	public void addPoint(Point newPoint, Line newLine) {
		if(cw == null) {
			cw = newPoint;
			cwLine = newLine;
			numLines++;
			return;
		}
		if(ccw == null) {
			ccw = newPoint;
			ccwLine = newLine;
			numLines++;
			return;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Point) {
			Point other = (Point) o;
			
			return (this.x == other.x && this.y == other.y && this.cw == other.cw && this.ccw == other.ccw);
		}
		return false;
	}
	
	public Line getClockwiseLine() {
		return this.cwLine;
	}
	
	public Line getCounterclockwiseLine() {
		return this.ccwLine;
	}
	
	public Point getClockwise() {
		return this.cw;
	}
	
	public Point getCounterclockwise() {
		return this.ccw;
	}
	
	public void swapPoints() {
		Point temp = cw;
		cw = ccw;
		ccw = temp;
		
		Line tempLine = cwLine;
		cwLine = ccwLine;
		ccwLine = tempLine;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public double getExactX() {
		return x;
	}
	
	public double getExactY() {
		return y;
	}
	
	public int getX() {
		return (int) x;
	}
	
	public int getY() {
		return (int) y;
	}
}
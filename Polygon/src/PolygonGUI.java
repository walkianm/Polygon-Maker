import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

public class PolygonGUI extends JFrame {
	
	public static final int DOUBLE_CLICK_WINDOW = 500;
	private static final int OFFSET = 12;
	private static final long serialVersionUID = 1L;
	
	private ImageIcon lock;
	private ImageIcon unlock;
	
	private JPanel main = new JPanel(new GridBagLayout());
	private JPanel top = new JPanel(new GridBagLayout());
	private JPanel bottom = new JPanel(new GridBagLayout());
	
	private Whiteboard whiteboard;
	
	private GridBagConstraints c = new GridBagConstraints();
	
	private JLabel greeting = new JLabel("Draw a polygon by clicking in the box!");
	
	private JTextArea areaText = new JTextArea();
	private JTextArea perimeterText = new JTextArea();
	
	private JButton extraInstructions = new JButton("How to use");
	private JButton clearAll = new JButton("Clear All");
	private JButton connectDots = new JButton("Connect Dots");
	private JButton toggleLock = new JButton();
	
	private ArrayList<Polygon> shapes = new ArrayList<Polygon>();
	private ArrayList<Point> points = new ArrayList<Point>();
	private ArrayList<Line> lines = new ArrayList<Line>();
	
	private EditValueGUI editor;
	
	private boolean editingValue;
	private boolean error;
	
	/** 
	 * Creates a new PolygonGUI and sets up its components.
	 */
	public PolygonGUI() {
		lock = new ImageIcon(getClass().getResource("Images/lock.png"));
		unlock = new ImageIcon(getClass().getResource("Images/unlock.png"));
		
		whiteboard = new Whiteboard(lines, this);
		editingValue = false;
		error = false;
		
		setUpWhiteBoard();
		setUpButtons();
		setUpTextAreas();
		setUpGUI();
		addKeyListener(new Keyboard());
		
		recalculate();
	}
	
	/** 
	 * Sets the value of 'editor' to the provided EditValue GUI
	 * 
	 * @param editor
	 */
	public void setEditor(EditValueGUI editor) {
		this.editor = editor;
	}
	
	/** 
	 * Sets the focus of the user to the editor
	 */
	public void showEditor() {
		editor.getTextField().requestFocus();
	}
	
	/** 
	 * Returns the dimensions of the whiteboard
	 * 
	 * @return
	 */
	public Dimension getWhiteboardSize() {
		return this.whiteboard.getSize();
	}
	
	/** 
	 * Sets up the text areas that the PolygonGUI uses.
	 */
	public void setUpTextAreas() {
		areaText.setFocusable(false);
		perimeterText.setFocusable(false);
	}
	
	/** 
	 * Returns the value of OFFSET, which represents the distance that each point image must be 
	 * shifted so that it appears at the correct location.
	 * 
	 * @return
	 */
	public int getOffset() {
		return OFFSET;
	}
	
	/** 
	 * Changes the length of Line l so that it matches its set length.
	 * If direction is true, then it does so in the clockwise direction. If false it does so in the counterclockwise direction.
	 * 
	 * @param l
	 * @param direction
	 */
	public void correctLine(Line l, boolean direction) {
		//direction is clockwise if true, counterclockwise if false
		
		double x = getXShift(l.getClockwise(), l, l.getCounterclockwise());
		double y = getYShift(l.getClockwise(), l, l.getCounterclockwise());
		
		Point p;
		if(direction) {
			p = l.getClockwise();
			p.setX(p.getExactX() + x);
			p.setY(p.getExactY() + y);
		}else {
			p = l.getCounterclockwise();
			p.setX(p.getExactX() - x);
			p.setY(p.getExactY() - y);
		}
		
		Point nextPoint;
		Line nextLine;
		
		if(direction) {
			nextPoint = p.getClockwise();
			nextLine = p.getClockwiseLine();
		}else {
			nextPoint = p.getCounterclockwise();
			nextLine = p.getCounterclockwiseLine();
		}
		
		if(p.getSet()) {
			correctPoint(p, direction);
		}else if(nextPoint != null && nextPoint.getSet()) {
			correctPoint(nextPoint, direction);
		}else if(nextLine != null && nextLine.getSet() && nextLine != l) {
			correctLine(nextLine, direction);
		}
	}
	
	/** 
	 * Changes the angle at a point to match the angle it is set to.
	 * If direction is true, it does so in the clockwise direction. If false, it does so in the counterclockwise direction.
	 * 
	 * @param p
	 * @param direction
	 */
	public void correctPoint(Point p, boolean direction) {
		//direction is clockwise if true, counterclockwise if false
		
		Point cw = p.getClockwise();
		Point ccw = p.getCounterclockwise();
		
		if(cw == null || ccw == null) {
			return;
		}
		
		double angle;
		if(direction) {
			angle = Polygon.getAngle(p, ccw);
		}else {
			angle = Polygon.getAngle(p, cw);
		}
		
		if(direction) {
			angle += p.getAngle();
		}else {
			angle += 360 - p.getAngle();
		}
		
		while(angle > 360) {
			angle -= 360;
		}
		
		double distance;
		Line l;
		if(direction) {
			l = p.getClockwiseLine();
		}else {
			l = p.getCounterclockwiseLine();
		}
		
		if(l.getSet()) {
			distance = l.getLength();
		}else {
			distance = l.calculateLength();
		}
		
		double x = distance * Math.cos(angle * Math.PI / 180);
		double y = distance * Math.sin(angle * Math.PI / 180);
		
		Point move;
		if(direction) {
			move = cw;
		}else {
			move = ccw;
		}
		
		move.setX(p.getExactX() + x);
		move.setY(p.getExactY() - y);
		
		Point next;
		Line nextLine;
		if(direction) {
			next = move.getClockwise();
			nextLine = move.getClockwiseLine();
		}else {
			next = move.getCounterclockwise();
			nextLine = move.getCounterclockwiseLine();
		}
		
		if(move.getSet()) {
			correctPoint(move, direction);
		}else if(next != null && next.getSet()){
			correctPoint(next, direction);
		}else if(nextLine != null && nextLine.getSet()) {
			correctLine(nextLine, direction);
		}
	}
	
	/** 
	 * Returns the Polygon containing the provided line 'l'.
	 * 
	 * @param l
	 * @return
	 */
	public Polygon getSourcePolygon(Line l) {
		return getSourcePolygon(l.getClockwise());
	}
	
	/** 
	 * Returns the Polygon containing the provided point 'p'.
	 * 
	 * @param p
	 * @return
	 */
	public Polygon getSourcePolygon(Point p) {
		for(Polygon shape : shapes) {
			if(shape.hasPoint(p)) {
				return shape;
			}
		}
		
		return null;
	}
	
	/** 
	 * Returns the distance the x component of a point 'moving' needs to shift in order to make line 'l' the proper length, where 'start' is the other endpoint of the line.
	 * 
	 * @param moving
	 * @param l
	 * @param start
	 * @return
	 */
	private double getXShift(Point moving, Line l, Point start) {
		double m = l.getSlope();
		
		if(m == Double.MAX_VALUE) {
			return 0;
		}else {
			
			double xPercentage = l.getXPercentage();
			double lengthChange = l.getLength() - l.calculateLength();
			
			if(moving.getX() < start.getX()) {
				xPercentage *= -1;
			}
			
			return xPercentage * lengthChange;	
		}
	}
	
	/** 
	 * Returns the distance the y component of a point 'moving' needs to shift in order to make line 'l' the proper length, where 'start' is the other endpoint of the line.
	 * 
	 * @param moving
	 * @param l
	 * @param start
	 * @return
	 */
	public double getYShift(Point moving, Line l, Point start) {
		double yPercentage = l.getYPercentage();
		double lengthChange = l.getLength() - l.calculateLength();
		
		if(moving.getY() < start.getY()) {
			yPercentage *= -1;
		}
		
		return yPercentage * lengthChange;
	}
	
	/**
	 * Shifts every point in polygon p by x, y, except point exception.
	 * 
	 * @param p Polygon to shift
	 * @param x
	 * @param y
	 * @param exception Point to leave alone
	 */
	public void movePolygon(Polygon p, double x, double y, Point exception) {
		for(Point q : p.getPoints()) {
			if(q != exception) {
				q.setX(q.getExactX() + x);
				q.setY(q.getExactY() + y);
			}
			movePoint(q);
		}
		
	}
	
	/**
	 * Updates the location of the JComponent of every point, so that they match where the image appears.
	 */
	public void movePoints() {
		for(Point p : points) {
			movePoint(p);
		}
		recalculate();
	}
	
	/**
	 * Sets up the action listeners and some properties of the buttons.
	 */
	private void setUpButtons() {
		clearAll.addActionListener(new ButtonListener());
		clearAll.setActionCommand("clearAll");
		clearAll.setFocusable(false);
		
		extraInstructions.addActionListener(new ButtonListener());
		extraInstructions.setActionCommand("extraInstructions");
		extraInstructions.setFocusable(false);
		
		connectDots.addActionListener(new ButtonListener());
		connectDots.setActionCommand("connectDots");
		connectDots.setFocusable(false);
		
		toggleLock.addActionListener(new ButtonListener());
		toggleLock.setActionCommand("toggleLock");
		toggleLock.setFocusable(false);
	}
	
	/**
	 * Shows the 'Toggle Lock' button.
	 */
	public void showToggleLock(Object o) {
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		
		boolean showUnlock = false;
		
		if(o instanceof Line) {
			Line l = (Line) o;
			showUnlock = l.getSet();
		}else if(o instanceof Point) {
			Point p = (Point) o;
			showUnlock = p.getSet();
		}
		
		if(showUnlock) {
			toggleLock.setIcon(unlock);
		}else {
			toggleLock.setIcon(lock);
		}
		
		top.add(toggleLock, c);
		revalidate();
		refresh();
	}
	
	/**
	 * Hides the 'Toggle Lock' button.
	 */
	public void hideToggleLock() {
		top.remove(toggleLock);
		
		revalidate();
		refresh();
	}
	
	/**
	 * Places each component of the gui in the proper place using GridBagLayout/GridBagConstraints.
	 */
	private void setUpGUI() {	
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//TOP SECTION
		//Add greeting text
		c.gridx = 0;
		top.add(greeting, c);
		
		//Add empty space
		c.weightx = 1.0;
		c.gridx = 1;
		top.add(new JPanel(), c);
		
		//Add connect all lines button
		c.weightx = 0.0;
		c.gridx = 3;
		top.add(connectDots, c);
		
		//Add clear all button
		c.gridx = 4;
		top.add(clearAll, c);
		
		//Add instructions button
		c.gridx = 5;
		top.add(extraInstructions, c);
		
		//BOTTOM SECTION		
		//Add area text (weightx carried down from above section)
		c.gridx = 1;
		c.weightx = 1.0;
		bottom.add(areaText, c);
		
		//Add perimeter text
		c.gridx = 3;
		c.weightx = 1.0;
		bottom.add(perimeterText, c);
		
		//MAIN SECTION
		//Add top to main
		c.gridx = 0;
		c.weightx = 0.0; //reset weightx
		main.add(top, c);
		
		//Add middle space to main
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.weightx = 1.0;
		main.add(whiteboard, c);
		
		//Add bottom to main
		c.gridy = 2;
		c.weightx = 0.0; //reset weightx
		c.weighty = 0.0; //reset weighty
		main.add(bottom, c);
	}
	
	/**
	 * Sets up the parameters of the whiteboard.
	 */
	private void setUpWhiteBoard() {
		whiteboard.addMouseListener(new PaneListener());
	}
	
	/**
	 * Returns the main JPane.
	 * 
	 * @return
	 */
	private JPanel getMainPane() {
		return this.main;
	}
	
	/**
	 * Repaints both the whiteboard and the Polygongui.
	 */
	public void refresh() {
		whiteboard.repaint();
		repaint();
	}
	
	/**
	 * Deselects every point.
	 */
	public void deselectAllPoints() {
		for(Point p : points) {
			p.setSelected(false);
			p.showDeselected();
		}
		hideToggleLock();
	}
	
	/**
	 * Creates and returns a new Point at the provided x and y coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Point makeNewPoint(int x, int y) {
		return new Point(x, y, this);
	}
	
	/**
	 * Returns true if there are no Points selected, false otherwise.
	 * 
	 * @return
	 */
	public boolean noPointsSelected() {
		for(Point p : points) {
			if(p.getSelected()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the first Point that is selected, or null if no points are selected.
	 * 
	 * @return
	 */
	public Point getSelectedPoint() {
		for(Point p : points) {
			if(p.getSelected()) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Sets all points as being NOT assigned to any Polygon, in preparation for assigning new polygons.
	 */
	public void clearAssignedPoints() {
		for(Point p : points) {
			p.setAssigned(false);
		}
	}
	
	/**
	 * Adds Line 'l' to the list of all lines. Automatically recalculates perimeter and area.
	 * 
	 * @param l
	 */
	public void addLine(Line l) {
		lines.add(l);
		
		recalculate();
	}
	
	/**
	 * Returns the list of all Lines on screen.
	 * 
	 * @return
	 */
	public ArrayList<Line> getLines(){
		return this.lines;
	}
	
	/**
	 * Attempts to add another point onto the Polygon 'shape'. Returns true if successful, false otherwise.
	 * 
	 * @param shape
	 * @return
	 */
	public boolean addNewPoint(Polygon shape) {
		Point p = shape.getLastPoint();
		
		if(p.getClockwise() == null && shape.hasPoint(p.getCounterclockwise())) {
			return false;
		}
		if(p.getCounterclockwise() == null && shape.hasPoint(p.getClockwise())) {
			return false;
		}
		
		if(p.getClockwise() != null && !shape.hasPoint(p.getClockwise())) {
			shape.addPoint(p.getClockwise());
			return true;
		}else if(p.getCounterclockwise() != null && !shape.hasPoint(p.getCounterclockwise())) {
			shape.addPoint(p.getCounterclockwise());
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the first point in the list of all points that has not been assigned to a Polygon.
	 * 
	 * @return
	 */
	public Point firstAvailablePoint() {
		for(Point p : points) {
			if(!p.getAssigned()) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Returns true if there are any points that have not been assigned to a Polygon, false otherwise.
	 * 
	 * @return
	 */
	public boolean pointsUnassigned() {
		for(Point p : points) {
			if(!p.getAssigned()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Moves the image of the provided point to its actual location.
	 * 
	 * @param p
	 */
	public void movePoint(Point p) {
		whiteboard.remove(p);
		whiteboard.add(p);
		p.setLocation(p.getX(), p.getY());
	}
	
	/**
	 * Returns true if the provided x and y coordinates lie along any of the lines.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onLine(int x, int y) {
		for(Line l : lines) {
			if(getDistanceToLine(l, x, y) < 10 && withinLineSegment(l, x, y)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the provided x and y coordinates fall between the ends of the Line Segment.
	 * 
	 * @param l
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean withinLineSegment(Line l, int x, int y) {
		double a = getDistanceBetweenPoints(x, y, l.getClockwise().getExactX(), l.getClockwise().getExactY());
		double b = getDistanceBetweenPoints(x, y, l.getCounterclockwise().getExactX(), l.getCounterclockwise().getExactY());
		double c = getDistanceBetweenPoints(l.getCounterclockwise().getExactX(), l.getCounterclockwise().getExactY(), l.getClockwise().getExactX(), l.getClockwise().getExactY());
		
		if(c < a || c < b) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the Line that is closest to the provided x and y coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Line getClosestLine(int x, int y) {
		double minDistance = Double.MAX_VALUE;
		double distance;
		Line closest = null;
		Line next;
		
		for(int i = 0; i < lines.size(); i++) {
			next = lines.get(i);
			distance = getDistanceToLine(next, x, y);
			if(distance < minDistance && withinLineSegment(next, x, y)) {
				minDistance = distance;
				closest = next;
			}
		}
		return closest;
	}
	
	/**
	 * Returns the distance between the coordinates (x, y) and Line l.
	 */
	public static double getDistanceToLine(Line l, int x, int y) {
		double x1 = l.getClockwise().getExactX();	//getting one of the endpoints of our line
		double y1 = l.getClockwise().getExactY();
		
		double m = l.getSlope();	//slope intercept form of our line
		if(m == 0) {
			return Math.abs(l.getClockwise().getExactY() - y);
		}else if(m == Double.MAX_VALUE) {
			return Math.abs(l.getClockwise().getExactX() - x);
		}else {
			double b = y1 - m * x1;
			
			double m2 = - 1 / m; //slope intercept form of the line to our point
			double b2 = y - m2 * x;
			
			double xIntersect = (b2 - b) / (m - m2); //find x and y intersection
			double yIntersect = m * xIntersect + b;
			
			return getDistanceBetweenPoints(x, y, xIntersect, yIntersect);
		}
	}
	
	/**
	 * Returns the distance between points (x1, y1) and (x2, y2).
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * Returns true if a value is currently being edited.
	 * 
	 * @return
	 */
	public boolean getEditingValue() {
		return this.editingValue;
	}
	
	/**
	 * Sets the value of editingValue, which tracks whether or not a value is being edited.
	 * 
	 * @param editing
	 */
	public void setEditingValue(boolean editing) {
		this.editingValue = editing;
	}
	
	/**
	 * Returns true if there is currently an error message on screen.
	 * 
	 * @return
	 */
	public boolean getError() {
		return this.error;
	}
	
	/**
	 * Sets the value of error, which tracks whether or not an error is on screen.
	 * @param error
	 */
	public void setError(boolean error) {
		this.error = error;
	}
	
	/**
	 * Removes all lines and points from the whiteboard.
	 */
	public void clearAll() {
		while(lines.size() > 0) {
			lines.remove(0);
		}
		while(points.size() > 0) {
			whiteboard.remove(points.get(0));
			points.remove(0);
		}
		recalculate();
		refresh();
	}
	
	/**
	 * Recalculates the perimeter and area of any polygons.
	 */
	public void recalculate() {
		calculateArea();
		calculatePerimeter();
	}
	
	/**
	 * Calculates the total area of all the polygons on the whiteboard.
	 */
	public void calculateArea() {
		double area = 0;
		shapes.clear();
		
		clearAssignedPoints();
		
		assignLonePoints();
		makeNonPolygons();
		makePolygons();
		
		for(Polygon p : shapes) {
			if(p.isTruePolygon()) {
				area += p.getArea();
			}else {
				p.calculateAngles();
			}
		}
		
		areaText.setText("Area: " + area);
	}
	
	/**
	 * Assigns every point that is not a part of a completed polygon to a 'partial polygon'.
	 */
	public void makeNonPolygons() {
		Polygon shape;
		
		while(unassignedEndpoints()) {
			shape = new Polygon(this, false);
			shape.addPoint(getFirstEndpoint());
			
			while(addNewPoint(shape)) {
			}
			shape.fixPoints();
			shape.fixLines();
			
			shapes.add(shape);
		}
	}
	
	/**
	 * Sets the value of 'assigned', which tracks whether or not a Point has been assigned to a Polygon, to true for all Points are not connected by any lines.
	 */
	public void assignLonePoints() {
		for(Point p : points) {
			if(p.getNumLines() == 0) {
				p.setAssigned(true);
			}
		}
	}
	
	/**
	 * Returns the first point in the list that is an endpoint (has only one line connecting to it).
	 * @return
	 */
	public Point getFirstEndpoint() {
		for(Point p : points) {
			if(!p.getAssigned() && p.getNumLines() < 2) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Returns true if there is an endpoint that has not been assigned.
	 * 
	 * @return
	 */
	public boolean unassignedEndpoints() {
		for(Point p : points) {
			if(!p.getAssigned() && p.getNumLines() < 2) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Assigns each group of points to a Polygon, then adds that Polygon to the list of all Polygons, 'shapes'.
	 */
	public void makePolygons() {
		Polygon shape;
		
		while(pointsUnassigned()) {
			shape = new Polygon(this, true);
			shape.addPoint(firstAvailablePoint());
			
			while(addNewPoint(shape)){
			}
			shape.done();
			shapes.add(shape);
		}
	}
	
	/**
	 * Calculates and displays the total perimeter of all Polygons.
	 */
	public void calculatePerimeter() {
		double perimeter = 0;
		
		for(Line l : lines) {
			if(l.getSet()) {
				perimeter += l.getLength();
			}else {
				perimeter += l.calculateLength();
			}
		}
		perimeterText.setText("Perimeter: " + perimeter);
	}
	
	/**
	 * Creates and displays an InstructionsGUI.
	 */
	public void displayInstructions() {
		new InstructionsGUI(this);
	}
	
	/**
	 * Returns true if no lines are selected.
	 * 
	 * @return
	 */
	public boolean noLinesHighlighted() {
		for(Line l : lines) {
			if(l.getSelected()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Deselects all lines.
	 */
	public void deselectAllLines() {
		for(Line l : lines) {
			if(l.getSelected()) {
				l.clicked(0, 0, false);
			}
		}
		hideToggleLock();
	}
	
	/**
	 * Removes whatever object is selected from the whiteboard.
	 */
	public void deleteSelected() {
		removeSelectedLines();
		if(!noPointsSelected()) {
			removeSelectedPoint();
		}
		
		recalculate();
		
		refresh();
	}
	
	/**
	 * Removes the selected point from the whiteboard.
	 */
	public void removeSelectedPoint() {
		Point remove = null;
		for(Point p : points) {
			if(p.getSelected()) {
				remove = p;
				removeNeighboringLines(p);
				
				if(p.getClockwise() != null) {
					p.getClockwise().removePoint(p);
				}
				if(p.getCounterclockwise() != null) {
					p.getCounterclockwise().removePoint(p);
				}
				
				break;
			}
		}
		whiteboard.remove(remove);
		points.remove(remove);
	}
	
	/**
	 * Removes any lines attached to Point p from the whiteboard.
	 * 
	 * @param p
	 */
	public void removeNeighboringLines(Point p) {
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).getClockwise() == p || lines.get(i).getCounterclockwise() == p) {
				lines.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * Removes the selected line from the whiteboard.
	 */
	public void removeSelectedLines() {
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).getSelected()) {
				Point a = lines.get(i).getClockwise();
				Point b = lines.get(i).getCounterclockwise();
				
				a.removePoint(b);
				b.removePoint(a);
				
				lines.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * Returns true if there is a Point with exactly one line.
	 * 
	 * @return
	 */
	public boolean pointWithOneLine() {
		for(Point p : points) {
			if(p.getNumLines() == 1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the first selected line. If no lines are selected, returns null.
	 * 
	 * @return
	 */
	private Line getSelectedLine() {
		for(Line l : lines) {
			if(l.getSelected()) {
				return l;
			}
		}
		return null;
	}
	
	/**
	 * Toggles whether on not the selected Object is locked in its length or angle.
	 */
	public void toggleLock() {
		if(noPointsSelected()) {
			Line toggle = getSelectedLine();
			if(!toggle.getSet()) {
				toggle.setLength(toggle.calculateLength());
			}else {
				toggle.setSet(false);
			}
			deselectAllLines();
		}else {
			Point toggle = getSelectedPoint();
			toggle.setSet(!toggle.getSet());
			deselectAllPoints();
		}
		
		if(toggleLock.getIcon() == lock) {
			toggleLock.setIcon(unlock);
		}else {
			toggleLock.setIcon(lock);
		}
	}
	
	/**
	 * Connects all points with lines in the order they were added to the world.
	 * Currently acts suboptimally in circumstances where some lines have been added before connecting all points.
	 */
	public void connectDots() {
		int next;
		int counter = 0;
		
		do {
			for(int i = 0; i < points.size(); i++) {
				if(points.get(i).getNumLines() < 2) {
					next = i + 1;
					next %= points.size();
					if(!(points.get(i).getClockwise() == points.get(next) || points.get(i).getCounterclockwise() == points.get(next))) {
						connectToNextAvailablePoint(i);
					}
				}
			}
			counter++;
		}while(pointWithOneLine() && counter < 2);
		
		deselectAllPoints();
		
		recalculate();
		refresh();
	}
	
	/**
	 * Connects the Point at index 'index' in points to the next point with less than two lines.
	 * 
	 * @param index
	 */
	public void connectToNextAvailablePoint(int index) {
		int i = index + 1;
		while(i != index) {
			if(i >= points.size()) {
				i = 0;
			}
			if(points.get(i).getNumLines() < 2 && points.get(index).getClockwise() != points.get(i) && points.get(index).getCounterclockwise() != points.get(i)) {
				Line l = new Line(points.get(i), points.get(index), this);
				lines.add(l);
				
				points.get(i).addPoint(points.get(index), l);
				points.get(index).addPoint(points.get(i), l);
				
				break;
			}
			i++;
		}
	}
	
	class Keyboard implements KeyListener{
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(editingValue || error) {
				return;
			}
			
			//Check if backspace or delete is pressed
			if(e.getKeyCode() == 8 || e.getKeyCode() == 127) {
				deleteSelected();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent areg0) {
		}		
	}
	
	class PaneListener implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent m) {
			if(editingValue || error) {
				return;
			}
			
			//This method is called we we click on the background or a line.			
			
			if(!noPointsSelected()) {
				//there is a currently highlighted point

				deselectAllPoints();
				
			}else if(onLine(m.getX() - OFFSET, m.getY() - OFFSET)){
				//if we clicked a line.
				if(!noLinesHighlighted()) {
					//if there is a line highlighted
					
					Line l = getClosestLine(m.getX() - OFFSET, m.getY() - OFFSET);
					if(l.getSelected()) {
						//if we clicked on the line that is currently selected
						l.clicked(m.getX(), m.getY(), true);
						hideToggleLock();
					}else {
						//if we clicked on an unselected line
						deselectAllLines();
					}
				}else {
					//No lines are currently highlighted
					Line l = getClosestLine(m.getX() - OFFSET, m.getY() - OFFSET);
					l.clicked(m.getX(), m.getY(), true);
					
					showToggleLock(l);
				}
			}else {
				//we clicked on empty space
				if(!noLinesHighlighted()) {
					//A line is highlighted
					
					deselectAllLines();
				}else {
					//Nothing is highlighted, so add a point
					Point p = makeNewPoint(m.getX() - OFFSET, m.getY() - OFFSET);
					points.add(p);
					whiteboard.add(p);
					p.setLocation(m.getX() - OFFSET, m.getY() - OFFSET);
				}
			}
			refresh();
		}
		
		//The below methods are not needed for this program, but must be implemented for MouseListener
		@Override
		public void mousePressed(MouseEvent m) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent m) {
		}

		@Override
		public void mouseExited(MouseEvent m) {
		}
	}
	
	class ButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(editingValue || error) {
				return;
			}
			
			switch(e.getActionCommand()) {
			case "clearAll":
				clearAll();
				break;
			case "extraInstructions":
				displayInstructions();
				break;
			case "connectDots":
				connectDots();
				break;
			case "toggleLock":
				toggleLock();
				break;
			default:
				System.out.println("Error, no action command."); //For testing buttons only
			}
		}
	}
	
	/**
	 * Creates the main gui and sets up some of its properties, such as title and size.
	 * @param args
	 */
	public static void main(String[] args) {
		PolygonGUI gui = new PolygonGUI();
		
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(825, 725);
		gui.setTitle("Polygon Maker");
		gui.setVisible(true);
		gui.setContentPane(gui.getMainPane());
		gui.setLocation(100, 50);
	}
}

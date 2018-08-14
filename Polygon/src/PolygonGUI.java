import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

public class PolygonGUI extends JFrame {
	
	public static final int DOUBLE_CLICK_WINDOW = 500;
	private static final int OFFSET = 12;
	private static final long serialVersionUID = 1L;
	
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
	private JButton toggleLock = new JButton("Toggle Lock");
	
	private ArrayList<Polygon> shapes = new ArrayList<Polygon>();
	private ArrayList<Point> points = new ArrayList<Point>();
	private ArrayList<Line> lines = new ArrayList<Line>();
	
	private EditValueGUI editor;
	
	private boolean editingValue;
	private boolean error;
	
	public PolygonGUI() {
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
	
	public void setEditor(EditValueGUI editor) {
		this.editor = editor;
	}
	
	public void showEditor() {
		editor.getTextField().requestFocus();
	}
	
	public Dimension getWhiteboardSize() {
		return this.whiteboard.getSize();
	}
	
	public void setUpTextAreas() {
		areaText.setFocusable(false);
		perimeterText.setFocusable(false);
	}
	
	public int getOffset() {
		return OFFSET;
	}
	
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
	
	public Polygon getSourcePolygon(Object start, boolean isLine) {
		Point p;
		
		if(isLine) {
			p = ((Line) start).getClockwise();
		}else {
			p = (Point) start;
		}
		
		for(Polygon shape : shapes) {
			if(shape.hasPoint(p)) {
				return shape;
			}
		}
		
		return null;
	}
	
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
	public void showToggleLock() {
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		
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
	
	private void setUpWhiteBoard() {
		whiteboard.addMouseListener(new PaneListener());
	}
	
	private JPanel getMainPane() {
		return this.main;
	}
	
	public void refresh() {
		whiteboard.repaint();
		repaint();
	}
	
	public void deselectAllPoints() {
		for(Point p : points) {
			p.setSelected(false);
			p.setIcon(Point.deselectedIcon);
		}
		hideToggleLock();
	}
	
	private Point makeNewPoint(int x, int y) {
		return new Point(x, y, this);
	}
	
	public boolean noPointsSelected() {
		for(Point p : points) {
			if(p.getSelected()) {
				return false;
			}
		}
		return true;
	}
	
	public Point getSelectedPoint() {
		for(Point p : points) {
			if(p.getSelected()) {
				return p;
			}
		}
		return null;
	}
	
	public void clearAssignedPoints() {
		for(Point p : points) {
			p.setAssigned(false);
		}
	}
	
	public void addLine(Line l) {
		lines.add(l);
		
		recalculate();
	}
	
	public ArrayList<Line> getLines(){
		return this.lines;
	}
	
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
	
	public Point firstAvailablePoint() {
		for(Point p : points) {
			if(!p.getAssigned()) {
				return p;
			}
		}
		return null;
	}
	
	public boolean pointsUnassigned() {
		for(Point p : points) {
			if(!p.getAssigned()) {
				return true;
			}
		}
		return false;
	}
	
	public void movePoint(Point p) {
		whiteboard.remove(p);
		whiteboard.add(p);
		p.setLocation(p.getX(), p.getY());
	}
	
	public boolean onLine(int x, int y) {
		for(Line l : lines) {
			if(getDistanceToLine(l, x, y) < 10 && withinLineSegment(l, x, y)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean withinLineSegment(Line l, int x, int y) {
		double a = getDistanceBetweenPoints(x, y, l.getClockwise().getExactX(), l.getClockwise().getExactY());
		double b = getDistanceBetweenPoints(x, y, l.getCounterclockwise().getExactX(), l.getCounterclockwise().getExactY());
		double c = getDistanceBetweenPoints(l.getCounterclockwise().getExactX(), l.getCounterclockwise().getExactY(), l.getClockwise().getExactX(), l.getClockwise().getExactY());
		
		if(c < a || c < b) {
			return false;
		}
		return true;
	}
	
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
	
	public static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public boolean getEditingValue() {
		return this.editingValue;
	}
	
	public void setEditingValue(boolean editing) {
		this.editingValue = editing;
	}
	
	public boolean getError() {
		return this.error;
	}
	
	public void setError(boolean error) {
		this.error = error;
	}
	
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
	
	public void recalculate() {
		calculateArea();
		calculatePerimeter();
	}
	
	public void calculateArea() {
		double area = 0;
		shapes.clear();
		
		clearAssignedPoints();
		
		markLonePoints();
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
	
	public void markLonePoints() {
		for(Point p : points) {
			if(p.getNumLines() == 0) {
				p.setAssigned(true);
			}
		}
	}
	
	public Point getFirstEndpoint() {
		for(Point p : points) {
			if(!p.getAssigned() && p.getNumLines() < 2) {
				return p;
			}
		}
		return null;
	}
	
	public boolean unassignedEndpoints() {
		for(Point p : points) {
			if(!p.getAssigned() && p.getNumLines() < 2) {
				return true;
			}
		}
		return false;
	}
	
	public double roundArea(double input) {
		double remainder = input % 1;
		
		if(remainder > 0.75) {
			remainder = 1;
		}else if(remainder < 0.75 && remainder > 0.25) {
			remainder = 0.5;
		}else if(remainder < 0.25) {
			remainder = 0;
		}
		
		input = (int) input;
		
		return input + remainder;
	}
	
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
	
	public void markPoints(Point p) {
		p.setAssigned(true);
		if(p.getClockwise() != null && !p.getClockwise().getAssigned()) {
			markPoints(p.getClockwise());
		}
		if(p.getCounterclockwise() != null && !p.getCounterclockwise().getAssigned()) {
			markPoints(p.getCounterclockwise());
		}
	}
	
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
	
	public void displayInstructions() {
		new InstructionsGUI(this);
	}
	
	public boolean noLinesHighlighted() {
		for(Line l : lines) {
			if(l.getSelected()) {
				return false;
			}
		}
		return true;
	}
	
	public void deselectAllLines() {
		for(Line l : lines) {
			if(l.getSelected()) {
				l.clicked(0, 0, false);
			}
		}
		hideToggleLock();
	}
	
	public void deleteSelected() {
		removeSelectedLines();
		if(!noPointsSelected()) {
			removeSelectedPoint();
		}
		
		recalculate();
		
		refresh();
	}
	
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
	
	public void removeNeighboringLines(Point p) {
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).getClockwise() == p || lines.get(i).getCounterclockwise() == p) {
				lines.remove(i);
				i--;
			}
		}
	}
	
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
	
	public boolean pointWithOneLine() {
		for(Point p : points) {
			if(p.getNumLines() == 1) {
				return true;
			}
		}
		return false;
	}
	
	private Line getSelectedLine() {
		for(Line l : lines) {
			if(l.getSelected()) {
				return l;
			}
		}
		return null;
	}
	
	public void toggleLock() {
		if(noPointsSelected()) {
			Line toggle = getSelectedLine();
			toggle.setSet(!toggle.getSet());
		}else {
			Point toggle = getSelectedPoint();
			toggle.setSet(!toggle.getSet());
		}
	}
	
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
					showToggleLock();
					getClosestLine(m.getX() - OFFSET, m.getY() - OFFSET).clicked(m.getX(), m.getY(), true);
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

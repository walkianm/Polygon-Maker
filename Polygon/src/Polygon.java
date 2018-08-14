import java.util.ArrayList;

public class Polygon {
	
	private ArrayList<Point> points = new ArrayList<Point>();
	private boolean filled;
	private PolygonGUI gui;
	private boolean truePolygon;
	
	public Polygon(PolygonGUI gui, boolean truePolygon) {
		this.gui = gui;
		this.filled = false;
		this.truePolygon = truePolygon;
	}
	
	public Polygon(Polygon other) {
		this.gui = other.gui;
		this.filled = other.filled;
		this.points = copyPoints(other.points);
		this.truePolygon = other.truePolygon;
	}
	
	public ArrayList<Point> copyPoints(ArrayList<Point> points){
		ArrayList<Point> copy = new ArrayList<Point>();
		Point temp;
		
		for(Point p : points) {
			temp = new Point(p.getExactX(), p.getExactY(), gui);
			temp.setAngle(p.getAngle(), false);
			copy.add(temp);
		}
		for(int i = 0; i < points.size(); i++) {
			if(i == 0) {
				copy.get(i).setCounterclockWise(copy.get(copy.size() - 1));
			}else {
				copy.get(i).setCounterclockWise(copy.get(i - 1));
			}
			if(i == copy.size() - 1) {
				copy.get(i).setClockwise(copy.get(0));
			}else {
				copy.get(i).setClockwise(copy.get(i + 1));
			}
		}
		return copy;
	}
	
	public ArrayList<Point> getPoints(){
		return this.points;
	}
	
	public Point getLastPoint() {
		return points.get(points.size() - 1);
	}
	
	public boolean hasPoint(Point other) {
		for(Point p : points) {
			if(p.equals(other)) {
				return true;
			}
		}
		return false;
	}
	
	public void addPoint(Point p) {
		points.add(p);
		p.setAssigned(true);
	}
	
	public void done() {
		filled = true;
		fixPoints();
		fixLines();
		calculateAngles();
	}
	
	public void calculateAngles() {
		if(truePolygon && !directionCorrect()) {
			for(Point p : points) {
				p.swapPoints();
			}
		}
		
		for(Point p : points) {
			calculateAngle(p, truePolygon);
		}
	}
	
	public boolean hasASA() {
		if(!truePolygon) {
			return true;
		}
		for(Point p : points) {
			if(!p.getSet() && !p.getClockwise().getSet() && !p.getClockwiseLine().getSet()) {
				return true;
			}
		}
		return false;
	}
	
	public double getArea() {
		double area = 0;
		
		calculateAngles();
		Polygon calcArea = new Polygon(this);
		
		area = negativeSpace(calcArea);
		area += positiveSpace(calcArea);
		
		return area;
	}
	
	public double positiveSpace(Polygon calcArea) {
		double area = 0;
		Point p;
		
		while(calcArea.points.size() > 3) {
			p = calcArea.points.get(0);
			Triangle t = new Triangle(p, p.getClockwise(), p.getCounterclockwise(), gui);
			area += t.getArea();
			removePoint(0, calcArea);
			area += negativeSpace(calcArea);
		}
		p = calcArea.points.get(0);
		Triangle t = new Triangle(p, p.getClockwise(), p.getCounterclockwise(), gui);
		area += t.getArea();
		return area;
	}
	
	public double negativeSpace(Polygon calcArea) {
		double area = 0;
		
		for(int i = 0; i < calcArea.points.size(); i++) {
			Point p = calcArea.points.get(i);
			if(p.getAngle() > 180) {
				Triangle t = new Triangle(p, p.getClockwise(), p.getCounterclockwise(), gui);
				area -= t.getArea();
				removePoint(i, calcArea);
				i = 0;
			}
		}
		return area;
	}
	
	public void removePoint(int index, Polygon shape) {
		int less = index - 1;
		int more = index + 1;
		
		if(less < 0) {
			less += shape.points.size();
		}
		if(more >= shape.points.size()) {
			more -= shape.points.size();
		}
		shape.points.get(less).setClockwise(shape.points.get(more));
		shape.points.get(more).setCounterclockWise(shape.points.get(less));
		
		calculateAngle(shape.points.get(less), true);
		calculateAngle(shape.points.get(more), true);
		
		shape.points.remove(index);
	}
	
	public static void calculateAngle(Point p, boolean fullPolygon) {
		if(p.getNumLines() < 2) {
			p.setAngle(0, false);
			return;
		}
		
		double cw = getAngle(p, p.getClockwise());
		double ccw = getAngle(p, p.getCounterclockwise());
		
		double angle = cw - ccw;
		while(angle < 0) {
			angle += 360;
		}
		
		if(!fullPolygon && angle > 180) {
			angle = 360 - angle;
		}
		
		p.setAngle(angle, false);
	}
	
	public boolean directionCorrect() {
		Point rightMost = getRightMostPoint();

		double clockwiseAngle = getAngle(rightMost, rightMost.getClockwise());
		double counterclockwiseAngle = getAngle(rightMost, rightMost.getCounterclockwise());
		
		return clockwiseAngle > counterclockwiseAngle;
	}
	
	public static double getAngle(Point origin, Point other) {
		double x = other.getExactX() - origin.getExactX();
		double y = other.getExactY() - origin.getExactY();
		
		double angle = 0;
		
		if(x == 0) {
			if(y < 0) {
				angle = 90;
			}else {
				angle = 270;
			}
		}else {
			angle = Math.toDegrees(Math.atan(- y / x));
		}
		
		if(x < 0) {
			angle += 180;
		}
		if(angle < 0) {
			angle += 360;
		}
		return angle;
	}
	
	public Point getRightMostPoint() {
		Point output = points.get(0);
		for(int i = 1; i < points.size(); i++) {
			if(points.get(i).getX() > output.getX()) {
				output = points.get(i);
			}
		}
		return output;
	}
	
	public void fixPoints() {
		for(int i = 0; i < points.size() - 1; i++) {
			Point p = points.get(i);
			if(p.getClockwise() != points.get(i + 1)) {
				p.swapPoints();
			}
		}
		
		Point p = points.get(points.size() - 1);
		
		if(truePolygon) {	
			if(p.getClockwise() != points.get(0)) {
				p.swapPoints();
			}
		}else {
			if(p.getCounterclockwise() != points.get(points.size() - 2)) {
				p.swapPoints();
			}
		}
	}
	
	public void fixLines() {
		for(Point p : points) {
			if(p.getClockwiseLine() != null) {
				if(p.getClockwiseLine().getCounterclockwise() != p) {
					p.getClockwiseLine().swapPoints();
				}
			}
		}
	}
	
	public boolean getFilled() {
		return this.filled;
	}
	
	public boolean isEmpty() {
		return this.points.size() == 0;
	}
	
	public boolean isTruePolygon() {
		return this.truePolygon;
	}
	
	public String toString() {
		String output = "Polygon:\n";
		for(Point p : points) {
			output += "(" + p.getX() + ", " + p.getY() + ")\n";
		}
		return output;
	}
}

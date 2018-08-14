
public class Line{
	
	private PolygonGUI gui;
	
	private long lastClicked;
	
	private double length;
	private boolean set;
	private boolean selected;
	
	private Point clockwise;
	private Point counterclockwise;
	
	public Line(Point clockwise, Point counterclockwise, PolygonGUI gui) {
		this.gui = gui;
		
		this.clockwise = clockwise;
		this.counterclockwise = counterclockwise;
		
		this.length = 0.0;
		this.set = false;
		this.selected = false;
	}
	
	public double getSlope() {
		if(clockwise.getX() == counterclockwise.getX()) {
			return Double.MAX_VALUE;
		}else {
			return (clockwise.getExactY() - counterclockwise.getExactY()) / (clockwise.getExactX() - counterclockwise.getExactX());
		}
	}
	
	public void setLength(double newLength) {
		this.set = true;
		this.length = newLength;
	}
	
	public void clicked(int x, int y, boolean active) {
		selected = !selected;
		
		if(active) {
			long nextClick = System.currentTimeMillis();
			
			if(nextClick - lastClicked < PolygonGUI.DOUBLE_CLICK_WINDOW) {
				gui.setEditor(new EditValueGUI(this, gui, x, y));
				selected = true;
			}
			lastClicked = nextClick;
		}
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean getSelected() {
		return this.selected;
	}
	
	public double getLength() {
		return this.length;
	}
	
	public double calculateLength() {
		return PolygonGUI.getDistanceBetweenPoints(clockwise.getExactX(), clockwise.getExactY(), counterclockwise.getExactX(), counterclockwise.getExactY());
	}
	
	public double getXPercentage() {
		double l = PolygonGUI.getDistanceBetweenPoints(clockwise.getExactX(), clockwise.getExactY(), counterclockwise.getExactX(), counterclockwise.getExactY());
		
		return Math.abs((clockwise.getExactX() - counterclockwise.getExactX()) /  l);
	}
	
	public double getYPercentage() {
		double l = PolygonGUI.getDistanceBetweenPoints(clockwise.getExactX(), clockwise.getExactY(), counterclockwise.getExactX(), counterclockwise.getExactY());
		
		return Math.abs((clockwise.getExactY() - counterclockwise.getExactY()) /  l);
	}
	
	public boolean getSet() {
		return this.set;
	}
	
	public void setSet(boolean set) {
		this.set = set;
	}
	
	public Point getClockwise() {
		return this.clockwise;
	}
	
	public Point getCounterclockwise() {
		return this.counterclockwise;
	}
	
	public void swapPoints() {
		Point temp = clockwise;
		clockwise = counterclockwise;
		counterclockwise = temp;
	}
}
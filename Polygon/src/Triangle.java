
public class Triangle {
	
	private Point a, b, c;
	
	public Triangle(Point a, Point b, Point c, PolygonGUI gui) {		
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public double getArea() {
		double ab = PolygonGUI.getDistanceBetweenPoints(a.getExactX(), a.getExactY(), b.getExactX(), b.getExactY());
		double bc = PolygonGUI.getDistanceBetweenPoints(c.getExactX(), c.getExactY(), b.getExactX(), b.getExactY());
		double ac = PolygonGUI.getDistanceBetweenPoints(a.getExactX(), a.getExactY(), c.getExactX(), c.getExactY());
		//ab, bc, and ac are all the length of lines between those points
		
		double p = (ab + bc + ac) / 2; //p is half the perimeter of the lines
		
		if(Double.isNaN(p)) {
			System.out.println("Error!");
		}
		
		if(p * (p - ab) * (p - bc) * (p - ac) < 0) { //Happens in rare cases due to the rounding of doubles. Number is often on the scale of 10^-8.
			return 0;
		}
		
		return Math.sqrt(p * (p - ab) * (p - bc) * (p - ac)); //Uses Heron's Formula for triangle area.
	}
}

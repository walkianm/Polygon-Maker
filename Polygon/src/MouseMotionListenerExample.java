import java.awt.*;  
import java.awt.event.*;  
public class MouseMotionListenerExample extends Frame implements MouseMotionListener{  
    MouseMotionListenerExample(){  
        addMouseMotionListener(this);  
          
        setSize(300,300);  
        setLayout(null);  
        setVisible(true);  
    }  
public void mouseDragged(MouseEvent e) {  
	System.out.println("Dragged!");
    Graphics g=getGraphics();  
    g.setColor(Color.BLUE);  
    g.fillOval(e.getX(),e.getY(),20,20);  
}  
public void mouseMoved(MouseEvent e) {
	System.out.println("Moved");
	
}  
  
public static void main(String[] args) {  
    new MouseMotionListenerExample();  
}  
}  
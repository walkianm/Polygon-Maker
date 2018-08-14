import java.awt.*;

import javax.swing.*;

public class InstructionsGUI extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private JPanel background;
	
	private String[] text = {"To place a point, simply click somewhere on the screen.", "To highlight a point, click on an existing point.",
			"To move a point, click and drag the desired point.", "To make a line, highlight the starting point, the click on the ending point.", 
			"To highlight a line, click on an existing line.", "To delete a line or point, highlight it, then click 'Delete' or 'Backspace'.",
			"To remove all lines and points at once, press the 'Clear All' button.", 
			"To set the size of an angle, double click on the point that makes up that angle.  ", "To set the length of a line, double click on that line.",
			"To connect a bunch of points at once, press the 'Connect Points' button."};
	
	private ImageIcon[] images = {new ImageIcon(getClass().getResource("Images/create-point.png")),
			new ImageIcon(getClass().getResource("Images/highlight-point.png")), new ImageIcon(getClass().getResource("Images/move-point.png")),
			new ImageIcon(getClass().getResource("Images/create-line.png")), new ImageIcon(getClass().getResource("Images/highlight-line.png")),
			new ImageIcon(getClass().getResource("Images/delete-point.png")), new ImageIcon(getClass().getResource("Images/clear-all.png")),
			new ImageIcon(getClass().getResource("Images/change-angle.png")), new ImageIcon(getClass().getResource("Images/change-length.png")),
			new ImageIcon(getClass().getResource("Images/connect-points.png"))};
	
	public InstructionsGUI(PolygonGUI gui) {
		
		setSize(700, 480);
		
		int x = gui.getLocation().x;
		int y = gui.getLocation().y;
		
		setLocation(x + 200, y + 150);
		setVisible(true);
		setTitle("How to Use Polygon Maker");
		
		addPanel();
		addInstructions();
	}
	
	private void addPanel() {
		background = new JPanel();
		background.setLayout(new GridBagLayout());
		
		this.add(background);
	}
	
	private void addInstructions() {		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		
		JLabel label;
		
		for(String s : text) {
			label = new JLabel(s);
			
			background.add(label, c);
			
			c.gridy++;
		}
		
		c.gridx = 1;
		c.gridy = 0;
		
		for(ImageIcon image : images) {
			label = new JLabel(image);
			
			background.add(label, c);
			
			c.gridy++;
		}
	}
}

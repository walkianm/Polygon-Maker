import java.awt.event.*;
import javax.swing.*;

public class EditValueGUI extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private Line line;
	private Point point;
	private PolygonGUI gui;
	
	private JPanel panel;
	private JButton enter;
	private GhostTextBox text;
	
	private boolean isLine;
	
	private int x;
	private int y;
	
	public EditValueGUI(Line l, PolygonGUI gui, int x, int y) {
		this.line = l;
		this.isLine = true;
		this.gui = gui;
		
		this.x = x;
		this.y = y;
		
		gui.setEditingValue(true);
		
		addComponents();
		setUpFrame();
	}
	
	public EditValueGUI(Point p, PolygonGUI gui, int x, int y) {
		this.point = p;
		this.isLine = false;
		this.gui = gui;
		
		this.x = x;
		this.y = y;
		
		gui.setEditingValue(true);
		
		addComponents();
		setUpFrame();
	}
	
	private void setUpFrame() {
		if(isLine) {
			setTitle("Set the length of this line:");
		}else {
			setTitle("Set the angle at this point:");
		}
		
		setResizable(false);
		setSize(325, 75);
		setLocation(x + gui.getX() - 162, y + gui.getY() + 100);
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public GhostTextBox getTextField() {
		return this.text;
	}
	
	private void addComponents() {
		panel = new JPanel();
		
		enter = new JButton("Enter");
		enter.addActionListener(new Listener());
		
		String ghostText = "Current value: ";
		if(isLine) {
			if(line.getSet()) {
				ghostText += line.getLength();
			}else {
				ghostText += line.calculateLength();
			}
		}else {
			ghostText += point.getAngle();
		}
		
		text = new GhostTextBox(20, ghostText);
		text.addActionListener(new Listener());
		
		panel.add(text);
		panel.add(enter);
		
		add(panel);
		addWindowFocusListener(new ListenForWindow());
		addWindowListener(new ListenForWindow());
	}
	
	public EditValueGUI getThis() {
		return this;
	}
	
	public void close() {
		gui.setEditingValue(false);
		gui.deselectAllLines();
		gui.deselectAllPoints();
		gui.movePoints();
		
		dispose();
		gui.refresh();
	}
		
	class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(validEntry()) {
				double newValue = Double.parseDouble(text.getText());
				
				if(isLine) {
					line.setLength(newValue);
					if(gui.getSourcePolygon(line).hasASA()) {
						gui.correctLine(line, true);
					}else {
						line.setSet(false);
						displayCannotSolvePolygon();
					}
				}else {
					point.setAngle(newValue, true);
					if(gui.getSourcePolygon(point).hasASA()) {
						gui.correctPoint(point, true);	
					}else {
						point.setSet(false);
						displayCannotSolvePolygon();
					}
				}
				
				close();
			}else {
				new ErrorMessage("Please input a decimal value!", getThis().getX(), getThis().getY(), gui);
			}
		}	
	}
	
	private void displayCannotSolvePolygon() {
		new ErrorMessage("<html>Cannot solve this polygon! Please leave a group of <br>an angle, side, and angle (ASA) unset on each polygon!</html>", getThis().getX(), getThis().getY(), gui);
	}
	
	private boolean validEntry() {
		String entry = text.getText();
		boolean decimal = false;
		
		for(int i = 0; i < entry.length(); i++) {
			if(entry.charAt(i) == '.' || entry.charAt(i) == ',') {
				//Only one decimal allowed!
				if(!decimal) {
					decimal = true;
				}else {
					return false;
				}
			}else {
				if(entry.charAt(i) > '9' || entry.charAt(i) < '0') {
					return false;
				}
			}
		}
		if(entry.length() == 0) {
			return false;
		}
		if(entry.length() == 1 && (entry.charAt(0) == '.' || entry.charAt(0) == ',')) {
			return false;
		}
		
		return true;
	}
	
	class ListenForWindow implements WindowFocusListener, WindowListener{

		@Override
		public void windowLostFocus(WindowEvent arg0) {
			if(!gui.getError()) {
				getThis().requestFocus(); //Keeps this window in focus.
			}
		}
		
		@Override
		public void windowClosing(WindowEvent arg0) {
			close();
		}
		
		
		//Below methods not needed for this program.
		@Override
		public void windowGainedFocus(WindowEvent arg0) {
		}

		@Override
		public void windowActivated(WindowEvent arg0) {
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
		}
		
	}
}

import java.awt.event.*;
import javax.swing.*;

public class ErrorMessage extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private JButton ok;
	private JLabel label;
	private PolygonGUI gui;
	private int divisor;
	
	public ErrorMessage(String message, int x, int y, PolygonGUI gui) {
		this.gui = gui;
		gui.setError(true);
		
		label = new JLabel(message + "     ");

		ok = new JButton("OK");
		ok.addActionListener(new ButtonListener());
		
		divisor = 1;
		while(message.indexOf("<br>") != -1) {
			divisor++;
			message = message.substring(message.indexOf("<br>") + 3);
		}
		
		JPanel panel = new JPanel();
		
		panel.add(label);
		panel.add(ok);
		
		add(panel);
		
		addWindowFocusListener(new ListenForWindow());
		addWindowListener(new ListenForWindow());
		addKeyListener(new Keyboard());
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setLocation(x + 15, y + 10);
		setTitle("Error!");
		setSize(((label.getText().length() * 5) / divisor) + 130, 65 + 10 * divisor);
		setVisible(true);
		
		this.requestFocus();
	}
	
	public void close() {
		gui.setError(false);
		if(gui.getEditingValue()) {
			gui.showEditor();
		}else {
			gui.deselectAllLines();
			gui.deselectAllPoints();
			
			gui.refresh();
		}
		dispose();
	}
	
	public ErrorMessage getThis() {
		return this;
	}
	
	class ButtonListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			close();
		}
	}
	
	class ListenForWindow implements WindowFocusListener, WindowListener{

		@Override
		public void windowLostFocus(WindowEvent arg0) {
			getThis().requestFocus();
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			gui.setError(false);
			close();
		}
		
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
	
	class Keyboard implements KeyListener{
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == 10) {
				close();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent areg0) {
		}		
	}
}

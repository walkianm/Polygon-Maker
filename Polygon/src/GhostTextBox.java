import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class GhostTextBox extends JTextField{
	
	private static final long serialVersionUID = 1L;
	private String text;
	boolean ghost;
	
	public GhostTextBox(int length, String text) {
		super(length);
		this.ghost = true;
		this.text = text;		
		
		this.setForeground(Color.GRAY);
		setText(text);
		addKeyListener(new KeyboardListener());
	}
	
	public GhostTextBox getThis() {
		return this;
	}
	
	public void removeText() {
		String currentText = getText();
		char c;
		
		for(int i = 0; i < text.length(); i++) {
			c = text.charAt(i);
			
			currentText = removeCharAt(currentText, currentText.indexOf(c));
		}
		
		setText(currentText);
	}
	
	public String removeCharAt(String oldString, int index) {
		String output = oldString.substring(0, index);
		
		if(index > oldString.length() - 2) {
			return output;
		}
		return output + oldString.substring(index + 1);
	}
	
	class KeyboardListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			if(ghost) {
				getThis().setForeground(Color.BLACK);
				removeText();
				ghost = false;
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}
	}
	
}

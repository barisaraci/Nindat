package server;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Terminal {
	private JTextPane textPane;
	private Style styleRed, styleBlue, styleGreen, styleYellow, styleWhite;
	
	public enum Status {
		ERROR,
		INFO,
		COMMAND,
		WARNING
	}
	
	public Terminal() {
		init();
	}
	
	private void init() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setTitle("Terminal");
		frame.setLayout(new BorderLayout());
		
		initTextPane();
		JScrollPane textScrollPane = new JScrollPane(textPane);
		textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JTextField textField = new JTextField();
		textField.addActionListener((ActionEvent e) -> {
			command(textField.getText());
			textField.setText("");
		});
		
		frame.add(textScrollPane, BorderLayout.CENTER);
		frame.add(textField, BorderLayout.SOUTH);
		frame.setVisible(true);
		textField.requestFocus();
	}
	
	private void initTextPane() {
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setBackground(Color.BLACK);
		
		styleRed = textPane.addStyle("I'm RED", null);
        StyleConstants.setForeground(styleRed, Color.RED);
        
        styleBlue = textPane.addStyle("I'm BLUE", null);
        StyleConstants.setForeground(styleBlue, new Color(0, 120, 255));
        
        styleGreen = textPane.addStyle("I'm GREEN", null);
        StyleConstants.setForeground(styleGreen, Color.GREEN);
        
        styleYellow = textPane.addStyle("I'm YELLOW", null);
        StyleConstants.setForeground(styleYellow, Color.YELLOW);
        
        styleWhite = textPane.addStyle("I'm WHITE", null);
        StyleConstants.setForeground(styleWhite, Color.WHITE);
	}
	
	private void command(String command) {
		print(command, Status.COMMAND);
	}
	
	public void print(String text, Status status) {
        StyledDocument doc = textPane.getStyledDocument();

		switch (status) {
			case ERROR: {
				try { 
					doc.insertString(doc.getLength(), "[ERROR] ", styleRed); 
				}
		        catch (BadLocationException e) {}
				break;
			}
			case INFO: {
				try { 
					doc.insertString(doc.getLength(), "[INFO] ", styleBlue); 
				}
		        catch (BadLocationException e) {}
				break;
			}
			case COMMAND: {
				try { 
					doc.insertString(doc.getLength(), "[COMMAND] ", styleGreen); 
				}
		        catch (BadLocationException e) {}
				break;
			}
			case WARNING: {
				try { 
					doc.insertString(doc.getLength(), "[WARNING] ", styleYellow); 
				}
		        catch (BadLocationException e) {}
				break;
			}
		}
		
		try { 
			doc.insertString(doc.getLength(), text + "\n", styleWhite); 
		}
        catch (BadLocationException e) {}
	}

	public void print(String text) {
		print(text, Status.INFO);
		
	}

}

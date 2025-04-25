package wr.newsletter;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class TextInputField extends JTextField {
	public static final long serialVersionUID = 1;
	private int max =0;
	public TextInputField(int cols) {
		super(cols);
		max = cols;
	}

	protected Document createDefaultModel() {
		return new LimitDocument();
	}

	private class LimitDocument extends PlainDocument {
		public static final long serialVersionUID = 1;
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

			if (str == null) {
				return;	
			}
			if (super.getContent().length() > max) {
				return;
			}
			boolean skip = false;
			char[] upper = str.toCharArray();
			for (int i = 0; i < upper.length; i++) {
				if (!Character.isLetter(upper[i])) {
					skip = true;
					continue;
				}
				upper[i] = Character.toLowerCase(upper[i]);
			}
			if (!skip) {
				super.insertString(offs, new String(upper), a);
			}
		}
	}
}
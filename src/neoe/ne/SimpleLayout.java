package neoe.ne;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class SimpleLayout {
	JPanel curr;
	JPanel p;

	public SimpleLayout(JPanel p) {
		this.p = p;
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		newCurrent();
	}

	public void add(JComponent co) {
		curr.add(co);
	}

	void newCurrent() {
		curr = new JPanel();
		curr.setLayout(new BoxLayout(curr, BoxLayout.LINE_AXIS));
	}

	public void newline() {
		p.add(curr);
		newCurrent();
	}
}
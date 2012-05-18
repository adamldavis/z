package neoe.ne;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import neoe.ne.U.SimpleLayout;

public class FindReplaceWindow implements ActionListener, KeyListener {

	JTextField jta1;
	JTextField jta2;
	JRadioButton jrb1;
	JButton jb1;
	JButton jb2;
	JButton jb3;
	private PlainPage page;
	private JRadioButton jrb2;
	private JDialog dialog;
	// private JFrame f;
	private JCheckBox jcb1;
	private JTextField jtadir;
	private JCheckBox jcb2;
	private JCheckBox jcb3;
	private JButton jb4;

	public FindReplaceWindow(JFrame f, PlainPage page) {
		this.page = page;
		// this.f = f;
		dialog = new JDialog(f, "Find/Replace");
		JPanel p = new JPanel();
		dialog.getContentPane().add(p);
		SimpleLayout s = new SimpleLayout(p);
		s.add(new JLabel("Find:"));
		s.add(jta1 = new JTextField());
		s.newline();
		s.add(new JLabel("Replace:"));
		s.add(jta2 = new JTextField());
		s.newline();
		s.add(jrb1 = new JRadioButton("IgnoreCase"));
		jrb1.setSelected(true);
		s.add(jrb2 = new JRadioButton("RegularExpression"));
		s.newline();
		s.add(jb1 = new JButton("Find"));
		s.add(jb4 = new JButton("FindAll"));
		s.add(jb2 = new JButton("Replace"));
		s.add(jb3 = new JButton("Replace All"));
		s.newline();
		s.add(jcb1 = new JCheckBox("in files", false));
		s.add(new JLabel("Dir:"));
		s.add(jtadir = new JTextField());
		s.newline();
		s.add(jcb2 = new JCheckBox("include subdir", true));
		s.add(jcb3 = new JCheckBox("skip binary", true));
		s.newline();
		jb1.setActionCommand("find");
		jb2.setActionCommand("replace");
		jb3.setActionCommand("replaceall");
		jb4.setActionCommand("findall");
		jb1.addActionListener(this);
		jb2.addActionListener(this);
		jb3.addActionListener(this);
		jb4.addActionListener(this);
		jcb2.setEnabled(false);
		jcb3.setEnabled(false);
		dialog.pack();
		dialog.setLocationRelativeTo(f);
		if (page != null && page.fn != null) {
			jtadir.setText(new File(page.fn).getParent());
		}
		jta1.addKeyListener(this);
		jta2.addKeyListener(this);
	}

	public void show() {
		dialog.setVisible(true);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		new FindReplaceWindow(f, null).show();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		page.history.beginAtom();
		try {
			String command = ae.getActionCommand();
			if (command.equals("find")) {
				page.ptFind.doFind(jta1.getText(), jrb1.isSelected(), jrb2
						.isSelected(), jcb1.isSelected(), jtadir.getText());
			} else if (command.equals("findall")) {
				U.doFindInPage(page, jta1.getText(), jrb1.isSelected());
			} else if (command.equals("replace")) {
				U.doReplace(page, jta1.getText(), jrb1.isSelected(), jrb2
						.isSelected(), jta2.getText(), true,  false, null);
			} else if (command.equals("replaceall")) {
				U.doReplaceAll(page, jta1.getText(), jrb1.isSelected(), jrb2
						.isSelected(), jta2.getText(),  jcb1.isSelected(),
						jtadir.getText());
			} else {
				return;
			}
			dialog.setVisible(false);
		} catch (Throwable e) {
			e.printStackTrace();
			page.ui.message(e.toString());
		}
		page.history.endAtom();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == jta1) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				ActionEvent ae = new ActionEvent(this, 1, "find");
				actionPerformed(ae);
			}
		} else {
			if (e.getSource() == jta2) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					ActionEvent ae = new ActionEvent(this, 1, "replaceall");
					actionPerformed(ae);
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}

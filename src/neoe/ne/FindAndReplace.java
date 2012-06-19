package neoe.ne;

import java.awt.Point;

class FindAndReplace {
	FindReplaceWindow findWindow;
	private PlainPage pp;
	String text2find;

	public FindAndReplace(PlainPage plainPage) {
		this.pp = plainPage;
	}

	void doFind(String text, boolean ignoreCase, boolean selected2,
			boolean inDir, String dir) throws Exception {
		if (!inDir) {
			text2find = text;
			pp.ignoreCase = ignoreCase;
			findNext();
			pp.uiComp.repaint();
		} else {
			U.doFindInDir(pp.uiComp, text, ignoreCase, selected2, inDir, dir);
		}
	}

	void findNext() {
		if (text2find != null && text2find.length() > 0) {
			Point p = U.find(pp, text2find, pp.cx + 1, pp.cy, pp.ignoreCase);
			if (p == null) {
				pp.ui.message("string not found");
			} else {
				pp.ptSelection.selectLength(p.x, p.y, text2find.length());

			}
		}
	}

	void showFindDialog() {
		String t = pp.ptSelection.getSelected();
		int p1 = t.indexOf("\n");
		if (p1 >= 0) {
			t = t.substring(0, p1);
		}
		if (t.length() == 0 && text2find != null) {
			t = text2find;
		}
		if (findWindow == null)
			findWindow = new FindReplaceWindow(pp.uiComp.frame, pp);
		if (t.length() > 0) {
			findWindow.jta1.setText(t);
		}
		findWindow.show();
		findWindow.jta1.grabFocus();
	}
}
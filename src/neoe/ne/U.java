package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import neoe.ne.PlainPage.Paint;
import neoe.util.FileIterator;

/**
 * util
 */
public class U {

	static enum BasicAction {
		Delete, DeleteEmtpyLine, Insert, InsertEmptyLine, MergeLine
	}

	static class BasicEdit {
		PlainPage page;
		boolean record;

		BasicEdit(boolean record, PlainPage page) {
			this.record = record;
			this.page = page;
		}

		void deleteEmptyLine(int y) {
			StringBuffer sb = lines().get(y);
			if (sb.length() > 0) {
				throw new RuntimeException("not a empty line " + y + ":" + sb);
			}
			if (lines().size() > 1) {
				lines().remove(y);
				if (record) {
					history().addOne(
							new HistoryCell(BasicAction.DeleteEmtpyLine, -1,
									-1, y, -1, null));
				}
			}
		}

		void deleteInLine(int y, int x1, int x2) {
			StringBuffer sb = lines().get(y);
			if (x1 >= sb.length())
				return;
			x2 = Math.min(x2, sb.length());
			String d = sb.substring(x1, x2);
			if (d.length() > 0) {
				sb.delete(x1, x2);
				if (record) {
					history().addOne(
							new HistoryCell(BasicAction.Delete, x1, x2, y, -1,
									d));
				}
			}
		}

		History history() {
			return page.history;
		}

		void insertEmptyLine(int y) {
			lines().add(y, new StringBuffer());
			if (record) {
				history().addOne(
						new HistoryCell(BasicAction.InsertEmptyLine, -1, -1, y,
								-1, null));
			}
		}

		void insertInLine(int y, int x, String s) {
			if (s.indexOf("\n") >= 0 || s.indexOf("\r") >= 0) {
				throw new RuntimeException("cannot contains line-seperator:["
						+ s + "]" + s.indexOf("\n"));
			}
			if (y == page.roLines.getLinesize()) {
				page.editRec.insertEmptyLine(y);
			}
			StringBuffer sb = lines().get(y);
			if (x > sb.length()) {
				sb.setLength(x);
			}
			sb.insert(x, s);
			if (record) {
				history().addOne(
						new HistoryCell(BasicAction.Insert, x, x + s.length(),
								y, -1, null));
			}
		}

		List<StringBuffer> lines() {
			return page.lines;
		}

		void mergeLine(int y) {
			StringBuffer sb1 = lines().get(y);
			StringBuffer sb2 = lines().get(y + 1);
			int x1 = sb1.length();
			sb1.append(sb2);
			lines().remove(y + 1);
			if (record) {
				history().addOne(
						new HistoryCell(BasicAction.MergeLine, x1, -1, y, -1,
								null));
			}
		}
	}

	static class FindAndReplace {
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
				doFindInDir(pp.uiComp, text, ignoreCase, selected2, inDir, dir);
			}
		}

		void findNext() {
			if (text2find != null && text2find.length() > 0) {
				Point p = find(pp, text2find, pp.cx + 1, pp.cy, pp.ignoreCase);
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

	static class History {
		public static int MAXSIZE = 200;
		List<HistoryCell> atom;
		LinkedList<List<HistoryCell>> data;
		int p;
		PlainPage page;

		public History(PlainPage page) {
			data = new LinkedList<List<HistoryCell>>();
			p = 0;
			atom = new ArrayList<HistoryCell>();
			this.page = page;
		}

		void add(List<HistoryCell> o) {
			if (p < data.size() && p >= 0) {
				for (int i = 0; i < data.size() - p; i++) {
					data.removeLast();
				}
			}
			List<HistoryCell> last = data.peekLast();
			// stem.out.println("last=" + last);
			if (!append(last, o)) {
				// System.out.println("add:" + o);
				data.add(o);
				if (data.size() > MAXSIZE) {
					data.removeFirst();
				} else {
					p += 1;
				}
			} else {
				// System.out.println("append:" + o);
			}
		}

		public void addOne(HistoryCell historyInfo) {
			historyInfo.page = this.page;
			atom.add(historyInfo);
		}

		/**
		 * try to append this change to the last ones
		 */
		boolean append(List<HistoryCell> lasts, List<HistoryCell> os) {
			if (lasts == null) {
				return false;
			}
			boolean ret = false;
			if (os.size() == 1) {
				HistoryCell o = os.get(0);
				HistoryCell last = lasts.get(lasts.size() - 1);
				if (o.canAppend(last)) {
					lasts.add(o);
					ret = true;
				}
			}
			return ret;
		}

		public void beginAtom() {
			if (atom.size() > 0) {
				endAtom();
			}
		}

		public void clear() {
			atom.clear();
			data.clear();
			p = 0;
		}

		public void endAtom() {
			if (atom.size() > 0) {
				// System.out.println("end atom");
				add(atom);
				atom = new ArrayList<HistoryCell>();
			}
		}

		public List<HistoryCell> get() {
			if (p <= 0) {
				return null;
			}
			p -= 1;
			// System.out.println("undo:" + data.get(p));
			return data.get(p);
		}

		public List<HistoryCell> getRedo() {
			if (p < data.size()) {
				p += 1;
				return data.get(p - 1);
			} else {
				return null;
			}
		}

		void redo() throws Exception {
			List<HistoryCell> os = getRedo();
			if (os == null) {
				return;
			}
			for (HistoryCell o : os) {
				o.redo();
			}
		}

		public int size() {
			return p;
		}

		void undo() throws Exception {
			List<HistoryCell> os = get();
			if (os == null) {
				return;
			}
			for (int i = os.size() - 1; i >= 0; i--) {
				HistoryCell o = os.get(i);
				o.undo();
			}
		}
	}

	static class HistoryCell {
		U.BasicAction action;
		PlainPage page;
		String s1;
		int x1, x2, y1, y2;

		public HistoryCell(U.BasicAction action, int x1, int x2, int y1,
				int y2, String s1) {
			super();
			this.s1 = s1;
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.action = action;
		}

		public boolean canAppend(HistoryCell last) {
			return ((last.action == U.BasicAction.Delete
					&& this.action == U.BasicAction.Delete && //
			((last.x1 == this.x1 || last.x1 == this.x2) && last.y1 == this.y1))//
			|| (last.action == U.BasicAction.Insert
					&& this.action == U.BasicAction.Insert && //
			((last.x1 == this.x1 || last.x2 == this.x1) && last.y1 == this.y1)));
		}

		U.BasicEdit editNoRec() {
			return page.editNoRec;
		}

		public void redo() {
			// System.out.println("redo:" + toString());
			switch (action) {
			case Delete:
				s1 = roLines().getInLine(y1, x1, x2);
				editNoRec().deleteInLine(y1, x1, x2);
				page.cursor.setSafePos(x1, y1);
				break;
			case DeleteEmtpyLine:
				editNoRec().deleteEmptyLine(y1);
				page.cursor.setSafePos(0, y1);
				break;
			case Insert:
				editNoRec().insertInLine(y1, x1, s1);
				page.cursor.setSafePos(x1 + s1.length(), y1);
				s1 = null;
				break;
			case InsertEmptyLine:
				editNoRec().insertEmptyLine(y1);
				page.cursor.setSafePos(0, y1 + 1);
				break;
			case MergeLine:
				editNoRec().mergeLine(y1);
				page.cursor.setSafePos(x1, y1);
				break;
			default:
				throw new RuntimeException("unkown action " + action);
			}
		}

		ReadonlyLines roLines() {
			return page.roLines;
		}

		@Override
		public String toString() {
			return "HistoryInfo [action=" + action + ", x1=" + x1 + ", x2="
					+ x2 + ", y1=" + y1 + ", y2=" + y2 + ", s1=" + s1 + "]\n";
		}

		public void undo() {
			// System.out.println("undo:" + toString());
			switch (action) {
			case Delete:
				editNoRec().insertInLine(y1, x1, s1);
				page.cursor.setSafePos(x1 + s1.length(), y1);
				s1 = null;
				break;
			case DeleteEmtpyLine:
				editNoRec().insertEmptyLine(y1);
				page.cursor.setSafePos(0, y1 + 1);
				break;
			case Insert:
				s1 = roLines().getInLine(y1, x1, x2);
				editNoRec().deleteInLine(y1, x1, x2);
				page.cursor.setSafePos(0, y1);
				break;
			case InsertEmptyLine:
				editNoRec().deleteEmptyLine(y1);
				page.cursor.setSafePos(0, y1);
				break;
			case MergeLine:
				String s2 = roLines().getInLine(y1, x1, Integer.MAX_VALUE);
				editNoRec().deleteInLine(y1, x1, Integer.MAX_VALUE);
				editNoRec().insertEmptyLine(y1 + 1);
				editNoRec().insertInLine(y1 + 1, 0, s2);
				page.cursor.setSafePos(0, y1 + 1);
				break;
			default:
				throw new RuntimeException("unkown action " + action);
			}
		}
	}

	static class Print implements Printable {
		Color colorLineNumber = new Color(0x30C200),
				colorGutterLine = new Color(0x30C200),
				colorNormal = Color.BLACK, colorDigit = new Color(0xA8002A),
				colorKeyword = new Color(0x0099CC),
				colorHeaderFooter = new Color(0x8A00B8),
				colorComment = new Color(200, 80, 50);
		Dimension dim;
		String fn;
		Font font = new Font("Monospaced", Font.PLAIN, 9);

		int lineGap = 3, lineHeight = 8, headerHeight = 20, footerHeight = 20,
				gutterWidth = 24, TAB_WIDTH_PRINT = 20;

		int linePerPage;
		ReadonlyLines roLines;
		int totalPage;
		Paint ui;
		EditPanel uiComp;

		Print(PlainPage pp) {
			this.ui = pp.ui;
			this.uiComp = pp.uiComp;
			this.roLines = pp.roLines;
			this.fn = pp.fn;
		}

		void drawReturn(Graphics2D g2, int w, int py) {
			g2.setColor(Color.red);
			g2.drawLine(w, py - lineHeight + font.getSize(), w + 3, py
					- lineHeight + font.getSize());
		}

		int drawStringLine(Graphics2D g2, String s, int x, int y) {
			int w = 0;
			String comment = ui.comment;
			int commentPos = comment == null ? -1 : s.indexOf(comment);
			if (commentPos >= 0) {
				String s1 = s.substring(0, commentPos);
				String s2 = s.substring(commentPos);
				int w1 = drawText(g2, s1, x, y, false);
				w = w1 + drawText(g2, s2, x + w1, y, true);
			} else {
				w = drawText(g2, s, x, y, false);
			}
			return w;
		}

		int drawText(Graphics2D g2, String s, int x, int y, boolean isComment) {
			int w = 0;
			if (isComment) {
				String[] ws = s.split("\t");
				int i = 0;
				for (String s1 : ws) {
					if (i++ != 0) {
						g2.drawImage(U.TabImgPrint, x + w, y - lineHeight, null);
						w += TAB_WIDTH_PRINT;
					}
					g2.setColor(colorComment);
					g2.drawString(s1, x + w, y);
					w += g2.getFontMetrics().stringWidth(s1);
					if (w > dim.width - gutterWidth) {
						break;
					}
				}
			} else {
				List<String> s1x = U.split(s);
				for (String s1 : s1x) {
					if (s1.equals("\t")) {
						g2.drawImage(U.TabImgPrint, x + w, y - lineHeight, null);
						w += TAB_WIDTH_PRINT;
					} else {
						// int highlightid =
						U.getHighLightID(s1, g2, colorKeyword, colorDigit,
								colorNormal);
						g2.drawString(s1, x + w, y);
						w += g2.getFontMetrics().stringWidth(s1);
					}
					if (w > dim.width - gutterWidth) {
						break;
					}
				}
			}
			return w;
		}

		void drawTextLine(Graphics2D g2, String s, int x0, int y0,
				int charCntInLine) {
			int w = drawStringLine(g2, s, x0, y0);
			drawReturn(g2, w + gutterWidth + 2, y0);
		}

		int getTotalPage(PageFormat pf) {
			linePerPage = ((int) pf.getImageableHeight() - footerHeight - headerHeight)
					/ (lineGap + lineHeight);
			System.out.println("linePerPage=" + linePerPage);
			if (linePerPage <= 0)
				return 0;
			int lines = roLines.getLinesize();
			int page = (lines % linePerPage == 0) ? lines / linePerPage : lines
					/ linePerPage + 1;
			return page;
		}

		public int print(Graphics graphics, PageFormat pf, int pageIndex)
				throws PrinterException {
			if (pageIndex > totalPage)
				return Printable.NO_SUCH_PAGE;
			// print
			ui.message("printing " + (pageIndex + 1) + "/" + totalPage);
			uiComp.repaint();
			Graphics2D g2 = (Graphics2D) graphics;
			g2.translate(pf.getImageableX(), pf.getImageableY());
			if (ui.noise) {
				U.paintNoise(g2, new Dimension((int) pf.getImageableWidth(),
						(int) pf.getImageableHeight()));
			}
			g2.setFont(font);
			g2.setColor(colorHeaderFooter);
			g2.drawString(fn == null ? "Unsaved" : new File(fn).getName(), 0,
					lineGap + lineHeight);
			{
				String s = (pageIndex + 1) + "/" + totalPage;
				g2.drawString(
						s,
						(int) pf.getImageableWidth()
								- U.strWidth(g2, s, TAB_WIDTH_PRINT) - 2,
						lineGap + lineHeight);
				s = new Date().toString() + " - NeoeEdit";
				g2.drawString(
						s,
						(int) pf.getImageableWidth()
								- U.strWidth(g2, s, TAB_WIDTH_PRINT) - 2,
						(int) pf.getImageableHeight() - 2);
				g2.setColor(colorGutterLine);
				g2.drawLine(gutterWidth - 4, headerHeight, gutterWidth - 4,
						(int) pf.getImageableHeight() - footerHeight);
			}
			int p = linePerPage * pageIndex;
			int charCntInLine = (int) pf.getImageableWidth() / 5 + 5;// inaccurate
			for (int i = 0; i < linePerPage; i++) {
				if (p >= roLines.getLinesize())
					break;
				int y = headerHeight + (lineGap + lineHeight) * (i + 1);
				g2.setColor(colorLineNumber);
				g2.drawString("" + (p + 1), 0, y);
				g2.setColor(colorNormal);
				String s = roLines.getline(p++).toString();
				if (s.length() > charCntInLine)
					s = s.substring(0, charCntInLine);
				drawTextLine(g2, s, gutterWidth, y, charCntInLine);

			}

			return Printable.PAGE_EXISTS;
		}

		void printPages() {

			new Thread() {
				public void run() {
					try {
						PrinterJob job = PrinterJob.getPrinterJob();
						PageFormat pf = job.pageDialog(job.defaultPage());
						totalPage = getTotalPage(pf);
						if (totalPage <= 0)
							return;
						dim = new Dimension((int) pf.getImageableWidth(),
								(int) pf.getImageableHeight());
						Book bk = new Book();
						bk.append(Print.this, pf, totalPage);
						job.setPageable(bk);
						if (job.printDialog()) {
							ui.message("printing...");
							uiComp.repaint();
							job.print();
							ui.message("print ok");
							uiComp.repaint();
						}
					} catch (Exception e) {
						ui.message("err:" + e);
						uiComp.repaint();
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	static class ReadonlyLines {
		PlainPage page;

		ReadonlyLines(PlainPage page) {
			this.page = page;
		}

		String getInLine(int y, int x1, int x2) {
			RoSb sb = getline(y);
			if (x2 > sb.length()) {
				x2 = sb.length();
			}
			if (x1 > sb.length()) {
				x1 = sb.length();
			}
			return sb.substring(x1, x2);
		}

		RoSb getline(int i) {
			return new RoSb(page.lines.get(i));
		}

		int getLinesize() {
			return page.lines.size();
		}

		String getTextInRect(Rectangle r) {
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			StringBuffer sb = new StringBuffer();
			if (page.rectSelectMode) {
				for (int i = y1; i <= y2; i++) {
					if (i != y1) {
						sb.append(page.lineSep);
					}
					sb.append(getInLine(i, x1, x2));
				}
			} else {
				if (y1 == y2 && x1 < x2) {
					sb.append(getInLine(y1, x1, x2));
				} else if (y1 < y2) {
					sb.append(getInLine(y1, x1, Integer.MAX_VALUE));
					for (int i = y1 + 1; i < y2; i++) {
						sb.append(page.lineSep);
						sb.append(getline(i));
					}
					sb.append(page.lineSep);
					sb.append(getInLine(y2, 0, x2));
				}
			}
			return sb.toString();
		}
	}

	/**
	 * read-only stringbuffer.
	 */
	static class RoSb {

		private StringBuffer sb;

		public RoSb(StringBuffer sb) {
			this.sb = sb;
		}

		public char charAt(int i) {
			return sb.charAt(i);
		}

		public int length() {
			return sb.length();
		}

		public String substring(int i) {
			return sb.substring(i);
		}

		public String substring(int a, int b) {
			return sb.substring(a, b);
		}

		public String toString() {
			return sb.toString();
		}

		public String toString(boolean ignoreCase) {
			String s = sb.toString();
			if (ignoreCase) {
				return s.toLowerCase();
			} else {
				return s;
			}
		}

	}

	public static class SimpleLayout {
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

	final static String[] kws = { "ArithmeticError", "AssertionError",
			"AttributeError", "BufferType", "BuiltinFunctionType",
			"BuiltinMethodType", "ClassType", "CodeType", "ComplexType",
			"DeprecationWarning", "DictProxyType", "DictType",
			"DictionaryType", "EOFError", "EllipsisType", "EnvironmentError",
			"Err", "Exception", "False", "FileType", "FloatType",
			"FloatingPointError", "FrameType", "FunctionType", "GeneratorType",
			"IOError", "ImportError", "IndentationError", "IndexError",
			"InstanceType", "IntType", "KeyError", "KeyboardInterrupt",
			"LambdaType", "ListType", "LongType", "LookupError", "MemoryError",
			"MethodType", "ModuleType", "NameError", "None", "NoneType",
			"NotImplemented", "NotImplementedError", "OSError", "ObjectType",
			"OverflowError", "OverflowWarning", "ReferenceError",
			"RuntimeError", "RuntimeWarning", "SliceType", "StandardError",
			"StopIteration", "StringType", "StringTypes", "SyntaxError",
			"SyntaxWarning", "SystemError", "SystemExit", "TabError",
			"TracebackType", "True", "TupleType", "TypeError", "TypeType",
			"UnboundLocalError", "UnboundMethodType", "UnicodeError",
			"UnicodeType", "UserWarning", "ValueError", "Warning",
			"WindowsError", "XRangeType", "ZeroDivisionError", "__abs__",
			"__add__", "__all__", "__author__", "__bases__", "__builtins__",
			"__call__", "__class__", "__cmp__", "__coerce__", "__contains__",
			"__debug__", "__del__", "__delattr__", "__delitem__",
			"__delslice__", "__dict__", "__div__", "__divmod__", "__doc__",
			"__docformat__", "__eq__", "__file__", "__float__", "__floordiv__",
			"__future__", "__ge__", "__getattr__", "__getattribute__",
			"__getitem__", "__getslice__", "__gt__", "__hash__", "__hex__",
			"__iadd__", "__import__", "__imul__", "__init__", "__int__",
			"__invert__", "__iter__", "__le__", "__len__", "__long__",
			"__lshift__", "__lt__", "__members__", "__metaclass__", "__mod__",
			"__mro__", "__mul__", "__name__", "__ne__", "__neg__", "__new__",
			"__nonzero__", "__oct__", "__or__", "__path__", "__pos__",
			"__pow__", "__radd__", "__rdiv__", "__rdivmod__", "__reduce__",
			"__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__",
			"__ror__", "__rpow__", "__rrshift__", "__rsub__", "__rtruediv__",
			"__rxor__", "__self__", "__setattr__", "__setitem__",
			"__setslice__", "__slots__", "__str__", "__sub__", "__truediv__",
			"__version__", "__xor__", "abs", "abstract", "all", "and", "any",
			"apply", "array", "as", "asc", "ascb", "ascw", "asm", "assert",
			"atn", "auto", "bool", "boolean", "break", "buffer", "byref",
			"byte", "byval", "call", "callable", "case", "catch", "cbool",
			"cbyte", "ccur", "cdate", "cdbl", "char", "chr", "chrb", "chrw",
			"cint", "class", "classmethod", "clng", "cmp", "coerce", "compile",
			"complex", "const", "continue", "cos", "createobject", "csng",
			"cstr", "date", "dateadd", "datediff", "datepart", "dateserial",
			"datevalue", "day", "def", "default", "del", "delattr", "dict",
			"dim", "dir", "divmod", "do", "double", "each", "elif", "else",
			"elseif", "empty", "end", "enum", "enumerate", "erase", "error",
			"eval", "except", "exec", "execfile", "execute", "exit", "exp",
			"explicit", "extends", "extern", "false", "file", "filter",
			"final", "finally", "fix", "float", "for", "formatcurrency",
			"formatdatetime", "formatnumber", "formatpercent", "from",
			"frozenset", "function", "get", "getattr", "getobject", "getref",
			"global", "globals", "goto", "hasattr", "hash", "hex", "hour",
			"id", "if", "imp", "implements", "import", "in", "inline", "input",
			"inputbox", "instanceof", "instr", "instrb", "instrrev", "int",
			"interface", "intern", "is", "isarray", "isdate", "isempty",
			"isinstance", "isnull", "isnumeric", "isobject", "issubclass",
			"iter", "join", "lambda", "lbound", "lcase", "left", "leftb",
			"len", "lenb", "let", "list", "loadpicture", "locals", "log",
			"long", "loop", "ltrim", "map", "max", "mid", "midb", "min",
			"minute", "mod", "month", "monthname", "msgbox", "native", "new",
			"next", "not", "nothing", "now", "null", "object", "oct", "on",
			"open", "option", "or", "ord", "package", "pass", "pow",
			"preserve", "print", "private", "property", "protected", "public",
			"raise", "randomize", "range", "raw_input", "redim", "reduce",
			"register", "reload", "rem", "replace", "repr", "resume", "return",
			"reversed", "rgb", "right", "rightb", "rnd", "round", "rtrim",
			"scriptengine", "scriptenginebuildversion",
			"scriptenginemajorversion", "scriptengineminorversion", "second",
			"select", "self", "set", "setattr", "sgn", "short", "signed",
			"sin", "sizeof", "slice", "sorted", "space", "split", "sqr",
			"static", "staticmethod", "step", "str", "strcomp", "strictfp",
			"string", "strreverse", "struct", "sub", "sum", "super", "switch",
			"synchronized", "tan", "then", "this", "throw", "throws", "time",
			"timeserial", "timevalue", "to", "transient", "trim", "true",
			"try", "tuple", "type", "typedef", "typename", "ubound", "ucase",
			"unichr", "unicode", "union", "unsigned", "until", "vars",
			"vartype", "vbAbort", "vbAbortRetryIgnore", "vbApplicationModal",
			"vbCancel", "vbCritical", "vbDefaultButton1", "vbDefaultButton2",
			"vbDefaultButton3", "vbDefaultButton4", "vbExclamation", "vbFalse",
			"vbGeneralDate", "vbIgnore", "vbInformation", "vbLongDate",
			"vbLongTime", "vbNo", "vbOK", "vbOKCancel", "vbOKOnly",
			"vbObjectError", "vbQuestion", "vbRetry", "vbRetryCancel",
			"vbShortDate", "vbShortTime", "vbSystemModal", "vbTrue",
			"vbUseDefault", "vbYes", "vbYesNo", "vbYesNoCancel", "vbarray",
			"vbblack", "vbblue", "vbboolean", "vbbyte", "vbcr", "vbcrlf",
			"vbcurrency", "vbcyan", "vbdataobject", "vbdate", "vbdecimal",
			"vbdouble", "vbempty", "vberror", "vbformfeed", "vbgreen",
			"vbinteger", "vblf", "vblong", "vbmagenta", "vbnewline", "vbnull",
			"vbnullchar", "vbnullstring", "vbobject", "vbred", "vbsingle",
			"vbstring", "vbtab", "vbvariant", "vbverticaltab", "vbwhite",
			"vbyellow", "void", "volatile", "weekday", "weekdayname", "wend",
			"while", "with", "xor", "xrange", "year", "yield", "zip" };

	static Random random = new Random();

	public static Image TabImg, TabImgPrint;

	final static TransferHandler th = new TransferHandler(null) {
		private static final long serialVersionUID = 5046626748299023865L;

		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}
			return true;
		}

		@SuppressWarnings("unchecked")
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			Transferable t = support.getTransferable();
			try {
				List<File> l = (List<File>) t
						.getTransferData(DataFlavor.javaFileListFlavor);
				for (File f : l) {
					if (f.isFile())
						try {
							U.openFile(f);
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	};

	static final String UTF8 = "utf8";

	static {
		try {
			System.out.println("welcome to " + PlainPage.WINDOW_NAME);
			loadTabImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void closePage(PlainPage page) throws Exception {
		EditPanel editor = page.uiComp;
		if (page.history.size() != 0) {
			if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(editor,
					"Are you sure to SAVE and close?", "Changes made",
					JOptionPane.YES_NO_OPTION)) {
				return;
			}
		}
		if (page.fn != null) {
			if (page.history.size() != 0) {
				saveFile(page);
			}
			saveFileHistory(page.fn, page.cy);
		}
		page.ui.closed = true;
		if (editor.frame != null)
			editor.frame.dispose();
	}

	/**
	 * quick find how much char can be shown in width
	 * 
	 * @param width
	 * @param g2
	 * @return
	 */
	static int computeShowIndex(String s, int width, Graphics2D g2, int TABWIDTH) {
		if (s.length() == 0) {
			return 0;
		}
		if (U.strWidth(g2, s, TABWIDTH) <= width) {
			return s.length();
		}
		int i = s.length() / 2;
		while (true) {
			if (i == 0) {
				return 0;
			}
			int w = U.strWidth(g2, s.substring(0, i), TABWIDTH);
			if (w <= width) {
				return i
						+ computeShowIndex(s.substring(i), width - w, g2,
								TABWIDTH);
			} else {
				i = i / 2;
			}
		}
	}

	static void doFindInDir(EditPanel editor, String text, boolean ignoreCase,
			boolean selected2, boolean inDir, String dir) throws Exception {
		Iterable<File> it = new FileIterator(dir);
		List<String> all = new ArrayList<String>();
		for (File f : it) {
			if (f.isDirectory()) {
				continue;
			}
			List<String> res = U.findInFile(f, text, ignoreCase);
			all.addAll(res);
		}
		showResult(editor, all, "dir " + dir, text);
		editor.repaint();
	}

	static void doFindInPage(PlainPage page, String text2find,
			boolean ignoreCase) throws Exception {
		if (text2find != null && text2find.length() > 0) {
			Point p = U.find(page, text2find, 0, 0, ignoreCase);
			if (p == null) {
				page.ui.message("string not found");
			} else {
				List<String> all = new ArrayList<String>();
				while (true) {
					all.add(String.format("%s:%s", p.y + 1,
							page.roLines.getline(p.y)));
					Point p2 = U.find(page, text2find, 0, p.y + 1, ignoreCase);
					if (p2 == null || p2.y <= p.y) {
						break;
					} else {
						p = p2;
					}
				}
				showResult(page.uiComp, all, "file " + page.fn, text2find);
				page.uiComp.repaint();
			}
		}
	}

	static void doReplace(PlainPage page, String text, boolean ignoreCase,
			boolean selected2, String text2, boolean all, boolean inDir,
			String dir) {
		page.ptFind.text2find = text;
		if (text != null && text.length() > 0) {
			Point p = replace(page, text, page.cx, page.cy, text2, all,
					ignoreCase);
			if (p == null) {
				page.ui.message("string not found");
			} else {
				page.cx = p.x;
				page.cy = p.y;
				// page.selectstartx = page.cx;
				// page.selectstarty = page.cy;
				// page.selectstopx = page.cx + text2.length();
				// page.selectstopy = page.cy;
				page.focusCursor();
				page.ptSelection.cancelSelect();
			}
		}
		page.uiComp.repaint();
	}

	static void doReplaceAll(PlainPage page, String text, boolean ignoreCase,
			boolean selected2, String text2, boolean inDir, String dir)
			throws Exception {
		if (inDir) {
			U.doReplaceInDir(page, text, ignoreCase, text2, inDir, dir);
		} else {
			U.doReplace(page, text, ignoreCase, selected2, text2, true, inDir,
					dir);
		}
	}

	static void doReplaceInDir(PlainPage page, String text,
			boolean ignoreCase2, String text2, boolean inDir, String dir)
			throws Exception {
		EditPanel editor = page.uiComp;
		Iterable<File> it = new FileIterator(dir);
		List<String> all = new ArrayList<String>();
		for (File f : it) {
			if (f.isDirectory()) {
				continue;
			}
			try {
				List<String> res = U.findInFile(f, text, page.ignoreCase);
				if (res.size() > 0) {
					PlainPage pi = new EditPanel(f).page;
					if (pi != null) {
						doReplaceAll(pi, text, ignoreCase2, false, text2,
								false, null);
					}
					pi.uiComp.openWindow();
				}
				all.addAll(res);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		showResult(editor, all, "dir " + dir, text);
		editor.repaint();
	}

	static int drawTwoColor(Graphics2D g2, String s, int x, int y, Color c1,
			Color c2, int d) {
		g2.setColor(c2);
		g2.drawString(s, x + d, y + d);
		g2.setColor(c1);
		g2.drawString(s, x, y);
		return g2.getFontMetrics().stringWidth(s);

	}

	static String removeTailR(String s) {
		while (s.endsWith("\r")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	static Point find(PlainPage page, String s, int x, int y, boolean ignoreCase) {
		if (y >= page.roLines.getLinesize())
			return null;
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		x = Math.min(x, page.roLines.getline(y).toString(ignoreCase).length());
		// first half row
		int p1 = page.roLines.getline(y).toString(ignoreCase).indexOf(s, x);
		if (p1 >= 0) {
			return new Point(p1, y);
		}
		// middle rows
		int fy = y;
		for (int i = 0; i < page.roLines.getLinesize() - 1; i++) {
			fy += 1;
			if (fy >= page.roLines.getLinesize()) {
				fy = 0;
			}
			p1 = page.roLines.getline(fy).toString(ignoreCase).indexOf(s);
			if (p1 >= 0) {
				return new Point(p1, fy);
			}
		}
		// last half row
		p1 = page.roLines.getline(y).toString(ignoreCase).substring(x)
				.indexOf(s);
		if (p1 >= 0) {
			return new Point(p1, fy);
		}
		return null;
	}

	static void findchar(PlainPage page, char ch, int inc, int[] c1, char chx) {
		int cx1 = c1[0];
		int cy1 = c1[1];
		RoSb csb = page.roLines.getline(cy1);
		int lv = 1;
		while (true) {
			if (inc == -1) {
				cx1--;
				if (cx1 < 0) {
					cy1--;
					if (cy1 < 0) {
						c1[0] = -1;
						c1[1] = -1;
						return;
					} else {
						csb = page.roLines.getline(cy1);
						cx1 = csb.length() - 1;
						if (cx1 < 0) {
							continue;
						}
					}
				}
				char ch2 = csb.charAt(cx1);
				if (ch2 == chx) {
					lv++;
				} else if (ch2 == ch) {
					lv--;
					if (lv == 0) {
						c1[0] = cx1;
						c1[1] = cy1;
						return;
					}
				}
			} else {
				cx1++;
				if (cx1 >= csb.length()) {
					cy1++;
					if (cy1 >= page.roLines.getLinesize()) {
						c1[0] = -1;
						c1[1] = -1;
						return;
					} else {
						csb = page.roLines.getline(cy1);
						cx1 = 0;
						if (cx1 >= csb.length()) {
							continue;
						}
					}
				}
				char ch2 = csb.charAt(cx1);
				if (ch2 == chx) {
					lv++;
				} else if (ch2 == ch) {
					lv--;
					if (lv == 0) {
						c1[0] = cx1;
						c1[1] = cy1;
						return;
					}
				}
			}
		}
	}

	static List<String> findInFile(File f, String text, boolean ignoreCase2) {
		// System.out.println("find in "+f.getName());
		int MAX_SHOW_CHARS_IN_LINE = 30;
		List<String> a = new ArrayList<String>();
		try {
			String enc = guessEncoding(f.getAbsolutePath());
			if (enc == null)
				enc = UTF8;// avoid wrong skip
			if (enc != null) {// skip binary
				String fn = f.getAbsolutePath();
				if (ignoreCase2) {
					text = text.toLowerCase();
				}
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), enc));
				String line;
				int lineno = 0;
				while ((line = in.readLine()) != null) {
					lineno++;
					String oline = line;
					if (ignoreCase2) {
						line = line.toLowerCase();
					}

					if (line.indexOf(text) >= 0) {
						if (line.length() > MAX_SHOW_CHARS_IN_LINE) {
							line = line.substring(0, MAX_SHOW_CHARS_IN_LINE)
									+ "...";
						}
						a.add(String.format("%s|%s:%s", fn, lineno, oline));
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	static void gc() {
		System.out.print(km(Runtime.getRuntime().freeMemory()) + "/"
				+ km(Runtime.getRuntime().totalMemory()) + " -> ");
		Runtime.getRuntime().gc();
		System.out.println(km(Runtime.getRuntime().freeMemory()) + "/"
				+ km(Runtime.getRuntime().totalMemory()));
	}

	static String getClipBoard() {
		String s;
		try {
			s = Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor).toString();
		} catch (Exception e) {
			s = "";
		}
		return s;
	}

	static File getFileHistoryName() throws IOException {
		String home = System.getProperty("user.home");
		File dir = new File(home, ".neoeedit");
		dir.mkdirs();

		File f = new File(dir, "fh.txt");
		if (!f.exists()) {
			new FileOutputStream(f).close();
		}
		return f;
	}

	static int getHighLightID(String s, Graphics2D g2, Color colorKeyword,
			Color colorDigital, Color color) {
		if (Arrays.binarySearch(kws, s) >= 0
				|| Arrays.binarySearch(kws, s.toLowerCase()) >= 0) {
			g2.setColor(colorKeyword);
		} else if (isAllDigital(s)) {
			g2.setColor(colorDigital);
		} else {
			g2.setColor(color);
		}
		return 0;
	}

	static String getIndent(String s) {
		int p = 0;
		while (p < s.length() && (s.charAt(p) == ' ' || s.charAt(p) == '\t')) {
			p += 1;
		}
		return s.substring(0, p);
	}

	static String getText(PlainPage pp) {
		StringBuffer sb = new StringBuffer();
		int len = pp.roLines.getLinesize();
		for (int i = 0; i < len; i++) {
			if (i > 0)
				sb.append(pp.lineSep);
			sb.append(pp.roLines.getline(i).toString());
		}
		return sb.toString();
	}

	static void gotoFileLine(String sb) throws Exception {
		int p1, p2;
		if ((p1 = sb.indexOf("|")) >= 0) {
			if ((p2 = sb.indexOf(":", p1)) >= 0) {
				String fn = sb.substring(0, p1);
				int line = -1;
				try {
					line = Integer.parseInt(sb.substring(p1 + 1, p2));
				} catch (Exception e) {
				}
				if (line >= 0) {
					openFile(fn, line);
				}
			}
		}
	}

	static void guessComment(PlainPage page) {
		String comment = null;
		String[] commentchars = { "#", "%", "'", "//", "!", ";", "--", "/*",
				"<!--" };
		int[] cnts = new int[commentchars.length];
		for (int i = 0; i < page.roLines.getLinesize(); i++) {
			RoSb sb = page.roLines.getline(i);
			for (int j = 0; j < cnts.length; j++) {
				if (sb.toString().trim().startsWith(commentchars[j])) {
					cnts[j]++;
				}
			}
		}
		int kind = 0;
		int max = 0;
		for (int j = 0; j < cnts.length; j++) {
			if (cnts[j] > 0) {
				kind++;
				max = Math.max(max, cnts[j]);
			}
		}
		if (kind == 1) {
			for (int j = 0; j < cnts.length; j++) {
				if (cnts[j] > 0) {
					comment = commentchars[j];

					break;
				}
			}
		} else {
			int k2 = 0;
			int lv2 = Math.max(5, max / 10);
			for (int j = 0; j < cnts.length; j++) {
				if (cnts[j] > lv2) {
					k2++;
				}
			}
			if (k2 == 1) {
				for (int j = 0; j < cnts.length; j++) {
					if (cnts[j] > lv2) {
						comment = commentchars[j];
						break;
					}
				}
			}
		}
		if (comment == null) {
			page.ui.message("no comment found" + Arrays.toString(cnts));
		} else {
			page.ui.message("comment found:" + comment);
		}
		page.ui.comment = comment;
		page.uiComp.repaint();
	}

	static String guessEncoding(String fn) throws Exception {
		// S/ystem.out.println("guessing encoding");
		String[] encodings = { "sjis", "gbk", UTF8, "unicode", };

		FileInputStream in = new FileInputStream(fn);
		final int defsize = 4096 * 2;
		int len = Math.min(defsize, (int) new File(fn).length());
		try {
			// S/ystem.out.println("len:" + len);
			byte[] buf = new byte[len];
			len = in.read(buf);
			// S/ystem.out.println("len2:" + len);
			if (len != defsize) {
				byte[] b2 = new byte[len];
				System.arraycopy(buf, 0, b2, 0, len);
				buf = b2;
			}
			for (String enc : encodings) {
				String s = new String(buf, enc);
				if (new String(s.getBytes(enc), enc).equals(s)
						&& s.indexOf("�") < 0) {
					return enc;
				}
				// byte[] b2 = new String(buf, enc).getBytes(enc);
				// if (b2.length != buf.length) {
				// continue;
				// }
				// int nlen = Math.max(0, len - 1);// for not last complete char
				// if (Arrays.equals(Arrays.copyOf(buf, nlen), Arrays.copyOf(b2,
				// nlen))) {
				// return enc;
				// }
			}
		} finally {
			in.close();
		}

		return null;
	}

	static String guessEncodingForEditor(String fn) {
		try {
			String s = guessEncoding(fn);
			if (s == null) {// unknow
				s = UTF8;
			}
			return s;
		} catch (Exception e) {
			return UTF8;
		}
	}

	static String guessLineSepForEditor(String fn) {
		try {
			// S/ystem.out.println("guessing encoding");
			FileInputStream in = new FileInputStream(fn);
			final int defsize = 4096;
			int len = Math.min(defsize, (int) new File(fn).length());
			try {
				// S/ystem.out.println("len:" + len);
				byte[] buf = new byte[len];
				len = in.read(buf);
				// S/ystem.out.println("len2:" + len);
				if (len != defsize) {
					byte[] b2 = new byte[len];
					System.arraycopy(buf, 0, b2, 0, len);
					buf = b2;
				}
				return new String(buf, "iso8859-1").indexOf("\r\n") >= 0 ? "\r\n"
						: "\n";
			} finally {
				in.close();
			}
		} catch (Exception e) {
			return "\n";
		}

	}

	static boolean isAllDigital(String s) {
		for (char c : s.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	static boolean isImageFile(File f) {
		String fn = f.getName().toLowerCase();
		return (fn.endsWith(".gif") || fn.endsWith(".jpg")
				|| fn.endsWith(".png") || fn.endsWith(".bmp"));
	}

	static boolean isSkipChar(char ch, char ch1) {
		if (Character.isSpaceChar(ch1) || ch1 == '\t') {
			return Character.isSpaceChar(ch) || ch == '\t';
		} else {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	static String km(long v) {
		float m = 1024 * 1024f;
		if (v > m) {
			return String.format("%.1fMB", v / m);
		} else if (v > 1024) {
			return String.format("%.1fKB", v / 1024f);
		}
		return "" + v;
	}

	static void loadTabImage() throws Exception {
		BufferedImage img = ImageIO.read(U.class
				.getResourceAsStream("/icontab.png"));
		TabImg = img.getScaledInstance(40, 8, Image.SCALE_SMOOTH);
		TabImgPrint = img.getScaledInstance(20, 8, Image.SCALE_SMOOTH);
	}

	static void openFile(File f) throws Exception {
		if (isImageFile(f)) {
			new PicView().show(f);
		} else {
			new EditPanel(f).openWindow();
		}
	}

	static void openFile(PlainPage page) throws Exception {
		JFileChooser chooser = new JFileChooser();

		if (page.fn != null) {
			chooser.setSelectedFile(new File(page.fn));
		} else if (page.workPath != null) {
			chooser.setSelectedFile(new File(page.workPath));// Fixme:cannot
			// set
			// correctly
		}
		int returnVal = chooser.showOpenDialog(page.uiComp);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: "
					+ chooser.getSelectedFile().getAbsolutePath());
			File f = chooser.getSelectedFile();
			openFile(f);
		}
	}

	static void openFile(String fn, int line) throws Exception {
		File f = new File(fn);
		if (isImageFile(f)) {
			new PicView().show(f);
			return;
		}
		final PlainPage pp = new EditPanel(new File(fn)).page;
		if (pp != null && pp.lines.size() > 0) {
			line -= 1;
			pp.cx = 0;
			pp.cy = Math.max(0, Math.min(line, pp.lines.size() - 1));
			pp.sy = Math.max(0, pp.cy - 3);
			pp.uiComp.openWindow();
			pp.uiComp.repaint();
		}
	}

	static void openFileHistory() throws Exception {
		File fhn = getFileHistoryName();
		PlainPage pp = new EditPanel(fhn).page;
		pp.cy = Math.max(0, pp.lines.size() - 1);
		pp.sy = Math.max(0, pp.cy - 5);
		pp.uiComp.openWindow();
		pp.uiComp.repaint();
	}

	static void paintNoise(Graphics2D g2, Dimension dim) {
		int cnt = 1000;
		int w = dim.width;
		int h = dim.height;
		int cs = 0xffffff;
		for (int i = 0; i < cnt; i++) {
			int x = random.nextInt(w);
			int y = random.nextInt(h);
			g2.setColor(new Color(random.nextInt(cs)));
			g2.drawLine(x, y, x + 1, y);
		}
	}

	static void readFile(PlainPage page, String fn) {
		page.isCommentChecked = false;
		if (page.encoding == null) {
			page.encoding = U.guessEncodingForEditor(fn);
		}
		page.lineSep = U.guessLineSepForEditor(fn);
		page.ptEdit.setLines(U.readFileForEditor(fn, page.encoding));
	}

	static List<StringBuffer> readFileForEditor(String fn, String encoding) {
		try {
			List<StringBuffer> lines = new ArrayList<StringBuffer>();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(fn), encoding));
				String line;
				while ((line = in.readLine()) != null) {
					lines.add(new StringBuffer(line));
				}
				in.close();
			} catch (OutOfMemoryError e) {
				lines = new ArrayList<StringBuffer>();
				lines.add(new StringBuffer(e.toString()));
				return lines;
			} catch (Throwable e1) {
				lines.add(new StringBuffer(e1.toString()));
			}
			if (lines.size() == 0) {
				lines.add(new StringBuffer());
			}
			return lines;
		} catch (OutOfMemoryError e) {
			List<StringBuffer> lines = new ArrayList<StringBuffer>();
			lines.add(new StringBuffer(e.toString()));
			return lines;
		}
	}

	static void reloadWithEncodingByUser(String fn, PlainPage pp) {
		if (fn == null) {
			pp.ui.message("file not saved.");
			return;
		}
		setEncodingByUser(pp, "Reload with Encoding:");
		readFile(pp, fn);
	}

	static void removeTrailingSpace(PlainPage page) {
		for (int i = 0; i < page.roLines.getLinesize(); i++) {
			RoSb sb = page.roLines.getline(i);
			int p = sb.length() - 1;
			while (p >= 0 && "\r\n\t ".indexOf(sb.charAt(p)) >= 0) {
				p--;
			}
			if (p < sb.length() - 1) {
				page.editRec.deleteInLine(i, p + 1, sb.length());
			}
		}
	}

	static void repaintAfter(final long t, final JComponent edit) {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(t);
					edit.repaint();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	static Point replace(PlainPage page, String s, int x, int y, String s2,
			boolean all, boolean ignoreCase) {
		int cnt = 0;
		U.BasicEdit editRec = page.editRec;
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		// first half row
		int p1 = x;
		while (true) {
			p1 = page.roLines.getline(y).toString(ignoreCase).indexOf(s, p1);
			if (p1 >= 0) {
				cnt++;
				editRec.deleteInLine(y, p1, p1 + s.length());
				editRec.insertInLine(y, p1, s2);
				if (!all) {
					return new Point(p1 + s2.length(), y);
				}
				p1 = p1 + s2.length();
			} else {
				break;
			}
		}
		// middle rows
		int fy = y;
		for (int i = 0; i < page.roLines.getLinesize() - 1; i++) {
			fy += 1;
			if (fy >= page.roLines.getLinesize()) {
				fy = 0;
			}
			p1 = 0;
			while (true) {
				p1 = page.roLines.getline(fy).toString(ignoreCase)
						.indexOf(s, p1);
				if (p1 >= 0) {
					cnt++;
					editRec.deleteInLine(fy, p1, p1 + s.length());
					editRec.insertInLine(fy, p1, s2);
					if (!all) {
						return new Point(p1 + s2.length(), fy);
					}
					p1 = p1 + s2.length();
				} else {
					break;
				}
			}
		}
		// last half row
		fy += 1;
		if (fy >= page.roLines.getLinesize()) {
			fy = 0;
		}
		p1 = 0;
		while (true) {
			p1 = page.roLines.getline(fy).toString(ignoreCase).substring(0, x)
					.indexOf(s, p1);
			if (p1 >= 0) {
				cnt++;
				editRec.deleteInLine(fy, p1, p1 + s.length());
				editRec.insertInLine(fy, p1, s2);
				if (!all) {
					return new Point(p1 + s2.length(), fy);
				}
				p1 = p1 + s2.length();
			} else {
				break;
			}
		}
		if (cnt > 0) {
			page.ui.message("replaced " + cnt + " places");
			return new Point(x, y);
		} else {
			return null;
		}
	}

	static void runScript(final PlainPage pp) throws Exception {
		final JFrame sf = new JFrame("Javascript");
		sf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		sf.getContentPane().add(p);
		SimpleLayout s = new SimpleLayout(p);
		String sample = "var i=0; \nfunction run(s,cur,max){\nreturn s;\n}";
		final EditPanel ed = new EditPanel(sample);
		final PlainPage pp1 = ed.page;
		ed.frame = sf;
		pp1.workPath = pp.workPath;
		s.add(pp1.uiComp);
		s.newline();
		JButton jb1 = new JButton("run");
		JButton jb2 = new JButton("close");
		s.add(jb1);
		s.add(jb2);
		s.newline();
		jb1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ed.grabFocus();
					List<StringBuffer> newLines = JS
							.run(pp.lines, getText(pp1));
					PlainPage pp = new EditPanel("").page;
					pp.workPath = pp1.workPath;
					pp.ptEdit.setLines(newLines);
					pp.uiComp.openWindow();
					pp.ui.applyColorMode(pp1.ui.colorMode);
				} catch (Exception e1) {
					System.out.println(e1);
					String s1 = "" + e1;
					String expect = "javax.script.ScriptException: sun.org.mozilla.javascript.internal.EvaluatorException:";
					if (s1.startsWith(expect))
						s1 = s1.substring(expect.length());
					pp1.ptEdit.append("\n//Error:" + s1 + "\n");
					ed.repaint();
				}

			}
		});
		jb2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pp1.history.size() != 0) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(ed, "Are you sure to close?",
									"Changes made", JOptionPane.YES_NO_OPTION)) {
						ed.grabFocus();
						return;
					}
				}
				sf.dispose();
			}
		});
		setFrameSize(sf, 800, 600);
		sf.setVisible(true);
	}

	static void runScriptOnDir(String workPath) throws Exception {
		final JFrame sf = new JFrame("Javascript On dir");
		sf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		sf.getContentPane().add(p);
		SimpleLayout s = new SimpleLayout(p);
		String sample = "var dir=\"d:/tmp\";\nfunction onFile(f){\n  return f.getAbsolutePath()+'\t'+f.length();\n}\n";
		final EditPanel ed = new EditPanel(sample);
		final PlainPage pp1 = ed.page;
		ed.frame = sf;
		pp1.workPath = workPath;
		s.add(pp1.uiComp);
		s.newline();
		JButton jb1 = new JButton("run");
		JButton jb2 = new JButton("close");
		s.add(jb1);
		s.add(jb2);
		s.newline();
		jb1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ed.grabFocus();
					List<StringBuffer> newLines = JS.runOnDir(getText(pp1));
					PlainPage pp = new EditPanel("").page;
					pp.workPath = pp1.workPath;
					pp.ptEdit.setLines(newLines);
					pp.uiComp.openWindow();
					pp.ui.applyColorMode(pp1.ui.colorMode);
				} catch (Exception e1) {
					e1.printStackTrace();
					String s1 = "" + e1;
					String expect = "javax.script.ScriptException: sun.org.mozilla.javascript.internal.EvaluatorException:";
					if (s1.startsWith(expect))
						s1 = s1.substring(expect.length());
					pp1.ptEdit.append("\n//Error:" + s1 + "\n");
					ed.repaint();
				}

			}
		});
		jb2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pp1.history.size() != 0) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(ed, "Are you sure to close?",
									"Changes made", JOptionPane.YES_NO_OPTION)) {
						ed.grabFocus();
						return;
					}
				}
				sf.dispose();
			}
		});
		sf.setSize(new Dimension(800, 600));
		sf.setVisible(true);
	}

	static void saveAs(PlainPage page) throws Exception {
		EditPanel editor = page.uiComp;
		JFileChooser chooser = new JFileChooser(page.fn);
		int returnVal = chooser.showSaveDialog(editor);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fn = chooser.getSelectedFile().getAbsolutePath();
			if (new File(fn).exists()) {
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						editor, "file exists, are you sure to overwrite?",
						"save as...", JOptionPane.YES_NO_OPTION)) {
					page.ui.message("not renamed");
					return;
				}
			}
			page.fn = fn;
			editor.changeTitle();
			page.ui.message("file renamed");
			savePageToFile(page);
		}
	}

	static boolean saveFile(PlainPage page) throws Exception {
		if (page.fn == null) {
			JFileChooser chooser = new JFileChooser(page.workPath);
			int returnVal = chooser.showSaveDialog(page.uiComp);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				page.isCommentChecked = false;
				String fn = chooser.getSelectedFile().getAbsolutePath();
				if (new File(fn).exists()) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(page.uiComp,
									"Are you sure to overwrite?",
									"File exists", JOptionPane.YES_NO_OPTION)) {
						page.ui.message("not saved");
						return false;
					}
				}
				page.fn = fn;
				page.uiComp.changeTitle();

			} else {
				return false;
			}
		}
		return savePageToFile(page);

	}

	static void saveFileHistory(String fn, int line) throws IOException {
		File fhn = getFileHistoryName();
		if (fhn.getAbsoluteFile().equals(new File(fn).getAbsoluteFile()))
			return;
		OutputStream out = new FileOutputStream(fhn, true);
		out.write(String.format("\n%s|%s:", fn, line).getBytes("utf8"));
		out.close();
	}

	static boolean savePageToFile(PlainPage page) throws Exception {
		System.out.println("save " + page.fn);
		if (page.encoding == null) {
			page.encoding = UTF8;
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(page.fn), page.encoding));
		for (int i = 0; i < page.roLines.getLinesize(); i++) {
			out.write(page.roLines.getline(i).toString());
			out.write(page.lineSep);
		}
		out.close();
		return true;
	}

	static void scale(int amount, Paint ui) {
		if (amount > 0) {
			ui.scalev *= 0.9f;
		} else if (amount < 0) {
			ui.scalev *= 1.1f;
		}
	}

	static void setClipBoard(String s) {
		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(s), null);
	}

	static void setEncodingByUser(PlainPage plainPage, String msg) {
		String s = JOptionPane.showInputDialog(plainPage.uiComp, msg,
				plainPage.encoding);
		if (s == null) {
			return;
		}
		try {
			"a".getBytes(s);
		} catch (Exception e) {
			plainPage.ui.message("bad encoding:" + s);
			return;
		}
		plainPage.encoding = s;
	}

	public static void setFrameSize(JFrame f, int w, int h) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		f.setSize(Math.min(800, Math.min(dim.width, w)),
				Math.min(600, Math.min(dim.height, h)));
	}

	static void showHelp(final Paint ui, final JComponent uiComp) {
		if (ui.aboutImg != null)
			return;
		new Thread() {
			public void run() {
				try {
					int w = uiComp.getWidth();
					int h = 60;
					ui.aboutImg = new BufferedImage(w, h,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D gi = ui.aboutImg.createGraphics();
					gi.setColor(Color.BLUE);
					gi.fillRect(0, 0, w, h);
					gi.setColor(Color.CYAN);
					gi.setFont(new Font("Arial", Font.BOLD, 40));
					gi.drawString("NeoeEdit", 6, h - 20);
					gi.setColor(Color.YELLOW);
					gi.setFont(new Font("Arial", Font.PLAIN, 16));
					String url = "http://code.google.com/p/neoeedit/";
					gi.drawString("visit " + url
							+ " for more info.(url copied)", 6, h - 6);
					setClipBoard(url);
					gi.dispose();
					ui.aboutY = -h;
					ui.aboutOn = true;
					for (int i = -h; i <= 0; i++) {
						ui.aboutY = i;
						uiComp.repaint();
						Thread.sleep(500 / h);
					}
					Thread.sleep(2000);
					for (int i = 0; i >= -h; i--) {
						ui.aboutY = i;
						uiComp.repaint();
						Thread.sleep(500 / h);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					ui.aboutOn = false;
					ui.aboutImg = null;
				}
			}
		}.start();

	}

	static void showResult(EditPanel editor, List<String> all, String dir,
			String text) throws Exception {
		EditPanel ep = new EditPanel("");
		PlainPage p2 = ep.page;
		p2.workPath = editor.page.workPath;
		p2.ui.applyColorMode(editor.page.ui.colorMode);
		List<StringBuffer> sbs = new ArrayList<StringBuffer>();
		sbs.add(new StringBuffer(String.format(
				"find %s results in %s for '%s'", all.size(), dir, text)));
		for (Object o : all) {
			sbs.add(new StringBuffer(o.toString()));
		}
		p2.ptEdit.setLines(sbs);
		ep.openWindow();
		gc();
	}

	static String spaces(int cx) {
		if (cx <= 0)
			return "";
		StringBuffer sb = new StringBuffer(cx);
		sb.setLength(cx);
		for (int i = 0; i < cx; i++)
			sb.setCharAt(i, ' ');
		return sb.toString();
	}

	static List<String> split(String s) {
		StringBuffer sb = new StringBuffer();
		List<String> sl = new ArrayList<String>();
		for (char c : s.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				if (sb.length() > 0) {
					sl.add(sb.toString());
					sb.setLength(0);
				}
				sl.add("" + c);
			} else {
				sb.append(c);
			}
		}
		if (sb.length() > 0) {
			sl.add(sb.toString());
			sb.setLength(0);
		}
		return sl;
	}

	static String[] splitLine(String s) {
		String sep = "\n";
		List<String> s1 = new ArrayList<String>();
		int p1 = 0;
		while (true) {
			int p2 = s.indexOf(sep, p1);
			if (p2 < 0) {
				String s2 = U.removeTailR(s.substring(p1));
				if (s2.indexOf('\r') >= 0) {
					String[] ss2 = s2.split("\\r");
					for (String ss : ss2)
						s1.add(ss);
				} else {
					s1.add(s2);
				}
				break;
			} else {
				String s2 = U.removeTailR(s.substring(p1, p2));
				if (s2.indexOf('\r') >= 0) {
					String[] ss2 = s2.split("\\r");
					for (String ss : ss2)
						s1.add(ss);
				} else {
					s1.add(s2);
				}
				p1 = p2 + 1;
			}
		}
		return (String[]) s1.toArray(new String[s1.size()]);
	}

	static void startNoiseThread(final Paint ui, final EditPanel uiComp) {
		Thread t = new Thread() {
			public void run() {
				try {// noise thread
					while (true) {
						if (ui.noise && !ui.closed) {
							uiComp.repaint();
							// System.out.println("paint noise");
							Thread.sleep(ui.noisesleep);
						} else {
							break;
						}
					}
					System.out.println("noise stopped");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	static int strWidth(Graphics2D g2, String s, int TABWIDTH) {
		if (s.indexOf("\t") < 0) {
			return g2.getFontMetrics().stringWidth(s);
		} else {
			int w = 0;
			int p1 = 0;
			while (true) {
				int p2 = s.indexOf("\t", p1);
				if (p2 < 0) {
					w += g2.getFontMetrics().stringWidth(s.substring(p1));
					break;
				} else {
					w += g2.getFontMetrics().stringWidth(s.substring(p1, p2));
					w += TABWIDTH;
					p1 = p2 + 1;
				}
			}
			return w;
		}
	}

	static String subs(RoSb sb, int a, int b) {
		return subs(sb.toString(), a, b);
	}

	static String subs(String sb, int a, int b) {
		if (a >= b) {
			return "";
		}
		if (a >= sb.length()) {
			return "";
		}
		if (a < 0 || b < 0) {
			return "";
		}
		if (b > sb.length()) {
			b = sb.length();
		}
		return sb.substring(a, b);
	}

	static String trimLeft(String s) {
		int i = 0;
		while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t'))
			i++;
		return i > 0 ? s.substring(i) : s;
	}
}

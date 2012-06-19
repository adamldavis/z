package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import neoe.util.ReadOnlyStrBuffer;


import com.adamldavis.z.gui.ColorManager;
import com.adamldavis.z.gui.ColorSetting;

public class PlainPage {
	class Cursor {
		void gotoLine() {
			String s = JOptionPane.showInputDialog(uiComp, "Goto Line");
			int line = -1;
			try {
				line = Integer.parseInt(s);
			} catch (Exception e) {
				line = -1;
			}
			if (line > roLines.getLinesize()) {
				line = -1;
			}
			if (line > 0) {
				line -= 1;
				sy = Math.max(0, line - showLineCnt / 2 + 1);
				cy = line;
				cx = 0;
				focusCursor();
			}
		}

		void moveDown() {
			cy += 1;
			if (cy >= roLines.getLinesize()) {
				if (rectSelectMode) {
					editRec.insertEmptyLine(cy);
					if (cx > 0) {
						editRec.insertInLine(cy, 0, U.spaces(cx));
					}
				} else {
					cy = roLines.getLinesize() - 1;
				}
			}
		}

		void moveEnd() {
			String line = roLines.getline(cy).toString();
			String lx = line.trim();
			int p1 = line.lastIndexOf(lx) + lx.length();
			if (cx < p1 || cx >= line.length()) {
				cx = p1;
			} else {
				cx = Integer.MAX_VALUE;
			}
		}

		void moveHome() {
			String line = roLines.getline(cy).toString();
			String lx = line.trim();
			int p1 = line.indexOf(lx);
			if (cx > p1 || cx == 0) {
				cx = p1;
			} else {
				cx = 0;
			}
		}

		void moveLeft() {
			cx -= 1;
			if (cx < 0) {
				if (cy > 0 && !ptSelection.isRectSelecting()) {
					cy -= 1;
					cx = roLines.getline(cy).length();
				} else {
					cx = 0;
				}
			}
		}

		void moveLeftWord() {
			ReadOnlyStrBuffer line = roLines.getline(cy);
			cx = Math.max(0, cx - 1);
			char ch1 = line.charAt(cx);
			while (cx > 0 && U.isSkipChar(line.charAt(cx), ch1)) {
				cx--;
			}
		}

		void movePageDown() {
			cy += showLineCnt;
			if (cy >= roLines.getLinesize()) {
				if (rectSelectMode) {
					String SP = U.spaces(cx);
					int cnt = cy - roLines.getLinesize() + 1;
					int p = roLines.getLinesize();
					for (int i = 0; i < cnt; i++) {
						editRec.insertEmptyLine(p);
						if (cx > 0) {
							editRec.insertInLine(p, 0, SP);
						}
					}
				} else {
					cy = roLines.getLinesize() - 1;
				}
			}
		}

		void movePageUp() {
			cy -= showLineCnt;
			if (cy < 0) {
				cy = 0;
			}
		}

		void moveRight() {
			cx += 1;
			if (ptSelection.isRectSelecting()) {
				if (cx > roLines.getline(cy).length()) {
					ptEdit.setLength(cy, cx);
				}
			} else if (cx > roLines.getline(cy).length()
					&& cy < roLines.getLinesize() - 1) {
				cy += 1;
				cx = 0;
			}
		}

		void moveRightWord() {
			ReadOnlyStrBuffer line = roLines.getline(cy);
			cx = Math.min(line.length(), cx + 1);
			if (cx < line.length()) {
				char ch1 = line.charAt(cx);
				while (cx < line.length() && U.isSkipChar(line.charAt(cx), ch1)) {
					cx++;
				}
			}
		}

		void moveUp() {
			cy -= 1;
			if (cy < 0) {
				cy = 0;
			}
		}

		void scroll(int amount) {
			sy += amount;
			if (sy >= roLines.getLinesize()) {
				sy = roLines.getLinesize() - 1;
			}
			if (sy < 0) {
				sy = 0;
			}
			uiComp.repaint();
		}

		void scrollHorizon(int amount) {
			sx += amount;
			if (sx < 0)
				sx = 0;
			uiComp.repaint();
		}

		void setSafePos(int x, int y) {
			cy = Math.max(0, Math.min(roLines.getLinesize() - 1, y));
			cx = Math.max(0, Math.min(roLines.getline(cy).length(), x));
		}
	}

	class EasyEdit {
		void append(String s) {
			cy = roLines.getLinesize() - 1;
			cx = roLines.getline(cy).length();
			ptEdit.insertString(s);
		}

		void deleteLine(int cy) {
			cx = 0;
			int len = roLines.getline(cy).length();
			if (len > 0) {
				editRec.deleteInLine(cy, 0, len);
			}
			editRec.deleteEmptyLine(cy);
		}

		void deleteRect(Rectangle r) {
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			if (rectSelectMode) {
				for (int i = y1; i <= y2; i++) {
					editRec.deleteInLine(i, x1, x2);
				}
				selectstartx = x1;
				selectstopx = x1;
			} else {
				if (y1 == y2 && x1 < x2) {
					editRec.deleteInLine(y1, x1, x2);
				} else if (y1 < y2) {
					editRec.deleteInLine(y1, x1, Integer.MAX_VALUE);
					editRec.deleteInLine(y2, 0, x2);
					for (int i = y1 + 1; i < y2; i++) {
						deleteLine(y1 + 1);
					}
					editRec.mergeLine(y1);
				}
			}
			cx = x1;
			cy = y1;
			if (y2 - y1 > 400) {
				U.gc();
			}
			focusCursor();
		}

		void insert(char ch) {
			if (ch == KeyEvent.VK_ENTER) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				}
				ReadOnlyStrBuffer sb = roLines.getline(cy);
				String indent = U.getIndent(sb.toString());
				String s = sb.substring(cx, sb.length());
				editRec.insertEmptyLine(cy + 1);
				editRec.insertInLine(cy + 1, 0, indent + U.trimLeft(s));
				editRec.deleteInLine(cy, cx, Integer.MAX_VALUE);
				cy += 1;
				cx = indent.length();
			} else if (ch == KeyEvent.VK_BACK_SPACE) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				} else {
					if (rectSelectMode) {
						if (cx > 0) {
							Rectangle r = ptSelection.getSelectRect();
							for (int i = r.y; i <= r.height; i++) {
								editRec.deleteInLine(i, cx - 1, cx);
							}
							cx--;
							selectstartx = cx;
							selectstopx = cx;
						}
					} else {
						if (cx > 0) {
							editRec.deleteInLine(cy, cx - 1, cx);
							cx -= 1;
						} else {
							if (cy > 0) {
								cx = roLines.getline(cy - 1).length();
								editRec.mergeLine(cy - 1);
								cy -= 1;
							}
						}
					}
				}
			} else if (ch == KeyEvent.VK_DELETE) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				} else {
					if (rectSelectMode) {
						Rectangle r = ptSelection.getSelectRect();
						for (int i = r.y; i <= r.height; i++) {
							editRec.deleteInLine(i, cx, cx + 1);
						}
						selectstartx = cx;
						selectstopx = cx;
					} else {
						if (cx < roLines.getline(cy).length()) {
							editRec.deleteInLine(cy, cx, cx + 1);
						} else {
							if (cy < roLines.getLinesize() - 1) {
								editRec.mergeLine(cy);
							}
						}
					}
				}
			} else if (ch == KeyEvent.VK_ESCAPE) {
				ptSelection.cancelSelect();
			} else {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				}
				if (rectSelectMode) {
					Rectangle r = ptSelection.getSelectRect();
					for (int i = r.y; i <= r.height; i++) {
						editRec.insertInLine(i, cx, "" + ch);
					}
					cx += 1;
					selectstartx = cx;
					selectstopx = cx;
				} else {
					editRec.insertInLine(cy, cx, "" + ch);
					cx += 1;
				}
			}
			focusCursor();
			if (!rectSelectMode) {
				ptSelection.cancelSelect();
			}
			uiComp.repaint();
		}

		void insertString(String s) {
			String[] ss = U.splitLine(s);

			if (rectSelectMode) {
				Rectangle rect = ptSelection.getSelectRect();
				int pi = 0;
				for (int iy = rect.y; iy <= rect.height; iy++) {
					String s1 = ss[pi];
					editRec.insertInLine(iy, cx, s1);
					pi++;
					if (pi >= ss.length)
						pi = 0;
				}
				if (ss.length == 1) {
					selectstartx += ss[0].length();
					selectstopx += ss[0].length();
					cx += ss[0].length();
					saveSelectionCancel = true;
				}
			} else {
				if (ss.length == 1) {
					editRec.insertInLine(cy, cx, ss[0]);
					cx += ss[0].length();
				} else {
					String rem = roLines.getInLine(cy, cx, Integer.MAX_VALUE);
					editRec.deleteInLine(cy, cx, Integer.MAX_VALUE);
					editRec.insertInLine(cy, cx, ss[0]);
					for (int i = 1; i < ss.length; i++) {
						editRec.insertEmptyLine(cy + i);
						editRec.insertInLine(cy + i, 0, ss[i]);
					}
					cy += ss.length - 1;
					cx = ss[ss.length - 1].length();
					editRec.insertInLine(cy, cx, rem);
				}
			}
			if (ss.length >= 5 && ui.comment == null) {
				new Thread() {
					public void run() {
						U.guessComment(PlainPage.this);
					}
				}.start();
			}
			focusCursor();
		}

		void moveLineLeft(int cy) {
			String s = roLines.getline(cy).toString();
			if (s.length() > 0 && (s.charAt(0) == '\t' || s.charAt(0) == ' ')) {
				editRec.deleteInLine(cy, 0, 1);
			}
			cx -= 1;
			if (cx < 0) {
				cx = 0;
			}
		}

		void moveLineRight(int cy) {
			editRec.insertInLine(cy, 0, "\t");
			cx += 1;
		}

		void moveRectLeft(int from, int to) {
			for (int i = from; i <= to; i++) {
				ptEdit.moveLineLeft(i);
			}
		}

		void moveRectRight(int from, int to) {
			for (int i = from; i <= to; i++) {
				ptEdit.moveLineRight(i);
			}
		}

		void setLength(int cy, int cx) {
			int oldLen = roLines.getline(cy).length();
			if (cx - oldLen > 0)
				editRec.insertInLine(cy, oldLen, U.spaces(cx - oldLen));
		}

		void setLines(List<StringBuffer> newLines) {
			lines = newLines;
			history.clear();
		}

		void setText(String s) {
			String[] ss = U.splitLine(s);
			List<StringBuffer> lines = new ArrayList<StringBuffer>();
			for (int i = 0; i < ss.length; i++) {
				lines.add(new StringBuffer(ss[i]));
			}
			if (lines.size() == 0) {
				lines.add(new StringBuffer("empty"));
				ptEdit.setLines(lines);
				ptSelection.selectAll();
			} else {
				ptEdit.setLines(lines);
			}
		}

		void wrapLines(int cx) throws Exception {
			int lineLen = 0;
			{
				int len = 0;
				String sb = roLines.getInLine(cy, 0, cx);
				for (int i = 0; i < sb.length(); i++) {
					len += (sb.charAt(i) > 255) ? 2 : 1;
				}
				lineLen = Math.max(10, len);
			}
			ui.message("wrapLine at " + lineLen);
			if (ptSelection.isSelected()) {
				ptSelection.cancelSelect();
			}
			List<StringBuffer> newtext = new ArrayList<StringBuffer>();
			for (int y = 0; y < lines.size(); y++) {
				if (lines.get(y).length() * 2 > lineLen) {
					int len = 0;
					ReadOnlyStrBuffer sb = roLines.getline(y);
					int start = 0;
					for (int i = 0; i < sb.length(); i++) {
						len += (sb.charAt(i) > 255) ? 2 : 1;
						if (len >= lineLen) {
							newtext.add(new StringBuffer(sb.substring(start,
									i + 1)));
							start = i + 1;
							len = 0;
						}
					}
					if (start < sb.length()) {
						newtext.add(new StringBuffer(sb.substring(start)));
					}
				} else {
					newtext.add(new StringBuffer(lines.get(y)));
				}
			}
			EditPanel ep = new EditPanel("");
			PlainPage p2 = ep.page;
			p2.workPath = workPath;
			p2.ptEdit.setLines(newtext);
			ep.openWindow();
			p2.ui.applyColorMode(ui.colorMode);
		}
	}

	class Paint {
		class Comment {
			void markBox(Graphics2D g2, int x, int y) {
				if (y >= sy && y <= sy + showLineCnt && x >= sx) {
					ReadOnlyStrBuffer sb = roLines.getline(y);
					int w1 = x > 0 ? U.strWidth(g2, sb.substring(sx, x),
							TABWIDTH) : 0;
					String c = sb.substring(x, x + 1);
					int w2 = U.strWidth(g2, c, TABWIDTH);
					g2.setColor(Color.WHITE);
					g2.drawRect(w1 - 1, (y - sy) * (lineHeight + lineGap) - 4,
							w2, 16);
					g2.setColor(colorNormal);
					g2.drawRect(w1, (y - sy) * (lineHeight + lineGap) - 3, w2,
							16);
					g2.drawString(c, w1, lineHeight + (y - sy)
							* (lineHeight + lineGap));
				}
			}

			void markGutLine(Graphics2D g2, int y1, int y2) {
				if (y1 > y2) {
					int t = y1;
					y1 = y2;
					y2 = t;
				}
				int o1 = y1, o2 = y2;
				y1 = Math.min(Math.max(y1, sy), sy + showLineCnt);
				y2 = Math.min(Math.max(y2, sy), sy + showLineCnt);
				int scy1 = 5 + (y1 - sy) * (lineHeight + lineGap);
				int scy2 = -8 + (y2 + 1 - sy) * (lineHeight + lineGap);
				g2.setColor(Color.WHITE);
				g2.drawLine(-6, scy1 - 1, -6, scy2 - 1);
				if (o1 == y1) {
					g2.setColor(Color.WHITE);
					g2.drawLine(-6, scy1 - 1, -1, scy1 - 1);
				}
				if (o2 == y2) {
					g2.setColor(Color.WHITE);
					g2.drawLine(-6, scy2 - 1, -1, scy2 - 1);
				}
				g2.setColor(Color.BLUE);
				g2.drawLine(-5, scy1, -5, scy2);
				if (o1 == y1) {
					g2.setColor(Color.BLUE);
					g2.drawLine(-5, scy1, 0, scy1);
				}
				if (o2 == y2) {
					g2.setColor(Color.BLUE);
					g2.drawLine(-5, scy2, 0, scy2);
				}
			}

			void pairMark(Graphics2D g2, int cx2, int cy2, char ch, char ch2,
					int inc) {
				int[] c1 = new int[] { cx2, cy2 };
				U.findchar(PlainPage.this, ch, inc, c1, ch2);
				if (c1[0] >= 0) {// found
					markBox(g2, cx2, cy2);
					markBox(g2, c1[0], c1[1]);
					if (cy2 != c1[1]) {
						markGutLine(g2, cy2, c1[1]);
					}
				}
			}
		}

		static final int TABWIDTH = 40;

		BufferedImage aboutImg;

		boolean aboutOn;
		int aboutY;
		boolean closed = false;
		Color colorBg, colorComment, colorComment2, colorCurrentLineBg,
				colorDigit, colorGutLine, colorGutNumber, colorKeyword;
		ColorManager colorMode;
		Color colorNormal = Color.BLACK;
		String comment = null;
		Comment commentor = new Comment();
		Dimension dim;
		Font font = new Font("Monospaced", Font.PLAIN, 12);
		int gutterWidth = 40;
		int lineGap = 5;
		int lineHeight = 10;

		boolean noise = false;

		int noisesleep = 500;

		float scalev = 1;

		boolean showLineNumbers = true;

		Paint() {
			applyColorMode(new ColorManager());
		}

		void applyColorMode(ColorManager cm) {
			colorMode = cm;
			colorBg = cm.getColorFor(ColorSetting.BACKGROUND);
			colorNormal = cm.getColorFor(ColorSetting.TEXT);
			colorKeyword = cm.getColorFor(ColorSetting.SELECTED_TASK);
			colorDigit = cm.getColorFor(ColorSetting.LINE);
			colorComment = cm.getColorFor(ColorSetting.TODO);
			colorGutNumber = cm.getColorFor(ColorSetting.TASK);
			colorGutLine = cm.getColorFor(ColorSetting.BACKGROUND);
			colorCurrentLineBg = cm.getColorFor(ColorSetting.BACKGROUND)
					.brighter();
			colorComment2 = cm.getColorFor(ColorSetting.TODO).darker().darker(); // shadow
		}

		void drawGutter(Graphics2D g2) {
			g2.setColor(colorGutNumber);
			if (!showLineNumbers) {
				return;
			}
			for (int i = 0; i < showLineCnt; i++) {
				if (sy + i + 1 > roLines.getLinesize()) {
					break;
				}
				g2.drawString("" + (sy + i + 1), 0, lineHeight
						+ (lineHeight + lineGap) * i);
			}
		}

		void drawReturn(Graphics2D g2, int w, int py) {
			g2.setColor(Color.red);
			g2.drawLine(w, py - lineHeight + font.getSize(), w + 3, py
					- lineHeight + font.getSize());
		}

		void drawSelect(Graphics2D g2, int y1, int x1, int x2) {
			int scry = y1 - sy;
			if (scry < showLineCnt) {
				String s = roLines.getline(y1).toString();
				if (sx > s.length()) {
					return;
				}
				s = U.subs(s, sx, s.length());
				x1 -= sx;
				x2 -= sx;
				if (x1 < 0) {
					x1 = 0;
				}
				if (x2 < 0) {
					x2 = 0;
				}
				if (x2 > s.length()) {
					x2 = s.length();
				}
				if (x1 > s.length()) {
					x1 = s.length();
				}
				if (x1 == x2) {
					int w1 = U.strWidth(g2, s.substring(0, x1), TABWIDTH);
					g2.fillRect(w1, scry * (lineHeight + lineGap), 3,
							lineHeight + lineGap);
				} else {
					int w1 = U.strWidth(g2, s.substring(0, x1), TABWIDTH);
					int w2 = U.strWidth(g2, s.substring(0, x2), TABWIDTH);
					g2.fillRect(w1, scry * (lineHeight + lineGap), (w2 - w1),
							lineHeight + lineGap);
				}
			}
		}

		int drawStringLine(Graphics2D g2, String s, int x, int y) {
			int w = 0;
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
						g2.drawImage(U.TabImg, x + w, y - lineHeight, null);
						w += TABWIDTH;
					}
					w += U.drawTwoColor(g2, s1, x + w, y, colorComment,
							colorComment2, 1);
					if (w > dim.width - gutterWidth) {
						break;
					}
				}
			} else {
				List<String> s1x = U.split(s);
				for (String s1 : s1x) {
					if (s1.equals("\t")) {
						g2.drawImage(U.TabImg, x + w, y - lineHeight, null);
						w += TABWIDTH;
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

		void drawTextLines(Graphics2D g2, int charCntInLine) {
			int y = sy;
			int py = lineHeight;
			for (int i = 0; i < showLineCnt; i++) {
				if (y >= roLines.getLinesize()) {
					break;
				}
				ReadOnlyStrBuffer sb = roLines.getline(y);
				if (sx < sb.length()) {
					int chari2 = Math.min(charCntInLine + sx, sb.length());
					String s = U.subs(sb, sx, chari2);
					g2.setColor(colorNormal);
					int w = drawStringLine(g2, s, 0, py); // U.strWidth(g2, s,
					// TABWIDTH);
					drawReturn(g2, w, py);
				} else {
					drawReturn(g2, 0, py);
				}
				y += 1;
				py += lineHeight + lineGap;
			}
		}

		void drawToolbar(Graphics2D g2) {

			long MSG_VANISH_TIME = 3000;
			String s1 = "<F1>:Help, " + (encoding == null ? "-" : encoding)
					+ (lineSep.equals("\n") ? ", U" : ", W") + ", Line:"
					+ roLines.getLinesize() + ", X:" + (cx + 1) + ", undo:"
					+ history.size() + ", " + (rectSelectMode ? "R, " : "")
					+ (fn == null ? "-" : fn);
			g2.setColor(Color.WHITE);
			g2.drawString(s1, 2, lineHeight + 2);
			g2.setColor(Color.BLACK);
			g2.drawString(s1, 1, lineHeight + 1);
			if (msg != null) {
				if (System.currentTimeMillis() - msgtime > MSG_VANISH_TIME) {
					msg = null;
				} else {
					int w = g2.getFontMetrics().stringWidth(msg);
					g2.setColor(new Color(0xee6666));
					g2.fillRect(dim.width - w, 0, dim.width, lineHeight
							+ lineGap);
					g2.setColor(Color.YELLOW);
					g2.drawString(msg, dim.width - w, lineHeight);
					U.repaintAfter(MSG_VANISH_TIME, uiComp);
				}
			}
		}

		void message(String s) {
			msg = s;
			msgtime = System.currentTimeMillis();
			uiComp.repaint();
			System.out.println(s);
		}

		void setNextColorMode() {
			// TODO: implement this?
		}

		void xpaint(Graphics g, Dimension size) {
			try {
				this.dim = size;
				if (!isCommentChecked) {// find comment pattern
					isCommentChecked = true;
					new Thread() {
						public void run() {
							U.guessComment(PlainPage.this);
						}
					}.start();
				}
				Graphics2D g2 = (Graphics2D) g;
				g2.setFont(font);
				showLineCnt = (int) ((size.height - toolbarHeight)
						/ (lineHeight + lineGap) / scalev);
				int charCntInLine = (int) ((size.width - gutterWidth)
						/ (lineHeight) * 2 / scalev);

				{ // change cy if needed
					if (cy >= roLines.getLinesize()) {
						cy = Math.max(0, roLines.getLinesize() - 1);
					}
				}
				// change sx if needed
				if (ptSelection.isRectSelecting()) {
					ptEdit.setLength(cy, cx);
				} else {
					cx = Math.min(roLines.getline(cy).length(), cx);
				}
				if (cx < sx) {
					sx = Math.max(0, cx - charCntInLine / 2);
				} else {
					if (U.strWidth(g2, U.subs(roLines.getline(cy), sx, cx),
							TABWIDTH) > size.width - lineHeight * 3) {
						sx = Math.max(0, cx - charCntInLine / 2);
						int xx = charCntInLine / 4;
						while (xx > 0
								&& U.strWidth(g2,
										U.subs(roLines.getline(cy), sx, cx),
										TABWIDTH) > size.width - lineHeight * 3) {
							sx = Math.max(0, cx - xx - 1);
							xx /= 2; // quick guess
						}
					}
				}
				if (my > 0)
					uiComp.grabFocus();
				// apply mouse click position
				if (my > 0 && my < toolbarHeight) {
				} else if (my > 0 && mx >= gutterWidth && my >= toolbarHeight) {
					mx -= gutterWidth;
					my -= toolbarHeight;
					mx = (int) (mx / scalev);
					my = (int) (my / scalev);
					cy = sy + my / (lineHeight + lineGap);// (int)((sy + my /
					// (lineHeight +
					// lineGap))/scalev);
					if (cy >= roLines.getLinesize()) {
						cy = roLines.getLinesize() - 1;
					}
					ReadOnlyStrBuffer sb = roLines.getline(cy);
					sx = Math.min(sx, sb.length());
					cx = sx
							+ U.computeShowIndex(sb.substring(sx), mx, g2,
									TABWIDTH);
					my = 0;
					ptSelection.mouseSelection(sb);
				}
				g2.setColor(colorBg);
				g2.fillRect(0, 0, size.width, size.height);
				if (noise) {
					U.paintNoise(g2, dim);
				}

				// draw toolbar
				drawToolbar(g2);
				// draw gutter
				g2.translate(0, toolbarHeight);
				g2.setColor(colorGutLine);
				g2.drawRect(gutterWidth, -1, dim.width - gutterWidth,
						dim.height - toolbarHeight);

				g2.scale(scalev, scalev);
				drawGutter(g2);
				// draw text
				g2.setClip(0, 0, dim.width, dim.height - toolbarHeight);
				g2.translate(gutterWidth / scalev, 0);

				{ // highlight current line
					int l1 = cy - sy;
					if (l1 >= 0 && l1 < showLineCnt) {
						g2.setColor(colorCurrentLineBg);
						g2.fillRect(0, l1 * (lineHeight + lineGap), size.width,
								lineHeight + lineGap - 1);
					}
				}
				g2.setColor(colorNormal);
				// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				// RenderingHints.VALUE_ANTIALIAS_ON);
				drawTextLines(g2, charCntInLine);
				if (rectSelectMode) {
					Rectangle r = ptSelection.getSelectRect();
					int x1 = r.x;
					int y1 = r.y;
					int x2 = r.width;
					int y2 = r.height;
					for (int i = y1; i <= y2; i++) {
						g2.setColor(Color.BLUE);
						g2.setXORMode(new Color(0xf0f030));
						drawSelect(g2, i, x1, x2);
					}
				} else {// select mode
					Rectangle r = ptSelection.getSelectRect();
					int x1 = r.x;
					int y1 = r.y;
					int x2 = r.width;
					int y2 = r.height;
					if (y1 == y2 && x1 < x2) {
						g2.setColor(Color.BLUE);
						g2.setXORMode(new Color(0xf0f030));
						drawSelect(g2, y1, x1, x2);
					} else if (y1 < y2) {
						g2.setColor(Color.BLUE);
						g2.setXORMode(new Color(0xf0f030));
						drawSelect(g2, y1, x1, Integer.MAX_VALUE);
						for (int i = y1 + 1; i < y2; i++) {
							drawSelect(g2, i, 0, Integer.MAX_VALUE);
						}
						drawSelect(g2, y2, 0, x2);
					}
				}
				if (true) {// (){}[]<> pair marking
					if (cx - 1 < roLines.getline(cy).length() && cx - 1 >= 0) {
						char c = roLines.getline(cy).charAt(cx - 1);
						String pair = "(){}[]<>";
						int p1 = pair.indexOf(c);
						if (p1 >= 0) {
							if (p1 % 2 == 0) {
								commentor.pairMark(g2, cx - 1, cy,
										pair.charAt(p1 + 1), c, 1);
							} else {
								commentor.pairMark(g2, cx - 1, cy,
										pair.charAt(p1 - 1), c, -1);
							}
						}
					}
				}
				// draw cursor
				if (cy >= sy && cy <= sy + showLineCnt) {
					g2.setXORMode(new Color(0x30f0f0));
					String s = U.subs(roLines.getline(cy), sx, cx);
					int w = U.strWidth(g2, s, TABWIDTH);
					g2.fillRect(w, (cy - sy) * (lineHeight + lineGap), 2,
							lineHeight);
				}

				if (aboutOn) {// about info
					g.setPaintMode();
					g.drawImage(aboutImg, 0, aboutY, null);
				}

			} catch (Throwable th) {
				th.printStackTrace();
				ui.message("Bug:" + th);
			}
		}

	}

	class Selection {
		void cancelSelect() {
			selectstartx = cx;
			selectstarty = cy;
			selectstopx = cx;
			selectstopy = cy;
		}

		void copySelected() {
			String s = getSelected();
			U.setClipBoard(s);
			ui.message("copied " + s.length());
		}

		void cutSelected() {
			copySelected();
			ptEdit.deleteRect(getSelectRect());
			cancelSelect();
		}

		String getSelected() {
			return roLines.getTextInRect(getSelectRect());
		}

		Rectangle getSelectRect() {
			int x1, x2, y1, y2;
			if (rectSelectMode) {
				y1 = selectstopy;
				y2 = selectstarty;
				x1 = selectstopx;
				x2 = selectstartx;
				if (y1 > y2) {
					int t = y1;
					y1 = y2;
					y2 = t;
				}
				if (x1 > x2) {
					int t = x1;
					x1 = x2;
					x2 = t;
				}
			} else {
				if (selectstopy < selectstarty) {
					y1 = selectstopy;
					y2 = selectstarty;
					x1 = selectstopx;
					x2 = selectstartx;
				} else {
					y2 = selectstopy;
					y1 = selectstarty;
					x2 = selectstopx;
					x1 = selectstartx;
					if (x1 > x2 && y1 == y2) {
						x1 = selectstopx;
						x2 = selectstartx;
					}
				}
			}
			return new Rectangle(x1, y1, x2, y2);
		}

		boolean isRectSelecting() {
			return mshift && rectSelectMode;
		}

		boolean isSelected() {
			Rectangle r = getSelectRect();
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			if (rectSelectMode) {
				return x1 < x2;
			} else {
				if (y1 == y2 && x1 < x2) {
					return true;
				} else if (y1 < y2) {
					return true;
				}
				return false;
			}
		}

		void mouseSelection(ReadOnlyStrBuffer sb) {
			if (mcount == 2) {
				int x1 = cx;
				int x2 = cx;
				if (sb.length() > x1
						&& Character.isJavaIdentifierPart(sb.charAt(x1)))
					while (x1 > 0
							&& Character
									.isJavaIdentifierPart(sb.charAt(x1 - 1))) {
						x1 -= 1;
					}
				if (sb.length() > x2
						&& Character.isJavaIdentifierPart(sb.charAt(x2)))
					while (x2 < sb.length() - 1
							&& Character
									.isJavaIdentifierPart(sb.charAt(x2 + 1))) {
						x2 += 1;
					}
				selectstartx = x1;
				selectstarty = cy;
				selectstopx = x2 + 1;
				selectstopy = cy;
			} else if (mcount == 3) {
				selectstartx = 0;
				selectstarty = cy;
				selectstopx = sb.length();
				selectstopy = cy;
			} else {
				if (mshift) {
					selectstopx = cx;
					selectstopy = cy;
				} else {
					cancelSelect();
				}
			}
		}

		void selectAll() {
			selectstartx = 0;
			selectstarty = 0;
			selectstopy = roLines.getLinesize() - 1;
			selectstopx = roLines.getline(selectstopy).length();
		}

		void selectLength(int x, int y, int length) {
			cx = x;
			cy = y;
			selectstartx = cx;
			selectstarty = cy;
			selectstopx = cx + length;
			selectstopy = cy;
			focusCursor();
		}
	}

	static final String REV = "$Rev: 129 $";
	static final String WINDOW_NAME = "neoeedit r"
			+ REV.substring(6, REV.length() - 2);

	Cursor cursor = new Cursor();

	int cx;
	int cy;
	BasicEdit editNoRec = new BasicEdit(false, this);
	BasicEdit editRec = new BasicEdit(true, this);
	String encoding;
	public String fn;
	History history;
	boolean ignoreCase = true;
	boolean isCommentChecked = false;
	List<StringBuffer> lines;
	public String lineSep = "\n";
	int mcount;
	String msg;
	long msgtime;
	boolean mshift;
	int mx, my;
	EasyEdit ptEdit = new EasyEdit();
	FindAndReplace ptFind = new FindAndReplace(this);
	Selection ptSelection = new Selection();
	boolean rectSelectMode = false;
	ReadonlyLines roLines = new ReadonlyLines(this);
	boolean saveSelectionCancel;
	int selectstartx, selectstarty, selectstopx, selectstopy;
	int showLineCnt;
	int sy, sx;
	int toolbarHeight = 25;
	Paint ui;
	EditPanel uiComp;
	String workPath;

	public PlainPage(EditPanel editor, File f) throws Exception {
		ui = new Paint();
		this.uiComp = editor;
		this.fn = f.getAbsolutePath();
		this.workPath = f.getParent();
		history = new History(this);
		U.readFile(this, f.getAbsolutePath());

	}

	public PlainPage(EditPanel editor, String text) throws Exception {
		ui = new Paint();
		this.uiComp = editor;
		history = new History(this);
		ptEdit.setText(text);
	}

	void focusCursor() {
		if (cy < sy) {
			sy = Math.max(0, cy - showLineCnt / 2 + 1);
		}
		if (showLineCnt > 0) {
			if (sy + showLineCnt - 1 < cy) {
				sy = Math.max(0, cy - showLineCnt / 2 + 1);
			}
		}
	}

	public void keyPressed(KeyEvent env) {
		history.beginAtom();
		try {
			mshift = env.isShiftDown();
			// System.out.println("press " + env.getKeyChar());
			int ocx = cx;
			int ocy = cy;
			int kc = env.getKeyCode();
			if (kc == KeyEvent.VK_F1) {
				U.showHelp(ui, uiComp);
			} else if (kc == KeyEvent.VK_F2) {
				U.saveAs(this);
			} else if (kc == KeyEvent.VK_F3) {
				ptFind.findNext();
			} else if (kc == KeyEvent.VK_F5) {
				U.reloadWithEncodingByUser(fn, this);
			}
			if (env.isAltDown()) {
				if (kc == KeyEvent.VK_LEFT) {
					ptEdit.moveLineLeft(cy);
					focusCursor();
				} else if (kc == KeyEvent.VK_RIGHT) {
					ptEdit.moveLineRight(cy);
					focusCursor();
				} else if (kc == KeyEvent.VK_BACK_SLASH) {
					rectSelectMode = !rectSelectMode;
				} else if (kc == KeyEvent.VK_N) {
					ui.noise = !ui.noise;
					if (ui.noise) {
						U.startNoiseThread(ui, uiComp);
					}
				} else if (kc == KeyEvent.VK_S) {
					if (lineSep.equals("\n"))
						lineSep = "\r\n";
					else
						lineSep = "\n";
				} else if (kc == KeyEvent.VK_W) {
					ptEdit.wrapLines(cx);
					focusCursor();
				} else if (kc == KeyEvent.VK_L) {
					ui.showLineNumbers = !ui.showLineNumbers;
					focusCursor();
				} else if (kc == KeyEvent.VK_J) {
					U.runScript(this);
				} else if (kc == KeyEvent.VK_D) {
					U.runScriptOnDir(this.workPath);
				} else if (kc == KeyEvent.VK_PAGE_UP) {
					cx = Math.max(0, cx - uiComp.getWidth() / 10);
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_DOWN) {
					cx = cx + uiComp.getWidth() / 10;
					focusCursor();
				} else if (kc == KeyEvent.VK_C) {
					ui.setNextColorMode();
					ui.applyColorMode(ui.colorMode);
				}
			} else if (env.isControlDown()) {
				if (kc == KeyEvent.VK_C) {
					ptSelection.copySelected();
				} else if (kc == KeyEvent.VK_V) {
					if (ptSelection.isSelected()) {
						ptEdit.deleteRect(ptSelection.getSelectRect());
					}
					ptEdit.insertString(U.getClipBoard());
				} else if (kc == KeyEvent.VK_X) {
					ptSelection.cutSelected();
				} else if (kc == KeyEvent.VK_A) {
					ptSelection.selectAll();
				} else if (kc == KeyEvent.VK_D) {
					if (ptSelection.isSelected()) {
						ptEdit.deleteRect(ptSelection.getSelectRect());
					} else {
						ptEdit.deleteLine(cy);
					}
					focusCursor();
				} else if (kc == KeyEvent.VK_O) {
					U.openFile(this);
				} else if (kc == KeyEvent.VK_N) {
					EditPanel ep = new EditPanel("empty");
					ep.openWindow();
					ep.page.workPath = this.workPath;
					ep.page.ptSelection.selectAll();
					ep.page.ui.applyColorMode(ui.colorMode);
				} else if (kc == KeyEvent.VK_S) {
					if (U.saveFile(this)) {
						System.out.println("saved");
						ui.message("saved");
					}
				} else if (kc == KeyEvent.VK_L) {
					cursor.gotoLine();
				} else if (kc == KeyEvent.VK_Z) {
					history.undo();
				} else if (kc == KeyEvent.VK_F) {
					ptFind.showFindDialog();
				} else if (kc == KeyEvent.VK_Y) {
					history.redo();
				} else if (kc == KeyEvent.VK_W) {
					U.closePage(this);
				} else if (kc == KeyEvent.VK_E) {
					U.setEncodingByUser(this, "Set Encoding:");
				} else if (kc == KeyEvent.VK_PAGE_UP) {
					cy = 0;
					cx = 0;
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_DOWN) {
					cy = roLines.getLinesize() - 1;
					cx = 0;
					focusCursor();
				} else if (kc == KeyEvent.VK_R) {
					U.removeTrailingSpace(PlainPage.this);
				} else if (kc == KeyEvent.VK_LEFT) {
					cursor.moveLeftWord();
					focusCursor();
				} else if (kc == KeyEvent.VK_RIGHT) {
					cursor.moveRightWord();
					focusCursor();
				} else if (kc == KeyEvent.VK_UP) {
					sy = Math.max(0, sy - 1);
				} else if (kc == KeyEvent.VK_DOWN) {
					sy = Math.min(sy + 1, roLines.getLinesize() - 1);
				} else if (kc == KeyEvent.VK_0) {
					ui.scalev = 1;
				} else if (kc == KeyEvent.VK_G) {
					if (cy < lines.size())
						U.gotoFileLine(roLines.getline(cy).toString());
				} else if (kc == KeyEvent.VK_H) {
					U.openFileHistory();
				} else if (kc == KeyEvent.VK_P) {
					new Print(PlainPage.this).printPages();
				} else if (kc == KeyEvent.VK_ENTER) {
					cursor.moveEnd();
					focusCursor();
				}
			} else {
				if (kc == KeyEvent.VK_LEFT) {
					cursor.moveLeft();
					focusCursor();
				} else if (kc == KeyEvent.VK_RIGHT) {
					cursor.moveRight();
					focusCursor();
				} else if (kc == KeyEvent.VK_UP) {
					cursor.moveUp();
					focusCursor();
				} else if (kc == KeyEvent.VK_DOWN) {
					cursor.moveDown();
					focusCursor();
				} else if (kc == KeyEvent.VK_HOME) {
					cursor.moveHome();
					focusCursor();
				} else if (kc == KeyEvent.VK_END) {
					cursor.moveEnd();
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_UP) {
					cursor.movePageUp();
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_DOWN) {
					cursor.movePageDown();
					focusCursor();
				} else if (kc == KeyEvent.VK_CONTROL || kc == KeyEvent.VK_SHIFT
						|| kc == KeyEvent.VK_ALT) {
					return;
				}
			}
			boolean cmoved = !(ocx == cx && ocy == cy);
			if (cmoved) {
				if (env.isShiftDown()) {
					selectstopx = cx;
					selectstopy = cy;
				} else {
					if (saveSelectionCancel) {
						saveSelectionCancel = false;
					} else {
						ptSelection.cancelSelect();
					}
				}
			}
			uiComp.repaint();
		} catch (Exception e) {
			ui.message("err:" + e);
			uiComp.repaint();
			e.printStackTrace();
		}
		history.endAtom();
	}

	public void keyReleased(KeyEvent env) {
	}

	public void keyTyped(KeyEvent env) {
		history.beginAtom();
		char kc = env.getKeyChar();
		if (kc == KeyEvent.VK_TAB && env.isShiftDown()) {
			Rectangle r = ptSelection.getSelectRect();
			if (r.y < r.height) {
				ptEdit.moveRectLeft(r.y, r.height);
			} else {
				ptEdit.moveLineLeft(cy);
			}
		} else if (kc == KeyEvent.VK_TAB && !env.isShiftDown()
				&& selectstarty != selectstopy && !rectSelectMode) {
			Rectangle r = ptSelection.getSelectRect();
			ptEdit.moveRectRight(r.y, r.height);
		} else if (env.isControlDown() || env.isAltDown()) {
			// ignore
		} else {
			ptEdit.insert(kc);
		}
		history.endAtom();
	}

	public void mouseClicked(MouseEvent evt) {
		int my = evt.getY();
		if (my > 0 && my < toolbarHeight) {
			if (fn != null) {
				U.setClipBoard(fn);
				ui.message("filename copied");
				my = 0;
				uiComp.repaint();
			} else {
				try {
					if (U.saveFile(this)) {
						ui.message("saved");
					}
				} catch (Exception e) {
					ui.message("err:" + e);
					e.printStackTrace();
				}
			}
		} else {
			int mx = evt.getX();
			if (mx > 0 && mx < ui.gutterWidth) {
				cursor.gotoLine();
				uiComp.repaint();
			}
		}
	}

	public void mouseDragged(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = true;
		uiComp.repaint();
	}

	public void mousePressed(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = env.isShiftDown();
		mcount = env.getClickCount();
		uiComp.repaint();
		// System.out.println("m press");
	}

	public void mouseWheelMoved(MouseWheelEvent env) {
		int amount = env.getWheelRotation() * env.getScrollAmount();
		if (env.isControlDown()) {// scale
			U.scale(amount, ui);
		} else if (env.isAltDown()) {// horizon scroll
			cursor.scrollHorizon(amount);
		} else {// scroll
			cursor.scroll(amount);
		}

	}

	public void xpaint(Graphics g, Dimension size) {
		ui.xpaint(g, size);
	}

}

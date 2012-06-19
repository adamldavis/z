package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Date;
import java.util.List;

import neoe.ne.PlainPage.Paint;

class Print implements Printable {
	Color colorLineNumber = new Color(0x30C200), colorGutterLine = new Color(
			0x30C200), colorNormal = Color.BLACK, colorDigit = new Color(
			0xA8002A), colorKeyword = new Color(0x0099CC),
			colorHeaderFooter = new Color(0x8A00B8), colorComment = new Color(
					200, 80, 50);
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
		g2.drawLine(w, py - lineHeight + font.getSize(), w + 3, py - lineHeight
				+ font.getSize());
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

	void drawTextLine(Graphics2D g2, String s, int x0, int y0, int charCntInLine) {
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
							- U.strWidth(g2, s, TAB_WIDTH_PRINT) - 2, lineGap
							+ lineHeight);
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
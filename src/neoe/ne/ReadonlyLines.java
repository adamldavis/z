package neoe.ne;

import java.awt.Rectangle;

import neoe.util.ReadOnlyStrBuffer;


class ReadonlyLines {
	PlainPage page;

	ReadonlyLines(PlainPage page) {
		this.page = page;
	}

	String getInLine(int y, int x1, int x2) {
		ReadOnlyStrBuffer sb = getline(y);
		if (x2 > sb.length()) {
			x2 = sb.length();
		}
		if (x1 > sb.length()) {
			x1 = sb.length();
		}
		return sb.substring(x1, x2);
	}

	ReadOnlyStrBuffer getline(int i) {
		return new ReadOnlyStrBuffer(page.lines.get(i));
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
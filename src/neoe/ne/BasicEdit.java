package neoe.ne;

import java.util.List;

import neoe.ne.U.BasicAction;

class BasicEdit {
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
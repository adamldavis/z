package neoe.ne;

class HistoryCell {
	U.BasicAction action;
	PlainPage page;
	String s1;
	int x1, x2, y1, y2;

	public HistoryCell(U.BasicAction action, int x1, int x2, int y1, int y2,
			String s1) {
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

	BasicEdit editNoRec() {
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
		return "HistoryInfo [action=" + action + ", x1=" + x1 + ", x2=" + x2
				+ ", y1=" + y1 + ", y2=" + y2 + ", s1=" + s1 + "]\n";
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

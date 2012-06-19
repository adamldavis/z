package neoe.ne;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Handles undo/redo history. */
class History {
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
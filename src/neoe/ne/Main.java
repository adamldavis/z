package neoe.ne;

import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {

		if (args.length > 0) {
			File f = new File(args[0]);
			U.openFile(f);
		} else {
			EditPanel editor = new EditPanel("neoeedit");
			editor.page.ptSelection.selectAll();
			editor.openWindow();
		}

	}
}

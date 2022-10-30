package net.github.douwevos.justflat.logging;

public class Log {

	private static final long startNs = System.nanoTime();
	private static final Log log = new Log();

	private int indent;

	public static Log instance() {
		return log;
	}

	public void debug(String text, Object... args) {
		log(text, args);
	}

	public void debug2(String text, Object... args) {
		log1(text, args);
	}

	public void log(String text, Object... args) {
		return;
	}

	public void log1(String text, Object... args) {
		StringBuilder buf = new StringBuilder();
		buf.append(System.nanoTime() - startNs).append(" ");
		for (int idx = 0; idx < indent; idx++) {
			buf.append("  ");
		}
		int argIdx = 0;
		int argOf = 0;
		while (true) {
			int nextArgOff = text.indexOf("{}", argOf);
			if (nextArgOff >= 0) {
				buf.append(text.substring(argOf, nextArgOff));
				Object arg = args[argIdx++];
				buf.append(arg == null ? "null" : arg);
				nextArgOff += 2;
			} else {
				buf.append(text.substring(argOf));
				break;
			}
			argOf = nextArgOff;
		}

		System.err.println(buf.toString());
		System.err.flush();
	}

	public int indent() {
		int result = indent;
		indent++;
		return result;
	}

	public void indentAt(int i) {
		indent = i;
	}

	public void dedent() {
		indent--;
	}

	public boolean isDebugEnabled() {
		return true;
	}

}

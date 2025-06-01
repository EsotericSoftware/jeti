
package com.esotericsoftware.jeti;

import java.io.PrintWriter;
import java.io.StringWriter;

/** @author Nathan Sweet <misc@n4te.com> */
public class Log {
	/** No logging at all. */
	static public final int LEVEL_NONE = 6;
	/** Critical errors. The application may no longer work correctly. */
	static public final int LEVEL_ERROR = 5;
	/** Important warnings. The application will continue to work correctly. */
	static public final int LEVEL_WARN = 4;
	/** Informative messages. Typically used for deployment. */
	static public final int LEVEL_INFO = 3;
	/** Debug messages. This level is useful during development. */
	static public final int LEVEL_DEBUG = 2;
	/** Trace messages. A lot of information is logged, so this level is usually only needed when debugging a problem. */
	static public final int LEVEL_TRACE = 1;

	static private int level = LEVEL_INFO;
	static public boolean ERROR = level <= LEVEL_ERROR;
	static public boolean WARN = level <= LEVEL_WARN;
	static public boolean INFO = level <= LEVEL_INFO;
	static public boolean DEBUG = level <= LEVEL_DEBUG;
	static public boolean TRACE = level <= LEVEL_TRACE;

	static private Logger logger = new Logger();

	static public void setLogger (Logger logger) {
		Log.logger = logger;
	}

	static public int getLevel () {
		return level;
	}

	static public void set (int level) {
		Log.level = level;
		ERROR = level <= LEVEL_ERROR;
		WARN = level <= LEVEL_WARN;
		INFO = level <= LEVEL_INFO;
		DEBUG = level <= LEVEL_DEBUG;
		TRACE = level <= LEVEL_TRACE;
	}

	static public void NONE () {
		set(LEVEL_NONE);
	}

	static public void ERROR () {
		set(LEVEL_ERROR);
	}

	static public void WARN () {
		set(LEVEL_WARN);
	}

	static public void INFO () {
		set(LEVEL_INFO);
	}

	static public void DEBUG () {
		set(LEVEL_DEBUG);
	}

	static public void TRACE () {
		set(LEVEL_TRACE);
	}

	static public void error (String message, Throwable ex) {
		if (ERROR) logger.log(LEVEL_ERROR, message, ex);
	}

	static public void error (String message) {
		if (ERROR) logger.log(LEVEL_ERROR, message, null);
	}

	static public void warn (String message, Throwable ex) {
		if (WARN) logger.log(LEVEL_WARN, message, ex);
	}

	static public void warn (String message) {
		if (WARN) logger.log(LEVEL_WARN, message, null);
	}

	static public void info (String message, Throwable ex) {
		if (INFO) logger.log(LEVEL_INFO, message, ex);
	}

	static public void info (String message) {
		if (INFO) logger.log(LEVEL_INFO, message, null);
	}

	static public void debug (String message, Throwable ex) {
		if (DEBUG) logger.log(LEVEL_DEBUG, message, ex);
	}

	static public void debug (String message) {
		if (DEBUG) logger.log(LEVEL_DEBUG, message, null);
	}

	static public void trace (String message, Throwable ex) {
		if (TRACE) logger.log(LEVEL_TRACE, message, ex);
	}

	static public void trace (String message) {
		if (TRACE) logger.log(LEVEL_TRACE, message, null);
	}

	private Log () {
	}

	static public class Logger {
		private final long firstLogTime = System.currentTimeMillis();

		public void log (int level, String message, Throwable ex) {
			StringBuilder builder = new StringBuilder(256);

			long time = System.currentTimeMillis() - firstLogTime;
			long minutes = time / (1000 * 60);
			long seconds = time / (1000) % 60;
			if (minutes <= 9) builder.append('0');
			builder.append(minutes);
			builder.append(':');
			if (seconds <= 9) builder.append('0');
			builder.append(seconds);

			builder.append(switch (level) {
			case LEVEL_TRACE -> " TRACE: ";
			case LEVEL_DEBUG -> " DEBUG: ";
			case LEVEL_INFO -> "  INFO: ";
			case LEVEL_WARN -> "  WARN: ";
			default -> " ERROR: ";
			});

			builder.append(message);

			if (ex != null) {
				StringWriter writer = new StringWriter(256);
				ex.printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString().trim());
				builder.append('\n');
			}

			print(builder.toString());
		}

		protected void print (String message) {
			System.out.println(message);
		}
	}
}

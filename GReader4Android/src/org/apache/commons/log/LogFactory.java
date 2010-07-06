package org.apache.commons.log;

public class LogFactory implements Log {

	private static Log log;

	public static Log getLog(Object o) {
		if (log == null) {
			log = new LogFactory();
		}
		return log;
	}

	@Override
	public void debug(Object arg0) {
		System.out.println(arg0);
	}

	@Override
	public void debug(Object arg0, Throwable arg1) {
		System.out.println(arg0);
	}

	@Override
	public void error(Object arg0) {
		System.out.println(arg0);
	}

	@Override
	public void error(Object arg0, Throwable arg1) {
		System.out.println(arg0);
	}

	@Override
	public void fatal(Object arg0) {
		System.out.println(arg0);
	}

	@Override
	public void fatal(Object arg0, Throwable arg1) {
		System.out.println(arg0);
	}

	@Override
	public void info(Object arg0) {
		System.out.println(arg0);
	}

	@Override
	public void info(Object arg0, Throwable arg1) {
		System.out.println(arg0);
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public boolean isFatalEnabled() {
		return false;
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public void trace(Object arg0) {
		System.out.println(arg0);
	}

	@Override
	public void trace(Object arg0, Throwable arg1) {
		System.out.println(arg0);
	}

	@Override
	public void warn(Object arg0) {
		System.out.println(arg0);
	}

	@Override
	public void warn(Object arg0, Throwable arg1) {
		System.out.println(arg0);
	}
}

/*
 * Logger.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 7/17/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

/**
 * The Log class provides logging semantics similar to Android's android.util.Log class.
 * All logging performed by the SmartDigitalHeadset library funnels through this class and
 * as such may be ignored entirely (in which case it is written nowhere), or managed by
 * a class registered as the Log.Delegate which will receive log messages when they are
 * called.
 *
 * @author Brian Doyle on 7/17/15.
 */
public final class Log {
	/**
	 * Set sLogDelegate to a class implementing the Log.Delegate protocol to receive
	 * messages logged by the SmartDigitalHeadset library.  If sLogDelegate is not set
	 * all log messages will be ignored.
	 */
	public static Delegate			sLogDelegate;

	private final static String		kLogTag = "SmartDigitalHeadset";

	/**
	 * Various level constants describing the Log message severity.  Values correspond to constants
	 * defined by android.util.Log.
	 */
	public enum Level {
		/**
		 * For debug messages, can be ignored.
		 */
		Debug,
		/**
		 * For error messages, usually reported after something goes severely wrong.
		 */
		Error,
		/**
		 * For informational messages, can be ignored.
		 */
		Info,
		/**
		 * For verbose messages, can be ignored.
		 */
		Verbose,
		/**
		 * For warning messages, usually reported when something goes wrong but we were able to
		 * recover from.
		 */
		Warning
	}

	private Log() { }

	/**
	 * The class implementing the Log.Delegate protocol and registering itself via the sLogDelegate
	 * member will receive messages logged by the SmartDigitalHeadset library.
	 */
	public interface Delegate {
		/**
		 * log() is called each time the SmartDigitalHeadset library writes a message to the log.
		 *
		 * @param level The level the message was logged at (Debug, Warning, etc.)
		 * @param tag A tag that can be passed to android.util.Log if desired.  Currently "SmartDigitalHeadset".
		 * @param message The message that was logged.
		 */
		void log( Level level, String tag, String message );
	}

	/**
	 * Delivers a Debug-level message to the Log delegate by calling <pre>sLogDelegate.log( Level.Debug, "SmartDigitalHeadset", message );</pre>
	 *
	 * @param message The message to log
	 */
	public static void d( String message ) {
		if ( sLogDelegate != null ) sLogDelegate.log( Level.Debug, kLogTag, message );
	}
	/**
	 * Delivers an Error-level message to the Log delegate by calling <pre>sLogDelegate.log( Level.Error, "SmartDigitalHeadset", message );</pre>
	 *
	 * @param message The message to log
	 */
	public static void e( String message ) {
		if ( sLogDelegate != null ) sLogDelegate.log( Level.Error, kLogTag, message );
	}
	/**
	 * Delivers an Info-level message to the Log delegate by calling <pre>sLogDelegate.log( Level.Info, "SmartDigitalHeadset", message );</pre>
	 *
	 * @param message The message to log
	 */
	public static void i( String message ) {
		if ( sLogDelegate != null ) sLogDelegate.log( Level.Info, kLogTag, message );
	}
	/**
	 * Delivers a Verbose-level message to the Log delegate by calling <pre>sLogDelegate.log( Level.Verbose, "SmartDigitalHeadset", message );</pre>
	 *
	 * @param message The message to log
	 */
	public static void v( String message ) {
		if ( sLogDelegate != null ) sLogDelegate.log( Level.Verbose, kLogTag, message );
	}
	/**
	 * Delivers a Warning-level message to the Log delegate by calling <pre>sLogDelegate.log( Level.Warning, "SmartDigitalHeadset", message );</pre>
	 *
	 * @param message The message to log
	 */
	public static void w( String message ) {
		if ( sLogDelegate != null ) sLogDelegate.log( Level.Warning, kLogTag, message );
	}
}

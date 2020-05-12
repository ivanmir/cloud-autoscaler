package isv.ateam.neo.neoautoscaler.monitor;

import isv.ateam.neo.neoautoscaler.apis.PlatformAPIHandler;

/**
 * This is the central singleton class that controls the scaling.
 */
public class ScaleCentral {

	private static Monitor monitor = null;
	private static Thread monitorThread = null;

	private ScaleCentral() {
	}
	
	public static void startMonitor(PlatformAPIHandler api) {
		if (monitor == null) {
			monitor = new Monitor(api);
			monitorThread = new Thread(monitor);
			monitorThread.start();
		}
	}
	
	public static String stopMonitor() {
		try {
			if (monitorThread != null) {
				monitor.stop();
				monitorThread.join();
				return "Monitor Thread Stopped Succefully";
			}
			return "Nothing is monitored now!";
		} catch (InterruptedException e) {
			return "Problem stopping the thread" + e.getCause();
		} finally {
			monitor = null;
		}
	}

}

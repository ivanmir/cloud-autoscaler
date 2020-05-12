package isv.ateam.neo.neoautoscaler.monitor;

import java.util.HashMap;
import java.util.Map;

import isv.ateam.neo.neoautoscaler.config.DestinationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleEngine {
	public static Map<String, Integer> upscaleThresholds = new HashMap<>();
	public static Map<String, Integer> downscaleThresholds = new HashMap<>();
	public static String DEFAULT_METRIC = "CPU Load";
	public static String DEFAULT_METRIC_VALUE = "25,50";

	private static enum ruleNames { UPSCALE, DOWNSCALE };
	private static String CPU_LOAD = "CPU Load";
	private static String BUSY_THREADS = "Busy Threads";
	
	
	/**
	 * Helper private method that runs a rule and returns true or false, in case it is fulfilled or not, respectively.
	 * @param params a list of parameters that should be checked against the rule
	 * @param ruleName the name of the rule - can be one of {@link ruleNames} enums
	 * @return boolean true if the rule was fulfilled or false otherwise
	 */
	private static boolean runRule(Map<String, Integer> params, ruleNames ruleName) {
		if (ruleName == ruleNames.UPSCALE) {
			for (String paramName : upscaleThresholds.keySet()) {
				if (params.containsKey(paramName)) {
					if (params.get(paramName) == null || params.get(paramName) < upscaleThresholds.get(paramName)) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		} else if (ruleName == ruleNames.DOWNSCALE) {
			for (String paramName : downscaleThresholds.keySet()) {
				if (params.containsKey(paramName)) {
					if (params.get(paramName).equals(null) || params.get(paramName) > downscaleThresholds.get(paramName)) {
						return false;
					}
				} else {
					return false;
				}
				
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Public method that triggers the run of the upscale rules for all the monitored metrics
	 * @param pidMetrics a list of metrics for each instance of the current application
	 * @return boolean true if the rule was fulfilled or false otherwise
	 */
	public static boolean upScaleRuleFulfilled(Map<String, Map<String, Integer>> pidMetrics) {
		boolean result= false;
		for (String pid : pidMetrics.keySet()) {
			if (runRule(pidMetrics.get(pid), ruleNames.UPSCALE)) {
				log.debug("Upscale rule fulfilled for pid: {}" + pid);
				result = true;
				break;
			} else {
				log.debug("Upscale rule NOT fulfilled for pid: {}" + pid);
			}
		}
		return result;
	}
	
	/**
	 * Public method that triggers the run of the downscale rules for all the monitored metrics
	 * @param pidMetrics a list of metrics for each instance of the current application
	 * @return boolean true if the rule was fulfilled or false otherwise
	 */
	public static boolean downScaleRuleFulfilled(Map<String, Map<String, Integer>> pidMetrics) {
		boolean result= false;
		for (String pid : pidMetrics.keySet()) {
			if (runRule(pidMetrics.get(pid), ruleNames.DOWNSCALE)) {
				log.debug("Downscale rule fulfilled for pid: {}" + pid);
				result = true;
				break;
			}
			log.debug("Downscale rule NOT fulfilled for pid: {}" + pid);
		}
		return result;
	}
	
	/**
	 * Method that should be used only to load the rule thresholds from the default location
	 * as specified in the DEFAULT_THRESHOLDS_FILE_LOCATION (should be "/WEB-INF/lib/params.properties")
	 * @param servletContext the servlet context used for opening the resource file
	 */
	public static void loadRuleThresholds() {
		upscaleThresholds.put(CPU_LOAD, Integer.valueOf(DestinationConfig.cpuLoad.substring(0, DestinationConfig.cpuLoad.indexOf(','))));
		downscaleThresholds.put(CPU_LOAD, Integer.valueOf(DestinationConfig.cpuLoad.substring(DestinationConfig.busyThreads.indexOf(',') + 1)));

		upscaleThresholds.put(BUSY_THREADS, Integer.valueOf(DestinationConfig.busyThreads.substring(0, DestinationConfig.cpuLoad.indexOf(','))));
		downscaleThresholds.put(BUSY_THREADS, Integer.valueOf(DestinationConfig.busyThreads.substring(DestinationConfig.busyThreads.indexOf(',') + 1)));
	}
}

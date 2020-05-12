package isv.ateam.neo.neoautoscaler.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import isv.ateam.neo.neoautoscaler.apis.PlatformAPIHandler;
import isv.ateam.neo.neoautoscaler.monitor.MetricUtils;
import isv.ateam.neo.neoautoscaler.monitor.RuleEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * Class containing the cached data for the monitored application plus some utilities.
 */
@Slf4j
public class AppData {
	protected static Map<String, String> monMap = new HashMap<>();
	protected static Map<String, Map<String, Integer>> parsedMonMap = new HashMap<>();

	protected String account;
	protected String app;
	
	private static AppProps props = new AppProps();
	//private static JsonElement metrics;
	
	public static String simpleMetrics;
	
	public AppData() {
	}

	public AppData(String account, String app, AppProps appData) {
		this.account = account;
		this.app = app;
		AppData.props = appData;
	}
	
	/**
	 * Method that updates the cached metrics from HCP monitoring API calls.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * 
	 */
	public synchronized static void updateCurrentMetrics(PlatformAPIHandler api) throws InterruptedException, ExecutionException  {
		monMap.clear();
		parsedMonMap.clear();
		simpleMetrics = "";
		for (String pid : AppProps.getPids()) {
			
			JsonElement metrics = null;
			String result = "";

			try {
				result = api.readCurrentMetricsForProcess(pid).get();
			} catch (Exception e) {
				log.debug("Exception: {}", e.getCause());
			}
			try {
				metrics = JsonParser.parseString(result);
			} catch (JsonParseException e1) {
				//Probably HTTP error 404 (not-found) because the app might've been stopped
			}
			
			if (!result.equalsIgnoreCase("")) {
				JsonArray metricsArray = metrics.getAsJsonArray();
				String defaultMetric = MetricUtils.getMetricValue(metricsArray, RuleEngine.DEFAULT_METRIC);
				simpleMetrics += "Process: [" + pid + "]. Metric: " + RuleEngine.DEFAULT_METRIC + ": ["+ defaultMetric + "]<br>";
				monMap.put(pid, result);
			}
		}
	}
	
	public void setAccount(String account) {
		this.account = account;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public static void setProps(AppProps props) {
		AppData.props = props;
	}

	public static AppProps getAppInfo() {
		return props;
	}
	public Set<String> getPids() {
		return AppProps.getPids();
	}

	public Map<String, String> getMetrics() {
		return monMap;
	}
	
	public static Map<String, Map<String, Integer>> getParsedMetrics() {
		return parsedMonMap;
	}
}

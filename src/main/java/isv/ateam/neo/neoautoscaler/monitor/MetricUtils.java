package isv.ateam.neo.neoautoscaler.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MetricUtils {

	protected static String DEFAULT_METRICS = "metrics";//"default-metrics";
	//protected static String USER_DEFINED_METRICS = "user-defined-metrics";
	
	/**
	 * This method returns the default metrics returned by the monitoring API 
	 * (this needs to be adapted for user-defined metrics)
	 * @param stringObj a stringified JSON
	 * @return JSONArray a JSON array of the metrics
	 */
	public static JsonArray getDefaultMetrics(String stringObj, String metricsType) {
		try {
			JsonArray parsedString = JsonParser.parseString(stringObj).getAsJsonArray();
			JsonArray result = new JsonArray();
			parsedString.forEach(a -> {
				result.add(a.getAsJsonObject().get("metrics"));	
			});
			return result;
		} catch (Exception e) {
			// just return an empty json array
			return new JsonArray();
		}
	}
	
	
	/**
	 * Method that returns a (integer) value of a specified metric 
	 * @param metricsArray - a JSON array of metrics
	 * @param metricName - a String containing the name of the metric whose value is sought
	 * @return int - the value of the metric
	 */
	public static String getMetricValue(JsonArray metricsArray, String metricName) {

		try {
			
			JsonArray objArray = metricsArray.getAsJsonArray();
			JsonObject obj = objArray.get(0).getAsJsonObject();
			JsonElement jMetrics = obj.get("metrics");
			JsonArray metrics = jMetrics.getAsJsonArray();
			
			for (Iterator<JsonElement> iterator = metrics.iterator(); iterator.hasNext();) {
				JsonElement type = (JsonElement) iterator.next();
				String metric = type.getAsJsonObject().get("name").getAsString();
				if (metric.equalsIgnoreCase(metricName)) {
					return type.getAsJsonObject().get("value").getAsString();
					
				}
			}
			
			return "-1";
		} catch (Exception e) {
			return "-0";
		}
				
	}
	
	/**
	 * Method that returns a (integer) value of a specified metric 
	 * @param metricsObj - a stringified JSON containing the metrics
	 * @param metricName - a String containing the name of the metric whose value is sought
	 * @return int - the value of the metric
	 */
	public static String getMetricValue(String metricsObj, String metricName) {
		return getMetricValue(getAllMetrics(metricsObj), metricName);
	}

	/**
	 * Method that returns values
	 * @param parsedMetrics - a Map of Strings to a Map<String, Integer> containing the parsed metrics for each process
	 * @param metricName - the filter metric name whose values are sought
	 * @return Map<String, Integer> - the map whose keys are the processes and the values are the values of the metric
	 */
	public static Map<String, Integer> getMetricValues(Map<String, Map<String, Integer>> parsedMetrics, String metricName) {
		Map<String, Integer> returnMap = new HashMap<>();

		try {
			parsedMetrics.keySet()
				.iterator()
				.forEachRemaining(key -> returnMap.put(key, parsedMetrics.get(key).get(metricName)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnMap;
	}
	
	/**
	 * Method that transforms a stringified JSON of metrics into a Map
	 * @param metricsObj - a stringified JSON containing the metrics
	 * @return Map<String, Integer> - the corresponding map transformed from the string 
	 */
	public static Map<String, Integer> parseMetrics(String metricsObj) {
		return parseMetrics(getAllMetrics(metricsObj));
	}

	/**
	 * Helper private function to concatenate and return an array containing both the custom and the default metrics
	 * retrieved from the monitoring API
	 * @param metricsObj - a stringified JSON
	 * @return JSONArray - a concatenated JSON array of the metrics
	 */
	private static JsonArray getAllMetrics(String metricsObj) {
		JsonArray allMetrics = getDefaultMetrics(metricsObj, DEFAULT_METRICS);
		return allMetrics;
	}

	/**
	 * Method that transforms a JSONArray of metrics into a Map
	 * @param metricsArray - a JSONArray of metrics
	 * @return Map<String, Integer> - the corresponding map transformed from the string 
	 */
	private static Map<String, Integer> parseMetrics(JsonArray metricsArray) {
		Map<String, Integer> returnMap = new HashMap<>();
		
		StreamSupport.stream(metricsArray.spliterator(), false)
		  .map(JsonElement::getAsJsonObject)
		  .forEach(e -> returnMap.put(e.get("name").getAsString(),e.get("value").getAsInt()));
		
		return returnMap;
	}
}

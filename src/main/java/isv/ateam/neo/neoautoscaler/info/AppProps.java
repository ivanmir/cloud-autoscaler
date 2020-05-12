package isv.ateam.neo.neoautoscaler.info;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

/**
 *	Class that holds the application properties and utilities for string / JSON manipulation.
 */
@Slf4j
public class AppProps {

	public static JsonObject props;
	
	public static int minProcesses, maxProcesses;
	public static Set<String> pids = new HashSet<String>(); 
	
	public AppProps() {
		minProcesses = 1;
		maxProcesses = 1;
	}

	public synchronized static void update(String rawProps) {
		JsonObject oldProps = getProps();
		try {
			setProps(JsonParser.parseString(rawProps).getAsJsonObject());
			setPids(getPidsFromProps(props));
			setMinProcesses(getMinProcessesFromProps(props));
			setMaxProcesses(getMaxProcessesFromProps(props));
		} catch (Exception e) {
			log.debug("General Exception Error: {}", e.getCause());
			setProps(oldProps);
		}
	}

	public static HashSet<String> getPidsFromProps(JsonObject props) throws Exception {
		HashSet<String> pids = new HashSet<>();
		JsonArray pidArray = props.getAsJsonObject("entity").getAsJsonObject("state").getAsJsonArray("processes");
		
		StreamSupport.stream(pidArray.spliterator(), false)
		  .map(JsonElement::getAsJsonObject)
		  .filter(r -> r.has("processId"))
		  .collect(Collectors.toList())
		  .forEach(e -> {
			  log.debug("ProcessID Found: {}", e.get("processId").getAsString());
			  pids.add(e.get("processId").getAsString());
		  });

		return pids;
	}
	
	public static int getMinProcessesFromProps(JsonObject props) {
		int minP = 1;
		try {
			minP = props.getAsJsonObject("entity").get("minProcesses").getAsInt();
			log.debug("Min Process: {}", minP);
		} catch (Exception e) {
			log.debug("General Exception Error: {}", e.getCause());
		}
		
		return minP;
	}
	
	public static int getMaxProcessesFromProps(JsonObject props) {
		int maxP = 1;
		try {
			maxP = props.getAsJsonObject("entity").get("maxProcesses").getAsInt();
			log.debug("Max Process: {}", maxP);
		} catch (Exception e) {
			log.debug("General Exception Error: {}", e.getCause());
		}
		
		return maxP;
	}

	private static void setProps(JsonObject props) {
		AppProps.props = props;
	}
	
	public static JsonObject getProps() {
		return props;
	}

	private static void setPids(HashSet<String> pids) {
		AppProps.pids = pids;
	}
	
	public static Set<String> getPids() {
		return pids;
	}
	
	public static int getMinProcesses() {
		return minProcesses;
	}

	private static void setMinProcesses(int minProcesses) {
		AppProps.minProcesses = minProcesses;
	}

	public static int getMaxProcesses() {
		return maxProcesses;
	}

	private static void setMaxProcesses(int maxProcesses) {
		AppProps.maxProcesses = maxProcesses;
	}

	public String toString() {
		return "<br>AppProps [minProcesses=" + minProcesses + ", maxProcesses=" + maxProcesses + "]"
				+ "<br> pids=" + pids + "]";
	}

	public static void removePid(String pid2Stop) {
		log.debug("Removing pid: {}", pid2Stop);
		pids.removeIf(e -> e.equalsIgnoreCase(pid2Stop));
	}
	
}
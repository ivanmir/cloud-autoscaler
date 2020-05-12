package isv.ateam.neo.neoautoscaler.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import isv.ateam.neo.neoautoscaler.apis.PlatformAPIHandler;
import isv.ateam.neo.neoautoscaler.info.AppData;
import isv.ateam.neo.neoautoscaler.info.AppProps;
import isv.ateam.neo.neoautoscaler.monitor.RuleEngine;
import isv.ateam.neo.neoautoscaler.monitor.ScaleCentral;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the GET entry point. It takes a parameter "action", whose value that defines what happens next:
 * 1. "start" - the monitoring of the application starts.
 * 2. "stop" - the monitoring of the application stops.
 * 3. "query" - get info about the current application. This call needs to be accompanied by two more parameters,
 * "accountName" and "applicationName". In fact, this should be the first call, that sets up the coordinates of the
 * monitored application. If not restricted from the UI, then all the other calls should be made after this one!
 * 4. "startApp" - start a new instance of the current application.
 * 5. "stopApp" - stops an instance of the current application.
 * @see RestController 
 */
@Slf4j
@RestController
@RequestMapping({"appscaler"})
public class NeoAutoScalerController {

	@Autowired
	private PlatformAPIHandler api;
	
	@PermitAll
	@GetMapping("/backend")
	@ResponseBody
	public String backend (HttpServletRequest request, HttpServletResponse response, @RequestParam String action, @RequestParam(required = true) String accountName, @RequestParam(required = true) String applicationName) {

    	String out = "";
    	
		//This class is autowired by Spring
		PlatformAPIHandler.accountName = accountName;  
		PlatformAPIHandler.applicationName = applicationName;  

		//Loading the Rules Engine that controls how to start/stop additional application processes
		RuleEngine.loadRuleThresholds();		
		
		switch (action) {
		case "start":
			out = "";
			try {
				AppData.updateCurrentMetrics(api);
			} catch (InterruptedException e) {
				log.debug("Interrupted Exception Error: {}", e.getCause());
			} catch (ExecutionException e) {
				log.debug("Execution Exception Error: {}", e.getCause());
			}
			out += AppData.getParsedMetrics().toString();
			ScaleCentral.startMonitor(api);
			return out;
			
		case "stop":
			out = "";
			try {
				out += "Stopped monitoring the application.<br>";
				out += ScaleCentral.stopMonitor();
				Thread.sleep(30000);
				api.updateAppInfo(true);
			} catch (InterruptedException e) {
				log.debug("Interrupted Exception Error: {}", e.getCause());
			}
			
			return out;
			
		case "query":
			api.updateAppInfo(true);
			out = AppData.getAppInfo().toString();
			return out;
			
		case "startApp":
			out = "";
			try {
				out += api.startApplication().get();
				Thread.sleep(30000);
			} catch (InterruptedException | ExecutionException e1) {
				log.debug("Execution or Interrupted Exception Error: {}", e1.getCause());
				out += e1.getMessage() + "<br>";
			}
			out += AppData.getAppInfo().toString();
			return out;
			
		case "stopApp":
			String pid2Stop = AppProps.getPids().stream().findAny().get();
			
			out = "";
			try {
				api.stopApplicationProcess(pid2Stop).get();
				Thread.sleep(30000);
				AppProps.removePid(pid2Stop);
			} catch (InterruptedException | ExecutionException e1) {
				log.debug("Execution or Interrupted Exception Error: {}", e1.getCause());
				out += e1.getMessage() + "<br>";
			}
			out += AppData.getAppInfo().toString();
			return out;
			
		default:
			return "Please select a valid action [start, stop, query, startApp, stopApp";
		}
	}

	@PermitAll
	@GetMapping("/monitor")
	@ResponseBody
	public String monitor() throws IOException {
		return AppData.simpleMetrics;
	}
	
}

package isv.ateam.neo.neoautoscaler.monitor;

import java.util.concurrent.ExecutionException;

import isv.ateam.neo.neoautoscaler.apis.PlatformAPIHandler;
import isv.ateam.neo.neoautoscaler.info.AppData;
import isv.ateam.neo.neoautoscaler.info.AppProps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Monitor implements Runnable {
	//public static AppData appData;
	
	private volatile boolean runFlag = true;
	private PlatformAPIHandler api;
	
	public Monitor(PlatformAPIHandler api) {
		this.api = api;
	}
	
	public void stop() {
		runFlag = false;
	}
	
	/**
	 * This is the monitoring trigger. It runs each 2 seconds and updates the application info, afterwards 
	 * issuing a REST call to the monitoring API for an update of the current metrics values for each
	 * application instance.
	 * After the information has been updated, the rules for scaling are being applied. 
	 */
	@Override
	public void run() {
		while (runFlag) {
			try {
				api.updateAppInfo(false);
				AppData.updateCurrentMetrics(api);
				try2Scale();
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				log.debug("Interrupted Exception: {}", e.getCause());
				runFlag = false;
			} catch (Exception e) {
				log.debug("General Exception: {}", e.getCause());
				runFlag = false;
			}
		}
	}

	/**
	 * Method that is called periodically and tries to do the scaling as follows:
	 * 1. If the number of running instances is lower than the number allowed, then check if the upscale rules are fulfilled
	 * 1.1. If yes, then start a new instance of the application.
	 * 2. If checks on #1 failed, then try to do something similar, but in the downscale direction. 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void try2Scale() throws InterruptedException, ExecutionException {
		String pid2Stop = AppProps.getPids().stream().findAny().get();

		//try to scale up first
		if (AppProps.getMaxProcesses() > AppProps.getPids().size() && 
				RuleEngine.upScaleRuleFulfilled(AppData.getParsedMetrics())) {
			try {
				api.startApplication().get();
			} catch (InterruptedException | ExecutionException e1) {
				log.debug("Error: {}", e1.getCause());
				e1.getMessage();
			}
		} else {
			if (AppProps.getMinProcesses() < AppProps.getPids().size() && 
					RuleEngine.downScaleRuleFulfilled(AppData.getParsedMetrics())) {
				try {
					api.stopApplicationProcess(pid2Stop).get();
					AppProps.removePid(pid2Stop);
				} catch (InterruptedException | ExecutionException e) {
					log.debug("Error: {}", e.getCause());
				}
			}
		}
	}

}

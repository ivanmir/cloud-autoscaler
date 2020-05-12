package isv.ateam.neo.neoautoscaler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"isv", "isv.ateam.neo.neoautoscaler.apis"})
public class NeoAutoScalerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoAutoScalerApplication.class, args);
	}
}

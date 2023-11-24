package eu.hansolo.nameservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;


@SpringBootApplication
public class NameserviceApplication {

	private static long startTime;
	private static long endTime;

	public static void main(String[] args) {
		startTime = System.nanoTime();
		SpringApplication.run(NameserviceApplication.class, args);
		if (null == System.getProperty("START_TIME")) {
			System.out.println("Started up in " + ((endTime - startTime) / 1_000_000) + "ms");
		}
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startApp() {
		if (null != System.getProperty("START_TIME")) {
			startTime = Long.parseLong(System.getProperty("START_TIME"));
			endTime   = Instant.now().toEpochMilli();
			System.out.println("Started up in " + (endTime - startTime) + "ms");
		} else {
			endTime = System.nanoTime();
		}
	}

}

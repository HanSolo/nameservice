package eu.hansolo.nameservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


@SpringBootApplication
public class NameserviceApplication {

	private static long startTime;
	private static long endTime;

	public static void main(String[] args) {
		startTime = System.nanoTime();
		SpringApplication.run(NameserviceApplication.class, args);
		System.out.println("Started up in " + ((endTime - startTime) / 1_000_000) + "ms");
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startApp() {
		endTime = System.nanoTime();
	}

}

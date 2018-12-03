package hotday;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan
public class DemoApplication {

    public static void main(String[] args) {
    	SpringApplication.run(DemoApplication.class);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    	return (args) -> {
    		for (;;) {
    			Http.get("http://localhost:8080/hello/test");
    			Thread.sleep(1000);
    		}
    	};
    } 
    
}
package hotday;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import hotday.util.PlainHttp;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan
public class FailingApplication {

    public static void main(String[] args) {
    	SpringApplication.run(FailingApplication.class);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    	return (args) -> {
    		ExecutorService threadPool = Executors.newCachedThreadPool();
    		for (;;) {
    			threadPool.submit(() -> {
    				PlainHttp.get("http://localhost:8080/hello/test");
    			});    			
    			Thread.sleep(1000);
    		}
  		
    	};
    }
    
    @Bean
    public DataSources dataSources(ApplicationContext ctx) {
    	return new DataSources(ctx);
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSources.DataSource1 dataSource1() {
    	return DataSourceBuilder.create().type(DataSources.DataSource1.class).build();
    }

//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource2")
//    public DataSources.DataSource2 dataSource2() {
//    	return DataSourceBuilder.create().type(DataSources.DataSource2.class).build();
//    }
//
//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource3")
//    public DataSources.DataSource3 dataSource3() {
//    	return DataSourceBuilder.create().type(DataSources.DataSource3.class).build();
//    }
    
}
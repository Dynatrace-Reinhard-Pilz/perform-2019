package hotday;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan
public class DemoApplication {

    private static final int NUM_THREADS = 1;

    public static void main(String[] args) {
    	SpringApplication.run(DemoApplication.class);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    	return (args) -> {
    		for (int i = 0; i < NUM_THREADS; i++ ) {
    			new ConnectionThread(ctx).start();
    		}
    		
    		synchronized (this) {
    			this.wait();
    		}
  		
    	};
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSources.DataSource1 dataSource1() {
    	return DataSourceBuilder.create().type(DataSources.DataSource1.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSources.DataSource2 dataSource2() {
    	return DataSourceBuilder.create().type(DataSources.DataSource2.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSources.DataSource3 dataSource3() {
    	return DataSourceBuilder.create().type(DataSources.DataSource3.class).build();
    }
    
}
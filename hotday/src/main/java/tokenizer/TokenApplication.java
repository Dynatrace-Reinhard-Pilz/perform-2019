package tokenizer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import tokenizer.config.Options;
import tokenizer.infra.Backend;
import tokenizer.util.LoadGenerator;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan
public class TokenApplication {
	
    public static void main(String[] args) {
    	SpringApplication.run(TokenApplication.class, args);
    }
    
    @Bean
    public Options options(ApplicationContext ctx) {
    	return new Options(ctx);
    }
    
    @Bean
    public Backend backend(ApplicationContext ctx) {
    	return new Backend(ctx);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    	return new LoadGenerator(ctx, "http://localhost:" + ctx.getEnvironment().getProperty("server.port") + "/token");
    }
    
    @Bean
    public StabilityManager stability(ApplicationContext ctx) {
    	return new StabilityManager();
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

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource2")
    public DataSources.DataSource2 dataSource2() {
    	return DataSourceBuilder.create().type(DataSources.DataSource2.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource3")
    public DataSources.DataSource3 dataSource3() {
    	return DataSourceBuilder.create().type(DataSources.DataSource3.class).build();
    }
    
}
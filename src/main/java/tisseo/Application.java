package tisseo;

import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import tisseo.db.DB;
import tisseo.db.Ligne;


@Configuration

@EnableAutoConfiguration
@ComponentScan
public class Application {
	public final static String URL = "jdbc:postgresql:Tisseo";
	public final static String NOM_BASE = "postgres";
	public final static String PWD = "romano";
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        DB db = new DB(URL, NOM_BASE, PWD);
    }
    
    @Bean
    public ViewResolver getViewResolver(){
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/src/main/resources/");
        resolver.setSuffix(".html");
        return resolver;
    }
}

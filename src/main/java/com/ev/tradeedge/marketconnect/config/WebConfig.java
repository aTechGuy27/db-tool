package com.ev.tradeedge.marketconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	 @Override
	    public void addResourceHandlers(ResourceHandlerRegistry registry) {
	        // Serve static files from the /static/app directory
	        registry.addResourceHandler("/app/**")
	                .addResourceLocations("classpath:/static/app/");

	        // Serve root level static files
	        registry.addResourceHandler("/*.js", "/*.css", "/*.ico", "/*.json", "/*.html")
	                .addResourceLocations("classpath:/static/app/");
	    }

	    @Override
	    public void addViewControllers(ViewControllerRegistry registry) {
	        // Forward requests to index.html for client-side routing
	        registry.addViewController("/app").setViewName("forward:/app/index.html");
	        registry.addViewController("/app/").setViewName("forward:/app/index.html");
	        registry.addViewController("/app/login").setViewName("forward:/app/index.html");
	        registry.addViewController("/app/login/").setViewName("forward:/app/index.html");
	        registry.addViewController("/app/{spring:[^\\.]*}").setViewName("forward:/app/index.html");
	        registry.addViewController("/app/**/{spring:[^\\.]*}").setViewName("forward:/app/index.html");
	    }

}

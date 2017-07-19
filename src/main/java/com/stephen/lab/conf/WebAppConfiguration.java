package com.stephen.lab.conf;

import com.stephen.lab.interceptor.AccessLogInterceptor;
import com.stephen.lab.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by stephen on 2017/7/15.
 */
@Configuration
public class WebAppConfiguration extends WebMvcConfigurerAdapter{
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns("/search");
        registry.addInterceptor(new AccessLogInterceptor());
        super.addInterceptors(registry);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
    }

//    @Override
//    public void configureViewResolvers(ViewResolverRegistry registry) {
//        //spring.view.prefix=/WEB-INF/jsp/
//        //spring.view.suffix=.jsp
//        //registry.freeMarker();
//        registry.velocity();
//        //registry.groovy();
//    }

}

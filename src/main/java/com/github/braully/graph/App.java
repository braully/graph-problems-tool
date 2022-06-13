/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import javax.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

/**
 *
 * @author strike
 */
@SpringBootApplication
//@MultipartConfig
public class App {

//    @Bean
////        public CommonsMultipartResolver multipartResolver() {
//    public MultipartResolver multipartResolver() {
//        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
//        commonsMultipartResolver.setMaxUploadSize(1000000);
//        commonsMultipartResolver.setDefaultEncoding("UTF-8");
//        return commonsMultipartResolver;
//    }
//
//    @Bean
////    @Order(0)
//    public MultipartFilter multipartFilter() {
//        MultipartFilter multipartFilter = new MultipartFilter();
////        multipartFilter.setMultipartResolverBeanName("multipartResolver");
//        multipartFilter.setMultipartResolverBeanName("multipartReso‌​lver");
//        return multipartFilter;
//    }

    //
//    @Bean
//    public MultipartResolver multipartResolver() {
//        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
//        commonsMultipartResolver.setMaxUploadSize(100000);
//        commonsMultipartResolver.setDefaultEncoding("UTF-8");
//        return commonsMultipartResolver;
//    }
//    @Bean
//    @ConditionalOnMissingBean({MultipartConfigElement.class,
//        CommonsMultipartResolver.class})
//    public MultipartConfigElement multipartConfigElement() {
//        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
//        return multipartConfigElement;
//    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

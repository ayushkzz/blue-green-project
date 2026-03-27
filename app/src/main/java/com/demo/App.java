package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class App {

    // Reads environment variables injected by Docker/Jenkins
    @Value("${APP_VERSION:v1}")
    private String appVersion;

    @Value("${DEPLOY_ENV:blue}")
    private String deployEnv;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("version", appVersion);
        model.addAttribute("env", deployEnv.toLowerCase()); // ensures it matches our CSS classes
        return "index";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "UP";
    }
}

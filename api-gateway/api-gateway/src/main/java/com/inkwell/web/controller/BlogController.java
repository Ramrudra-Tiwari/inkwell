package com.inkwell.web.controller;

import com.inkwell.web.service.BlogWebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BlogController {

    @Autowired
    private BlogWebsiteService blogWebsiteService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAllAttributes(blogWebsiteService.loadHomePageData());
        return "home";
    }

    @GetMapping("/post/{slug}")
    public String viewPost(@PathVariable String slug, Model model) {
        model.addAllAttributes(blogWebsiteService.loadPostPageData(slug));
        return "post";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword, Model model) {
        model.addAllAttributes(blogWebsiteService.loadSearchResults(keyword));
        return "search_results";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Register");
        return "register";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "login";
    }
}

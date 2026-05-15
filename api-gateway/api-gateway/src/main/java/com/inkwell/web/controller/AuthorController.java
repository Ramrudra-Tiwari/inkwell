package com.inkwell.web.controller;

import com.inkwell.web.service.AuthorWebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/author")
public class AuthorController {

    @Autowired
    private AuthorWebsiteService authorWebsiteService;

    @GetMapping("/dashboard")
    public String authorDashboard(@RequestParam(defaultValue = "1") Integer authorId, Model model) {
        model.addAllAttributes(authorWebsiteService.loadAuthorDashboard(authorId));
        return "author/dashboard";
    }

    @GetMapping("/post/create")
    public String createPost(@RequestParam(defaultValue = "1") Integer authorId, Model model) {
        model.addAllAttributes(authorWebsiteService.loadCreatePostData(authorId));
        return "author/create_post";
    }

    @GetMapping("/posts")
    public String viewMyPosts(@RequestParam(defaultValue = "1") Integer authorId, Model model) {
        model.addAllAttributes(authorWebsiteService.loadAuthorPosts(authorId));
        return "author/my_posts";
    }
}

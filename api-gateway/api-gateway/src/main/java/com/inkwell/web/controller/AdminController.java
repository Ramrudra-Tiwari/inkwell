package com.inkwell.web.controller;

import com.inkwell.web.service.AdminWebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminWebsiteService adminWebsiteService;

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAllAttributes(adminWebsiteService.loadUsersData());
        return "admin/users";
    }

    @GetMapping("/posts")
    public String manageAllPosts(Model model) {
        model.addAllAttributes(adminWebsiteService.loadAllPostsData());
        return "admin/posts";
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAllAttributes(adminWebsiteService.loadCategoriesData());
        return "admin/categories";
    }
}
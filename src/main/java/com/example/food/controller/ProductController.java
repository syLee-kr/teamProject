package com.example.food.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

public class ProductController {
	@Controller
	public class ProductController {

	    @GetMapping("/product")
	    public String main() {
	        return "product";
	    }
}

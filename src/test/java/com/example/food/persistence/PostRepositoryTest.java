package com.example.food.persistence;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.food.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootTest
public class PostRepositoryTest {
	
	private final PostRepository postRepo;
	
	//@Disabled
	@Test
	public void testInsertPost() {
		
	}
}

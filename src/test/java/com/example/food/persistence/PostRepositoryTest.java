package com.example.food.persistence;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.example.food.domain.Post;
import com.example.food.domain.Users;
import com.example.food.repository.PostRepository;
import com.example.food.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestEntityManager
public class PostRepositoryTest {
	
	@Autowired
	private PostRepository postRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Disabled
	@Test
	@Transactional   //테스트 후 롤백 > 데이터베이스 저장안됨
	public void testInsertPost() {
		// User 객체 생성
		Users user = new Users();
		user.setUserId("test123");
		user.setName("테스트유저");
		
		// Gender enum 값 설정
		user.setGender(Users.Gender.MALE);
		
		// Role 설정
		user.setRole(Users.Role.ROLE_USER);
		//user.setRole(Users.Role.ROLE_ADMIN); // 명시적으로 ROLE_ADMIN 설정
		
		// User 엔티티에 먼저 저장
		//Users savedUser = userRepo.save(user);
		
		//TestEntityManager 적용 User
		Users savedUser = entityManager.persistAndFlush(user);
		
		// Post 객체 생성
		Post post = new Post();
		post.setTitle("테스트 제목");
		post.setContent("테스트 내용");
		post.setPriority(1);
		post.setIsNotice(false);
		post.setCnt(0L);
		post.setUser(savedUser); // 저장된 user를 Post에 설정
		
		// Post 엔티티에 저장
		//Post savePost = postRepo.save(post);
		
		//TestEntityManager 적용 Post
		Post savedPost = entityManager.persistAndFlush(post);
		
		// 데이터 확인(저장된 데이터가 기대하는 값과 일치하는지 체크) > 롤백으로 인한 데이터확인임
		assertNotNull(savedPost.getPSeq());
		assertEquals("테스트 제목", savedPost.getTitle());
		assertEquals("테스트 내용", savedPost.getContent());
		assertEquals(1, savedPost.getPriority());
		assertEquals(0L, savedPost.getCnt());
		assertEquals("test123", savedPost.getUser().getUserId());
		assertEquals("테스트유저", savedPost.getUser().getName());
	
	}
}

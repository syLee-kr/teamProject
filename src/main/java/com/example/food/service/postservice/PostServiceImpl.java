package com.example.food.service.postservice;

import com.example.food.entity.Post;
import com.example.food.entity.Users;
import com.example.food.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(String keyword, Pageable pageable) {
        return postRepository.findAllPosts(keyword, pageable);
    }

    @Override
    @Transactional
    public Post getPostDetail(Long pSeq) {
        // 게시글 조회
        Post post = postRepository.findById(pSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. pSeq=" + pSeq));

        // 조회수 증가
        post.setCnt(post.getCnt() + 1);
        postRepository.save(post); // 조회수 증가 후 저장

        return post;  // 수정된 게시글 반환
    }

    @Override
    @Transactional
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long pSeq) {
        Post post = postRepository.findById(pSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. pSeq=" + pSeq));
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getPostsByUser(Users user, Pageable pageable) {
        return postRepository.findByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getNoticePosts(String keyword) {
        return postRepository.findNoticePosts(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getRegularPosts(String keyword, Pageable pageable) {
        return postRepository.findRegularPosts(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPages(String keyword, int size) {
        long count;
        if (keyword == null || keyword.trim().isEmpty()) {
            count = postRepository.count();
        } else {
            count = postRepository.countAllPosts(keyword);
        }
        return (int) Math.ceil((double) count / size);
    }

    @Override
    public List<Integer> getPageList(int currentPage, int totalPages) {
        int start = Math.max(1, currentPage - 2);
        int end = Math.min(totalPages, currentPage + 2);
        List<Integer> pages = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}

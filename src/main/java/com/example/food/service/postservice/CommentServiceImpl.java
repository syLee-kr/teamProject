package com.example.food.service.postservice;

import com.example.food.entity.Comments;
import com.example.food.entity.Post;
import com.example.food.entity.Users;
import com.example.food.repository.CommentRepository;
import com.example.food.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public Comments createComment(Long postId, String content, Users user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        Comments comment = Comments.builder()
                .post(post)
                .content(content)
                .user(user)
                .build();

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long cSeq) {
        Comments comment = commentRepository.findById(cSeq)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. cSeq=" + cSeq));
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comments> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }
}

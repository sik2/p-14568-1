package com.back.global.initData;

import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Configuration
public class BaseInitData {

    private final PostService postService;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner(){
        return  args -> {
            self.work1();
            self.work2();
            self.work3();
            self.work4();
        };
    }

    @Transactional
    public void work1() {
        if (postService.count() > 0) return;

        postService.write("제목 1", "내용 1");
        postService.write("제목 2", "내용 2");
    }

    @Transactional(readOnly = true)
    public void work2() {
        postService.findAll()
                .forEach(post -> System.out.println("post id : " + post.getId()));
    }

    @Transactional(readOnly = true)
    public void work3() {
        postService.findAll()
                .forEach(post -> System.out.println("post id : " + post.getId()));
    }

    @Transactional(readOnly = true)
    public void work4() {
        postService.findAll()
                .forEach(post -> System.out.println("post id : " + post.getId()));
    }

}

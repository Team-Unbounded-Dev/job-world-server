package com.example.jobworldserver.application;

import com.example.jobworldserver.jobs.entity.Card;
import com.example.jobworldserver.jobs.entity.Tag;
import com.example.jobworldserver.jobs.repository.CardRepository;
import com.example.jobworldserver.jobs.repository.TagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataLoader implements CommandLineRunner {
    private final CardRepository jobsCardRepository;
    private final TagRepository tagRepository;

    public DataLoader(CardRepository jobsCardRepository, TagRepository tagRepository) {
        this.jobsCardRepository = jobsCardRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (jobsCardRepository.count() == 0) {
            Card card1 = new Card();
            card1.setName("소프트웨어 엔지니어");
            card1.setImageUrl("http://example.com/image1.jpg");
            card1.setDescription("코드를 작성하여 소프트웨어를 개발하는 직업입니다.");
            card1.setRating(4.0);
            card1.setLikes(1000L);
            card1.setTags(createOrGetTags(Arrays.asList("IT", "고소득")));

            Card card2 = new Card();
            card2.setName("의사");
            card2.setImageUrl("http://example.com/image2.jpg");
            card2.setDescription("환자를 진료하고 치료하는 직업입니다.");
            card2.setRating(5.0);
            card2.setLikes(9200L);
            card2.setTags(createOrGetTags(Arrays.asList("의료", "안정적")));

            Card card3 = new Card();
            card3.setName("소방관");
            card3.setImageUrl("https://www.google.com/imgres?q=%EC%9D%B4%EB%AF%B8%EC%A7%80&imgurl=https%3A%2F%2Fimage.utoimage.com%2Fpreview%2Fcp872722%2F2022%2F12%2F202212008462_500.jpg&imgrefurl=https%3A%2F%2Fwww.utoimage.com%2F%3Fm%3Dgoods.free%26mode%3Dview%26idx%3D22250682&docid=ndiXgrntLEKe9M&tbnid=W6ySxPkcFXMkBM&vet=12ahUKEwjU7YPYk82NAxXKqFYBHSfVL9kQM3oECBQQAA..i&w=500&h=750&hcb=2&ved=2ahUKEwjU7YPYk82NAxXKqFYBHSfVL9kQM3oECBQQAA");
            card3.setDescription("화재를 진압하고 인명을 구하는 직업입니다.");
            card3.setRating(3.0);
            card3.setLikes(12000L);
            card3.setTags(createOrGetTags(Arrays.asList("소방", "위험")));

            jobsCardRepository.saveAll(Arrays.asList(card1, card2, card3));
        }
    }

    private List<Tag> createOrGetTags(List<String> tagNames) {
        return tagNames.stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> {
                            Tag tag = new Tag();
                            tag.setName(name);
                            return tagRepository.save(tag);
                        }))
                .collect(Collectors.toList());
    }
}
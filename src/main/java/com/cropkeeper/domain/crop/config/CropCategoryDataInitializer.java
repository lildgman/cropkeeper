package com.cropkeeper.domain.crop.config;

import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // 테스트 환경에서는 실행 안함
public class CropCategoryDataInitializer implements ApplicationRunner {

    private final CropCategoryRepository cropCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (cropCategoryRepository.count() > 0) {
            log.info("작물 카테고리 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("작물 카테고리 초기 데이터를 삽입합니다...");

        String[] categories = {
                "과채류",    // 토마토, 오이, 호박, 가지 등
                "엽채류",    // 배추, 상추, 시금치 등
                "근채류",    // 무, 당근, 감자, 고구마 등
                "과수류",    // 사과, 배, 포도 등
                "화훼류",    // 장미, 국화 등
                "특용작물"   // 참깨, 들깨, 약용작물 등
        };

        for (String categoryName : categories) {
            CropCategory category = CropCategory.builder()
                    .categoryName(categoryName)
                    .build();
            cropCategoryRepository.save(category);
        }

        log.info("작물 카테고리 {}개가 성공적으로 삽입되었습니다.", categories.length);
    }
}

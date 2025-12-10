package com.cropkeeper.domain.crop.service;

import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropRepository;
import com.cropkeeper.domain.crop.repository.CropVarietyRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CropServiceTest {

    @Mock
    private CropCategoryRepository cropCategoryRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private CropVarietyRepository cropVarietyRepository;
}

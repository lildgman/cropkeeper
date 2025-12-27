package com.cropkeeper.domain.crop.service;

import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropTypeRepository;
import com.cropkeeper.domain.crop.repository.CropVarietyRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CropTypeServiceTest {

    @Mock
    private CropCategoryRepository cropCategoryRepository;

    @Mock
    private CropTypeRepository cropTypeRepository;

    @Mock
    private CropVarietyRepository cropVarietyRepository;
}

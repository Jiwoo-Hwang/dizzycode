package com.dizzycode.dizzycode.category.service;

import com.dizzycode.dizzycode.category.domain.dto.CategoryCreateDTO;
import com.dizzycode.dizzycode.category.domain.dto.CategoryDetailDTO;
import com.dizzycode.dizzycode.category.domain.dto.CategoryPostResponseDTO;
import com.dizzycode.dizzycode.category.exception.NoCategoryException;
import com.dizzycode.dizzycode.category.service.port.CategoryRepository;
import com.dizzycode.dizzycode.category.domain.Category;
import com.dizzycode.dizzycode.channel.service.port.ChannelRepository;
import com.dizzycode.dizzycode.channel.domain.dto.ChannelDetailDTO;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;

    @Transactional
    public CategoryPostResponseDTO createCategory(Long roomId, CategoryCreateDTO categoryCreateDTO) {
        Category category = categoryRepository.save(roomId, categoryCreateDTO.getCategoryName());
        return CategoryPostResponseDTO.from(category);
    }

    @Transactional
    public List<CategoryDetailDTO> categoryList(Long roomId) {
        return categoryRepository.findCategoriesByRoom(roomId);
    }

    @Transactional
    public CategoryDetailDTO categoryRetrieve(Long roomId, Long categoryId) {

        Category category = categoryRepository.findCategoryByCategoryId(categoryId).orElseThrow(() -> new NoCategoryException("카테고리가 존재하지 않습니다."));
        List<ChannelDetailDTO> channelDetailDTOs = channelRepository.findChannelsByCategory(category).stream()
                .map(ChannelDetailDTO::from)
                .toList();

        CategoryDetailDTO categoryDetailDTO = new CategoryDetailDTO();
        categoryDetailDTO.setCategoryId(category.getCategoryId());
        categoryDetailDTO.setCategoryName(category.getCategoryName());
        categoryDetailDTO.setRoomId(roomId);
        categoryDetailDTO.setChannels(channelDetailDTOs);

        return categoryDetailDTO;
    }
}

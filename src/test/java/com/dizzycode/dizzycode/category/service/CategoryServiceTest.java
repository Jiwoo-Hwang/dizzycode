package com.dizzycode.dizzycode.category.service;

import com.dizzycode.dizzycode.category.domain.dto.CategoryDetailDTO;
import com.dizzycode.dizzycode.category.domain.dto.CategoryCreateDTO;
import com.dizzycode.dizzycode.category.domain.dto.CategoryPostResponseDTO;
import com.dizzycode.dizzycode.category.exception.NoCategoryException;
import com.dizzycode.dizzycode.channel.domain.ChannelType;
import com.dizzycode.dizzycode.channel.domain.dto.ChannelCreateDTO;
import com.dizzycode.dizzycode.mock.category.FakeCategoryRepository;
import com.dizzycode.dizzycode.mock.channel.FakeChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryServiceTest {

    private CategoryService categoryService;
    private FakeCategoryRepository categoryRepository;
    private FakeChannelRepository channelRepository;

    @BeforeEach
    void init() {
        categoryRepository = new FakeCategoryRepository();
        channelRepository = new FakeChannelRepository(categoryRepository);
        categoryService = new CategoryService(categoryRepository, channelRepository);
    }

    @Test
    void 카테고리_생성() {
        // given
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setCategoryName("공지사항");

        // when
        CategoryPostResponseDTO result = categoryService.createCategory(1L, dto);

        // then
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("공지사항");
        assertThat(result.getRoomId()).isEqualTo(1L);
    }

    @Test
    void 룸의_카테고리_목록_조회() {
        // given
        CategoryCreateDTO dto1 = new CategoryCreateDTO();
        dto1.setCategoryName("공지사항");
        CategoryCreateDTO dto2 = new CategoryCreateDTO();
        dto2.setCategoryName("일반");
        categoryService.createCategory(1L, dto1);
        categoryService.createCategory(1L, dto2);

        // when
        List<CategoryDetailDTO> result = categoryService.categoryList(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("공지사항");
        assertThat(result.get(1).getCategoryName()).isEqualTo("일반");
    }

    @Test
    void 카테고리_단건_조회() {
        // given - 카테고리와 채널 미리 생성
        CategoryCreateDTO categoryDto = new CategoryCreateDTO();
        categoryDto.setCategoryName("텍스트채널");
        categoryService.createCategory(1L, categoryDto);

        ChannelCreateDTO channelDto = new ChannelCreateDTO();
        channelDto.setChannelName("일반");
        channelDto.setChannelType(ChannelType.CHAT);
        channelRepository.save(1L, channelDto);

        // when
        CategoryDetailDTO result = categoryService.categoryRetrieve(1L, 1L);

        // then
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("텍스트채널");
        assertThat(result.getChannels()).hasSize(1);
        assertThat(result.getChannels().get(0).getChannelName()).isEqualTo("일반");
    }

    @Test
    void 카테고리_단건_조회시_채널_없으면_빈_리스트_반환() {
        // given
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setCategoryName("빈카테고리");
        categoryService.createCategory(1L, dto);

        // when
        CategoryDetailDTO result = categoryService.categoryRetrieve(1L, 1L);

        // then
        assertThat(result.getChannels()).isEmpty();
    }

    @Test
    void 존재하지_않는_카테고리_조회_시_예외() {
        assertThatThrownBy(() -> categoryService.categoryRetrieve(1L, 999L))
                .isInstanceOf(NoCategoryException.class);
    }
}

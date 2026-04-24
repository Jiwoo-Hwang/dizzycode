package com.dizzycode.dizzycode.channel.service;

import com.dizzycode.dizzycode.category.domain.dto.CategoryCreateDTO;
import com.dizzycode.dizzycode.category.exception.NoCategoryException;
import com.dizzycode.dizzycode.category.service.CategoryService;
import com.dizzycode.dizzycode.channel.domain.ChannelType;
import com.dizzycode.dizzycode.channel.domain.dto.ChannelCreateDTO;
import com.dizzycode.dizzycode.channel.domain.dto.ChannelDetailDTO;
import com.dizzycode.dizzycode.channel.exception.ChannelNotFoundException;
import com.dizzycode.dizzycode.mock.category.FakeCategoryRepository;
import com.dizzycode.dizzycode.mock.channel.FakeChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ChannelServiceTest {

    private ChannelService channelService;
    private FakeCategoryRepository categoryRepository;

    @BeforeEach
    void init() {
        categoryRepository = new FakeCategoryRepository();
        FakeChannelRepository channelRepository = new FakeChannelRepository(categoryRepository);
        channelService = new ChannelService(channelRepository, categoryRepository);

        // 카테고리 미리 생성 (categoryId = 1L)
        categoryRepository.save(1L, "텍스트채널");
    }

    @Test
    void 채널_생성() {
        // given
        ChannelCreateDTO dto = new ChannelCreateDTO();
        dto.setChannelName("일반");
        dto.setChannelType(ChannelType.CHAT);

        // when
        ChannelDetailDTO result = channelService.createChannel(1L, dto);

        // then
        assertThat(result.getChannelId()).isEqualTo(1L);
        assertThat(result.getChannelName()).isEqualTo("일반");
        assertThat(result.getChannelType()).isEqualTo(ChannelType.CHAT);
        assertThat(result.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void 채널_목록_조회() {
        // given
        ChannelCreateDTO dto1 = new ChannelCreateDTO();
        dto1.setChannelName("일반");
        dto1.setChannelType(ChannelType.CHAT);
        ChannelCreateDTO dto2 = new ChannelCreateDTO();
        dto2.setChannelName("음성채널");
        dto2.setChannelType(ChannelType.VOICE);
        channelService.createChannel(1L, dto1);
        channelService.createChannel(1L, dto2);

        // when
        List<ChannelDetailDTO> result = channelService.channelList(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getChannelName()).isEqualTo("일반");
        assertThat(result.get(1).getChannelName()).isEqualTo("음성채널");
    }

    @Test
    void 채널_단건_조회() {
        // given
        ChannelCreateDTO dto = new ChannelCreateDTO();
        dto.setChannelName("공지");
        dto.setChannelType(ChannelType.CHAT);
        channelService.createChannel(1L, dto);

        // when
        ChannelDetailDTO result = channelService.channelRetrieve(1L, 1L);

        // then
        assertThat(result.getChannelId()).isEqualTo(1L);
        assertThat(result.getChannelName()).isEqualTo("공지");
        assertThat(result.getChannelType()).isEqualTo(ChannelType.CHAT);
    }

    @Test
    void 존재하지_않는_채널_조회_시_예외() {
        assertThatThrownBy(() -> channelService.channelRetrieve(1L, 999L))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    void 존재하지_않는_카테고리의_채널_목록_조회_시_예외() {
        assertThatThrownBy(() -> channelService.channelList(999L))
                .isInstanceOf(NoCategoryException.class);
    }
}

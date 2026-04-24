package com.dizzycode.dizzycode.channel.service;

import com.dizzycode.dizzycode.category.domain.Category;
import com.dizzycode.dizzycode.category.exception.NoCategoryException;
import com.dizzycode.dizzycode.channel.exception.ChannelNotFoundException;
import com.dizzycode.dizzycode.category.service.port.CategoryRepository;
import com.dizzycode.dizzycode.channel.domain.Channel;
import com.dizzycode.dizzycode.channel.service.port.ChannelRepository;
import com.dizzycode.dizzycode.channel.domain.dto.ChannelCreateDTO;
import com.dizzycode.dizzycode.channel.domain.dto.ChannelDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ChannelDetailDTO createChannel(Long categoryId, ChannelCreateDTO channelCreateDTO) {
        Channel channel = channelRepository.save(categoryId, channelCreateDTO);
        return ChannelDetailDTO.from(channel);
    }

    @Transactional
    public List<ChannelDetailDTO> channelList(Long categoryId) {
        Category category = categoryRepository.findCategoryByCategoryId(categoryId).orElseThrow(() -> new NoCategoryException("카테고리가 존재하지 않습니다."));

        return channelRepository.findChannelsByCategory(category).stream()
                .map(ChannelDetailDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChannelDetailDTO channelRetrieve(Long categoryId, Long channelId) {
        Channel channel = channelRepository.findChannelByChannelId(channelId).orElseThrow(() -> new ChannelNotFoundException("채널이 존재하지 않습니다."));
        return ChannelDetailDTO.from(channel);
    }
}

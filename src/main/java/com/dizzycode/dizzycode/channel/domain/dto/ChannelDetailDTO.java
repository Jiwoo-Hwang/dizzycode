package com.dizzycode.dizzycode.channel.domain.dto;

import com.dizzycode.dizzycode.channel.domain.Channel;
import com.dizzycode.dizzycode.channel.domain.ChannelType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelDetailDTO {

    private Long channelId;
    private Long categoryId;
    private String channelName;
    private ChannelType channelType;

    public static ChannelDetailDTO from(Channel channel) {
        ChannelDetailDTO dto = new ChannelDetailDTO();
        dto.channelId = channel.getChannelId();
        dto.channelName = channel.getChannelName();
        dto.channelType = channel.getChannelType();
        dto.categoryId = channel.getCategory().getCategoryId();
        return dto;
    }
}

package com.dizzycode.dizzycode.channel.domain.dto;

import com.dizzycode.dizzycode.channel.domain.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static com.dizzycode.dizzycode.channel.infrastructure.ChannelEntity.*;

@Getter
@Setter
public class ChannelCreateDTO {

    @NotBlank
    private String channelName;

    @NotNull
    private ChannelType channelType;
}

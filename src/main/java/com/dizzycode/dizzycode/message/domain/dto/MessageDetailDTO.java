package com.dizzycode.dizzycode.message.domain.dto;

import com.dizzycode.dizzycode.message.domain.DirectMessage;
import com.dizzycode.dizzycode.message.domain.RoomMessage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MessageDetailDTO {

    private String messageId;
    private String senderUsername;
    private String content;
    private List<String> url;
    private LocalDateTime timestamp;

    public static MessageDetailDTO from(RoomMessage message) {
        MessageDetailDTO dto = new MessageDetailDTO();
        dto.messageId = message.getId();
        dto.senderUsername = message.getMemberUsername();
        dto.content = message.getContent();
        dto.url = message.getUrl();
        dto.timestamp = message.getCreatedAt();
        return dto;
    }

    public static MessageDetailDTO from(DirectMessage message) {
        MessageDetailDTO dto = new MessageDetailDTO();
        dto.messageId = message.getId();
        dto.senderUsername = message.getMemberUsername();
        dto.content = message.getContent();
        dto.url = message.getUrl();
        dto.timestamp = message.getCreatedAt();
        return dto;
    }
}

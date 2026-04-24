package com.dizzycode.dizzycode.message.service;

import com.dizzycode.dizzycode.message.domain.DirectMessage;
import com.dizzycode.dizzycode.member.infrastructure.MemberEntity;
import com.dizzycode.dizzycode.message.domain.dto.MessageCreateDTO;
import com.dizzycode.dizzycode.message.domain.dto.MessageDetailDTO;
import com.dizzycode.dizzycode.member.exception.NoMemberException;
import com.dizzycode.dizzycode.message.infrastructure.DirectMessageRepository;
import com.dizzycode.dizzycode.member.infrastructure.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final MemberJpaRepository memberJpaRepository;

    public MessageDetailDTO saveDirectMessage(MessageCreateDTO messageCreateDTO, Long roomId) {
        Long senderId = messageCreateDTO.getSenderId();
        String content = messageCreateDTO.getContent();
        MemberEntity member = memberJpaRepository.findById(senderId)
                .orElseThrow(() -> new NoMemberException("존재하지 않는 회원입니다."));

        DirectMessage directMessage = new DirectMessage();
        directMessage.setContent(content);
        directMessage.setMemberId(senderId);
        directMessage.setMemberUsername(member.getUsername());
        directMessage.setUrl(messageCreateDTO.getUrl());
        directMessage.setRoomId(roomId);
        DirectMessage newDirectMessage = directMessageRepository.save(directMessage);
        return MessageDetailDTO.from(newDirectMessage);
    }

    public List<MessageDetailDTO> messageList(Long roomId, LocalDateTime last) {
        return directMessageRepository.findMessages(roomId, last).stream()
                .map(MessageDetailDTO::from)
                .collect(Collectors.toList());
    }
}

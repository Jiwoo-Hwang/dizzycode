package com.dizzycode.dizzycode.message.service;

import com.dizzycode.dizzycode.member.exception.NoMemberException;
import com.dizzycode.dizzycode.roommember.exception.RoomMemberNotFoundException;
import com.dizzycode.dizzycode.member.infrastructure.MemberEntity;
import com.dizzycode.dizzycode.message.domain.RoomMessage;
import com.dizzycode.dizzycode.roommember.infrastructure.RoomMemberEntity;
import com.dizzycode.dizzycode.roommember.infrastructure.RoomMemberIdEntity;
import com.dizzycode.dizzycode.message.domain.dto.MessageCreateDTO;
import com.dizzycode.dizzycode.message.domain.dto.MessageDetailDTO;
import com.dizzycode.dizzycode.member.infrastructure.MemberJpaRepository;
import com.dizzycode.dizzycode.message.infrastructure.MessageRepository;
import com.dizzycode.dizzycode.roommember.infrastructure.RoomMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final RoomMemberJpaRepository roomMemberJpaRepository;

    public MessageDetailDTO saveMessage(MessageCreateDTO messageCreateDTO, Long roomId, Long categoryId, Long channelId) {
        RoomMessage roomMessage = new RoomMessage();

        // 로그인한 사용자 확인
        MemberEntity member = memberJpaRepository.findById(messageCreateDTO.getSenderId())
                .orElseThrow(() -> new NoMemberException("존재하지 않는 회원입니다."));

        // 메시지 생성
        roomMessage.setMemberId(member.getId());
        roomMessage.setMemberUsername(member.getUsername());
        roomMessage.setRoomId(roomId);
        roomMessage.setCategoryId(categoryId);
        roomMessage.setChannelId(channelId);
        roomMessage.setContent(messageCreateDTO.getContent());
        roomMessage.setUrl(messageCreateDTO.getUrl());
        RoomMessage newRoomMessage = messageRepository.save(roomMessage);

        log.info("messageId={}", newRoomMessage.getId());

        return MessageDetailDTO.from(newRoomMessage);
    }

    public List<MessageDetailDTO> messageList(Long channelId, LocalDateTime last, Long roomId) {
        MemberEntity memberEntity = getMemberFromSession();
        RoomMemberIdEntity roomMemberIdEntity = new RoomMemberIdEntity(memberEntity.getId(), roomId);
        Optional<RoomMemberEntity> roomMember = roomMemberJpaRepository.findById(roomMemberIdEntity);

        return messageRepository.findMessages(channelId, last, roomMember.orElseThrow(() -> new RoomMemberNotFoundException("방 멤버 정보가 존재하지 않습니다.")).getCreatedAt()).stream()
                .map(MessageDetailDTO::from)
                .collect(Collectors.toList());
    }

    private MemberEntity getMemberFromSession() {
        // 현재 인증된 사용자의 인증 객체를 가져옴
        String[] memberInfo = SecurityContextHolder.getContext().getAuthentication().getName().split(" ");
        String email = memberInfo[1];

        return memberJpaRepository.findByEmail(email).orElseThrow(() -> new NoMemberException("존재하지 않는 회원입니다."));
    }
}

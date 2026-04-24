package com.dizzycode.dizzycode.roommember.domain.dto;

import com.dizzycode.dizzycode.roommember.domain.RoomMember;
import com.dizzycode.dizzycode.roommember.domain.RoomMemberId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomMemberDetailDTO {

    private RoomMemberId roomMemberId;

    public static RoomMemberDetailDTO from(RoomMember roomMember) {
        RoomMemberDetailDTO dto = new RoomMemberDetailDTO();
        dto.roomMemberId = roomMember.getRoomMemberId();
        return dto;
    }
}

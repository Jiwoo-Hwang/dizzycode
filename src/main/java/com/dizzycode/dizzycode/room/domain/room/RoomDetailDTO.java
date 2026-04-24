package com.dizzycode.dizzycode.room.domain.room;

import com.dizzycode.dizzycode.room.domain.Room;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDetailDTO {

    private Long roomId;
    private String roomName;
    private boolean open;

    public static RoomDetailDTO from(Room room) {
        RoomDetailDTO dto = new RoomDetailDTO();
        dto.roomId = room.getRoomId();
        dto.roomName = room.getRoomName();
        dto.open = room.isOpen();
        return dto;
    }
}

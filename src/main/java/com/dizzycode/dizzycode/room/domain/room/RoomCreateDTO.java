package com.dizzycode.dizzycode.room.domain.room;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RoomCreateDTO {

    @NotBlank
    private String roomName;

    private boolean open;
}

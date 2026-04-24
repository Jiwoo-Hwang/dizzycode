package com.dizzycode.dizzycode.room.domain.room;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class DMRoomCreateDTO {

    private String roomName;

    @NotNull
    @Size(min = 2, message = "DM방은 최소 2명이 필요합니다.")
    private List<String> userNames;
}

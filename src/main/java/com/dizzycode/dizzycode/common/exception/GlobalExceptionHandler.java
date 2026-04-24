package com.dizzycode.dizzycode.common.exception;

import com.dizzycode.dizzycode.category.exception.NoCategoryException;
import com.dizzycode.dizzycode.channel.exception.ChannelNotFoundException;
import com.dizzycode.dizzycode.friendship.exception.FriendshipAlreadyExistsException;
import com.dizzycode.dizzycode.friendship.exception.FriendshipNotFoundException;
import com.dizzycode.dizzycode.friendship.exception.InvalidFriendshipRequestException;
import com.dizzycode.dizzycode.member.exception.ExistMemberException;
import com.dizzycode.dizzycode.member.exception.NoMemberException;
import com.dizzycode.dizzycode.room.exception.NoRoomException;
import com.dizzycode.dizzycode.roommember.exception.RoomMemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResult handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("입력값이 유효하지 않습니다.");
        log.info("validation error: {}", message);
        return new ErrorResult("400", message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoMemberException.class, NoRoomException.class, NoCategoryException.class,
            ChannelNotFoundException.class, FriendshipNotFoundException.class, RoomMemberNotFoundException.class})
    public ErrorResult handleNotFound(RuntimeException e) {
        log.info("not found: {}", e.getMessage());
        return new ErrorResult("404", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExistMemberException.class)
    public ErrorResult handleExistMember(ExistMemberException e) {
        log.info("bad request: {}", e.getMessage());
        return new ErrorResult("400", e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidFriendshipRequestException.class)
    public ResponseEntity<ErrorResult> handleInvalidFriendshipRequest(InvalidFriendshipRequestException e) {
        log.info("unprocessable: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResult("422", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({FriendshipAlreadyExistsException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResult> handleConflict(RuntimeException e) {
        String message = e instanceof DataIntegrityViolationException
                ? "같은 키를 가지는 엔티티를 중복 생성할 수 없습니다."
                : e.getMessage();
        log.info("conflict: {}", message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResult("409", message));
    }
}

package com.dizzycode.dizzycode.friendship.service;

import com.dizzycode.dizzycode.friendship.domain.FriendshipStatus;
import com.dizzycode.dizzycode.friendship.domain.dto.FriendshipDetailDTO;
import com.dizzycode.dizzycode.friendship.exception.FriendshipAlreadyExistsException;
import com.dizzycode.dizzycode.friendship.exception.FriendshipNotFoundException;
import com.dizzycode.dizzycode.friendship.exception.InvalidFriendshipRequestException;
import com.dizzycode.dizzycode.member.domain.Member;
import com.dizzycode.dizzycode.member.domain.Role;
import com.dizzycode.dizzycode.message.service.DirectMessageRoomService;
import com.dizzycode.dizzycode.mock.friendship.FakeFriendshipRepository;
import com.dizzycode.dizzycode.mock.member.FakeMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    @Mock
    private DirectMessageRoomService directMessageRoomService;

    private FriendshipService friendshipService;
    private FakeMemberRepository memberRepository;

    @BeforeEach
    void init() {
        memberRepository = new FakeMemberRepository();
        FakeFriendshipRepository friendshipRepository = new FakeFriendshipRepository(memberRepository);

        memberRepository.save(Member.builder()
                .email("sender@test.com").username("sender").password("pw").role(Role.ROLE_USER).build());
        memberRepository.save(Member.builder()
                .email("receiver@test.com").username("receiver").password("pw").role(Role.ROLE_USER).build());

        friendshipService = new FriendshipService(friendshipRepository, memberRepository, directMessageRoomService);
    }

    @Test
    void 친구_신청_성공() {
        // when
        FriendshipDetailDTO result = friendshipService.requestFriendship(1L, 2L);

        // then
        assertThat(result.getFriendId()).isEqualTo(2L);
        assertThat(result.getFriendName()).isEqualTo("receiver");
        assertThat(result.getCurrentStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    void 자신에게_친구_신청_시_예외() {
        assertThatThrownBy(() -> friendshipService.requestFriendship(1L, 1L))
                .isInstanceOf(InvalidFriendshipRequestException.class);
    }

    @Test
    void 이미_존재하는_친구_관계에_재신청_시_예외() {
        // given
        friendshipService.requestFriendship(1L, 2L);

        // then
        assertThatThrownBy(() -> friendshipService.requestFriendship(1L, 2L))
                .isInstanceOf(FriendshipAlreadyExistsException.class);
    }

    @Test
    void 역방향_중복_친구_신청_시_예외() {
        // given
        friendshipService.requestFriendship(1L, 2L);

        // then - receiver가 sender에게 신청해도 중복 처리
        assertThatThrownBy(() -> friendshipService.requestFriendship(2L, 1L))
                .isInstanceOf(FriendshipAlreadyExistsException.class);
    }

    @Test
    void 유저명으로_친구_신청_성공() {
        // when
        FriendshipDetailDTO result = friendshipService.requestFriendshipByUsername(1L, "receiver");

        // then
        assertThat(result.getFriendId()).isEqualTo(2L);
        assertThat(result.getCurrentStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    void 친구_요청_수락() {
        // given
        friendshipService.requestFriendship(1L, 2L);

        // when
        FriendshipDetailDTO result = friendshipService.acceptFriendshipRequest(1L, 2L);

        // then
        assertThat(result.getCurrentStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void 친구_요청_거절() {
        // given
        friendshipService.requestFriendship(1L, 2L);

        // when
        FriendshipDetailDTO result = friendshipService.rejectFriendshipRequest(1L, 2L);

        // then
        assertThat(result.getCurrentStatus()).isEqualTo(FriendshipStatus.REJECTED);
    }

    @Test
    void 존재하지_않는_친구_관계_수락_시_예외() {
        assertThatThrownBy(() -> friendshipService.acceptFriendshipRequest(1L, 2L))
                .isInstanceOf(FriendshipNotFoundException.class);
    }

    @Test
    void 존재하지_않는_친구_관계_거절_시_예외() {
        assertThatThrownBy(() -> friendshipService.rejectFriendshipRequest(1L, 2L))
                .isInstanceOf(FriendshipNotFoundException.class);
    }

    @Test
    void 수락된_친구_목록_조회() {
        // given - 신청 후 수락
        friendshipService.requestFriendship(1L, 2L);
        friendshipService.acceptFriendshipRequest(1L, 2L);

        // when
        List<FriendshipDetailDTO> result = friendshipService.friendshipList(1L);

        // then - ACCEPTED 상태만 반환
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendId()).isEqualTo(2L);
    }

    @Test
    void 친구_목록_조회시_대기중인_요청은_미포함() {
        // given - 수락하지 않은 상태
        friendshipService.requestFriendship(1L, 2L);

        // when
        List<FriendshipDetailDTO> result = friendshipService.friendshipList(1L);

        // then - PENDING은 친구 목록에 포함되지 않음
        assertThat(result).isEmpty();
    }

    @Test
    void 대기중인_친구_요청_목록_조회() {
        // given - sender가 receiver에게 신청
        friendshipService.requestFriendship(1L, 2L);

        // when - receiver(2L) 입장에서 받은 요청 조회
        List<FriendshipDetailDTO> result = friendshipService.friendshipPendingList(2L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendId()).isEqualTo(1L);
        assertThat(result.get(0).getCurrentStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    void 친구_삭제() {
        // given
        friendshipService.requestFriendship(1L, 2L);
        friendshipService.acceptFriendshipRequest(1L, 2L);

        // when
        friendshipService.removeFriendship(1L, 2L);

        // then - 삭제 후 친구 목록이 비어 있음
        assertThat(friendshipService.friendshipList(1L)).isEmpty();
    }

    @Test
    void 존재하지_않는_친구_관계_삭제_시_예외() {
        assertThatThrownBy(() -> friendshipService.removeFriendship(1L, 2L))
                .isInstanceOf(FriendshipNotFoundException.class);
    }
}

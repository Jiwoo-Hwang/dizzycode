package com.dizzycode.dizzycode.mock.friendship;

import com.dizzycode.dizzycode.friendship.domain.Friendship;
import com.dizzycode.dizzycode.friendship.domain.FriendshipId;
import com.dizzycode.dizzycode.friendship.domain.FriendshipStatus;
import com.dizzycode.dizzycode.friendship.exception.FriendshipAlreadyExistsException;
import com.dizzycode.dizzycode.friendship.exception.FriendshipNotFoundException;
import com.dizzycode.dizzycode.friendship.exception.InvalidFriendshipRequestException;
import com.dizzycode.dizzycode.friendship.service.port.FriendshipRepository;
import com.dizzycode.dizzycode.member.domain.Member;
import com.dizzycode.dizzycode.mock.member.FakeMemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FakeFriendshipRepository implements FriendshipRepository {

    private final List<Friendship> data = new ArrayList<>();
    private final FakeMemberRepository memberRepository;

    public FakeFriendshipRepository(FakeMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Friendship> findFriendshipsByMemberId(Long memberId) {
        return data.stream()
                .filter(f -> f.getMember1().getId().equals(memberId) || f.getMember2().getId().equals(memberId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Friendship> findFriendshipById(Long memberId1, Long memberId2) {
        return data.stream()
                .filter(f ->
                        (f.getMember1().getId().equals(memberId1) && f.getMember2().getId().equals(memberId2)) ||
                        (f.getMember1().getId().equals(memberId2) && f.getMember2().getId().equals(memberId1)))
                .findFirst();
    }

    @Override
    public Friendship saveByUsername(Long senderId, String username) {
        Member sender = memberRepository.findById(senderId).orElseThrow();
        Member receiver = memberRepository.findByUsername(username).orElseThrow();
        return doSave(sender, receiver);
    }

    @Override
    public Friendship saveById(Long senderId, Long receiverId) {
        Member sender = memberRepository.findById(senderId).orElseThrow();
        Member receiver = memberRepository.findById(receiverId).orElseThrow();
        return doSave(sender, receiver);
    }

    @Override
    public Friendship accept(Long memberId1, Long memberId2) {
        Friendship friendship = findFriendshipById(memberId1, memberId2)
                .orElseThrow(() -> new FriendshipNotFoundException("친구 관계가 존재하지 않습니다."));
        Friendship updated = friendship.update(FriendshipStatus.ACCEPTED);
        data.remove(friendship);
        data.add(updated);
        return updated;
    }

    @Override
    public Friendship reject(Long memberId1, Long memberId2) {
        Friendship friendship = findFriendshipById(memberId1, memberId2)
                .orElseThrow(() -> new FriendshipNotFoundException("친구 관계가 존재하지 않습니다."));
        Friendship updated = friendship.update(FriendshipStatus.REJECTED);
        data.remove(friendship);
        data.add(updated);
        return updated;
    }

    @Override
    public void delete(Friendship friendship) {
        data.remove(friendship);
    }

    @Override
    public Boolean existsById(FriendshipId friendshipId) {
        return data.stream().anyMatch(f ->
                f.getId().getMemberId1().equals(friendshipId.getMemberId1()) &&
                f.getId().getMemberId2().equals(friendshipId.getMemberId2()));
    }

    private Friendship doSave(Member sender, Member receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new InvalidFriendshipRequestException("친구 신청을 보낼 수 없는 대상입니다.");
        }
        FriendshipId id1 = new FriendshipId(sender.getId(), receiver.getId());
        FriendshipId id2 = new FriendshipId(receiver.getId(), sender.getId());
        if (existsById(id1) || existsById(id2)) {
            throw new FriendshipAlreadyExistsException("친구 관계가 이미 존재합니다.");
        }
        Friendship friendship = Friendship.builder()
                .friendshipId(new FriendshipId(sender.getId(), receiver.getId()))
                .status(FriendshipStatus.PENDING)
                .member1(sender)
                .member2(receiver)
                .build();
        data.add(friendship);
        return friendship;
    }
}

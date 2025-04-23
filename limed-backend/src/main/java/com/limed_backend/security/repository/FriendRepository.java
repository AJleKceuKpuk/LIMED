package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Friends;
import com.limed_backend.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friends, Long> {
    Optional<Friends> findByUser_IdAndFriend_Id(Long senderId, Long receiverId);
    Optional<Friends> findByUser_IdAndFriend_IdAndStatus(Long senderId, Long receiverId, String status);

    @Query("SELECT f FROM Friends f " +
            "WHERE (f.user.id = :userId OR f.friend.id = :userId) " +
            "AND f.status = 'Accepted'")
    List<Friends> findAcceptedFriendsForUser(@Param("userId") Long userId);
}

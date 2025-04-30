package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockingRepository extends JpaRepository<Blocking, Long> {
    @Query("SELECT b FROM Blocking b " +
            "WHERE b.user = :user " +
            "AND b.blockingType = :blockingType " +
            "AND b.revokedBlock = false")
    List<Blocking> findActiveBlockings(@Param("user") User user,
                                       @Param("blockingType") String blockingType);

}

package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactsRepository extends JpaRepository<Contacts, Long> {

    // Находит связь между пользователями с учетом статуса в 1 сторону sender -> receiver
    @Query("SELECT c FROM Contacts c " +
            "WHERE c.sender.id = :senderId " +
            "  AND c.receiver.id = :receiverId " +
            "  AND c.status = :status")
    Optional<Contacts> findDirectContact(@Param("senderId") Long senderId,
                                            @Param("receiverId") Long receiverId,
                                            @Param("status") String status);

    // находит связь между sender -> receiver и receiver -> sender по status
    @Query("SELECT c FROM Contacts c " +
            "WHERE ((c.sender.id = :user1 AND c.receiver.id = :user2) " +
            "   OR (c.sender.id = :user2 AND c.receiver.id = :user1)) " +
            "  AND c.status = :status")
    Optional<Contacts> findContactBetween(@Param("user1") Long user1,
                                              @Param("user2") Long user2,
                                              @Param("status") String status);

    //находит все контакты, что связаны с user связью Accepted
    @Query("SELECT c FROM Contacts c " +
            "WHERE c.status = 'Accepted' " +
            "AND (c.sender.id = :userId OR c.receiver.id = :userId)")
    List<Contacts> findAcceptedByUser(@Param("userId") Long userId);

    // Выбирает контакты по отправителю (исходящие запросы) с заданным статусом
    List<Contacts> findBySender_IdAndStatus(Long senderId, String status);

    // Выбирает контакты по получателю (входящие запросы) с заданным статусом
    List<Contacts> findByReceiver_IdAndStatus(Long receiverId, String status);

}

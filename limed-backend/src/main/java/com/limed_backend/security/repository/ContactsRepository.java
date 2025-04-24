package com.limed_backend.security.repository;

import com.limed_backend.security.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactsRepository extends JpaRepository<Contacts, Long> {

    // Находит связь между пользователями по направлению: sender и receiver
    Optional<Contacts> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId);

    // Находит связь между пользователями с учетом статуса, например, "Accepted" или "Ignored"
    Optional<Contacts> findBySender_IdAndReceiver_IdAndStatus(Long senderId, Long receiverId, String status);

    // Выбирает все контакты, где текущий пользователь является либо отправителем, либо получателем с указанным статусом
    List<Contacts> findBySender_IdAndStatusOrReceiver_IdAndStatus(Long senderId, String status1, Long receiverId, String status2);

    // Выбирает контакты по отправителю (исходящие запросы) с заданным статусом
    List<Contacts> findBySender_IdAndStatus(Long senderId, String status);

    // Выбирает контакты по получателю (входящие запросы) с заданным статусом
    List<Contacts> findByReceiver_IdAndStatus(Long receiverId, String status);
}

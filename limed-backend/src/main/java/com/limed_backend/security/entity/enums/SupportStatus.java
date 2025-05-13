package com.limed_backend.security.entity.enums;

public enum SupportStatus {
    NEW,                            // Новый запрос
    OPEN,                           // Запрос в процессе обработки
    IN_PROGRESS,                    // Активное решение проблемы
    WAITING_USER_RESPONSE,          // Ожидание ответа от пользователя
    WAITING_SUPPORT_RESPONSE,       // Ожидание ответа от тех поддержки
    CLOSED,                         // Запрос полностью закрыт
    ESCALATED                       // Запрос передан на более высокий уровень поддержки
}
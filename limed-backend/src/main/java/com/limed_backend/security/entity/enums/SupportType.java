package com.limed_backend.security.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SupportType {
    GAME_SUPPORT("Тех.поддержка по игре"),
    ACCOUNT_SUPPORT("Тех.поддержка по аккаунту"),
    UNBLOCKING("Разблокировка"),
    PAYMENT("Вопросы по оплате"),
    BUG_REPORT("Сообщение об ошибке"),
    GENERAL("Общие вопросы");

    private final String displayName;
}
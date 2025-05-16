package com.limed_backend.security.service;

import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.ContactsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactsCacheService {

    private final CacheManager cacheManager;
    private final ContactsRepository contactsRepository;

    /** Получение списка друзей и добавление его в кэш*/
    @Cacheable(value = "contacts", key = "#id")
    public List<Contacts> findAllAccept(Long id){
        return contactsRepository.findAcceptedByUser(id)
                .orElseGet(ArrayList::new);
    }
    /** Получение черного списка и добавление его в кэш*/
    @Cacheable(value = "contacts-ignore", key = "#id")
    public List<Contacts> findAllIgnore(Long id){
        return contactsRepository.findIgnoreByUser(id)
                .orElseGet(ArrayList::new);
    }
    /** Получение списка исходящих предложений*/
    @Cacheable(value = "contacts-pending", key = "#id")
    public List<Contacts> findAllPending(Long id){
        return contactsRepository.findPendingByUser(id)
                .orElseGet(ArrayList::new);
    }
    /** Получение списка входящих предложений*/
    @Cacheable(value = "contacts-invite", key = "#id")
    public List<Contacts> findAllInvite(Long id){
        return contactsRepository.findInviteByUser(id)
                .orElseGet(ArrayList::new);
    }


    /** Извлечение из указанного кэша списка контактов*/
    public List<Contacts> getListContactsFromCache(Cache cache, User user) {
        List<?> rawList = cache.get(user.getId(), List.class);
        List<Contacts> listContacts = new ArrayList<>();
        if (rawList != null) {
            for (Object item : rawList) {
                if (item instanceof Contacts) {
                    listContacts.add((Contacts) item);
                }
            }
        }
        return listContacts;
    }

    /** метод удаления контакта из кэша*/
    public void removeContactsFromCache(User user, Contacts contact, String typeCache) {
        Cache cache = cacheManager.getCache(typeCache);
        List<Contacts> contacts = null;
        if (cache != null) {
            contacts = getListContactsFromCache(cache, user);
        }
        if (contacts != null && contacts.isEmpty()) {
            System.out.println("Contacts empty -> find all " + typeCache);
            contacts = switch (typeCache) {
                case "contacts" -> findAllAccept(user.getId());
                case "contacts-ignore" -> findAllIgnore(user.getId());
                case "contacts-pending" -> findAllPending(user.getId());
                case "contacts-invite" -> findAllInvite(user.getId());
                default -> new ArrayList<>();
            };
        }
        if (contacts != null) {
            contacts.removeIf(c -> c.getId().equals(contact.getId()));
        }
        if (cache != null) {
            cache.put(user.getId(), contacts);
        }
    }
    /** метод добавления контакта в кэш */
    public void addContactToCache(User user, Contacts contact, String typeCache) {
        Cache cache = cacheManager.getCache(typeCache);
        List<Contacts> contacts = null;
        if (cache != null) {
            contacts = getListContactsFromCache(cache, user);
        }
        if (contacts != null && contacts.isEmpty()) {
            System.out.println("Contacts empty -> find all " + typeCache);
            contacts = switch (typeCache) {
                case "contacts" -> findAllAccept(user.getId());
                case "contacts-ignore" -> findAllIgnore(user.getId());
                case "contacts-pending" -> findAllPending(user.getId());
                case "contacts-invite" -> findAllInvite(user.getId());
                default -> new ArrayList<>();
            };
        }
        if (contacts != null && contacts.stream().noneMatch(c -> c.getId().equals(contact.getId()))) {
            contacts.add(contact);
            System.out.println(contact.getId());
        }
        if (cache != null) {
            cache.put(user.getId(), contacts);
        }
    }
}

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

    //Список друзей
    @Cacheable(value = "contacts", key = "#id")
    public List<Contacts> findAllAccept(Long id){
        System.out.println("find test");
        return contactsRepository.findAcceptedByUser(id)
                .orElseGet(ArrayList::new);
    }

    //Список игнорируемых
    @Cacheable(value = "contacts-ignore", key = "#id")
    public List<Contacts> findAllIgnore(Long id){
        return contactsRepository.findIgnoreByUser(id)
                .orElseGet(ArrayList::new);
    }

    //список исходящих
    @Cacheable(value = "contacts-pending", key = "#id")
    public List<Contacts> findAllPending(Long id){
        return contactsRepository.findPendingByUser(id)
                .orElseGet(ArrayList::new);
    }

    //список входящих
    @Cacheable(value = "contacts-invite", key = "#id")
    public List<Contacts> findAllInvite(Long id){
        return contactsRepository.findInviteByUser(id)
                .orElseGet(ArrayList::new);
    }


    //извлекаем из указанного кэша список контактов
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

    //метод удаления контакта из кэша
    public void removeContactsFromCache(User user, Contacts contact, String typeCache) {
        Cache cache = cacheManager.getCache(typeCache);
        List<Contacts> contacts = getListContactsFromCache(cache, user);
        if (contacts.isEmpty()) {
            System.out.println("Contacts empty -> find all " + typeCache);
            contacts = switch (typeCache) {
                case "contacts" -> findAllAccept(user.getId());
                case "contacts-ignore" -> findAllIgnore(user.getId());
                case "contacts-pending" -> findAllPending(user.getId());
                case "contacts-invite" -> findAllInvite(user.getId());
                default -> new ArrayList<>();
            };
        }
        contacts.removeIf(c -> c.getId().equals(contact.getId()));
        cache.put(user.getId(), contacts);
    }

    //метод добавления контакта в кэш
    public void addContactToCache(User user, Contacts contact, String typeCache) {
        Cache cache = cacheManager.getCache(typeCache);
        List<Contacts> contacts = getListContactsFromCache(cache, user);
        if (contacts.isEmpty()) {
            System.out.println("Contacts empty -> find all " + typeCache);
            contacts = switch (typeCache) {
                case "contacts" -> findAllAccept(user.getId());
                case "contacts-ignore" -> findAllIgnore(user.getId());
                case "contacts-pending" -> findAllPending(user.getId());
                case "contacts-invite" -> findAllInvite(user.getId());
                default -> new ArrayList<>();
            };
        }
        if (contacts.stream().noneMatch(c -> c.getId().equals(contact.getId()))) {
            contacts.add(contact);
            System.out.println(contact.getId());
        }
        cache.put(user.getId(), contacts);
    }
}

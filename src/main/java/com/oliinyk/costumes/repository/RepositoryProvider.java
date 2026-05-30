package com.oliinyk.costumes.repository;

/**
 * Singleton-провайдер для доступу до репозиторіїв.
 * Гарантує єдиний екземпляр CachedCostumeRepository для ефективного кешування.
 */
public class RepositoryProvider {
    private static CostumeRepository costumeRepository;
    private static CustomLookRepository customLookRepository;

    private RepositoryProvider() {}

    public static synchronized CostumeRepository getCostumeRepository() {
        if (costumeRepository == null) {
            costumeRepository = new CachedCostumeRepository(new JdbcCostumeRepository(), 100);
        }
        return costumeRepository;
    }

    public static synchronized CustomLookRepository getCustomLookRepository() {
        if (customLookRepository == null) {
            customLookRepository = new JdbcCustomLookRepository();
        }
        return customLookRepository;
    }
}

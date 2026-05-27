package com.oliinyk.costumes.repository;

/**
 * Singleton-провайдер для доступу до репозиторіїв.
 * Гарантує єдиний екземпляр CachedCostumeRepository для ефективного кешування.
 */
public class RepositoryProvider {
    private static CostumeRepository costumeRepository;

    private RepositoryProvider() {}

    public static synchronized CostumeRepository getCostumeRepository() {
        if (costumeRepository == null) {
            costumeRepository = new CachedCostumeRepository(new JdbcCostumeRepository(), 100);
        }
        return costumeRepository;
    }
}

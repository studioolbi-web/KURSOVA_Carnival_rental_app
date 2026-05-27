package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.model.Costume;
import com.oliinyk.costumes.util.LruCache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Патерн Proxy / Decorator.
 * Кешуючий репозиторій для костюмів. Делегує запити реальному репозиторію,
 * якщо даних немає в кеші.
 */
public class CachedCostumeRepository implements CostumeRepository {

    private final CostumeRepository delegate;
    private final LruCache<UUID, Costume> cache;
    private List<Costume> allCostumesCache = null; // Простий кеш для повного списку
    private boolean isCacheValid = false;

    public CachedCostumeRepository(CostumeRepository delegate, int cacheCapacity) {
        this.delegate = delegate;
        this.cache = new LruCache<>(cacheCapacity);
    }

    @Override
    public void save(Costume costume) {
        delegate.save(costume);
        cache.put(costume.getId(), costume);
        isCacheValid = false; // Інвалідація повного списку
    }

    @Override
    public Optional<Costume> findById(UUID id) {
        Costume cached = cache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<Costume> costumeOpt = delegate.findById(id);
        costumeOpt.ifPresent(c -> cache.put(c.getId(), c));
        return costumeOpt;
    }

    @Override
    public List<Costume> findAll() {
        if (isCacheValid && allCostumesCache != null) {
            return allCostumesCache;
        }
        allCostumesCache = delegate.findAll();
        for (Costume c : allCostumesCache) {
            cache.put(c.getId(), c);
        }
        isCacheValid = true;
        return allCostumesCache;
    }

    @Override
    public void update(Costume costume) {
        delegate.update(costume);
        cache.put(costume.getId(), costume);
        isCacheValid = false;
    }

    @Override
    public void delete(UUID id) {
        delegate.delete(id);
        cache.remove(id);
        isCacheValid = false;
    }
}

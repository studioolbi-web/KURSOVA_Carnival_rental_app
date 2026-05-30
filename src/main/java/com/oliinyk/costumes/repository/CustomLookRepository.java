package com.oliinyk.costumes.repository;

import com.oliinyk.costumes.model.CustomLook;

import java.util.List;
import java.util.UUID;

public interface CustomLookRepository extends Repository<CustomLook> {
    List<CustomLook> findByUserId(UUID userId);
}

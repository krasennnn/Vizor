package com.krasen.vizor.business.IRepo;

import java.util.Optional;
import java.util.Set;

public interface IRoleRepository {
    Optional<String> findByName(String name);
    
    Set<String> findAll();
}


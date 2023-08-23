package com.example.poc;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<POCUser, Long> {
    POCUser findByUsername(String username);
}

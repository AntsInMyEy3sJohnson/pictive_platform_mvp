package io.pictive.platform.persistence.repositories;

import io.pictive.platform.domain.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserRepository extends JpaRepository<User, UUID> {
}

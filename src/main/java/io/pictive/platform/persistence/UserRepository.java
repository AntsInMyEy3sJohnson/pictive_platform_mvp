package io.pictive.platform.persistence;

import io.pictive.platform.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserRepository extends JpaRepository<User, UUID> {
}
package io.pictive.platform.persistence;

import io.pictive.platform.domain.DomainObject;
import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.image.Image;
import io.pictive.platform.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class PersistenceAccessConfiguration {

    private final List<JpaRepository<? extends DomainObject, UUID>> repositories;

    @Bean
    public PersistenceAccessService<User> userPersistenceAccessService() {

        return new PersistenceAccessService<>((JpaRepository<User, UUID>) retrieveDesiredRepository(User.class));

    }

    @Bean
    public PersistenceAccessService<Collection> collectionPersistenceAccessService() {

        return new PersistenceAccessService<>((JpaRepository<Collection, UUID>) retrieveDesiredRepository(Collection.class));

    }

    @Bean
    public PersistenceAccessService<Image> imagePersistenceAccessService() {

        return new PersistenceAccessService<>((JpaRepository<Image, UUID>) retrieveDesiredRepository(Image.class));

    }

    private JpaRepository<? extends DomainObject, UUID> retrieveDesiredRepository(Class<? extends DomainObject> domainObjectClass) {

        for (JpaRepository<? extends DomainObject, UUID> repository : repositories) {
            if (domainObjectClass.getTypeName().equals(ResolvableType.forClass(repository.getClass())
                    .as(JpaRepository.class).getGeneric(0).getType().getTypeName())) {
                return repository;
            }
        }

        throw new IllegalStateException("No JPA repository for domain object class: " + domainObjectClass.getCanonicalName());

    }

}

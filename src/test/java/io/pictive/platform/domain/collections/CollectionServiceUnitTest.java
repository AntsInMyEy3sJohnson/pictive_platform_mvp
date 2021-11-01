package io.pictive.platform.domain.collections;

import io.pictive.platform.domain.collections.exceptions.IncorrectPinGivenException;
import io.pictive.platform.domain.collections.exceptions.SourcingNotAllowedException;
import io.pictive.platform.domain.collections.exceptions.OwnerCannotSourceOwnedCollectionException;
import io.pictive.platform.domain.collections.exceptions.UserAlreadySourcedCollectionException;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollectionServiceUnitTest {

    @Mock
    private PersistenceContext<User> userPersistenceContext;

    @Mock
    private PersistenceContext<Collection> collectionPersistenceContext;

    @Test
    void testSourceWhenIncorrectPinIsGiven() {

        final CollectionService collectionService = new CollectionService(collectionPersistenceContext, userPersistenceContext, null);

        var owner = User.withProperties("sauron@mordor.com", "evil");
        var nonOwner = User.withProperties("gollum@somecave.org", "my_precious");
        var collection = Collection.withProperties("Just Me And The Ring", false, 1234, true, false);

        collection.setOwner(owner);
        collection.getSourcedBy().add(owner);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(nonOwner);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        assertThrows(IncorrectPinGivenException.class, () -> collectionService.source(nonOwner.getId(), collection.getId(), 4242));

    }

    @Test
    void testSourceWhenOwnerHasNotAllowedSourcing() {

        final CollectionService collectionService = new CollectionService(collectionPersistenceContext, userPersistenceContext, null);

        var owner = User.withProperties("gandalf@theundyinglands.org", "some_password");
        var nonOwner = User.withProperties("saruman@isengard.org", "some_other_password");
        var collection = Collection.withProperties("My Fight With The Balrog", false, 42, false, false);

        collection.setOwner(owner);
        collection.getSourcedBy().add(owner);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(nonOwner);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        assertThrows(SourcingNotAllowedException.class, () -> collectionService.source(nonOwner.getId(), collection.getId(), 42));

        verify(collectionPersistenceContext, never()).persist(any());

    }

    /**
     * Collection owners always have access to their owned collections (i. e. they've implicitly "sourced" them upon creation),
     * so it makes no sense for a user to source a collection he's the owner of
     */
    @Test
    void testSourceWhenSourcingUserIsCollectionOwner() {

        final CollectionService collectionService = new CollectionService(collectionPersistenceContext, userPersistenceContext, null);

        var owner = User.withProperties("bilbo@shire.org", "awesome_password");
        int pin = 1234;
        var collection = Collection.withProperties("There And Back Again", false, pin, true, false);
        collection.setOwner(owner);
        collection.getSourcedBy().add(owner);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(owner);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        assertThrows(OwnerCannotSourceOwnedCollectionException.class,
                () -> collectionService.source(owner.getId(), collection.getId(), pin));

        verify(collectionPersistenceContext, never()).persist(any());

    }

    /**
     * The sourcing user has already sourced the collection in question, so cannot source it again.
     */
    @Test
    void testSourceWhenSourcingUserHasAlreadySourcedCollection() {

        final CollectionService collectionService = new CollectionService(collectionPersistenceContext, userPersistenceContext, null);

        var owner = User.withProperties("bilbo@shire.org", "awesome_password");
        var nonOwner = User.withProperties("frodo@shire.org", "another_awesome_password");
        int pin = 1234;
        var collection = Collection.withProperties("There And Back Again", false, pin, true, false);
        collection.setOwner(owner);
        collection.getSourcedBy().addAll(List.of(owner, nonOwner));

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(nonOwner);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        assertThrows(UserAlreadySourcedCollectionException.class,
                () -> collectionService.source(owner.getId(), collection.getId(), pin));

        verify(collectionPersistenceContext, never()).persist(any());

    }

    /**
     * "Happy path"
     */
    @Test
    void testSourceWhenSourcingUserIsNotOwnerAndOwnerHasAllowedSourcingAndPinIsCorrect() {

        final CollectionService collectionService = new CollectionService(collectionPersistenceContext, userPersistenceContext, null);

        var nonOwner = User.withProperties("balrog@minesofmoria.org", "s3cret");
        var owner = User.withProperties("saruman@isengard.com", "super_s3cret");
        var pin = 1234;
        var collection = Collection.withProperties("Funny little dwarves", false, pin, true, false);
        collection.setOwner(owner);
        collection.getSourcedBy().add(owner);

        when(userPersistenceContext.find(isA(UUID.class))).thenReturn(nonOwner);
        when(collectionPersistenceContext.find(isA(UUID.class))).thenReturn(collection);

        var collectionAfterImport = collectionService.source(nonOwner.getId(), collection.getId(), pin);

        assertThat(collectionAfterImport.getSourcedBy()).containsExactlyInAnyOrder(owner, nonOwner);
        assertThat(nonOwner.getSourcedCollections()).containsOnly(collection);

        verify(userPersistenceContext).find(eq(nonOwner.getId()));
        verify(collectionPersistenceContext).find(eq(collection.getId()));
        verify(collectionPersistenceContext).persist(eq(collection));

    }

}

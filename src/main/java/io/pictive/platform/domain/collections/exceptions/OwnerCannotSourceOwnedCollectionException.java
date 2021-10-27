package io.pictive.platform.domain.collections.exceptions;

public class OwnerCannotSourceOwnedCollectionException extends CollectionException{
    public OwnerCannotSourceOwnedCollectionException(String message) {
        super(message);
    }
}

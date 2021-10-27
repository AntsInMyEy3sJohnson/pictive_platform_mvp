package io.pictive.platform.domain.collections.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class CollectionException extends RuntimeException {

    private final String message;

}

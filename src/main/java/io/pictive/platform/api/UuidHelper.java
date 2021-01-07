package io.pictive.platform.api;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UuidHelper {

    public List<UUID> asUuid(List<String> ids) {

        return ids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

    }

    public UUID asUuid(String id) {

        return UUID.fromString(id);

    }
}

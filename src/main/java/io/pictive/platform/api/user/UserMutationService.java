package io.pictive.platform.api.user;

import graphql.kickstart.tools.GraphQLMutationResolver;
import io.pictive.platform.domain.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserMutationService implements GraphQLMutationResolver {

    private final UserService userService;

    public UserBag createUserWithDefaultCollection(String mail) {

        return UserBag.of(Collections.singletonList(userService.createWithDefaultCollection(mail)));

    }

}

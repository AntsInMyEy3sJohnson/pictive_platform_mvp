package io.pictive.platform.api.user;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.pictive.platform.domain.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserQueryService implements GraphQLQueryResolver {

    private final UserService userService;

    public UserBag getUsers() {

        return UserBag.of(userService.getAll());

    }

    public UserBag getUserByMail(String mail) {

        return UserBag.of(Collections.singletonList(userService.getByMail(mail)));

    }

}

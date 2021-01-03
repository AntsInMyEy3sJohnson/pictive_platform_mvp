package io.pictive.platform.api.user;


import io.pictive.platform.domain.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class UserBag {

    private final List<User> users;

}

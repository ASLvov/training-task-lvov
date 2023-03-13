package ru.gazprom.gptrans.trainingtasklvov.screen.user;

import ru.gazprom.gptrans.trainingtasklvov.entity.User;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;

@UiController("ttl_User.browse")
@UiDescriptor("user-browse.xml")
@LookupComponent("usersTable")
@Route("users")
public class UserBrowse extends StandardLookup<User> {
}
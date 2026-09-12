package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.entity.UsersType;
import com.HireHub.hirehub.services.UsersService;
import com.HireHub.hirehub.services.UsersTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersControllerTest {

    @Test
    void registerUserWithSelectedRoleShouldBindTheChosenUserType() {
        UsersTypeService usersTypeService = mock(UsersTypeService.class);
        UsersService usersService = mock(UsersService.class);
        UsersController controller = new UsersController(usersTypeService, usersService);

        UsersType recruiterType = new UsersType();
        recruiterType.setUserTypeId(1);
        recruiterType.setUserTypeName("Recruiter");

        Users user = new Users();
        user.setEmail("candidate@example.com");
        user.setPassword("Secret1234");
        user.setUserTypeId(recruiterType);

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.empty());

        String viewName = controller.userRegistration(user, new ExtendedModelMap());

        assertEquals("redirect:/dashboard/", viewName);
        verify(usersService).addNew(argThat(savedUser -> savedUser.getUserTypeId() != null
                && savedUser.getUserTypeId().getUserTypeId() == 1));
    }
}

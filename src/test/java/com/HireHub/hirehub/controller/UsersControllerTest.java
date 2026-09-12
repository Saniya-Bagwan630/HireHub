package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.entity.UsersType;
import com.HireHub.hirehub.services.UsersService;
import com.HireHub.hirehub.services.UsersTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersControllerTest {

    @Test
    void registerUserWithSelectedRoleShouldBindTheChosenUserTypeAndSaveProfileData() {
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

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = controller.userRegistration(user, "John", "Doe", "New York", "NY", "USA", "TechCorp", null, null, model);

        assertEquals("redirect:/dashboard/", viewName);
        verify(usersService).addNew(argThat(savedUser -> savedUser.getUserTypeId() != null
                        && savedUser.getUserTypeId().getUserTypeId() == 1),
                eq("John"), eq("Doe"), eq("New York"), eq("NY"), eq("USA"), eq("TechCorp"), eq(null), eq(null));
    }

    @Test
    void registerUserWithMissingRequiredFieldsReturnsRegisterViewWithError() {
        UsersTypeService usersTypeService = mock(UsersTypeService.class);
        UsersService usersService = mock(UsersService.class);
        UsersController controller = new UsersController(usersTypeService, usersService);

        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPassword("Secret1234");

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = controller.userRegistration(user, "", "", "New York", "NY", "USA", null, null, null, model);

        assertEquals("register", viewName);
        assertTrue(model.containsKey("error"));
    }
}

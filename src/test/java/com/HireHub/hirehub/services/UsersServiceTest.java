package com.HireHub.hirehub.services;

import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.entity.RecruiterProfile;
import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.entity.UsersType;
import com.HireHub.hirehub.repository.JobSeekerProfileRepository;
import com.HireHub.hirehub.repository.RecruiterProfileRepository;
import com.HireHub.hirehub.repository.UsersRepository;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersServiceTest {

    @Test
    void addNewRecruiterProfileSavesProfileDetailsCorrectly() {
        UsersRepository usersRepository = mock(UsersRepository.class);
        JobSeekerProfileRepository jobSeekerProfileRepository = mock(JobSeekerProfileRepository.class);
        RecruiterProfileRepository recruiterProfileRepository = mock(RecruiterProfileRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UsersService usersService = new UsersService(usersRepository, jobSeekerProfileRepository, recruiterProfileRepository, passwordEncoder);

        UsersType recruiterType = new UsersType();
        recruiterType.setUserTypeId(1);
        recruiterType.setUserTypeName("Recruiter");

        Users user = new Users();
        user.setUserId(10);
        user.setEmail("recruiter@example.com");
        user.setPassword("secretPass");
        user.setUserTypeId(recruiterType);

        when(passwordEncoder.encode("secretPass")).thenReturn("encodedSecret");
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        when(recruiterProfileRepository.findById(10)).thenReturn(Optional.empty());

        usersService.addNew(user, "Alice", "Smith", "Chicago", "IL", "USA", "Acme Inc", null, null);

        verify(recruiterProfileRepository).save(argThat(profile ->
                "Alice".equals(profile.getFirstName()) &&
                        "Smith".equals(profile.getLastName()) &&
                        "Chicago".equals(profile.getCity()) &&
                        "Acme Inc".equals(profile.getCompany())
        ));
    }

    @Test
    void addNewJobSeekerProfileSavesProfileDetailsCorrectly() {
        UsersRepository usersRepository = mock(UsersRepository.class);
        JobSeekerProfileRepository jobSeekerProfileRepository = mock(JobSeekerProfileRepository.class);
        RecruiterProfileRepository recruiterProfileRepository = mock(RecruiterProfileRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UsersService usersService = new UsersService(usersRepository, jobSeekerProfileRepository, recruiterProfileRepository, passwordEncoder);

        UsersType seekerType = new UsersType();
        seekerType.setUserTypeId(2);
        seekerType.setUserTypeName("Job Seeker");

        Users user = new Users();
        user.setUserId(20);
        user.setEmail("seeker@example.com");
        user.setPassword("secretPass");
        user.setUserTypeId(seekerType);

        when(passwordEncoder.encode("secretPass")).thenReturn("encodedSecret");
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        when(jobSeekerProfileRepository.findById(20)).thenReturn(Optional.empty());

        usersService.addNew(user, "Bob", "Jones", "Austin", "TX", "USA", null, "US Citizen", "Full-Time");

        verify(jobSeekerProfileRepository).save(argThat(profile ->
                "Bob".equals(profile.getFirstName()) &&
                        "Jones".equals(profile.getLastName()) &&
                        "Austin".equals(profile.getCity()) &&
                        "US Citizen".equals(profile.getWorkAuthorization()) &&
                        "Full-Time".equals(profile.getEmploymentType())
        ));
    }
}

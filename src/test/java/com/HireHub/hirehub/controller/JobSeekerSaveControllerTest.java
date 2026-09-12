package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.entity.JobSeekerSave;
import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.services.JobPostActivityService;
import com.HireHub.hirehub.services.JobSeekerProfileService;
import com.HireHub.hirehub.services.JobSeekerSaveService;
import com.HireHub.hirehub.services.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobSeekerSaveControllerTest {

    private UsersService usersService;
    private JobSeekerProfileService jobSeekerProfileService;
    private JobPostActivityService jobPostActivityService;
    private JobSeekerSaveService jobSeekerSaveService;
    private JobSeekerSaveController controller;

    @BeforeEach
    void setUp() {
        usersService = mock(UsersService.class);
        jobSeekerProfileService = mock(JobSeekerProfileService.class);
        jobPostActivityService = mock(JobPostActivityService.class);
        jobSeekerSaveService = mock(JobSeekerSaveService.class);
        controller = new JobSeekerSaveController(usersService, jobSeekerProfileService, jobPostActivityService, jobSeekerSaveService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void saveShouldRedirectToLoginWhenUnauthenticated() {
        String view = controller.save(1, new JobSeekerSave());
        assertEquals("redirect:/login", view);
    }

    @Test
    void saveShouldRedirectToLoginWhenAnonymousUser() {
        Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(anonymous);
        SecurityContextHolder.setContext(securityContext);

        String view = controller.save(1, new JobSeekerSave());
        assertEquals("redirect:/login", view);
    }

    @Test
    void saveShouldSuccessfullySaveNewJobForCandidate() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("candidate@example.com");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Users user = new Users();
        user.setUserId(10);
        user.setEmail("candidate@example.com");

        JobSeekerProfile seekerProfile = new JobSeekerProfile();
        seekerProfile.setUserAccountId(10);

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(100);

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(seekerProfile));
        when(jobPostActivityService.getOne(100)).thenReturn(job);
        when(jobSeekerSaveService.alreadySaved(seekerProfile, job)).thenReturn(false);

        String view = controller.save(100, new JobSeekerSave());

        assertEquals("redirect:/dashboard/", view);
        verify(jobSeekerSaveService).addNew(any(JobSeekerSave.class));
    }

    @Test
    void saveShouldPreventDuplicateSaves() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("candidate@example.com");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Users user = new Users();
        user.setUserId(10);

        JobSeekerProfile seekerProfile = new JobSeekerProfile();
        seekerProfile.setUserAccountId(10);

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(100);

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(seekerProfile));
        when(jobPostActivityService.getOne(100)).thenReturn(job);
        when(jobSeekerSaveService.alreadySaved(seekerProfile, job)).thenReturn(true);

        String view = controller.save(100, new JobSeekerSave());

        assertEquals("redirect:/dashboard/", view);
        verify(jobSeekerSaveService, never()).addNew(any(JobSeekerSave.class));
    }

    @Test
    void saveShouldHandleInvalidJobIdSafely() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("candidate@example.com");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Users user = new Users();
        user.setUserId(10);

        JobSeekerProfile seekerProfile = new JobSeekerProfile();
        seekerProfile.setUserAccountId(10);

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(seekerProfile));
        when(jobPostActivityService.getOne(999)).thenThrow(new RuntimeException("Job not found"));

        String view = controller.save(999, new JobSeekerSave());

        assertEquals("redirect:/dashboard/", view);
        verify(jobSeekerSaveService, never()).addNew(any(JobSeekerSave.class));
    }

    @Test
    void savedJobsShouldPopulateModelForCandidate() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        JobSeekerProfile profile = new JobSeekerProfile();
        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(5);

        JobSeekerSave saveRecord = new JobSeekerSave();
        saveRecord.setJob(job);
        saveRecord.setUserId(profile);

        when(usersService.getCurrentUserProfile()).thenReturn(profile);
        when(jobSeekerSaveService.getCandidatesJob(profile)).thenReturn(Collections.singletonList(saveRecord));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.savedJobs(model);

        assertEquals("saved-jobs", view);
        List<?> savedList = (List<?>) model.get("jobPost");
        assertEquals(1, savedList.size());
        assertTrue(job.getIsSaved());
    }
}

package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.entity.JobSeekerApply;
import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobSeekerApplyControllerTest {

    private JobPostActivityService jobPostActivityService;
    private UsersService usersService;
    private JobSeekerApplyService jobSeekerApplyService;
    private JobSeekerSaveService jobSeekerSaveService;
    private RecruiterProfileService recruiterProfileService;
    private JobSeekerProfileService jobSeekerProfileService;
    private JobSeekerApplyController controller;

    @BeforeEach
    void setUp() {
        jobPostActivityService = mock(JobPostActivityService.class);
        usersService = mock(UsersService.class);
        jobSeekerApplyService = mock(JobSeekerApplyService.class);
        jobSeekerSaveService = mock(JobSeekerSaveService.class);
        recruiterProfileService = mock(RecruiterProfileService.class);
        jobSeekerProfileService = mock(JobSeekerProfileService.class);

        controller = new JobSeekerApplyController(
                jobPostActivityService,
                usersService,
                jobSeekerApplyService,
                jobSeekerSaveService,
                recruiterProfileService,
                jobSeekerProfileService
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    void applyShouldRedirectToLoginWhenUnauthenticated() {
        String view = controller.apply(1, "existing", null, "Cover letter");
        assertEquals("redirect:/login", view);
    }

    @Test
    void applyShouldUseExistingResumeWhenOptionIsExisting() {
        setupMockAuthentication("candidate@example.com");

        Users user = new Users();
        user.setUserId(10);
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUserAccountId(10);
        profile.setResume("profile_resume.pdf");

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(50);

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(profile));
        when(jobPostActivityService.getOne(50)).thenReturn(job);
        when(jobSeekerApplyService.alreadyApplied(profile, job)).thenReturn(false);

        String view = controller.apply(50, "existing", null, "Hello recruiter");

        assertEquals("redirect:/job-details-apply/50", view);
        verify(jobSeekerApplyService).addNew(argThat(apply ->
                "profile_resume.pdf".equals(apply.getResume()) &&
                "Hello recruiter".equals(apply.getCoverLetter()) &&
                apply.getJob().getJobPostId().equals(50)
        ));
    }

    @Test
    void applyShouldUploadNewResumeWhenOptionIsUpload() {
        setupMockAuthentication("candidate@example.com");

        Users user = new Users();
        user.setUserId(10);
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUserAccountId(10);
        profile.setResume("profile_resume.pdf");

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(50);

        MockMultipartFile newResume = new MockMultipartFile(
                "resumeFile",
                "new_resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Sample Word Content".getBytes()
        );

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(profile));
        when(jobPostActivityService.getOne(50)).thenReturn(job);
        when(jobSeekerApplyService.alreadyApplied(profile, job)).thenReturn(false);

        String view = controller.apply(50, "upload", newResume, "Cover letter text");

        assertEquals("redirect:/job-details-apply/50", view);
        verify(jobSeekerApplyService).addNew(argThat(apply ->
                "new_resume.docx".equals(apply.getResume()) &&
                "profile_resume.pdf".equals(profile.getResume()) // Profile resume remains unchanged
        ));
    }

    @Test
    void applyShouldRejectInvalidFileExtension() {
        setupMockAuthentication("candidate@example.com");

        Users user = new Users();
        user.setUserId(10);
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUserAccountId(10);

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(50);

        MockMultipartFile invalidFile = new MockMultipartFile(
                "resumeFile",
                "script.exe",
                "application/octet-stream",
                "binary".getBytes()
        );

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(profile));
        when(jobPostActivityService.getOne(50)).thenReturn(job);

        String view = controller.apply(50, "upload", invalidFile, "Cover letter");

        assertEquals("redirect:/job-details-apply/50", view);
        verify(jobSeekerApplyService, never()).addNew(any());
    }

    @Test
    void applyShouldPreventDuplicateApplications() {
        setupMockAuthentication("candidate@example.com");

        Users user = new Users();
        user.setUserId(10);
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUserAccountId(10);
        profile.setResume("profile_resume.pdf");

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(50);

        when(usersService.getUserByEmail("candidate@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileService.getOne(10)).thenReturn(Optional.of(profile));
        when(jobPostActivityService.getOne(50)).thenReturn(job);
        when(jobSeekerApplyService.alreadyApplied(profile, job)).thenReturn(true);

        String view = controller.apply(50, "existing", null, "Cover letter");

        assertEquals("redirect:/job-details-apply/50", view);
        verify(jobSeekerApplyService, never()).addNew(any());
    }

    private void setupMockAuthentication(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }
}

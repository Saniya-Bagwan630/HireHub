package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.services.JobPostActivityService;
import com.HireHub.hirehub.services.JobSeekerApplyService;
import com.HireHub.hirehub.services.JobSeekerSaveService;
import com.HireHub.hirehub.services.UsersService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobPostActivityControllerTest {

    @Test
    void recruiterCanOpenEditJobForm() {
        UsersService usersService = mock(UsersService.class);
        JobPostActivityService jobPostActivityService = mock(JobPostActivityService.class);
        JobSeekerApplyService jobSeekerApplyService = mock(JobSeekerApplyService.class);
        JobSeekerSaveService jobSeekerSaveService = mock(JobSeekerSaveService.class);
        JobPostActivityController controller = new JobPostActivityController(usersService, jobPostActivityService, jobSeekerApplyService, jobSeekerSaveService);

        JobPostActivity job = new JobPostActivity();
        job.setJobPostId(55);
        when(jobPostActivityService.getOne(55)).thenReturn(job);

        String viewName = controller.editJob(55, new ExtendedModelMap());

        assertEquals("add-jobs", viewName);
    }
}

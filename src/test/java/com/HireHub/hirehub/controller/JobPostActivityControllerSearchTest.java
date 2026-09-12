package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.services.JobPostActivityService;
import com.HireHub.hirehub.services.JobSeekerApplyService;
import com.HireHub.hirehub.services.JobSeekerSaveService;
import com.HireHub.hirehub.services.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JobPostActivityControllerSearchTest {

    private UsersService usersService;
    private JobPostActivityService jobPostActivityService;
    private JobSeekerApplyService jobSeekerApplyService;
    private JobSeekerSaveService jobSeekerSaveService;
    private JobPostActivityController controller;

    @BeforeEach
    void setUp() {
        usersService = mock(UsersService.class);
        jobPostActivityService = mock(JobPostActivityService.class);
        jobSeekerApplyService = mock(JobSeekerApplyService.class);
        jobSeekerSaveService = mock(JobSeekerSaveService.class);
        controller = new JobPostActivityController(usersService, jobPostActivityService, jobSeekerApplyService, jobSeekerSaveService);
    }

    @Test
    void globalSearchWithEmptyFiltersShouldPopulateAllJobsAndSetModel() {
        ExtendedModelMap model = new ExtendedModelMap();
        JobPostActivity job1 = new JobPostActivity();
        job1.setJobTitle("Java Developer");
        when(jobPostActivityService.search(eq(null), eq(null), anyList(), anyList(), eq(null)))
                .thenReturn(Collections.singletonList(job1));

        String view = controller.globalSearch(model, null, null, null, null, null, null, null, null, false, false, false);

        assertEquals("global-search", view);
        List<?> jobPostList = (List<?>) model.get("jobPost");
        assertNotNull(jobPostList);
        assertEquals(1, jobPostList.size());
    }

    @Test
    void searchJobsWithZeroResultsShouldSetEmptyListInModel() {
        ExtendedModelMap model = new ExtendedModelMap();
        when(jobPostActivityService.search(any(), any(), anyList(), anyList(), any()))
                .thenReturn(Collections.emptyList());

        String view = controller.globalSearch(model, "NonExistentJob", "Nowhere", null, null, null, null, null, null, false, false, false);

        assertEquals("global-search", view);
        List<?> jobPostList = (List<?>) model.get("jobPost");
        assertNotNull(jobPostList);
        assertTrue(jobPostList.isEmpty());
    }

    @Test
    void processSearchShouldPassTrimmedParametersAndSelectedFilters() {
        ExtendedModelMap model = new ExtendedModelMap();
        JobPostActivity job = new JobPostActivity();
        job.setJobTitle("Spring Boot Engineer");

        when(jobPostActivityService.search(
                eq("Engineer"),
                eq("California"),
                argThat(types -> types.contains("Full-Time") && types.contains("Full-time")),
                argThat(remotes -> remotes.contains("Remote-Only")),
                argThat(date -> date != null && date.equals(LocalDate.now().minusDays(7)))
        )).thenReturn(Collections.singletonList(job));

        String view = controller.globalSearch(
                model,
                "  Engineer  ",
                " California ",
                null, "Full-Time", null,
                "Remote-Only", null, null,
                false, true, false
        );

        assertEquals("global-search", view);
        assertEquals(true, model.get("fullTime"));
        assertEquals(true, model.get("remoteOnly"));
        assertEquals(true, model.get("days7"));

        List<?> resultList = (List<?>) model.get("jobPost");
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }
}

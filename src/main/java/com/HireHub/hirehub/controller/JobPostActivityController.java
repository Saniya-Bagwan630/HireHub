package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.*;
import com.HireHub.hirehub.services.JobPostActivityService;
import com.HireHub.hirehub.services.JobSeekerApplyService;
import com.HireHub.hirehub.services.JobSeekerSaveService;
import com.HireHub.hirehub.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class JobPostActivityController {

    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobPostActivityController(UsersService usersService, JobPostActivityService jobPostActivityService, JobSeekerApplyService jobSeekerApplyService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("/dashboard/")
    public String searchJobs(Model model,
                             @RequestParam(value = "job", required = false) String job,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "partTime", required = false) String partTime,
                             @RequestParam(value = "fullTime", required = false) String fullTime,
                             @RequestParam(value = "freelance", required = false) String freelance,
                             @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required = false) String officeOnly,
                             @RequestParam(value = "partialRemote", required = false) String partialRemote,
                             @RequestParam(value = "today", required = false) boolean today,
                             @RequestParam(value = "days7", required = false) boolean days7,
                             @RequestParam(value = "days30", required = false) boolean days30) {

        processJobSearch(model, job, location, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, today, days7, days30);

        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);

            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                if (currentUserProfile instanceof RecruiterProfile recruiterProfile) {
                    List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(recruiterProfile.getUserAccountId());
                    model.addAttribute("jobPost", recruiterJobs != null ? recruiterJobs : Collections.emptyList());
                }
            } else if (currentUserProfile instanceof JobSeekerProfile seekerProfile) {
                @SuppressWarnings("unchecked")
                List<JobPostActivity> jobPostList = (List<JobPostActivity>) model.getAttribute("jobPost");
                if (jobPostList != null && !jobPostList.isEmpty()) {
                    List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidatesJobs(seekerProfile);
                    List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob(seekerProfile);

                    for (JobPostActivity jobActivity : jobPostList) {
                        boolean exist = false;
                        boolean saved = false;
                        if (jobSeekerApplyList != null) {
                            for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                                if (jobSeekerApply.getJob() != null && Objects.equals(jobActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId())) {
                                    exist = true;
                                    break;
                                }
                            }
                        }

                        if (jobSeekerSaveList != null) {
                            for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                                if (jobSeekerSave.getJob() != null && Objects.equals(jobActivity.getJobPostId(), jobSeekerSave.getJob().getJobPostId())) {
                                    saved = true;
                                    break;
                                }
                            }
                        }

                        jobActivity.setIsActive(exist);
                        jobActivity.setIsSaved(saved);
                    }
                }
            }
        }

        model.addAttribute("user", currentUserProfile);

        return "dashboard";
    }

    @GetMapping("global-search/")
    public String globalSearch(Model model,
                               @RequestParam(value = "job", required = false) String job,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "partTime", required = false) String partTime,
                               @RequestParam(value = "fullTime", required = false) String fullTime,
                               @RequestParam(value = "freelance", required = false) String freelance,
                               @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                               @RequestParam(value = "officeOnly", required = false) String officeOnly,
                               @RequestParam(value = "partialRemote", required = false) String partialRemote,
                               @RequestParam(value = "today", required = false) boolean today,
                               @RequestParam(value = "days7", required = false) boolean days7,
                               @RequestParam(value = "days30", required = false) boolean days30) {

        processJobSearch(model, job, location, partTime, fullTime, freelance, remoteOnly, officeOnly, partialRemote, today, days7, days30);
        return "global-search";
    }

    private void processJobSearch(Model model,
                                 String job,
                                 String location,
                                 String partTime,
                                 String fullTime,
                                 String freelance,
                                 String remoteOnly,
                                 String officeOnly,
                                 String partialRemote,
                                 boolean today,
                                 boolean days7,
                                 boolean days30) {

        boolean isPartTime = Objects.equals(partTime, "Part-Time");
        boolean isFullTime = Objects.equals(fullTime, "Full-Time");
        boolean isFreelance = Objects.equals(freelance, "Freelance");

        boolean isRemoteOnly = Objects.equals(remoteOnly, "Remote-Only");
        boolean isOfficeOnly = Objects.equals(officeOnly, "Office-Only");
        boolean isPartialRemote = Objects.equals(partialRemote, "Partial-Remote");

        model.addAttribute("partTime", isPartTime);
        model.addAttribute("fullTime", isFullTime);
        model.addAttribute("freelance", isFreelance);

        model.addAttribute("remoteOnly", isRemoteOnly);
        model.addAttribute("officeOnly", isOfficeOnly);
        model.addAttribute("partialRemote", isPartialRemote);

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        if (days30) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today) {
            searchDate = LocalDate.now();
        }

        List<String> types = new ArrayList<>();
        if (isPartTime) {
            types.add("Part-Time");
            types.add("Part-time");
        }
        if (isFullTime) {
            types.add("Full-Time");
            types.add("Full-time");
        }
        if (isFreelance) {
            types.add("Freelance");
            types.add("freelance");
        }
        if (types.isEmpty()) {
            types.addAll(Arrays.asList("Part-Time", "Part-time", "Full-Time", "Full-time", "Freelance", "freelance", "Internship", "internship"));
        }

        List<String> remotes = new ArrayList<>();
        if (isRemoteOnly) {
            remotes.add("Remote-Only");
        }
        if (isOfficeOnly) {
            remotes.add("Office-Only");
        }
        if (isPartialRemote) {
            remotes.add("Partial-Remote");
        }
        if (remotes.isEmpty()) {
            remotes.addAll(Arrays.asList("Remote-Only", "Office-Only", "Partial-Remote"));
        }

        String searchJobParam = (StringUtils.hasText(job) && !job.trim().equalsIgnoreCase("null")) ? job.trim() : null;
        String searchLocParam = (StringUtils.hasText(location) && !location.trim().equalsIgnoreCase("null")) ? location.trim() : null;

        List<JobPostActivity> jobPostList = jobPostActivityService.search(searchJobParam, searchLocParam, types, remotes, searchDate);
        if (jobPostList == null) {
            jobPostList = new ArrayList<>();
        }

        model.addAttribute("jobPost", jobPostList);
    }

    @GetMapping("/dashboard/add")
    public String addJobs(Model model) {
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model model) {

        Users user = usersService.getCurrentUser();
        if (user != null) {
            jobPostActivity.setPostedById(user);
        }
        jobPostActivity.setPostedDate(new Date());
        model.addAttribute("jobPostActivity", jobPostActivity);
        JobPostActivity saved = jobPostActivityService.addNew(jobPostActivity);
        return "redirect:/dashboard/";
    }

    @PostMapping("dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model) {

        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/deleteJob/{id}")
    public String deleteJob(@PathVariable("id") int id) {

        jobPostActivityService.deleteJob(id);

        return "redirect:/dashboard/";
    }
}
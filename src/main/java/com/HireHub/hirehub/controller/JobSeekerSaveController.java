package com.HireHub.hirehub.controller;

import com.HireHub.hirehub.entity.JobPostActivity;
import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.entity.JobSeekerSave;
import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.services.JobPostActivityService;
import com.HireHub.hirehub.services.JobSeekerProfileService;
import com.HireHub.hirehub.services.JobSeekerSaveService;
import com.HireHub.hirehub.services.UsersService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;

@Controller
public class JobSeekerSaveController {

    private final UsersService usersService;
    private final JobSeekerProfileService jobSeekerProfileService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerSaveService jobSeekerSaveService;

    public JobSeekerSaveController(UsersService usersService, JobSeekerProfileService jobSeekerProfileService, JobPostActivityService jobPostActivityService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("job-details/save/{id}")
    public String saveDirectAccess(@PathVariable("id") int id) {
        return "redirect:/dashboard/";
    }

    @PostMapping("job-details/save/{id}")
    public String save(@PathVariable("id") int id, JobSeekerSave jobSeekerSave) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            String currentUsername = authentication.getName();
            Optional<Users> userOpt = usersService.getUserByEmail(currentUsername);
            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }

            Users user = userOpt.get();
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            if (seekerProfile.isEmpty()) {
                return "redirect:/dashboard/";
            }

            JobPostActivity jobPostActivity = null;
            try {
                jobPostActivity = jobPostActivityService.getOne(id);
            } catch (Exception e) {
                return "redirect:/dashboard/";
            }

            if (jobPostActivity != null) {
                JobSeekerProfile currentProfile = seekerProfile.get();
                if (!jobSeekerSaveService.alreadySaved(currentProfile, jobPostActivity)) {
                    JobSeekerSave newSave = new JobSeekerSave();
                    newSave.setJob(jobPostActivity);
                    newSave.setUserId(currentProfile);
                    jobSeekerSaveService.addNew(newSave);
                }
            }
        } catch (Exception e) {
            return "redirect:/dashboard/";
        }

        return "redirect:/dashboard/";
    }

    @GetMapping("saved-jobs/")
    public String savedJobs(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        List<JobPostActivity> jobPost = new ArrayList<>();
        Object currentUserProfile = usersService.getCurrentUserProfile();

        if (currentUserProfile instanceof JobSeekerProfile seekerProfile) {
            List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob(seekerProfile);
            if (jobSeekerSaveList != null) {
                for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                    if (jobSeekerSave.getJob() != null) {
                        jobSeekerSave.getJob().setIsSaved(true);
                        jobPost.add(jobSeekerSave.getJob());
                    }
                }
            }
        }

        model.addAttribute("jobPost", jobPost);
        model.addAttribute("user", currentUserProfile);

        return "saved-jobs";
    }
}
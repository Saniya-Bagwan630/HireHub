package com.HireHub.hirehub.services;

import com.HireHub.hirehub.entity.JobSeekerProfile;
import com.HireHub.hirehub.entity.RecruiterProfile;
import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.repository.JobSeekerProfileRepository;
import com.HireHub.hirehub.repository.RecruiterProfileRepository;
import com.HireHub.hirehub.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository usersRepository, JobSeekerProfileRepository jobSeekerProfileRepository, RecruiterProfileRepository recruiterProfileRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users addNew(Users users) {
        return addNew(users, null, null, null, null, null, null, null, null);
    }

    public Users addNew(Users users, String firstName, String lastName, String city, String state, String country,
                        String company, String workAuthorization, String employmentType) {
        users.setActive(true);
        users.setRegistrationDate(new Date(System.currentTimeMillis()));
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        Users savedUser = usersRepository.save(users);
        int userTypeId = (users.getUserTypeId() != null) ? users.getUserTypeId().getUserTypeId() : 2;

        if (userTypeId == 1) {
            RecruiterProfile recruiterProfile = recruiterProfileRepository.findById(savedUser.getUserId())
                    .orElse(new RecruiterProfile(savedUser));
            recruiterProfile.setFirstName(firstName);
            recruiterProfile.setLastName(lastName);
            recruiterProfile.setCity(city);
            recruiterProfile.setState(state);
            recruiterProfile.setCountry(country);
            recruiterProfile.setCompany(company);
            recruiterProfileRepository.save(recruiterProfile);
        } else {
            JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findById(savedUser.getUserId())
                    .orElse(new JobSeekerProfile(savedUser));
            jobSeekerProfile.setFirstName(firstName);
            jobSeekerProfile.setLastName(lastName);
            jobSeekerProfile.setCity(city);
            jobSeekerProfile.setState(state);
            jobSeekerProfile.setCountry(country);
            jobSeekerProfile.setWorkAuthorization(workAuthorization);
            jobSeekerProfile.setEmploymentType(employmentType);
            jobSeekerProfileRepository.save(jobSeekerProfile);
        }

        return savedUser;
    }


    public Object getCurrentUserProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users users = usersRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Could not found " + "user"));
            int userId = users.getUserId();
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                RecruiterProfile recruiterProfile = recruiterProfileRepository.findById(userId).orElse(new RecruiterProfile());
                return recruiterProfile;
            } else {
                JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findById(userId).orElse(new JobSeekerProfile());
                return jobSeekerProfile;
            }
        }

        return null;
    }

    public Users getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users user = usersRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Could not found " + "user"));
            return user;
        }

        return null;
    }

    public Users findByEmail(String currentUsername) {
        return usersRepository.findByEmail(currentUsername).orElseThrow(() -> new UsernameNotFoundException("User not " +
                "found"));
    }

    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

}
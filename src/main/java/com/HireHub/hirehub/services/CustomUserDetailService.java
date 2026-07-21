package com.HireHub.hirehub.services;

import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.repository.UsersRepository;
import com.HireHub.hirehub.util.CustmUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public CustomUserDetailService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user= usersRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Could Not Found User"));
        return new CustmUserDetails(user);
    }
}

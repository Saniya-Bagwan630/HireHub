package com.HireHub.hirehub.repository;

import com.HireHub.hirehub.entity.Users;
import com.HireHub.hirehub.entity.UsersType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersTypeRepository extends JpaRepository<UsersType,Integer> {
}

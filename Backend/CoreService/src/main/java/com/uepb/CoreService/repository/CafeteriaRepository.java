package com.uepb.CoreService.repository;

import com.uepb.CoreService.domain.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface CafeteriaRepository extends JpaRepository<Cafeteria, String> {

    UserDetails findByEmail(String email);
}

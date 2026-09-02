package com.clinc_cms.management.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.clinc_cms.management.models.Admin;
public interface AdminRepository extends JpaRepository<Admin , Long>{

    Admin findByUsername(String username);
    Admin findByEmail(String email);

}

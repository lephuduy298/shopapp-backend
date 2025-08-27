package com.project.shopapp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.shopapp.models.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber);

//    Optional<User> findByEmail(String phoneNumber);

    User findByRefreshTokenAndPhoneNumber(String refreshToken, String subject);
    //SELECT * FROM users WHERE phoneNumber=?

    Optional<User> findById(Long id);

    void deleteById(Long id);

    @Query("""
       SELECT u FROM User u
       WHERE (:roleId IS NULL OR u.role.id = :roleId)
         AND (:isActive IS NULL OR u.active = :isActive)
         AND (
              :keyword IS NULL
              OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(u.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
         )
       """)
    Page<User> findUserWithFilter(@Param("roleId") Long roleId,
                                  @Param("keyword") String keyword,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);

    User findByEmail(String email);



//    User findUserByCommentId(Long commentId);
}

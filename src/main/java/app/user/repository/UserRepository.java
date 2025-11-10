package app.user.repository;

import app.user.model.User;
import app.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
   Optional<User> findByUsername(String username);


   @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
   long countByRole(@Param("role") UserRole role);

    Optional<User> findByEmail(String email);
}




package it.epicode.u5w3d2pratica.repository;

import it.epicode.u5w3d2pratica.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    public Optional<User> findByEmail(String email);
}

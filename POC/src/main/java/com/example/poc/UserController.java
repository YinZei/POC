package com.example.poc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.beans.Encoder;
import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserController(UserRepository u, PasswordEncoder passwordEncoder) {
        userRepository = u;
        encoder = passwordEncoder;
    }

    @PostMapping
    public ReturnUser addUser(@RequestBody POCUser pocUser) {
        pocUser.setPassword(encoder.encode(pocUser.getPassword()));
        if (userRepository.findByUsername(pocUser.getUsername()) != null) {
            throw new UserExistedException(pocUser.getUsername());
        } else {
            return userRepository.save(pocUser).toReturnUser();
        }
    }

    @GetMapping
    public List<ReturnUser> getAllUser() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false).map(POCUser::toReturnUser).toList();
    }

    @GetMapping("/{id}")
    public ReturnUser getUser(@PathVariable long id) {
        return userRepository.findById(id).orElseThrow(() -> new IDNotExistedException(id)).toReturnUser();
    }

    @PutMapping("/{id}")
    public ReturnUser updateUser(@PathVariable long id, @RequestBody POCUser pocUser, @AuthenticationPrincipal POCUser principal) {
        pocUser.setId(id);
        pocUser.setPassword(encoder.encode(pocUser.getPassword()));
        if (principal.getId() == id || principal.getUsername().equals("admin")) {
            return userRepository.save(pocUser).toReturnUser();
        } else throw new AccessDeniedException("Not Allowed To Update");
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id, @AuthenticationPrincipal POCUser principal) {
        if (principal.getId() == id || principal.getUsername().equals("admin")) {
            userRepository.deleteById(id);
        } else throw new AccessDeniedException("Not Allowed To Delete");
    }

    @ExceptionHandler
    ErrorResponse handleUserExisted(UserExistedException e) {
        return ErrorResponse.create(e, HttpStatusCode.valueOf(409), e.getMessage());
    }

    @ExceptionHandler
    ErrorResponse handleUserExisted(IDNotExistedException e) {
        return ErrorResponse.create(e, HttpStatusCode.valueOf(404), e.getMessage());
    }
}

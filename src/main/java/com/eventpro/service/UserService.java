package com.eventpro.service;

import com.eventpro.dto.UserCreateRequest;
import com.eventpro.dto.UserResponse;
import com.eventpro.entity.Administrator;
import com.eventpro.entity.Client;
import com.eventpro.entity.ControlAgent;
import com.eventpro.entity.Organizer;
import com.eventpro.entity.User;
import com.eventpro.exception.BadRequestException;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.mapper.UserMapper;
import com.eventpro.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    //Build User
    private User buildUser(UserCreateRequest request, String email) {
        String userType = request.userType().trim().toUpperCase(Locale.ROOT);
        User user;
        switch (userType) {
            case "ADMINISTRATOR" -> user = new Administrator();
            case "ORGANIZER" -> {
                if (request.organisationName() == null || request.organisationName().isBlank()) {
                    throw new BadRequestException("name of organisation is obligatory");
                }
                Organizer organizer = new Organizer();
                organizer.setOrganisationName(request.organisationName().trim());
                user = organizer;
            }

            case "CLIENT" -> {
                Client client = new Client();
                client.setPhone(request.phone());
                user = client;
            }
            case "CONTROL_AGENT" -> user = new ControlAgent();
            default -> throw new BadRequestException("invalid user type" + userType);

        }
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return user;

    }


    //Get all users
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    //find by id
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id:" + id));
        return userMapper.toResponse(user);
    }

    //get by email
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email obligatory");
        }
        String normalizeedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizeedEmail).orElseThrow(() -> new ResourceNotFoundException("User not found with email:" + normalizeedEmail));
        return userMapper.toResponse(user);
    }

    //Creat User
    public UserResponse creatUser(UserCreateRequest userCreateRequest) {
        if (userCreateRequest == null) {
            throw new BadRequestException("UserCreateRequest cannot be null");
        }
        String name = userCreateRequest.name().trim();
        String email = userCreateRequest.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
        User user = buildUser(userCreateRequest, email);
        user.setName(name);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }
    public UserResponse updateUser(Long id, UserCreateRequest request) {

        if (request == null) {
            throw new BadRequestException("User data is obligatory");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        String name = request.name().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (!user.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        user.setName(name);
        user.setEmail(email);

        if (user instanceof Client client) {
            client.setPhone(request.phone());
        }

        if (user instanceof Organizer organizer) {
            if (request.organisationName() == null
                    || request.organisationName().isBlank()) {
                throw new BadRequestException(
                        "Organization name is obligatory"
                );
            }
            organizer.setOrganisationName(
                    request.organisationName().trim()
            );
        }

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }
    //delete user

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        userRepository.delete(user);
    }

}

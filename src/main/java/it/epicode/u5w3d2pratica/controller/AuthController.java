package it.epicode.u5w3d2pratica.controller;

import it.epicode.u5w3d2pratica.dto.LoginDto;
import it.epicode.u5w3d2pratica.dto.UserDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.model.User;
import it.epicode.u5w3d2pratica.security.JwtTool;
import it.epicode.u5w3d2pratica.service.AuthService;
import it.epicode.u5w3d2pratica.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTool jwtTool;

    @PostMapping("/auth/register")
    public User register(@RequestBody @Validated UserDto userDto, BindingResult bindingResult) throws ValidationException, NotFoundException {
        if(bindingResult.hasErrors()){
            throw  new NotFoundException(bindingResult.getAllErrors().stream().
                    map(objectError -> objectError.getDefaultMessage()).
                    reduce("", (String s, String e)->s+e));
        }

      return userService.saveUser(userDto);

    }
    @GetMapping("/auth/login")
    public String login(@RequestBody  LoginDto loginDto ) throws  NotFoundException {

        return authService.login(loginDto);
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String refreshedToken = jwtTool.refreshToken(token);
        return ResponseEntity.ok(refreshedToken);
    }
}

package org.example.studentdistributionbot.client;

import lombok.RequiredArgsConstructor;
import org.example.studentdistributionbot.dto.RegisterDto;
import org.example.studentdistributionbot.dto.UserRole;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ClientFacade {
    private final RegisterStudentClient registerStudentClient;
    private final ApproveFileLoadingClient approveFileLoadingClient;
    private final UserRoleResolverClient userRoleResolverClient;

    public UserRole getUserRole(Long telegramId) {
        return userRoleResolverClient.getUserRole(telegramId);
    }

    public String registerUser(RegisterDto registerDto) {
        return registerStudentClient.registerUser(registerDto);
    }

    public HttpStatusCode uploadFile(String filename, InputStream stream) {
        return approveFileLoadingClient.uploadFile(filename, stream);
    }
}

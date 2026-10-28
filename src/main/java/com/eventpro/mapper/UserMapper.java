package com.eventpro.mapper;
import com.eventpro.dto.UserResponse;
import com.eventpro.entity.Administrator;
import com.eventpro.entity.Client;
import com.eventpro.entity.ControlAgent;
import com.eventpro.entity.Organizer;
import com.eventpro.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user){
        if(user == null){
            return null;
        }
        String userType;
        String phone =null;
        String organizationName =null;


        if (user instanceof Administrator) {
            userType = "ADMINISTRATOR";
        } else if (user instanceof Organizer organizer) {
            userType = "ORGANIZER";
            organizationName = organizer.getOrganisationName();
        } else if (user instanceof Client client) {
            userType = "CLIENT";
            phone = client.getPhone();
        } else if (user instanceof ControlAgent) {
            userType = "CONTROL_AGENT";
        } else {
            throw new IllegalArgumentException(
                    "Type of user is not recognized"
            );
        }

        return new UserResponse(user.getId(),user.getName(),user.getEmail(),userType,phone,organizationName);
    }
}

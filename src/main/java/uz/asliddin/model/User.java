package uz.asliddin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//Asliddin Kenjaev 12/18/2021 4:17 PM
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    String fullName;
    String userName;
    String phoneNumber;
    double round;
    Role role;
}

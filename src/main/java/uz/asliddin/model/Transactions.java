package uz.asliddin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


//Asliddin Kenjaev 12/22/2021 9:43 AM

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transactions {
    private User user;
    private String currencyType;
    private String amount;
    private String inSum;
    private LocalDateTime localDateTime;
}

package com.majdi.book_network.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder                        //* makes it easier to create instances of the class in a readable way
@AllArgsConstructor
@NoArgsConstructor
@Entity                         //* map user Java Object to a PostgreSQL relational db
public class Token {

    @Id
    @GeneratedValue
    private Integer Id;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;

    @ManyToOne
    @JoinColumn(name= "usreId", nullable = false)
    private User user;



}

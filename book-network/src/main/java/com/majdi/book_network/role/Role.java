package com.majdi.book_network.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.majdi.book_network.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder                                                //* makes it easier to create instances of the class in a readable way
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)          //* enable automatic auditing of an entity’s creation and modification timestamps
public class Role {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String name;

    //! non Owning side of the relation: it only references the join table but doesnt reflect changes on it.
    @ManyToMany(mappedBy = "roles")                   //* each Role can be associated with multiple User entities, and each User can have multiple Role entities
    @JsonIgnore
    private List<User> users;

    @CreatedDate                                        //* automatically sets the field to the current date when a new entity is created and saved for the first time
    @Column(nullable = false, updatable = false)        //* not null and not updatable after the initial insertion
    private LocalDateTime createdDate;
    @LastModifiedDate                                   //* automatically updates the field with the current date
    @Column(insertable = false)                         //* prevents the field from being populated when a new record is inserted. Instead, it is only populated on update
    private LocalDateTime lastModifiedDate;

}

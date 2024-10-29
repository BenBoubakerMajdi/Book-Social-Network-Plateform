package com.majdi.book_network.user;

import com.majdi.book_network.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder                        //* makes it easier to create instances of the class in a readable way
@AllArgsConstructor
@NoArgsConstructor
@Entity                         //* map user Java Object to a PostgreSQL relational db
@Table(name = "_user")          //* the name of the database table that the entity should map to
@EntityListeners(AuditingEntityListener.class)      //* enable automatic auditing of an entity’s creation and modification timestamps
public class User implements UserDetails, Principal {

    @Id                         //* Specify the primary key of the entity
    @GeneratedValue             //* Automatically generated by the db
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    @Column(unique = true)
    private String email;
    private String password;
    private Boolean accountLocked;
    private Boolean enabled;

    //! the Owning Side of the relation: any update on USER affects the join table USER_ROLE
    @ManyToMany(fetch = FetchType.EAGER)
    //* when a User gets retrieved from the database, the associated Role entities are also retrieved immediately, in the same query.
    private List<Role> roles;

    @CreatedDate
    //* automatically sets the field to the current date when a new entity is created and saved for the first time
    @Column(nullable = false, updatable = false)        //* not null and not updatable after the initial insertion
    private LocalDateTime createdDate;
    @LastModifiedDate                                   //* automatically updates the field with the current date
    @Column(insertable = false)
    //* prevents the field from being populated when a new record is inserted. Instead, it is only populated on update
    private LocalDateTime lastModifiedDate;

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private String getFullName() {
        return firstName + " " + lastName;
    }
}
package com.HireHub.hirehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(unique = true)
    private String email;

    @NotEmpty
    private String password;

    @Column(name = "is_active")
    private boolean isActive;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(name = "registration_date")
    private Date registrationDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_type_id", referencedColumnName = "user_type_id")
    private UsersType usersType;

    public Users(int userId, String email, String password, boolean isActive, Date registrationDate, UsersType usersType) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.registrationDate = registrationDate;
        this.usersType = usersType;
    }

    public Users(){}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public UsersType getUsersType() {
        return usersType;
    }

    public void setUsersType(UsersType usersType) {
        this.usersType = usersType;
    }

    @Override
    public String toString() {
        return "Users{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", isActive=" + isActive +
                ", registrationDate=" + registrationDate +
                ", usersType=" + usersType +
                '}';
    }
}

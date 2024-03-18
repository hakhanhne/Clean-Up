package rmit.aad.cleanup.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.firebase.database.IgnoreExtraProperties;

//import java.time.Date;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@IgnoreExtraProperties
public class User {
    public enum Gender {Female, Male, Other};
    public static ArrayList<String> genderList() {
        ArrayList<String> names = new ArrayList<>();
        for(Gender value: Gender.values()) {
            names.add(value.name());
        }
        return names;
    }
    String id;
    String username;
    String password;
    String name;
    String email;
    String phone;
    Gender gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date dob;
    String address;
    String avatar;
    Boolean isSuperUser;

    public User() {};

    public User(String id, String email, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String id, String username, String password, String name, String email, String phone, Gender gender, Date dob, String address, String avatar, Boolean isSuperUser) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.dob = dob;
        this.address = address;
        this.avatar = avatar;
        this.isSuperUser = isSuperUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }
//    public void setDob(String dob) {
//        this.dob = new Date(dob);
//    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getIsSuperUser() {
        return isSuperUser;
    }
    public void setIsSuperUser(Boolean superUser) {
        isSuperUser = superUser;
    }

}

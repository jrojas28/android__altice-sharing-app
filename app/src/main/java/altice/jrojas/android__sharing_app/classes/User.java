package altice.jrojas.android__sharing_app.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import altice.jrojas.android__sharing_app.interfaces.Hashable;

/**
 * Created by jaime on 6/3/2018.
 */

public class User implements Hashable {
    private String id;
    private String profilePictureUrl;
    private String firstName;
    private String lastName;
    private String username;
    private String email;


    public static List<String> passwordVerification(String password, String matchedPassword) {
        List<String> errors = new ArrayList<String>();
        Pattern specialOrDigitPatten = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
        Pattern upperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");

        //First verification: Password must match matchedPassword
        if(!password.equals(matchedPassword)) {
            errors.add("La contrasena debe ser iguales en ambas instancias.");
        }
        //Second verification: Minimum of 6 letters
        if(password.length() < 6) {
            errors.add("La contrasena debe contener 6 o mas caracteres.");
        }
        //Third verification: At least 1 uppercase
        if(!upperCasePatten.matcher(password).find()) {
            errors.add("La contrasena debe contener al menos una mayuscula.");
        }
        //Fourth verification: At least 1 lowercase
        if(!lowerCasePatten.matcher(password).find()) {
            errors.add("La contrasena debe contener al menos una minuscula.");
        }
        //Fifth verification: At least 1 Special Char or Digit
        if(!specialOrDigitPatten.matcher(password).find()) {
            errors.add("La contrasena debe contener al menos un caracter especial o un digito.");
        }
        return errors;
    }

    public User(){}

    public User(String email, String username, String firstName, String lastName) {
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureUrl = null;
    }

    public User(String id, String email, String username, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureUrl = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", this.id);
        user.put("profilePictureUrl", this.profilePictureUrl);
        user.put("firstName", this.firstName);
        user.put("lastName", this.lastName);
        user.put("username", this.username);
        user.put("email", this.email);

        return user;
    }
}

package com.openiptv.code;


/**
 * this class is used to check password in the parent control fragment
 */
public class ParentControlPassword {
    private String password;
    private String confirmPassword;
    private Boolean pass = true;

    public ParentControlPassword(String password, String confirmPassword) {
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public void checkPassword()
    {
        this.checkEqual(this.password, this.confirmPassword);
        this.checkWhiteSpace(this.password, this.confirmPassword);
    }

    private void checkWhiteSpace(String password, String confirmPassword)
    {
        if (password.contains(" ") || confirmPassword.contains(" "))
        {
            this.pass = false;
        }
    }

    private void checkEqual(String password, String confirmPassword)
    {
        if (password.equals(confirmPassword) == false)
        {
            this.pass = false;
        }
    }

    public Boolean getPass() {
        return pass;
    }

    public void setPass(Boolean pass) {
        this.pass = pass;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

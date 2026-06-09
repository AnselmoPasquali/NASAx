package com.example.nasax.ui.login;

/** Simple sealed-like state for the login flow. */
public class LoginState {

    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final String error;

    private LoginState(Status status, String error) {
        this.status = status;
        this.error  = error;
    }

    public static LoginState idle()              { return new LoginState(Status.IDLE,    null);  }
    public static LoginState loading()           { return new LoginState(Status.LOADING, null);  }
    public static LoginState success()           { return new LoginState(Status.SUCCESS, null);  }
    public static LoginState error(String msg)   { return new LoginState(Status.ERROR,   msg);   }

    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError()   { return status == Status.ERROR;   }
    public String  getError()  { return error; }
}

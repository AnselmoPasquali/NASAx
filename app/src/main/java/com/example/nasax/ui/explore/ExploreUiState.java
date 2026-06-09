package com.example.nasax.ui.explore;

import com.example.nasax.domain.model.Apod;
import java.util.List;

public class ExploreUiState {

    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status       status;
    private final List<Apod>   data;
    private final String       errorMessage;

    private ExploreUiState(Status status, List<Apod> data, String errorMessage) {
        this.status       = status;
        this.data         = data;
        this.errorMessage = errorMessage;
    }

    public static ExploreUiState loading()                   { return new ExploreUiState(Status.LOADING, null,  null);    }
    public static ExploreUiState success(List<Apod> data)    { return new ExploreUiState(Status.SUCCESS, data,  null);    }
    public static ExploreUiState error(String msg)           { return new ExploreUiState(Status.ERROR,   null,  msg);     }

    public boolean     isLoading()      { return status == Status.LOADING; }
    public boolean     isSuccess()      { return status == Status.SUCCESS; }
    public boolean     isError()        { return status == Status.ERROR;   }
    public List<Apod>  getData()        { return data;         }
    public String      getErrorMessage(){ return errorMessage;  }
}

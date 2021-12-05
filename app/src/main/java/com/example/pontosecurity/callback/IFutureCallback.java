package com.example.pontosecurity.callback;

import java.util.List;

public interface IFutureCallback {
    void onSuccess();

    void onError(Exception exception);
}

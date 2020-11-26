package com.example.newtattooandroid.ui.seemore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SeemoreViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SeemoreViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
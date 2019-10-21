package com.example.driver;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class App {
    public static void refresh(FragmentActivity fragmentActivity) {
        fragmentActivity.recreate();

    }
}

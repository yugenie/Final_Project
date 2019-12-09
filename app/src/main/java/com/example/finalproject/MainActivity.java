package com.example.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newGame = findViewById(R.id.newGame);
        Intent intent = new Intent(this, NewGameActivity.class);
        newGame.setOnClickListener(unused -> {
            startActivity(intent);
            finish();
        });
        Button gameRule = findViewById(R.id.gameRule);
        gameRule.setOnClickListener(unused -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Game Rule");
            builder.setMessage("I don't know what to say yet.");
            builder.setNegativeButton("Back", null);
            builder.create().show();
        });
    }
}

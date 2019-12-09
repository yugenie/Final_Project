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
            builder.setMessage("Walk around and get to cure some zombies.\nYou need sprays to cure the zombies." +
                    "The blue marker represents the sprays. Once you get within the range of 15 meters of it, you can" +
                    "obtain the spray. The red markers represent the zombies. To cure the zombies, you need to get " +
                    "within the range of 15 meters of them and press cure button if you have sprays. Also, be aware " +
                    "that once you get too close to them (within the range of 10 meters) you are infected and the " +
                    "game is over.\nYou can set the time of the game, the region of the game, and the number of zombies " +
                    "by yourself.\nIf you cure all the zombies within the game time, you win. Once the time is out, the game is over.");
            builder.setNegativeButton("Back", null);
            builder.create().show();
        });
    }
}

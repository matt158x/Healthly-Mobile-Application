package com.example.healthlyandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView goalCalorieText;
    private boolean isIngredientListVisible = false;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ListView ingredientsListView;
    private ArrayList<String> ingredientsList;
    private ArrayAdapter<String> adapter;

    private int caloriesValue = 0;
    private int goalCalories;
    private int proteinsValue = 0;
    private int carbsValue = 0;
    private int fatValue = 0;

    private TextView leftTextView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        goalCalorieText = findViewById(R.id.goal_calorie_text);
        leftTextView = findViewById(R.id.left_text_view);

        ingredientsListView = findViewById(R.id.ingredients_list_view);
        ingredientsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientsList);
        ingredientsListView.setAdapter(adapter);

        findViewById(R.id.breakfast_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MealActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        retrieveGoalCalorie();
        setupIngredientList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                double proteinToAdd = data.getDoubleExtra("proteinToAdd", 0);
                double carbsToAdd = data.getDoubleExtra("carbsToAdd", 0);
                double fatToAdd = data.getDoubleExtra("fatToAdd", 0);
                double caloriesToAdd = data.getDoubleExtra("caloriesToAdd", 0);

                proteinsValue += (int) proteinToAdd;
                carbsValue += (int) carbsToAdd;
                fatValue += (int) fatToAdd;
                caloriesValue += (int) caloriesToAdd;

                TextView proteinsTextView = findViewById(R.id.proteins_text_view);
                TextView carbsTextView = findViewById(R.id.carbs_text_view);
                TextView fatTextView = findViewById(R.id.fat_text_view);
                TextView caloriesTextView = findViewById(R.id.left_text_view);

                proteinsTextView.setText(String.valueOf(proteinsValue));
                carbsTextView.setText(String.valueOf(carbsValue));
                fatTextView.setText(String.valueOf(fatValue));
                caloriesTextView.setText(String.valueOf(caloriesValue));

                updateRemainingCalories();
            }
        }
    }


    private void updateRemainingCalories() {
        int leftCalories = Integer.parseInt(leftTextView.getText().toString());
        int remainingCalories = goalCalories - leftCalories;

        // Ustaw minimalną wartość 0, jeśli wartość wynikowa jest mniejsza
        if (remainingCalories < 0) {
            remainingCalories = 0;
        }

        goalCalorieText.setText(String.valueOf(remainingCalories));
    }

    private void retrieveGoalCalorie() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userReference = mDatabase.child("users").child(userId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    goalCalories = snapshot.child("goalCalorie").getValue(Integer.class);
                    goalCalorieText.setText(String.valueOf(goalCalories));
                    updateRemainingCalories();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Obsługa błędu odczytu z bazy danych
            }
        });
    }

    private void setupIngredientList() {
        DatabaseReference ingredientsReference = mDatabase.child("ingredients");

        ingredientsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ingredientsList.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String ingredientName = childSnapshot.getKey();
                    long calories = childSnapshot.child("calories").getValue(Long.class);
                    long protein = childSnapshot.child("protein").getValue(Long.class);
                    long carbs = childSnapshot.child("carbs").getValue(Long.class);
                    long fat = childSnapshot.child("fat").getValue(Long.class);

                    String ingredientInfo = ingredientName + " - Calories: " + calories + ", Protein: " + protein + "g, Carbs: " + carbs + "g, Fat: " + fat + "g";
                    ingredientsList.add(ingredientInfo);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Obsługa błędu odczytu z bazy danych
            }
        });
    }
}
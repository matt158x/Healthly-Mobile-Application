package com.example.healthlyandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.example.healthlyandroid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.util.Calendar;



public class ProgressActivity extends AppCompatActivity {

    private TextView resultTextView;
    private TextView weightTextView;
    private TextView weightGoalTextView;
    private TextView calorieTextView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private int weight;
    private int height;
    private int age;
    private String userId;
    private String activityLevel; // Dodana deklaracja zmiennej activityLevel

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        resultTextView = findViewById(R.id.resultTextView);
        weightTextView = findViewById(R.id.weight);
        weightGoalTextView = findViewById(R.id.weight_goal);
        calorieTextView = findViewById(R.id.calorieTextView);
        View chartContainer = findViewById(R.id.chart_container);
        View chart2Container = findViewById(R.id.chart2_container);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            DatabaseReference userReference = databaseReference.child("users").child(userId);
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        weight = snapshot.child("weight").getValue(Integer.class);
                        height = snapshot.child("height").getValue(Integer.class);
                        String gender = snapshot.child("gender").getValue(String.class);
                        int goalWeight = snapshot.child("goalWeight").getValue(Integer.class);
                        String goal = snapshot.child("goal").getValue(String.class);
                        age = snapshot.child("age").getValue(Integer.class);
                        activityLevel = snapshot.child("activityLevel").getValue(String.class); // Przypisanie wartości activityLevel
                        if ("GAIN WEIGHT".equals(goal)) {
                            displayGainWeightResult(goalWeight);
                            chartContainer.setVisibility(View.VISIBLE);
                            chart2Container.setVisibility(View.INVISIBLE);
                        } else if ("LOSE WEIGHT".equals(goal)) {
                            displayLoseWeightResult(goalWeight);
                            chartContainer.setVisibility(View.INVISIBLE);
                            chart2Container.setVisibility(View.VISIBLE);
                        }
                        weightTextView.setText(String.valueOf(weight) + " kg");
                        weightGoalTextView.setText(String.valueOf(goalWeight) + " kg");
                        calculateCalorieIntake(gender, goal);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Obsługa błędu odczytu z bazy danych
                }
            });
        }

        Button nextButton = findViewById(R.id.next_button4);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGoalDateToDatabase();
                openHomeActivity();
            }
        });
    }

    private void displayGainWeightResult(int goalWeight) {
        double weeklyWeightGain = 0.5;
        int daysToGoalWeight = (int) Math.ceil((goalWeight - weight) / (weeklyWeightGain / 7));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToGoalWeight);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String month = getMonthName(calendar.get(Calendar.MONTH));
        int year = calendar.get(Calendar.YEAR);
        String resultMessage = dayOfMonth + " " + month + " " + year;
        resultTextView.setText(resultMessage);
    }

    private void displayLoseWeightResult(int goalWeight) {
        double weeklyWeightLoss = 0.5;
        int daysToGoalWeight = (int) Math.ceil((weight - goalWeight) / (weeklyWeightLoss / 7));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToGoalWeight);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String month = getMonthName(calendar.get(Calendar.MONTH));
        int year = calendar.get(Calendar.YEAR);
        String resultMessage = dayOfMonth + " " + month + " " + year;
        resultTextView.setText(resultMessage);
    }

    private String getMonthName(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }

    private void calculateCalorieIntake(String gender, String goal) {
        double bmr;
        if (gender.equals("Male")) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        double activityMultiplier;
        if (activityLevel.equals("Low")) {
            activityMultiplier = 1.2;
        } else if (activityLevel.equals("Medium")) {
            activityMultiplier = 1.5;
        } else {
            activityMultiplier = 1.7;
        }

        double goalCalories;
        if (goal.equals("GAIN WEIGHT")) {
            goalCalories = (bmr * activityMultiplier) + 500;
        } else if (goal.equals("LOSE WEIGHT")) {
            goalCalories = (bmr * activityMultiplier) - 500;
        } else {
            goalCalories = bmr * activityMultiplier ;
        }


        calorieTextView.setText(String.valueOf((int) goalCalories + " kcal"));

        // Zapisanie wartości goalCalories w bazie danych
        DatabaseReference userReference = databaseReference.child("users").child(userId);
        userReference.child("goalCalorie").setValue((int) goalCalories);

    }

    private void saveGoalDateToDatabase() {
        String goalDate = resultTextView.getText().toString();

        DatabaseReference userReference = databaseReference.child("users").child(userId);
        userReference.child("goalDate").setValue(goalDate);
    }

    private void openHomeActivity() {
        Intent intent = new Intent(ProgressActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
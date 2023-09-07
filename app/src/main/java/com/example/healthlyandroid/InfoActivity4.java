package com.example.healthlyandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InfoActivity4 extends AppCompatActivity {

    private NumberPicker heightPicker1;
    private NumberPicker weightPicker1;
    private Button nextButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info4);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        heightPicker1 = findViewById(R.id.heightPicker1);
        weightPicker1 = findViewById(R.id.weightPicker1);
        nextButton = findViewById(R.id.next_button3);

        // Ustawienie zakresów dla NumberPicker
        heightPicker1.setMinValue(100);
        heightPicker1.setMaxValue(200);
        weightPicker1.setMinValue(50);
        weightPicker1.setMaxValue(150);

        // Ustawienie wartości domyślnych dla NumberPicker
        heightPicker1.setValue(170);
        weightPicker1.setValue(70);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToDatabase();
                checkGoalAndProceed();
            }
        });
    }

    private void saveDataToDatabase() {
        String userId = mAuth.getCurrentUser().getUid();
        int height = heightPicker1.getValue();
        int weight = weightPicker1.getValue();

        // Zapisywanie danych do bazy danych
        mDatabase.child("users").child(userId).child("height").setValue(height);
        mDatabase.child("users").child(userId).child("weight").setValue(weight);
    }

    private void checkGoalAndProceed() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userReference = mDatabase.child("users").child(userId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String goal = snapshot.child("goal").getValue(String.class);
                    if ("MAINTAIN WEIGHT".equals(goal)) {
                        int height = snapshot.child("height").getValue(Integer.class);
                        int weight = snapshot.child("weight").getValue(Integer.class);
                        int age = snapshot.child("age").getValue(Integer.class);
                        String gender = snapshot.child("gender").getValue(String.class);

                        double bmr;
                        if (gender.equals("Male")) {
                            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
                        } else {
                            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
                        }

                        double averageMultiplier = 1.5; // Ustalony stały współczynnik dla każdego użytkownika uśredniając aktywnośc fizyczną"
                        double goalCalories = bmr * averageMultiplier; // Przeliczenie zapotrzebowania kalorycznego do utrzymania wagi

                        // Zapisanie wartości goalCalories w bazie danych
                        userReference.child("goalCalorie").setValue((int) goalCalories);

                        openHomeActivity();
                    } else {
                        openGoalWeightActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Obsługa błędu odczytu z bazy danych
            }
        });
    }

    private void openGoalWeightActivity() {
        startActivity(new Intent(InfoActivity4.this, GoalWeightActivity.class));
        finish();
    }

    private void openHomeActivity() {
        startActivity(new Intent(InfoActivity4.this, HomeActivity.class));
        finish();
    }
}
package com.example.healthlyandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.healthlyandroid.R;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MealActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ArrayList<String> ingredientsList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> originalIngredientsList;

    private TextView caloriesTextView;
    private TextView proteinTextView;
    private TextView carbsTextView;
    private TextView fatTextView;
    private TextView ingredientTextView;
    private EditText gramsEditText;

    private int gramsValue = 100;
    private double selectedCalories = 0;
    private double selectedProtein = 0;
    private double selectedCarbs = 0;
    private double selectedFat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ListView ingredientsListView = findViewById(R.id.ingredients_list_view);

        ingredientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedIngredient = ingredientsList.get(position);
                showSelectedIngredient(selectedIngredient);
            }
        });

        originalIngredientsList = new ArrayList<>();
        retrieveIngredients();

        ingredientsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientsList);
        ingredientsListView.setAdapter(adapter);

        EditText searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterIngredients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    ingredientsList.clear();
                    ingredientsList.addAll(originalIngredientsList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        caloriesTextView = findViewById(R.id.calories_text_view);
        proteinTextView = findViewById(R.id.proteins_text_view);
        carbsTextView = findViewById(R.id.carbs_text_view);
        fatTextView = findViewById(R.id.fat_text_view);
        ingredientTextView = findViewById(R.id.ingredient);
        gramsEditText = findViewById(R.id.grams_edit_text);

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double caloriesToAdd = Double.parseDouble(caloriesTextView.getText().toString());
                double proteinToAdd = Double.parseDouble(proteinTextView.getText().toString());
                double carbsToAdd = Double.parseDouble(carbsTextView.getText().toString());
                double fatToAdd = Double.parseDouble(fatTextView.getText().toString());

                Intent intent = new Intent();
                intent.putExtra("caloriesToAdd", caloriesToAdd);
                intent.putExtra("proteinToAdd", proteinToAdd);
                intent.putExtra("carbsToAdd", carbsToAdd);
                intent.putExtra("fatToAdd", fatToAdd);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        Button minusButton = findViewById(R.id.button_minus);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseGramsBy5();
                updateMacros();
            }
        });

        Button plusButton = findViewById(R.id.button_plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseGramsBy5();
                updateMacros();
            }
        });
    }

    private void decreaseGramsBy5() {
        gramsValue -= 5;
        if (gramsValue < 0) {
            gramsValue = 0;
        }
        updateGramsEditText();
    }

    private void increaseGramsBy5() {
        gramsValue += 5;
        updateGramsEditText();
    }

    private void updateGramsEditText() {
        gramsEditText.setText(String.valueOf(gramsValue));
    }

    private void updateMacros() {
        double percentage = (double) gramsValue / 100.0;
        double calories = selectedCalories * percentage;
        double protein = selectedProtein * percentage;
        double carbs = selectedCarbs * percentage;
        double fat = selectedFat * percentage;

        caloriesTextView.setText(String.format("%.1f", calories));
        proteinTextView.setText(String.format("%.1f", protein));
        carbsTextView.setText(String.format("%.1f", carbs));
        fatTextView.setText(String.format("%.1f", fat));
    }

    private void retrieveIngredients() {
        DatabaseReference ingredientsReference = mDatabase.child("ingredients");

        ingredientsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                originalIngredientsList.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String ingredientName = childSnapshot.getKey();
                    originalIngredientsList.add(ingredientName);
                }

                ingredientsList.clear();
                ingredientsList.addAll(originalIngredientsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void showSelectedIngredient(String ingredient) {
        ingredientTextView.setText(ingredient);

        DatabaseReference ingredientReference = mDatabase.child("ingredients").child(ingredient);
        ingredientReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double calories = snapshot.child("calories").getValue(Double.class);
                    Double protein = snapshot.child("protein").getValue(Double.class);
                    Double carbs = snapshot.child("carbs").getValue(Double.class);
                    Double fat = snapshot.child("fat").getValue(Double.class);

                    selectedCalories = calories;
                    selectedProtein = protein;
                    selectedCarbs = carbs;
                    selectedFat = fat;

                    updateMacros();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void filterIngredients(String searchText) {
        ArrayList<String> filteredList = new ArrayList<>();

        if (searchText.isEmpty()) {
            filteredList.addAll(originalIngredientsList);
        } else {
            for (String ingredient : originalIngredientsList) {
                if (ingredient.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(ingredient);
                }
            }
        }

        Collections.sort(filteredList, String.CASE_INSENSITIVE_ORDER);

        ingredientsList.clear();
        ingredientsList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
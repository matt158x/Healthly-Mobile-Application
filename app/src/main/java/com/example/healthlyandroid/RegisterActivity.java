package com.example.healthlyandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmEditText;
    private Button registerButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        backButton = findViewById(R.id.back_arrow_reg);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        confirmEditText = findViewById(R.id.signup_confirm);
        registerButton = findViewById(R.id.register_button);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Zamyka bieżące Activity i wraca do poprzedniego
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirm = confirmEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Proszę wprowadzić adres e-mail i hasło.", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirm)) {
                    Toast.makeText(RegisterActivity.this, "Hasło i potwierdzenie hasła nie są identyczne.", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(email, password);
                }
            }
        });
    }

    private void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Rejestracja powiodła się, dodawanie informacji o użytkowniku do bazy danych
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            databaseReference.child("users").child(userId).child("email").setValue(user.getEmail());
                        }

                        Toast.makeText(RegisterActivity.this, "Użytkownik został pomyślnie utworzony.", Toast.LENGTH_SHORT).show();
                        openGoalActivity1();
                    } else {
                        // Rejestracja nie powiodła się
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this, "Konto o podanym adresie e-mail już istnieje.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Wystąpił błąd podczas rejestracji użytkownika.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openGoalActivity1() {
        // Otwieranie GoalActivity
        // Upewnij się, że GoalActivity jest prawidłowo zdefiniowane jako aktywność docelowa w pliku manifestu Twojej aplikacji
        // Możesz użyć następującego kodu do otwarcia StartActivity:
        Intent intent = new Intent(RegisterActivity.this, GoalActivity1.class);
        startActivity(intent);
        finish();
    }
}
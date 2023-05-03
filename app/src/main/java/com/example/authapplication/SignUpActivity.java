package com.example.authapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    EditText editTextEmail;
    EditText editTextPass;
    EditText editTextName;
    EditText editTextPhone;
    ImageView userImage;
    Button btnSign;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    TextView signInbtn;
    String userAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    Uri fileUserImageURI;
    Uri imageUriUser;
    StorageReference storageReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editEmail);
        editTextPass = findViewById(R.id.editPass);
        editTextName = findViewById(R.id.editName);
        editTextPhone = findViewById(R.id.editPhone);
        userImage = findViewById(R.id.userImage);
        btnSign = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        signInbtn = findViewById(R.id.signIn);
        userImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 100);
        });
        signInbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        });
        btnSign.setOnClickListener(v -> {
            storageReference = firebaseStorage.getReference();
            addNewUser();
        });
    }

    public void addNewUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = editTextEmail.getText().toString();
        String password = editTextPass.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Your Email is Empty , Fill it", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Your Password is Empty , Fill it", Toast.LENGTH_SHORT).show();
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        assert firebaseUser != null;
                        userAuth = firebaseUser.getUid();
                        Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
                        uploadImg();

                    } else {
                        Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    public void uploadImg() {
        String name = editTextName.getText().toString();
        String phone = editTextPhone.getText().toString();
        StorageReference imgRefUser = storageReference.child("Users");
        Bitmap bitmapUser = ((BitmapDrawable) userImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapUser.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataUser = baos.toByteArray();
        StorageReference childRefLogo = imgRefUser.child(System.currentTimeMillis() + "_UserImage");
        UploadTask uploadTask = childRefLogo.putBytes(dataUser);
        uploadTask.addOnFailureListener(exception -> Log.e("TAG", exception.getMessage())).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(SignUpActivity.this, "Image Logo Upload Successfully", Toast.LENGTH_SHORT).show();
            childRefLogo.getDownloadUrl().addOnSuccessListener(uri -> {
                fileUserImageURI = uri;
                addUser(name,phone,userAuth,fileUserImageURI);
            });
        });
    }

    public void addUser(String userName, String userPhone, String userAuth, Uri imgUserUri) {
        Map<String, Object> consultion = new HashMap<>();
        consultion.put("userName", userName);
        consultion.put("userphone", userPhone);
        consultion.put("userAuth", userAuth);
        consultion.put("userImage", imgUserUri);
        db.collection("Users").add(consultion).addOnSuccessListener(documentReference -> {
            Toast.makeText(SignUpActivity.this, "Added successfully", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra("userAuth",userAuth);
            startActivity(intent);
            editTextPhone.setText("");
            editTextName.setText("");
            editTextEmail.setText("");
            editTextPass.setText("");
            userImage.setImageURI(null);

        }).addOnFailureListener(e -> {

        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUriUser = data.getData();
            userImage.setImageURI(imageUriUser);
        }
    }
}
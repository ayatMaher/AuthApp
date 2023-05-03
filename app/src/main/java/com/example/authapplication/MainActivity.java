package com.example.authapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    EditText editTextName;
    EditText editTextPhone;
    ImageView imageView;
    Button btnUpdate;
    Button btnLogOut;
    ProgressBar progressBarUpdate;
    String userAuth;
    String userId;
    Uri fileUserImageURI;
    Uri imageUriUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextName = findViewById(R.id.editNameP);
        editTextPhone = findViewById(R.id.editPhoneP);
        imageView = findViewById(R.id.userImageP);
        btnUpdate = findViewById(R.id.update);
        btnLogOut=findViewById(R.id.btnLogOut);
        progressBarUpdate = findViewById(R.id.progressBarUpdate);
        btnUpdate.setVisibility(View.GONE);
        progressBarUpdate.setVisibility(View.GONE);
        Intent intent = getIntent();
        userAuth = intent.getStringExtra("userAuth");
        FirebaseMessaging.getInstance().subscribeToTopic("profile")
                .addOnCompleteListener(task -> {
                    Log.e("ayat", "Done");
                    if (!task.isSuccessful()) {
                        Log.e("ayat", "false");
                    }
                });
        getUser();
        editTextName.setOnClickListener(v -> btnUpdate.setVisibility(View.VISIBLE));
        editTextPhone.setOnClickListener(v -> btnUpdate.setVisibility(View.VISIBLE));
        btnUpdate.setOnClickListener(v -> {
            storageReference = firebaseStorage.getReference();
            uploadImg();
        });
        imageView.setOnClickListener(v -> {
            btnUpdate.setVisibility(View.VISIBLE);
            Intent intentImg = new Intent();
            intentImg.setType("image/*");
            intentImg.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intentImg, 100);
        });
        btnLogOut.setOnClickListener(v -> {
            startActivity(new Intent(this,SignInActivity.class));
        });
    }

    public void getUser() {
        db.collection("Users").whereEqualTo("userAuth", userAuth).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                userId = documentSnapshot.getId();
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("userName");
                    String phone = documentSnapshot.getString("userphone");
                    String image = documentSnapshot.getString("userImage");
                    Log.e("", "" + name);
                    Log.e("TAG", "onSuccess: " + phone);
                    Picasso.get().load(image).into(imageView);
                    editTextName.setText(name);
                    editTextPhone.setText(phone);
                }
            }
        }).addOnFailureListener(e -> {

        });
    }

    public void uploadImg() {
        progressBarUpdate.setVisibility(View.VISIBLE);
        StorageReference imgRefUser = storageReference.child("Users");
        Bitmap bitmapUser = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapUser.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataUser = baos.toByteArray();
        StorageReference childRefLogo = imgRefUser.child(System.currentTimeMillis() + "_UserImage");
        UploadTask uploadTask = childRefLogo.putBytes(dataUser);
        uploadTask.addOnFailureListener(exception -> Log.e("TAG", exception.getMessage())).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(MainActivity.this, "Image Logo Upload Successfully", Toast.LENGTH_SHORT).show();
            childRefLogo.getDownloadUrl().addOnSuccessListener(uri -> {
                fileUserImageURI = uri;
                updateUser();
            });
        });
    }

    public void updateUser() {
        db.collection("Users").document(userId).update("userName", editTextName.getText().toString(), "userphone", editTextPhone.getText().toString(), "userImage", fileUserImageURI.toString())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,"DocumentSnapshot successfully updated!",Toast.LENGTH_SHORT).show();
                    Log.d("ayat", "DocumentSnapshot successfully updated!");
                    progressBarUpdate.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.d("ayat", "DocumentSnapshot Failed updated!");
                    progressBarUpdate.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUriUser = data.getData();
            imageView.setImageURI(imageUriUser);
        }
    }
}
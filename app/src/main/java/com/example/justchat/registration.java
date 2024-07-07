package com.example.justchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    TextView loginbut;
    EditText rg_username,rg_email,rg_password,rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileimg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("creating your account");
        progressDialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();



        loginbut = findViewById(R.id.loginbutton);
        rg_email = findViewById(R.id.rgEmail);
        rg_username = findViewById(R.id.rgusername);
        rg_password = findViewById(R.id.rgPassword);
        rg_repassword = findViewById(R.id.rgrePassword);
        rg_signup = findViewById(R.id.signupbutton);
        rg_profileimg = findViewById(R.id.profile_image);


        loginbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registration.this,login.class);
                startActivity(intent);
                finish();
            }
        });



        rg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = rg_username.getText().toString();
                String email = rg_email.getText().toString();
                String Password = rg_password.getText().toString();
                String cPassword = rg_repassword.getText().toString();
                String status = "Hey! I'm using this Application";

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(Password) || TextUtils.isEmpty(cPassword)){
                    progressDialog.dismiss();
                    Toast.makeText(registration.this, "Please enter valid information", Toast.LENGTH_SHORT).show();
                } else if (!email.matches(emailPattern)) {
                    progressDialog.dismiss();
                    rg_email.setError("please enter valid email address");
                } else if (Password.length()<6) {
                    progressDialog.dismiss();
                    rg_password.setError("Password must be 6 character or more");
                } else if (!Password.equals(cPassword)) {
                    progressDialog.dismiss();
                    rg_password.setError("Password doesn't matched");
                }else{
                    auth.createUserWithEmailAndPassword(email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String id = task.getResult().getUser().getUid();
                                DatabaseReference reference = database.getReference().child("user").child(id);
                                StorageReference storageReference = storage.getReference().child("Upload").child(id);

                                if (imageURI == null) {
                                    storageReference.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageuri = uri.toString();
                                                        Users users = new Users(id,name,email,Password,imageuri,status);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.show();
                                                                    Intent intent = new Intent(registration.this,MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                                else {
                                                                    Toast.makeText(registration.this, "error in creating user", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }else {
                                    String status = "Hey! I'm using this Application";
                                    imageuri = "https://firebasestorage.googleapis.com/v0/b/just-chat-5a050.appspot.com/o/man.png?alt=media&token=b345fcea-c179-47a8-95a3-5de8d74d212d";
                                    Users users = new Users(id,name,email,Password,imageuri,status);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.show();
                                                Intent intent = new Intent(registration.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else {
                                                Toast.makeText(registration.this, "error in creating user", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }else {
                                Toast.makeText(registration.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });



        rg_profileimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),10);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10){
            if(data != null){
                imageURI = data.getData();
                rg_profileimg.setImageURI(imageURI);
            }
        }
    }
}
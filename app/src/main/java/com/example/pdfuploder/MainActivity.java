package com.example.pdfuploder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static int PICK_PDF_CODE = 2342;
    private TextView mTextMessage;
    TextInputEditText textInputEditText;
    Button button;
    ProgressBar progressBar;

    //firebase storage objects
    StorageReference storageReference;
    DatabaseReference db;

//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FirebaseApp.initializeApp(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseDatabase.getInstance().getReference(Contant.DATABASE_UPLOAD);
        textInputEditText=findViewById(R.id.editName);
        button=findViewById(R.id.button);
        progressBar=findViewById(R.id.progress_circular);

        findViewById(R.id.upload).setOnClickListener(this);


    }

    //it will get pdf from storage
    public void upload(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&& ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;}

            //for file selector

            Intent intent1 = new Intent();
            intent1.setType("application/pdf");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent1, "SELECT FILE"), PICK_PDF_CODE);

        }




    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        //if user can choose the file
        if(requestCode==PICK_PDF_CODE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            //file selected
            if(data.getData()!=null){
                uploadFile(data.getData());
            }
            else{
                Toast.makeText(this,"No file is selected",Toast.LENGTH_LONG).show();
            }
        }
    }


    //This method will upload the data
    private void uploadFile(Uri data) {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference sRef = storageReference.child(Contant.STORAGE_UPLOADS + System.currentTimeMillis() + ".pdf");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        mTextMessage.setText("File Uploaded Successfully");

                        Upload upload = new Upload(textInputEditText.getText().toString(), taskSnapshot.getMetadata().toString());
                        db.child(db.push().getKey()).setValue(upload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    mTextMessage.setText((int)progress+"% Uploading......!!!!");

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.upload:
               startActivity(new Intent(this,ViewActivity.class));
                break;
        }
    }
}

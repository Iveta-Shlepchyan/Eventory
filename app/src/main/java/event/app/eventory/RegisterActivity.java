package event.app.eventory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import event.app.eventory.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends Activity {

    private EditText inputEmail, inputPassword, inputConfirmPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private final String emailPattern = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*"
            + "@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

   /* #TODO fix email validation (cause of problem -- 2 spaces in start of edittext) DDNE
        fix logout and sign in DONE
        make more error handling
        add FacebookSignInActivity
        make app opening animation
        remove error logs  */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("RegisterActivity","started");
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        inputEmail = findViewById(R.id.email_input);
        inputPassword = findViewById(R.id.pass_input);
        inputConfirmPassword = findViewById(R.id.pass_conf_input);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if(mUser!=null)
        {
            Intent i = new Intent(RegisterActivity.this, ContainerActivity.class);
            i.putExtra("fragment", "profile");
            startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }





    public void go_back(View view) {
        finish();
    }

    public void signUp(View view) {
        performAuth();
    }

    public void google_reg(View view) {
        startActivity(new Intent(RegisterActivity.this, GoogleSignInActivity.class));

    }

    public void facebook_reg(View view) {
    }

    public void loginLink(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }




    private void performAuth()
    {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if(!email.matches(emailPattern)){
            showError(inputEmail, "Email is not valid.");
        }
        else if(password.isEmpty() || password.length() < 7){
            showError(inputPassword, "Password must be at least 7 characters long.");
        }
        else if(confirmPassword.isEmpty() || !confirmPassword.equals(password)){
            showError(inputConfirmPassword, "Password do not match.");
        }
        else{
            progressDialog.setMessage("Please wait. Registration...");
            progressDialog.setTitle("Registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Intent intent = new Intent(RegisterActivity.this, ContainerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("fragment", "profile");
                        startActivity(intent);
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

}

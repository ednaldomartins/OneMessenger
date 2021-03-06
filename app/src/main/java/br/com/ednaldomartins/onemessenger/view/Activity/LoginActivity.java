package br.com.ednaldomartins.onemessenger.view.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;

import br.com.ednaldomartins.onemessenger.R;
import br.com.ednaldomartins.onemessenger.business.control.ControllerData;
import br.com.ednaldomartins.onemessenger.business.control.ControllerPermissao;
import br.com.ednaldomartins.onemessenger.data.local.ConfiguracaoPreferencias;
import br.com.ednaldomartins.onemessenger.data.remote.ConfiguracaoFirebase;
import br.com.ednaldomartins.onemessenger.business.model.UsuarioFirebase;


/****************************************************************************************************************************
 * TELA DE LOGIN DO USUARIO                                                                                                 *
 * exemplo do FireBase para autenticacao com a conta Google, disponivel no gitHub no link:                                  *
 * https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth    *
 ****************************************************************************************************************************/
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    //vai sair dessa activity
    ControllerPermissao controllerPermissao = new ControllerPermissao();
    ControllerData controllerData = new ControllerData();



    /****************************************************************************
     * Os metodos onStart e onCreate estao responsaveis em chamar os metodos    *
     * para autenticacao e para atualizar as informacoes do usuario, tanto      *
     * localmente como remotamente.                                             *
     ****************************************************************************/
    @Override
    public void onStart()
    {
        super.onStart();
        //signOut da ultima conta, pra perguntar novamente qual conta do Gmail quer logar
        mGoogleSignInClient.signOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        iniciarViews();
        iniciarListener();


    }



    /****************************************************************************
     * Abaixo estao os metodos necessarios para realizar autenticacao do usuario*
     * no app e atualizar dados do usuario no app.                              *
     ****************************************************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        ConfiguracaoFirebase.getAutenticacao().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            controllerData.salvarUsuario(LoginActivity.this);

                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_constraintFragmentLogin), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /****************************************************************************
     * Abaixo estao os metodos necessarios para trabalhar com a view da tela    *
     * de login do usuario.                                                     *
     ****************************************************************************/

    //colocar aqui os finds necessarios para a tela de login
    protected void iniciarViews ()
    {
        //logoApp = (ImageView) findViewById(R.id.login_logoApp);
        //nomeApp = (TextView) findViewById(R.id.login_nomeApp);
        //botaoSignIn = (SignInButton) findViewById(R.id.login_botaoSignIn);
    }

    //colocar aqui os listener para os elementos da tela de login
    protected void iniciarListener ()
    {
        //findViewById(R.id.login_logoApp).setOnClickListener(this);
        //findViewById(R.id.login_nomeApp).setOnClickListener(this);
        findViewById(R.id.login_botaoSignInGoogle).setOnClickListener(this);
    }

    //implementacao do metodo para executar funcoes dos clicks
    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if(id == R.id.login_botaoSignInGoogle) {
            signIn();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        controllerPermissao.garantirPermissao(this, grantResults, requestCode);
    }
}


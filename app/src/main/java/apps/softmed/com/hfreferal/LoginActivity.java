package apps.softmed.com.hfreferal;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rey.material.widget.EditText;
import com.rey.material.widget.ProgressView;

import java.util.List;

import apps.softmed.com.hfreferal.api.Endpoints;
import apps.softmed.com.hfreferal.base.AppDatabase;
import apps.softmed.com.hfreferal.base.BaseActivity;
import apps.softmed.com.hfreferal.customviews.LargeDiagonalCutPathDrawable;
import apps.softmed.com.hfreferal.dom.objects.AppData;
import apps.softmed.com.hfreferal.dom.objects.HealthFacilities;
import apps.softmed.com.hfreferal.dom.objects.HealthFacilityServices;
import apps.softmed.com.hfreferal.dom.objects.Referal;
import apps.softmed.com.hfreferal.dom.objects.UserData;
import apps.softmed.com.hfreferal.dom.responces.LoginResponse;
import apps.softmed.com.hfreferal.dom.responces.ReferalResponce;
import apps.softmed.com.hfreferal.utils.ServiceGenerator;
import apps.softmed.com.hfreferal.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by issy on 11/23/17.
 */

public class LoginActivity extends BaseActivity {

    private EditText usernameEt;
    private EditText passwordEt;
    private Button loginButton;
    private TextView loginMessages;
    private ProgressView loginProgress;
    private ImageView loginBgImage, background;
    private ImageView tanzaniaLogo, usaidLogo, fhiLogo, deloitteLogo;
    private RelativeLayout credentialCard;

    private String usernameValue, passwordValue;
    private Endpoints.ReferalService referalService;

    // Session Manager Class
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupview();

        // Session Manager
        session = new SessionManager(getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getAuthenticationCredentials()){
                    loginProgress.setVisibility(View.VISIBLE);
                    loginUser();
                }
            }
        });

    }

    private boolean getAuthenticationCredentials(){

        if (usernameEt.getText().length() <= 0){
            Toast.makeText(this, "Username can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }else if (passwordEt.getText().length() <= 0){
            Toast.makeText(this, "Password can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            usernameValue = usernameEt.getText().toString();
            passwordValue = passwordEt.getText().toString();

            return true;

        }
    }

    private void loginUser(){

        loginButton.setText("");
        loginMessages.setVisibility(View.VISIBLE);
        loginMessages.setText("Loggin in..");

        //Use Retrofit to make http request calls
        Endpoints.LoginService loginService =
                ServiceGenerator.createService(Endpoints.LoginService.class, usernameValue, passwordValue, null);
        Call<LoginResponse> call = loginService.basicLogin();
        Log.d("BTC", "calling...");
        call.enqueue(new Callback<LoginResponse >() {


            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful()) {
                    // user object available

                    loginMessages.setTextColor(getResources().getColor(R.color.green_a700));
                    loginMessages.setText("Success..");

                    LoginResponse loginResponse = response.body();
                    Log.d("BTC", "responce is : "+loginResponse.getTeam().getTeam().getLocation().getUuid());

                    UserData userData = new UserData();
                    userData.setUserUIID(loginResponse.getUser().getAttributes().getPersonUUID());
                    userData.setUserName(loginResponse.getUser().getUsername());
                    userData.setUserFacilityId(loginResponse.getTeam().getTeam().getLocation().getUuid());

                    new AddUserData(baseDatabase).execute(userData);

                    session.createLoginSession(
                            loginResponse.getUser().getUsername(),
                            loginResponse.getUser().getAttributes().getPersonUUID(),
                            passwordValue,
                            loginResponse.getTeam().getTeam().getLocation().getUuid());

                    referalService = ServiceGenerator.createService(Endpoints.ReferalService.class, session.getUserName(), session.getUserPass(), session.getKeyHfid());

                    callReferralList();
                    callServices();
                    getHealthFacilities();

                } else {
                    // error response, no access to resource?
                    Log.d("BTC", "responce is error : "+response.toString());
                    loginMessages.setText("Error Loging in");
                    loginMessages.setTextColor(getResources().getColor(R.color.red_a700));
                    loginProgress.setVisibility(View.GONE);
                    loginButton.setText("Login");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                try {
                    Log.d("Error", t.getMessage());
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

        });

    }

    private void callReferralList(){
        Log.d("ReferralCheck", "Begin the call");
        loginMessages.setText("Initializing Data...");
        loginMessages.setTextColor(getResources().getColor(R.color.amber_a700));

        if (session.isLoggedIn()){

            Call<List<ReferalResponce>> call = referalService.getHfReferrals();
            call.enqueue(new Callback<List<ReferalResponce>>() {

                @Override
                public void onResponse(Call<List<ReferalResponce>> call, Response<List<ReferalResponce>> response) {
                    //Here will handle the responce from the server
                    //createDummyReferralData();
                    Log.d("ReferralCheck", response.body().size()+"");

                    addReferralsAsyncTask task = new addReferralsAsyncTask(response.body());
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                @Override
                public void onFailure(Call<List<ReferalResponce>> call, Throwable t) {
                    //Error!
                    //createDummyReferralData();
                    Log.e("", "An error encountered!");
                    Log.d("ReferralCheck", "failed with "+t.getMessage()+" "+t.toString());
                }
            });
        }
    }

    public void callServices(){
        Call<List<HealthFacilityServices>> call = referalService.getAllServices();
        call.enqueue(new Callback<List<HealthFacilityServices>>() {
            @Override
            public void onResponse(Call<List<HealthFacilityServices>> call, Response<List<HealthFacilityServices>> response) {
                Log.d("SAMPLE", response.body()+"");
                List<HealthFacilityServices> receivedServices = response.body();

                List<HealthFacilityServices> servicesList =  baseDatabase.servicesModelDao().getAllServices();
                for (HealthFacilityServices service : servicesList){
                    baseDatabase.servicesModelDao().deleteService(service);
                }

                for (HealthFacilityServices newService : receivedServices){
                    baseDatabase.servicesModelDao().addService(newService);
                }

            }

            @Override
            public void onFailure(Call<List<HealthFacilityServices>> call, Throwable t) {
                Log.d("SAMPLE", t.getMessage());

            }
        });
    }

    public void getHealthFacilities(){

        Call<List<HealthFacilities>> call = referalService.getHealthFacilities();
        call.enqueue(new Callback<List<HealthFacilities>>() {
            @Override
            public void onResponse(Call<List<HealthFacilities>> call, Response<List<HealthFacilities>> response) {
                Log.d("SAMPLE", "HEALTH FACILITIES : "+response.body()+"");
                List<HealthFacilities> receivedHF = response.body();

                List<HealthFacilities> oldHealthFacilities = baseDatabase.healthFacilitiesModelDao().getAllHealthFacilities();
                for (HealthFacilities hf : oldHealthFacilities){
                    baseDatabase.healthFacilitiesModelDao().deleteHealthFacility(hf);
                }

                for (HealthFacilities hf : receivedHF){
                    baseDatabase.healthFacilitiesModelDao().addHealthFacility(hf);
                }

            }

            @Override
            public void onFailure(Call<List<HealthFacilities>> call, Throwable t) {
                Log.d("SAMPLE", "HEALTH FACILITIES : "+t.getMessage());
            }
        });

    }

    public class addReferralsAsyncTask extends AsyncTask<Void, Void, Void> {

        List<ReferalResponce> results;

        addReferralsAsyncTask(List<ReferalResponce> responces){
            this.results = responces;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginMessages.setText("Finalizing..");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            for (ReferalResponce mList : results){
                baseDatabase.patientModel().addPatient(mList.getPatient());
                for (Referal referal : mList.getPatientReferalList()){
                    baseDatabase.referalModel().addReferal(referal);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loginMessages.setText("");
            loginProgress.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

        }
    }

    class AddUserData extends AsyncTask<UserData, Void, Void>{

        AppDatabase database;

        AddUserData(AppDatabase db){
            this.database = db;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(UserData... userData) {
            database.userDataModelDao().addUserData(userData[0]);
            return null;
        }
    }

    private void setupview(){

        credentialCard = (RelativeLayout) findViewById(R.id.credential_card);
        credentialCard.setBackground(new LargeDiagonalCutPathDrawable(50));

        tanzaniaLogo = (ImageView) findViewById(R.id.tanzania_logo);

        usaidLogo = (ImageView) findViewById(R.id.usaid_logo);
        fhiLogo = (ImageView) findViewById(R.id.fhi_logo);
        deloitteLogo = (ImageView) findViewById(R.id.deloitte_logo);

        background = (ImageView) findViewById(R.id.background);
        Glide.with(this).load(R.drawable.bg2).into(background);

        loginMessages = (TextView) findViewById(R.id.login_messages);
        loginMessages.setVisibility(View.GONE);

        loginProgress = (ProgressView) findViewById(R.id.login_progress);
        loginProgress.setVisibility(View.GONE);

        loginButton = (Button) findViewById(R.id.login_button);

        usernameEt = (EditText) findViewById(R.id.username_et);
        passwordEt = (EditText) findViewById(R.id.password_et);
    }

}

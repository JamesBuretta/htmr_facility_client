package apps.softmed.com.hfreferal.utils;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import apps.softmed.com.hfreferal.api.Endpoints;
import apps.softmed.com.hfreferal.base.BaseActivity;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * Created by issy on 2/1/17.
 */

public class FirebaseInstanseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("", "Firebase Device ID : "+refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){

        SessionManager sess = BaseActivity.session;

        String datastream = "";
        JSONObject object   = new JSONObject();
        RequestBody body;

        try {
            object.put("user_uuid", "");
            object.put("device_id", token);
            object.put("hf_uuid", "");

            datastream = object.toString();

            body = RequestBody.create(MediaType.parse("application/json"), datastream);

        }catch (Exception e){
            e.printStackTrace();
            body = RequestBody.create(MediaType.parse("application/json"), datastream);
        }


        if (sess.isLoggedIn()){
            Endpoints.NotificationServices notificationServices = ServiceGenerator.createService(Endpoints.NotificationServices.class, sess.getUserName(), sess.getUserPass());
            retrofit2.Call call = notificationServices.registerDevice(body);
            call.enqueue(new retrofit2.Callback() {
                @Override
                public void onResponse(retrofit2.Call call, Response response) {
                    Log.d("", response.body().toString());
                }

                @Override
                public void onFailure(retrofit2.Call call, Throwable t) {
                    Log.d("", t.getMessage());
                }
            });
        }
    }

}

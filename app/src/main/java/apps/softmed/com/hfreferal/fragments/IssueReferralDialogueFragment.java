package apps.softmed.com.hfreferal.fragments;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

import apps.softmed.com.hfreferal.R;
import apps.softmed.com.hfreferal.base.AppDatabase;
import apps.softmed.com.hfreferal.base.BaseActivity;
import apps.softmed.com.hfreferal.dom.objects.Patient;
import apps.softmed.com.hfreferal.dom.objects.PostOffice;
import apps.softmed.com.hfreferal.dom.objects.Referal;
import fr.ganfra.materialspinner.MaterialSpinner;

import static apps.softmed.com.hfreferal.utils.constants.ENTRY_NOT_SYNCED;
import static apps.softmed.com.hfreferal.utils.constants.HIV_SERVICE;
import static apps.softmed.com.hfreferal.utils.constants.HIV_SERVICE_ID;
import static apps.softmed.com.hfreferal.utils.constants.MALARIA_SERVICE;
import static apps.softmed.com.hfreferal.utils.constants.MALARIA_SERVICE_ID;
import static apps.softmed.com.hfreferal.utils.constants.POST_DATA_TYPE_ENCOUNTER;
import static apps.softmed.com.hfreferal.utils.constants.POST_DATA_TYPE_REFERRAL;
import static apps.softmed.com.hfreferal.utils.constants.REFERRAL_STATUS_NEW;
import static apps.softmed.com.hfreferal.utils.constants.SOURCE_HF;
import static apps.softmed.com.hfreferal.utils.constants.TB_SERVICE;
import static apps.softmed.com.hfreferal.utils.constants.TB_SERVICE_ID;

/**
 * Created by issy on 1/6/18.
 *
 * @issyzac issyzac.iz@gmail.com
 * On Project HFReferralApp
 */

public class IssueReferralDialogueFragment extends DialogFragment{

    private TextView patientNames;
    private MaterialSpinner spinnerService, spinnerToHealthFacility;
    private LinearLayout tbIndicatorWrapper, hivIndicatorsWrapper;
    private CheckBox twoWeeksCough, bloodCough, weightLoss, severeSweating, fever, involvedInRiskySituations, doesNotTrustPartner, pregnant, hivPregnant;
    private EditText referralReasons, otherClinicalInformation;
    private Button cancelButton, issueButton;
    private View tbSeparator, hivSeparator;

    private Patient currentPatient;
    private boolean twoWeeksCoughFlag, bloodCoughFlag, weightLossFlag, severeSweatingFlag, feverFlag, involvedInRiskySituationsFlag;
    private boolean doesNotTrustPartnerFlag, pregnantFlag, hivPregnantFlag;
    private String referralReasonsValue, otherClinicalInformationValue, serviceID, toHealthFacilityID;
    private boolean currentServiceTb, currentServiceHiv;

    private AppDatabase database;

    public IssueReferralDialogueFragment() {}

    public static IssueReferralDialogueFragment newInstance(Patient patient) {
        Bundle args = new Bundle();
        IssueReferralDialogueFragment fragment = new IssueReferralDialogueFragment();
        args.putSerializable("currentPatient", patient);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_dialogue_layout, container);
        database = AppDatabase.getDatabase(this.getContext());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //this.getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        currentPatient = (Patient) getArguments().getSerializable("currentPatient");

        setupviews(view);

        if (currentPatient != null){
            patientNames.setText(
                    currentPatient.getPatientFirstName()+" "+
                    currentPatient.getPatientMiddleName()+" "+
                    currentPatient.getPatientSurname());
        }

        String[] servicesList = {HIV_SERVICE, TB_SERVICE, MALARIA_SERVICE };
        String[] hflist = {"Lugalo Hospital", "Kaloleni Dispensary", "Mount Meru Hospital" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, servicesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerService.setAdapter(adapter);

        ArrayAdapter<String> hfAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, hflist);
        hfAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToHealthFacility.setAdapter(hfAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        issueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentInputs()){
                    createReferralObject();
                }
            }
        });

        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("IssueReferral", i+"");
                if (i == 0){
                    tbIndicatorWrapper.setVisibility(View.GONE);
                    tbSeparator.setVisibility(View.GONE);
                    hivIndicatorsWrapper.setVisibility(View.VISIBLE);
                    hivSeparator.setVisibility(View.VISIBLE);
                    currentServiceTb = true;
                    currentServiceHiv = false;
                }else if (i==1){
                    tbIndicatorWrapper.setVisibility(View.VISIBLE);
                    tbSeparator.setVisibility(View.VISIBLE);
                    hivIndicatorsWrapper.setVisibility(View.GONE);
                    hivSeparator.setVisibility(View.GONE);
                    currentServiceTb = false;
                    currentServiceHiv = true;
                }
                else {
                    tbIndicatorWrapper.setVisibility(View.GONE);
                    hivIndicatorsWrapper.setVisibility(View.GONE);
                    tbSeparator.setVisibility(View.GONE);
                    hivSeparator.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerToHealthFacility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("IssueReferral", i+" : to health Facility");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void createReferralObject(){

        Referal referal = new Referal();

        long range = 1234567L;
        Random r = new Random();
        long number = (long)(r.nextDouble()*range);

        referal.setReferral_id(number+"");
        referal.setId(number);
        referal.setPatient_id(currentPatient.getPatientId());
        referal.setCommunityBasedHivService("");
        referal.setReferralReason(referralReasonsValue);
        referal.setServiceId(getServiceID(serviceID));
        referal.setCtcNumber("");
        referal.setHas2WeeksCough(twoWeeksCoughFlag);
        referal.setHasBloodCough(bloodCoughFlag);
        referal.setHasSevereSweating(severeSweatingFlag);
        referal.setHasFever(feverFlag);
        referal.setHadWeightLoss(weightLossFlag);
        referal.setServiceProviderUIID("");
        referal.setServiceProviderGroup("");
        referal.setVillageLeader("");
        referal.setReferralDate(new Date());
        referal.setFacilityId(toHealthFacilityID);
//        referal.setFromFacilityId(BaseActivity.session.getKeyHfid());
        referal.setFromFacilityId("001"); //This is a fake facility ID for this logged in user
        referal.setReferralStatus(REFERRAL_STATUS_NEW);
        referal.setReferralSource(SOURCE_HF);
        referal.setCreatedAt(new Date());
        referal.setUpdatedAt(new Date());

        SaveReferral saveReferral = new SaveReferral(database);
        saveReferral.execute(referal);

    }

    private int getServiceID(String serviceIDInString){
        if (serviceIDInString.equals(TB_SERVICE)){
            return TB_SERVICE_ID;
        }else if (serviceIDInString.equals(HIV_SERVICE)){
            return HIV_SERVICE_ID;
        }else if (serviceIDInString.equals(MALARIA_SERVICE)){
            return MALARIA_SERVICE_ID;
        }else {
            return 0;
        }
    }

    private boolean getCurrentInputs(){
        Log.d("IssueReferral", spinnerService.getSelectedItemPosition()+" Service Selected Position");
        Log.d("IssueReferral", spinnerToHealthFacility.getSelectedItemPosition()+" To Facility Selected Position");
        if (spinnerService.getSelectedItemPosition() == 0){
            toastThis("Chagua huduma ya kutoa rufaa");
            return false;
        }else {
            serviceID = (String) spinnerService.getSelectedItem();
        }
        if (spinnerToHealthFacility.getSelectedItemPosition() == 0){
            toastThis("Chagua Kituo cha afya cha kutuma rufaa");
            return false;
        }else {
            toHealthFacilityID = (String) spinnerToHealthFacility.getSelectedItem();
        }

        if (referralReasons.getText().toString().isEmpty()){
            toastThis("Tafadhali andika sababu za rufaa");
            return false;
        }else {
            referralReasonsValue = referralReasons.getText().toString();
        }

        if (currentServiceTb){
            twoWeeksCoughFlag = twoWeeksCough.isChecked();
            bloodCoughFlag = bloodCough.isChecked();
            weightLossFlag = weightLoss.isChecked();
            severeSweatingFlag = severeSweating.isChecked();
            feverFlag = fever.isChecked();
            pregnantFlag = pregnant.isChecked();
        }

        if (currentServiceHiv) {
            involvedInRiskySituationsFlag = involvedInRiskySituations.isChecked();
            doesNotTrustPartnerFlag = doesNotTrustPartner.isChecked();
            hivPregnantFlag = hivPregnant.isChecked();
        }

        return true;

    }

    private void toastThis(String message){
        Toast.makeText(IssueReferralDialogueFragment.this.getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void setupviews(View v){

        patientNames = (TextView) v.findViewById(R.id.patient_name);

        spinnerService = (MaterialSpinner) v.findViewById(R.id.spin_service);
        spinnerToHealthFacility = (MaterialSpinner) v.findViewById(R.id.spin_to_facility);

        tbIndicatorWrapper = (LinearLayout) v.findViewById(R.id.tb_indicators_wrapper);
        hivIndicatorsWrapper = (LinearLayout) v.findViewById(R.id.hiv_indicators_wrapper);

        twoWeeksCough = (CheckBox) v.findViewById(R.id.two_weeks_cough);
        bloodCough = (CheckBox) v.findViewById(R.id.blood_cough);
        weightLoss = (CheckBox) v.findViewById(R.id.weight_loss);
        severeSweating = (CheckBox) v.findViewById(R.id.severe_sweating);
        fever = (CheckBox) v.findViewById(R.id.fever);
        involvedInRiskySituations = (CheckBox) v.findViewById(R.id.risky_situation);
        doesNotTrustPartner = (CheckBox) v.findViewById(R.id.partner_distrust);
        pregnant = (CheckBox) v.findViewById(R.id.pregnant);
        hivPregnant = (CheckBox)v.findViewById(R.id.hiv_pregnant);

        referralReasons = (EditText) v.findViewById(R.id.referal_reasons_text);
        otherClinicalInformation = (EditText) v.findViewById(R.id.other_clinical_information_text);

        cancelButton = (Button) v.findViewById(R.id.cancel_button);
        issueButton = (Button) v.findViewById(R.id.tuma_button);

        tbSeparator = (View) v.findViewById(R.id.tb_separator);
        hivSeparator = (View) v.findViewById(R.id.hiv_separator);

    }

    class SaveReferral extends AsyncTask<Referal, Void, Void>{

        AppDatabase database;

        SaveReferral(AppDatabase db){
            this.database = db;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            toastThis("Referral Stored Successfully");
        }

        @Override
        protected Void doInBackground(Referal... referrals) {

            database.referalModel().addReferal(referrals[0]);

            PostOffice postOffice = new PostOffice();
            postOffice.setPost_id(referrals[0].getReferral_id());
            postOffice.setPost_data_type(POST_DATA_TYPE_REFERRAL);
            postOffice.setSyncStatus(ENTRY_NOT_SYNCED);

            database.postOfficeModelDao().addPostEntry(postOffice);

            return null;
        }
    }

}

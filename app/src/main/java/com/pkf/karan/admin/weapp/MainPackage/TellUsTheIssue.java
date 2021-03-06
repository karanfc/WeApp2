package com.pkf.karan.admin.weapp.MainPackage;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pkf.karan.admin.weapp.R;
import com.pkf.karan.admin.weapp.UserInformation;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TellUsTheIssue extends AppCompatActivity {

    CheckBox startDate, endDate, clientLocation, engagementType, bench, holiday;
    TextView title, subtitle, startDateTitle, endDateTitle, clientLocationTitle, engagementTypeTitle, benchTitle, holidayTitle;
    Button send;
    Typeface font;
    EditText remark;
    String rejectionType, userId, entityId, reason, remarks;
    ProgressBar progressBar;
    UserInformation userInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tell_us_the_issue);
        userInfo = (UserInformation)getApplicationContext();

        rejectionType = getIntent().getStringExtra("rejectionType");
        userId = getIntent().getStringExtra("userId");
        entityId = getIntent().getStringExtra("entityId");

        Log.e("entity", entityId);

        font = Typeface.createFromAsset(getAssets(),  "fonts/OpenSans-Regular.ttf");
        initializeViews();
    }

    private void initializeViews() {

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        startDate = (CheckBox)findViewById(R.id.startDate);
        endDate = (CheckBox)findViewById(R.id.endDate);
        clientLocation = (CheckBox)findViewById(R.id.clientLocation);
        engagementType = (CheckBox)findViewById(R.id.engagementType);
        bench = (CheckBox)findViewById(R.id.bench);
        holiday = (CheckBox)findViewById(R.id.Holiday);

        title = (TextView)findViewById(R.id.title);
        title.setTypeface(font);

        subtitle = (TextView)findViewById(R.id.subtitle);
        subtitle.setTypeface(font);

        startDateTitle = (TextView)findViewById(R.id.startDateTitle);
        startDateTitle.setTypeface(font);

        endDateTitle = (TextView)findViewById(R.id.endDateTitle);
        endDateTitle.setTypeface(font);

        clientLocationTitle = (TextView)findViewById(R.id.clientLocationTitle);
        clientLocationTitle.setTypeface(font);

        engagementTypeTitle = (TextView)findViewById(R.id.engagmentTypeTitle);
        engagementTypeTitle.setTypeface(font);

        benchTitle = (TextView)findViewById(R.id.benchTitle);
        benchTitle.setTypeface(font);

        holidayTitle = (TextView)findViewById(R.id.holidayTitle);
        holidayTitle.setTypeface(font);

        send = (Button)findViewById(R.id.send);
        send.setTypeface(font);

        remark = (EditText)findViewById(R.id.remark);
        remark.setTypeface(font);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startDate.isChecked() | endDate.isChecked() | clientLocation.isChecked() | engagementType.isChecked() | bench.isChecked() | holiday.isChecked() && remark.getText().length() > 0)
                {
                    remarks = remark.getText().toString();
                    reason = "";
                    if(startDate.isChecked())   reason = reason + "Start Date,";
                    if(endDate.isChecked())   reason = reason + "End Date,";
                    if(clientLocation.isChecked())   reason = reason + "Client Location,";
                    if(engagementType.isChecked())   reason = reason + "Type Of Engagement,";
                    if(bench.isChecked())   reason = reason + "Bench,";
                    if(holiday.isChecked()) reason = reason + "Holiday,";
                    reason = reason.substring(0, reason.length()-1);
                    Log.e("reason", reason);

                    showConfirmDialog(rejectionType, userId, entityId, reason, remarks);
                }
                else
                {
                    showAlertDialog();
                }
            }
        });
    }

    private void showConfirmDialog(final String rejectionType, final String userId, final String entityId, final String reason, final String remarks) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to submit the request?");

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                progressBar.setVisibility(View.VISIBLE);
                submitIssue(rejectionType, userId, entityId, reason, remarks);
            }
        });

        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(font);
    }

    private void submitIssue(String rejectionType, String userId, String entityId, String reason, String remarks)
    {
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("rejectionType", rejectionType)
                .add("userId", userId)
                .add("entityId", entityId)
                .add("reason", reason)
                .add("remarks", remarks)
                .build();


        final Request request = new Request.Builder()
                .url(userInfo.getServerUrl()+"/api/AllocationApi/RejectDailyAllocation")
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseString = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(responseString.equals("true"))
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "You have already submitted a request.", Toast.LENGTH_SHORT).show();
                            TellUsTheIssue.this.finish();
                        }

                        else
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Request submitted successfully", Toast.LENGTH_SHORT).show();
                            TellUsTheIssue.this.finish();
                        }


                    }
                });
            }
        });

    }

    private void showAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Please select atleast one issue and enter a remark.");

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(font);

    }
}

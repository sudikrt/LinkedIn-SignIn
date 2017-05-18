package com.example.foolish_guy.linkedinsigntest;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.schema.Person;
import com.google.code.linkedinapi.schema.Skill;
import com.google.code.linkedinapi.schema.Skills;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.StringTokenizer;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends AppCompatActivity {

    private LinkedInApiClient client;
    private LinkedInAccessToken accessToken;

    private AccessToken twitterAccessToken;
    private Twitter twitter;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        generateHash();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button btnLinkedinMain = (Button) findViewById(R.id.btnLinkedin);
        btnLinkedinMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkedInLogin(NETWORK.Linkedin.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void generateHash () {
        try {
            PackageInfo info = getPackageManager()
                    .getPackageInfo("com.example.foolish_guy.linkedinsigntest", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());

                Log.e(TAG, Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    LinkedInSignInDialog linkedInSignInDialog;
    private void linkedInLogin(String domain) {

        linkedInSignInDialog = new LinkedInSignInDialog(MainActivity.this, domain);
        linkedInSignInDialog.show();

        linkedInSignInDialog.setOnverifyListener(new LinkedInSignInDialog.OnVerifyListener() {

            @SuppressLint("NewApi")
            @Override
            public void onVerify(String verifier, String netWork) {

                if (netWork.equalsIgnoreCase(NETWORK.Linkedin.toString())) {

                    try {
                        accessToken = LinkedInSignInDialog.oAuthService.getOAuthAccessToken(LinkedInSignInDialog.requestToken, verifier);
                        client = LinkedInSignInDialog.clientFactory.createLinkedInApiClient(accessToken);

                        AppLogs.printLogs("LinkedinSample", "ln_access_token: " + accessToken.getToken());

                        com.google.code.linkedinapi.schema.Person person = null;

                        final Person user_Profile = client.getProfileForCurrentUser(EnumSet.of(ProfileField.ID));
                        person = client.getProfileById(user_Profile.getId(), EnumSet.of(
                                ProfileField.FIRST_NAME,
                                ProfileField.LAST_NAME,
                                ProfileField.PICTURE_URL,
                                ProfileField.INDUSTRY,
                                ProfileField.MAIN_ADDRESS,
                                ProfileField.HEADLINE,
                                ProfileField.SUMMARY,
                                ProfileField.POSITIONS,
                                ProfileField.EDUCATIONS,
                                ProfileField.LANGUAGES,
                                ProfileField.SKILLS,
                                ProfileField.INTERESTS,
                                ProfileField.PHONE_NUMBERS,
                                ProfileField.DATE_OF_BIRTH,
                                ProfileField.PUBLIC_PROFILE_URL));


                        /*person = client.getProfileForCurrentUser(EnumSet.of(
                                ProfileField.ID, ProfileField.FIRST_NAME,
                                ProfileField.LAST_NAME, ProfileField.HEADLINE,
                                ProfileField.SUMMARY, ProfileField.PUBLIC_PROFILE_URL,
                                ProfileField.INDUSTRY, ProfileField.PICTURE_URL,
                                ProfileField.LOCATION, ProfileField.LOCATION_NAME,
                                ProfileField.EDUCATIONS, ProfileField.EDUCATIONS_ACTIVITIES,
                                ProfileField.LANGUAGES, ProfileField.EDUCATIONS_DEGREE,
                                ProfileField.INTERESTS, ProfileField.SKILLS_PROFICIENCY_NAME,
                                ProfileField.SKILLS_ID,ProfileField.SKILLS_SKILL, ProfileField.SKILLS_YEARS,
                                ProfileField.SKILLS_SKILL_NAME, ProfileField.SKILLS_YEARS_NAME, ProfileField.SKILLS_YEARS,
                                ProfileField.SKILLS, ProfileField.SKILLS_PROFICIENCY));*/
                        AppLogs.printLogs("linkedin id", " :: " + person.getId());

                        AppLogs.printLogs("Name :", person.getFirstName());
                        AppLogs.printLogs("HeadLine", person.getHeadline());
                        AppLogs.printLogs("Last Name :", person.getLastName());
                        AppLogs.printLogs("Public profile :", person.getPublicProfileUrl());

                        Skills skills = person.getSkills();
                        for (Skill skill : skills.getSkillList()) {
                            AppLogs.printLogs("Skills : ", skill.getSkill().getName());
                        }


                        Toast.makeText(getApplication(),
                                "Success \n " + "Name : " + person.getFirstName()
                                        + "\nLast Name : " + person.getLastName(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (netWork.equalsIgnoreCase(NETWORK.Twitter.toString())) {
                    try {
                        twitter = LinkedInSignInDialog.twitter;
                        TwitterFactory factory = LinkedInSignInDialog.factory;
                        twitterAccessToken = LinkedInSignInDialog.twitter
                                .getOAuthAccessToken(LinkedInSignInDialog.twitterRequestToken, verifier);
                        Long userID =  twitterAccessToken.getUserId();
                        User user = twitter.showUser(userID);
                        AppLogs.printLogs("Name :", user.getName());
                        AppLogs.printLogs("URl :", String.valueOf(user.getURL()));
                        AppLogs.printLogs("URl :", String.valueOf(user.getURLEntity()));
                        AppLogs.printLogs("Use ID:", String.valueOf(userID));

                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

package com.cs477.courseproject_mshah22;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class VoiceCommandActivity extends AppCompatActivity {
    private static final int SPEECH_REQUEST_CODE = 0;

    private TextView introTextView;
    private String reminderDescription;
    private String reminderDay;
    private String reminderTime;
    private int currentStep;
    private boolean remindersMode;
    private boolean calendarMode;

    private Reminder reminder;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_command);
        introTextView = (TextView)findViewById(R.id.commandsInstructionView);
        currentStep = 0;
    }

    // Create an intent that can start the Speech Recognizer activity
    public void displaySpeechRecognizer(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            System.out.println(spokenText);

            Toast.makeText(getApplicationContext(),spokenText,Toast.LENGTH_SHORT).show();
            if(spokenText.equalsIgnoreCase("Create Reminder")){
                remindersMode = true;
                calendarMode = false;
                currentStep++;
                introTextView.setText(R.string.reminderStep1);
            }else if(spokenText.equalsIgnoreCase("Create Calendar Event")){
                calendarMode = true;
                remindersMode = false;
                currentStep++;
            }else if(spokenText.equalsIgnoreCase("Next")){
                currentStep = 1;
                if(remindersMode){
                    remindersMode = true;
                    introTextView.setText(R.string.reminderStep1);
                }else if(calendarMode){
                    calendarMode = true;
                }
            }else if(spokenText.equalsIgnoreCase("Exit")){
                remindersMode = false;
                calendarMode = false;
                currentStep = 0;
                introTextView.setText(R.string.commandsIntro);
                Reminder reminder = new Reminder(reminderDescription,reminderDay,reminderTime);
                System.out.println(reminder);

            }else {
                if (remindersMode) {
                    processReminders(spokenText);
                }
                if (calendarMode) {
                    processCalendarEvent(spokenText);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processReminders(String input)
    {
        if(currentStep == 1){
            introTextView.setText(R.string.reminderStep2);
            System.out.println("Description: " +input);
            reminderDescription = input;
        }else if(currentStep == 2){
            introTextView.setText(R.string.reminderStep3);
            reminderDay = input;
            System.out.println("Date: " +input);
        }else if(currentStep == 3){
            introTextView.setText(R.string.reminderStep4);
            reminderTime = input;
            System.out.println("Time: " +input);
        }else if(currentStep == 4){
            currentStep = 0;
        }
        currentStep++;
    }
    private void processCalendarEvent(String input)
    {

    }


    private class Reminder {

        private String desc, date, time;

        public Reminder(String desc, String date, String time)
        {
            this.desc = desc;
            this.date = date;
            this.time = time;
        }

        public String getDesc() {
            return desc;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "Reminder{" +
                    "desc='" + desc + '\'' +
                    ", date='" + date + '\'' +
                    ", time='" + time + '\'' +
                    '}';
        }
    }
}
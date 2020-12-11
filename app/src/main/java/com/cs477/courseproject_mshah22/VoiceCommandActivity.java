package com.cs477.courseproject_mshah22;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

public class VoiceCommandActivity extends AppCompatActivity {
    private static final int SPEECH_REQUEST_CODE = 0;
    final String GROUP_REMINDERS = "com.cs477.courseproject_mshah22.reminders";
    private static int NOTIFICATION_ID = 1;

    private TextView introTextView, speechInputView;
    private String calendarDescription;
    private String availability, calendarTitle;
    private String calendarEventLocation;
    private int currentStep;
    private boolean remindersMode, allDay;
    private boolean calendarMode;
    private View view;

    private String savedReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_command);
        introTextView = (TextView)findViewById(R.id.commandsInstructionView);
        currentStep = 0;

    }

    public void displaySpeechRecognizer(View view) {
        this.view = view;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            System.out.println(spokenText);

            if(spokenText.equalsIgnoreCase("Create Reminder")){
                remindersMode = true;
                calendarMode = false;
                currentStep++;
                introTextView.setText(R.string.reminderStep1);
            }else if(spokenText.equalsIgnoreCase("Create Calendar Event")){
                calendarMode = true;
                remindersMode = false;
                currentStep++;
                introTextView.setText(R.string.calendarStep1);
            }else if(spokenText.equalsIgnoreCase("Next")){
                currentStep = 1;
                if(remindersMode){
                    remindersMode = true;
                    introTextView.setText(R.string.reminderStep1);
                }else if(calendarMode){
                    calendarMode = true;
                    introTextView.setText(R.string.calendarStep1);
                }
            }else if(spokenText.equalsIgnoreCase("Exit")){
                remindersMode = false;
                calendarMode = false;
                currentStep = 0;
                introTextView.setText(R.string.commandsIntro);
            }else if(remindersMode || calendarMode){
                if (remindersMode) {
                    processReminders(spokenText);
                }
                if (calendarMode) {
                    processCalendarEvent(spokenText);
                }
            }else{
                Snackbar.make(this.view,"Invalid Command: " + spokenText, Snackbar.LENGTH_LONG)
                        .show();
                System.out.println("Not recognized");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processReminders(String input)
    {
        if(currentStep == 1){
            savedReminder = input;
            String str = getResources().getString(R.string.reminderConfirm) + "\n" + input;
            introTextView.setText(str);
        }else if(currentStep == 2){
            if(input.equalsIgnoreCase("yes")) {
                introTextView.setText(R.string.reminderStep4);
                finalizeReminder(savedReminder);
                currentStep = 0;
            }else{
                introTextView.setText(R.string.reminderRepeat);
                currentStep = 0;
            }
        }
        currentStep++;
    }
    private void processCalendarEvent(String input)
    {
        if(currentStep == 1){
            introTextView.setText(R.string.calendarStep2);
            System.out.println("Title: " +input);
            calendarTitle = input;
        }else if(currentStep == 2){
            introTextView.setText(R.string.calendarStep3);
            calendarDescription= input;
            System.out.println("Description: " +input);
        }else if(currentStep == 3){
            introTextView.setText(R.string.calendarStep4);
            calendarEventLocation = input;
            System.out.println("Location: " +input);
        }else if(currentStep == 4){
            introTextView.setText(R.string.calendarStep5);
            allDay = input.equalsIgnoreCase("yes");
            System.out.println("All Day: " +allDay);
        }else if(currentStep == 5) {
            availability = input;
            System.out.println("Availability: " + input);
            currentStep = 0;
            introTextView.setText(R.string.commandsIntro);
            final CalendarEvent event = new CalendarEvent(allDay,calendarDescription,calendarTitle,
                    calendarEventLocation,availability);
            System.out.println(event.toString());
            finalizeToCalendar(event);
        }
        currentStep++;
    }

    private void finalizeReminder(String reminder)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_small)
                .setContentTitle("Reminder")
                .setContentText(reminder)
                .setGroup(GROUP_REMINDERS)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        NOTIFICATION_ID++;
    }

    private void finalizeToCalendar(CalendarEvent event)
    {
        addToCalendar(event);
    }

    public void addToCalendar(CalendarEvent event)
    {
        int avail = Events.AVAILABILITY_FREE;
        if(event.getAvailability().equalsIgnoreCase("busy")){
            avail = Events.AVAILABILITY_BUSY;
        }
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(Events.TITLE, event.getTitle())
                .putExtra(Events.DESCRIPTION, event.getEventDescript())
                .putExtra(Events.EVENT_LOCATION, event.getLocation())
                .putExtra(Events.AVAILABILITY, avail);
        if(allDay){
            intent.putExtra(Events.EVENT_TIMEZONE,"UTC");
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,true);
        }
        startActivity(intent);
    }

    private static class CalendarEvent {

        private String eventDescript, title, location, availability;
        private boolean allDay;

        public CalendarEvent(boolean allDay, String...args)
        {
            this.eventDescript = args[0];
            this.title = args[1];
            this.location = args[2];
            this.availability = args[3];
            this.allDay = allDay;
        }
        public String getEventDescript() {
            return eventDescript;
        }

        public String getTitle() {
            return title;
        }

        public String getLocation() {
            return location;
        }

        public String getAvailability() {
            return availability;
        }

        @Override
        public String toString() {
            return "CalendarEvent{" +
                    "eventDescript='" + eventDescript + '\'' +
                    ", title='" + title + '\'' +
                    ", location='" + location + '\'' +
                    ", availability='" + availability + '\'' +
                    '}';
        }
    }
}
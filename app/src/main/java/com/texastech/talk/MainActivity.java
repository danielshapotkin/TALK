package com.texastech.talk;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.texastech.talk.database.AppDatabase;
import com.texastech.talk.database.Mood;
import com.texastech.talk.database.MoodDao;
import com.texastech.talk.database.Resources;
import com.texastech.talk.database.ResourcesDao;
import com.texastech.talk.intro.IntroActivity;
import com.texastech.talk.notification.AlarmReceiver;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public static final String QUERY_MOOD_PARAMETER = "MainActivity.QueryMood";
    public static final String NOTIFICATION_CHANNEL_ID = "MainActivity.NotificationChan";

    private int mCurrentMood = 1;
    private int mCurrentMoodIntensity = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences.Editor editor = sharedPrefs.edit();
        //editor.remove(IntroActivity.LAUNCHED_APP_BEFORE); // Удаляем флаг
        //editor.apply();
        if (!sharedPrefs.getBoolean(IntroActivity.LAUNCHED_APP_BEFORE, false)) {
            Intent intent = new Intent(this, IntroActivity.class);
            finish();
            startActivity(intent);
            setupResourcesDatabase();
        }

        registerNotificationChannel();
        setupBottomNavigation();



        // Перенес блок сюда, потому что перестался вызваться метод. Не забыть
        {

            CharSequence[] moods = {
                    // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
                    "Деспрессивный", "Грустный", "Злой", "Испуганный", "Спокойный", "Счастливый"
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkAlertDialog);
            builder.setTitle("Как вы себя чувствуете?");
            builder.setSingleChoiceItems(moods, 0, new MoodDialogChoiceListener());
            builder.setPositiveButton("Далее", new MoodDialogListener());
            builder.show();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        // TODO: Remove, this is for demo purposes
        boolean resumingFromNotification = getIntent().getBooleanExtra(QUERY_MOOD_PARAMETER, false);
        if (resumingFromNotification) {
            showCurrentMoodDialog();
        } else {
            // Show notification if opening the app
            showNotification();
        }
    }

    void registerNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "DailyNotification", importance);
            channel.setDescription("Talk.Notifications");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    void setupResourcesDatabase() {

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        ResourcesDao resDao = database.resourcesDao();

        // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
        final int MoodDepressed = 1;
        final int MoodSad = 2;
        final int MoodAngry = 3;
        final int MoodScared = 4;
        final int MoodModerate = 5;
        final int MoodHappy = 6;


        Resources[] allResources = {
                // Depressed
                new Resources("Как справиться с депрессией с депрессией", "Когда вы в депресии, вы не можете просто заставить себя выйти из этого состояния. Но эти советы могут помочь вам начать путь к выздоровлению.", "https://www.helpguide.org/articles/depression/coping-with-depression.htm", MoodDepressed),
                new Resources("Что такое депрессия?", "Депрессия — это расстройство, которое проявляется избыточной печалью, потерей интереса к приятным вещам и низкой мотивацией.", "https://thiswayup.org.au/how-do-you-feel/sad/", MoodDepressed),
                new Resources("Кот", "Посмотрите это видео.", "https://www.youtube.com/watch?v=xbs7FT7dXYc", MoodDepressed),
                new Resources("Симптомы и признаки депрессии", "Вы думаете, что можете быть депрессивны? Вот некоторые признаки и симптомы, на которые стоит обратить внимание, а также советы по получению необходимой помощи.", "https://www.helpguide.org/articles/depression/depression-symptoms-and-warning-signs.htm", MoodDepressed),

                // Sad
                new Resources("Один среди толпы - Как одиночество влияет на разум и тело", "Посмотрите это видео о чувстве одиночества.", "https://www.youtube.com/watch?v=R8A7JodFx4s", MoodSad),
                new Resources("Я депрессивен или просто мне очень грустно?", "Люди часто считают, что они депрессивны, когда они грустят, или грустят, когда они депрессивны.", "https://www.vice.com/en_us/article/9kzqa7/am-i-depressed-difference-sadness-depression", MoodSad),
                new Resources("Почему я постоянно грущу?", "Когда-нибудь чувствовали себя грустными или напряженными без видимой причины?", "https://au.reachout.com/articles/why-am-i-sad-all-the-time", MoodSad),
                new Resources("Как узнать, я грущу или депрессивен?", "Если вы боитесь, что вы депрессивны, есть много вещей, которые вы можете сделать, чтобы понять это.", "https://www.7cups.com/qa-depression-3/how-do-i-know-if-im-sad-or-depressed-650/", MoodSad),

                // Angry
                new Resources("Управление гневом", "Овладевает ли вашей жизнью ваш характер? Эти советы и техники могут помочь вам контролировать гнев и выражать свои чувства более здоровым образом.", "https://www.helpguide.org/articles/relationships-communication/anger-management.htm", MoodAngry),
                new Resources("Как контролировать гнев, прежде чем он завладеет вами", "Мы все знаем, что такое гнев, и мы все его испытывали: будь то мимолетное раздражение или полноценная ярость.", "https://www.apa.org/topics/anger/control", MoodAngry),
                new Resources("Я злой", "Посмотрите это видео.", "https://www.youtube.com/watch?v=vyMx7s9cThU", MoodAngry),
                new Resources("Почему я такой злой?", "Гнев может быть силой для добра. Но продолжительный, интенсивный гнев не является полезным и здоровым. Вот как взять себя в руки.", "https://www.webmd.com/mental-health/features/why-am-i-so-angry#1", MoodAngry),

                // Scared
                new Resources("Фобии и иррациональные страхи", "Мешает ли вам фобия делать то, что вы хотели бы делать? Узнайте, как распознать, лечить и преодолеть эту проблему.", "https://www.helpguide.org/articles/anxiety/phobias-and-irrational-fears.htm", MoodScared),
                new Resources("Я испуган", "Тот факт, что вы чувствуете страх от этих навязчивых мыслей, означает, что вам нужно посетить психотерапевта.", "https://www.mentalhelp.net/advice/i-m-scared/", MoodScared),
                new Resources("Jeremy Zucker - Испуганный ", "Послушайте песню о одиночестве.", "https://www.youtube.com/watch?v=iyEUvUcMHgE", MoodScared),
                new Resources("Как перестать быть чертовски испуганным постоянно", "Так что, вы испуганы. Давайте, наконец, поговорим об этом, хорошо?", "https://ittybiz.com/how-to-stop-being-scared/", MoodScared),

                // Moderate
                new Resources("5 Шагов для избежания самодовольства", "Помните ли вы огонь в животе, который вы чувствовали на пути к достижению цели?", "https://thetobincompany.com/5-steps-to-avoid-complacency/", MoodModerate),
                new Resources("Как быть человеком: что значит чувствовать себя нормально", "Ли Рейх была одной из первых интернет-консультантов по вопросам советов.", "https://www.theverge.com/2017/2/5/14514224/how-to-be-human-depression-anxiety-feeling-normal", MoodModerate),
                new Resources("НИКОГДА НЕ РАССЛАБЛЯЙСЯ - Лучшее мотивационное видео", "Мотивируйте себя этим видео", "https://www.youtube.com/watch?v=2o8fmUlHAyk", MoodModerate),
                new Resources("10 Лучших Вещей, Которые Можно Делать в Свободное Время", "Посмотрите это видео о том, как использовать свое свободное время", "https://www.youtube.com/watch?v=afoAXho6EHs", MoodModerate),

                // Happy
                new Resources("Чувствовать себя счастливым и быть счастливым — не одно и то же", "Можете ли вы ошибаться в том, что чувствуете себя счастливым?", "https://www.psychologytoday.com/us/blog/am-i-right/201310/feeling-happy-and-being-happy-arent-the-same", MoodHappy),
                new Resources("Как чувствовать себя счастливее, согласно нейроученым и психологам", "Исследователи знают уже десятилетия, что определенные действия заставляют нас чувствовать себя лучше, и только начинают понимать, что происходит в мозгу, чтобы поднять настроение.", "https://www.businessinsider.com/how-feel-happy-happier-better-2017-7", MoodHappy),
                new Resources("Pharrell Williams - Счастлив", "Послушайте, как Фаррелл поет о счастье!", "https://www.youtube.com/watch?v=ZbZSe6N_BXs", MoodHappy),
                new Resources("Наука о счастье: что на самом деле делает нас счастливыми", "Мы все хотим быть счастливыми. В любом случае. Фактически, я бы утверждал, что почти все, что мы делаем, будь то работа, брак, бег или даже заполнение налоговой декларации, делается с главной целью: почувствовать себя счастливее.", "https://medium.com/@MaxWeigand/the-science-of-happiness-what-actually-makes-us-happy-78edcc9bdd58", MoodHappy),
        };


        resDao.insertAll(allResources);

        Toast.makeText(this, "Resource database loaded", Toast.LENGTH_LONG).show();
    }

    void showCurrentMoodDialog()
    {

        CharSequence[] moods = {
                // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
                "Деспрессивный", "Грустный", "Злой", "Испуганный", "Спокойный", "Счастливый"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkAlertDialog);
        builder.setTitle("Как вы себя чувствуете?");
        builder.setSingleChoiceItems(moods, 0, new MoodDialogChoiceListener());
        builder.setPositiveButton("Далее", new MoodDialogListener());
        builder.show();
    }

    void showMoodIntensityDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DarkAlertDialog);
        SeekBar seekBar = new SeekBar(MainActivity.this);
        seekBar.setMax(4);
        seekBar.setOnSeekBarChangeListener(new MoodIntensityDialogSeekListener());

        builder.setTitle("Насколько интенсивно это чувство?");
        builder.setView(seekBar);
        builder.setPositiveButton("Сохранить", new MoodIntesityDialogListener());

        builder.show();
    }

    void saveMoodToDatabase() {

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        MoodDao moodDao = database.moodDao();

        // Get the last entered date
        List<Mood> allMoods = moodDao.getAll();
        int numberOfMoods = allMoods.size();
        int lastEnteredDate = 0;
        if (numberOfMoods > 0) {
             lastEnteredDate = allMoods.get(numberOfMoods - 1).date;
        }

        // Create the new Mood
        Mood currentMood = new Mood(lastEnteredDate + 1, mCurrentMood, mCurrentMoodIntensity);
        moodDao.insert(currentMood);

        // Ask the user (again)
        showNotification();
    }

    void showNotification() {

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 2 * 1000,
                    pendingIntent
            );
        }
    }

    class MoodDialogListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            showMoodIntensityDialog();
        }
    }

    class MoodDialogChoiceListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Offset the choice because it goes from 0-5
            mCurrentMood = which + 1;
        }
    }

    class MoodIntesityDialogListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            saveMoodToDatabase();
            Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_LONG).show();
        }
    }

    class MoodIntensityDialogSeekListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Progress goes from 0-5 but we use 1-6
            mCurrentMoodIntensity = progress + 1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
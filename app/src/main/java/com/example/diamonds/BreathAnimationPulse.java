package com.example.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BreathAnimationPulse extends AppCompatActivity {
    private Handler handlerAnimation = new Handler();
    private boolean statusAnimation = false;
    private ImageView imgAnimation;
    private Button breathGuideBtn;
    private TextView breathDescription, timerText, goBack;
    private CountDownTimer countDownTimer;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            breathGuideBtn.setText("Εισπνοή");

            // Timer starts running
            if (countDownTimer == null) {
                countDownTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long minutes = (millisUntilFinished / 1000) / 60;
                        long seconds = (millisUntilFinished / 1000) % 60;
                        timerText.setText(String.format("%02d:%02d", minutes, seconds));
                    }

                    @Override
                    public void onFinish() {
                        timerText.setText("00:00");
                        breathGuideBtn.setText("Τέλος αναπνοής");
                        stopPulse(); // σταματά το animation
                    }
                }.start();
            }

            // Zoom-out starts
            imgAnimation.animate()
                    .scaleX(3.5f)
                    .scaleY(3.5f)
                    .alpha(0.3f)
                    .setDuration(5000)
                    .start();

            // Zoom-in starts before zoom-out ends
            handlerAnimation.postDelayed(new Runnable() {
                @Override
                public void run() {
                    breathGuideBtn.setText("Εκπνοή");
                    imgAnimation.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(5000)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    // Repeat of circle animation
                                    handlerAnimation.postDelayed(runnable, 0);
                                }
                            })
                            .start();
                }
            }, 4500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath_animation_pulse);

        timerText = findViewById(R.id.timerText);
        breathDescription = findViewById(R.id.breathText);
        goBack = findViewById(R.id.goRelaxationExercisesFromAnimation);

        goBack.setOnClickListener(view -> {
            Intent intent = new Intent(BreathAnimationPulse.this, RelaxationExercises.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        breathDescription.setText(Html.fromHtml("1. Κάθισε ή ξάπλωσε άνετα.<br>" +
                "2. Κλείσε τα μάτια.<br>" +
                "3. <b>Εισπνοή</b> από τη μύτη μετρώντας αργά ως το 5.<br>" +
                "4. <b>Εκπνοή</b> από το στόμα, επίσης μετρώντας ως το 5.<br>" +
                "5. Επανάλαβε για 5–10 λεπτά, διατηρώντας ρυθμό και ήρεμη αναπνοή.<br><br>" +
                "Συγκεντρώσου στην αναπνοή και άφησε τις σκέψεις να περνούν χωρίς να τις κρατάς."));


        imgAnimation = findViewById(R.id.imgAnimation);
        breathGuideBtn = findViewById(R.id.breathBtn);

        breathGuideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statusAnimation) {
                    stopPulse();
                    breathGuideBtn.setText(R.string.start);
                } else {
                    startPulse();
                }
                statusAnimation = !statusAnimation;
            }
        });
    }

    private void startPulse() {
        runnable.run();
    }

    private void stopPulse() {
        handlerAnimation.removeCallbacksAndMessages(null);

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        imgAnimation.animate().cancel();
        imgAnimation.setScaleX(1f);
        imgAnimation.setScaleY(1f);
        imgAnimation.setAlpha(1f);

        timerText.setText("05:00");
        breathGuideBtn.setText("Έναρξη");
    }
}
